package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.{GpuTextBuffer, TextBuffer}
import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context, LimitReachedException, Machine}
import totoro.ocelot.brain.nbt.{NBTTagCompound, NBTTagList}
import totoro.ocelot.brain.network.{Message, Network, Node, Visibility}
import totoro.ocelot.brain.util.{ColorDepth, GenericTextBuffer, PackedColor}
import totoro.ocelot.brain.workspace.Workspace

import scala.util.matching.Regex

// IMPORTANT: usually methods with side effects should *not* be direct
// callbacks to avoid the massive headache synchronizing them ensues, in
// particular when it comes to world saving. I'm making an exception for
// screens, though since they'd be painfully sluggish otherwise. This also
// means we have to use a somewhat nasty trick in Buffer's
// save function: we wait for all computers in the same network to finish
// their current execution and then pause them, to ensure the state of the
// buffer is "clean", meaning the computer has the correct state when it is
// saved in turn. If we didn't, a computer might change a screen after it was
// saved, but before the computer was saved, leading to mismatching states in
// the save file - a Bad Thing (TM).

trait GenericGPU extends Environment with Tiered with VideoRamDevice {
  override val node: Node = Network.newNode(this, Visibility.Neighbors).
    withComponent("gpu").
    create()

  protected def maxResolution: (Int, Int) = Settings.screenResolutionsByTier(tier)

  protected def maxDepth: ColorDepth.Value = Settings.screenDepthsByTier(tier)

  private var screenAddress: Option[String] = None

  private var screenInstance: Option[TextBuffer] = None

  private var bufferIndex: Int = RESERVED_SCREEN_INDEX // screen is index zero

  private def screen(index: Int, f: TextBufferProxy => Array[AnyRef]): Array[AnyRef] = {
    if (index == RESERVED_SCREEN_INDEX) {
      screenInstance match {
        case Some(screen) => screen.synchronized(f(screen))
        case _ => Array(null, "no screen")
      }
    } else {
      getBuffer(index) match {
        case Some(buffer: TextBufferProxy) => f(buffer)
        case _ => Array(null, "invalid buffer index")
      }
    }
  }

  private def screen(f: TextBufferProxy => Array[AnyRef]): Array[AnyRef] = screen(bufferIndex, f)

  final val setBackgroundCosts = Array(1.0 / 32, 1.0 / 64, 1.0 / 128)
  final val setForegroundCosts = Array(1.0 / 32, 1.0 / 64, 1.0 / 128)
  final val setPaletteColorCosts = Array(1.0 / 2, 1.0 / 8, 1.0 / 16)
  final val setCosts = Array(1.0 / 64, 1.0 / 128, 1.0 / 256)
  final val copyCosts = Array(1.0 / 16, 1.0 / 32, 1.0 / 64)
  final val fillCosts = Array(1.0 / 32, 1.0 / 64, 1.0 / 128)
  // These are dirty page bitblt budget costs
  // a single bitblt can send a screen of data, which is n*set calls where set is writing an entire line
  // So for each tier, we multiple the set cost with the number of lines the screen may have
  final val bitbltCost: Double = Settings.get.bitbltCost * scala.math.pow(2, tier)
  final val totalVRAM: Double = (maxResolution._1 * maxResolution._2) * Settings.get.vramSizes(0 max tier min 2)

  var budgetExhausted: Boolean = false // for especially expensive calls, bitblt

  /**
   * Binds the GPU to some Screen node.
   * WARNING! Use with caution. This may disrupt the normal emulation flow.
   */
  def forceBind(node: Node, reset: Boolean = false): Unit = {
    node.host match {
      case buffer: TextBuffer =>
        screenAddress = Option(node.address)
        screenInstance = Some(buffer)
        screen(s => {
          if (reset) {
            val (gmw, gmh) = maxResolution
            val smw = s.getMaximumWidth
            val smh = s.getMaximumHeight
            s.setResolution(math.min(gmw, smw), math.min(gmh, smh))
            s.setColorDepth(ColorDepth(math.min(maxDepth.id, s.getMaximumColorDepth.id)))
            s.setForegroundColor(0xFFFFFF)
            s.setBackgroundColor(0x000000)
          }
          Array.empty
        })
      case _ =>
    }
  }

  def isBoundTo(address: String): Boolean = screenAddress.contains(address)

  def getScreenAddress: String = screenAddress.orNull

  // ----------------------------------------------------------------------- //

  private def resolveInvokeCosts(idx: Int, context: Context, budgetCost: Double): Boolean = {
    idx match {
      case RESERVED_SCREEN_INDEX =>
        context.consumeCallBudget(budgetCost)
        true
      case _ => true
    }
  }

  @Callback(direct = true, doc = """function(): number -- returns the index of the currently selected buffer. 0 is reserved for the screen. Can return 0 even when there is no screen""")
  def getActiveBuffer(context: Context, args: Arguments): Array[AnyRef] = {
    result(bufferIndex)
  }

  @Callback(direct = true, doc = """function(index: number): number -- Sets the active buffer to `index`. 1 is the first vram buffer and 0 is reserved for the screen. returns nil for invalid index (0 is always valid)""")
  def setActiveBuffer(context: Context, args: Arguments): Array[AnyRef] = {
    val previousIndex: Int = bufferIndex
    val newIndex: Int = args.checkInteger(0)
    if (newIndex != RESERVED_SCREEN_INDEX && getBuffer(newIndex).isEmpty) {
      result((), "invalid buffer index")
    } else {
      bufferIndex = newIndex
      if (bufferIndex == RESERVED_SCREEN_INDEX) {
        screen(s => result(true))
      }
      result(previousIndex)
    }
  }

  @Callback(direct = true, doc = """function(): number -- Returns an array of indexes of the allocated buffers""")
  def buffers(context: Context, args: Arguments): Array[AnyRef] = {
    result(bufferIndexes())
  }

  @Callback(direct = true, doc = """function([width: number, height: number]): number -- allocates a new buffer with dimensions width*height (defaults to max resolution) and appends it to the buffer list. Returns the index of the new buffer and returns nil with an error message on failure. A buffer can be allocated even when there is no screen bound to this gpu. Index 0 is always reserved for the screen and thus the lowest index of an allocated buffer is always 1.""")
  def allocateBuffer(context: Context, args: Arguments): Array[AnyRef] = {
    val width: Int = args.optInteger(0, maxResolution._1)
    val height: Int = args.optInteger(1, maxResolution._2)
    val size: Int = width * height
    if (width <= 0 || height <= 0) {
      result((), "invalid page dimensions: must be greater than zero")
    }
    else if (size > (totalVRAM - calculateUsedMemory)) {
      result((), "not enough video memory")
    } else {
      val format: PackedColor.ColorFormat = PackedColor.Depth.format(Settings.screenDepthsByTier(tier))
      val buffer = new GenericTextBuffer(width, height, format)
      val page = GpuTextBuffer.wrap(node.address, nextAvailableBufferIndex, buffer)
      addBuffer(page)
      result(page.id)
    }
  }

  // this event occurs when the gpu is told a page was removed - we need to notify the screen of this
  // we do this because the VideoRamDevice trait only notifies itself, it doesn't assume there is a screen
  override def onBufferRamDestroy(id: Int): Unit = {
    // first protect our buffer index - it needs to fall back to the screen if its buffer was removed
    if (id != RESERVED_SCREEN_INDEX) {
      screen(RESERVED_SCREEN_INDEX, s => s match {
        case oc: VideoRamRasterizer => result(oc.removeBuffer(node.address, id))
        case _ => result(true)// addon mod screen type that is not video ram aware
      })
    }
    if (id == bufferIndex) {
      bufferIndex = RESERVED_SCREEN_INDEX
    }
  }

  @Callback(direct = true, doc = """function(index: number): boolean -- Closes buffer at `index`. Returns true if a buffer closed. If the current buffer is closed, index moves to 0""")
  def freeBuffer(context: Context, args: Arguments): Array[AnyRef] = {
    val index: Int = args.optInteger(0, bufferIndex)
    if (removeBuffers(Array(index)) == 1) result(true)
    else result((), "no buffer at index")
  }

  @Callback(direct = true, doc = """function(): number -- Closes all buffers and returns the count. If the active buffer is closed, index moves to 0""")
  def freeAllBuffers(context: Context, args: Arguments): Array[AnyRef] = result(removeAllBuffers())

  @Callback(direct = true, doc = """function(): number -- returns the total memory size of the gpu vram. This does not include the screen.""")
  def totalMemory(context: Context, args: Arguments): Array[AnyRef] = {
    result(totalVRAM)
  }

  @Callback(direct = true, doc = """function(): number -- returns the total free memory not allocated to buffers. This does not include the screen.""")
  def freeMemory(context: Context, args: Arguments): Array[AnyRef] = {
    result(totalVRAM - calculateUsedMemory)
  }

  @Callback(direct = true, doc = """function(index: number): number, number -- returns the buffer size at index. Returns the screen resolution for index 0. returns nil for invalid indexes""")
  def getBufferSize(context: Context, args: Arguments): Array[AnyRef] = {
    val idx = args.optInteger(0, bufferIndex)
    screen(idx, s => result(s.getWidth, s.getHeight))
  }

  private def determineBitbltBudgetCost(dst: TextBufferProxy, src: TextBufferProxy): Double = {
    // large dirty buffers need throttling so their budget cost is more
    // clean buffers have no budget cost.
    src match {
      case page: GpuTextBuffer => dst match {
        case _: GpuTextBuffer => 0.0 // no cost to write to ram
        case _ if page.dirty => // screen target will need the new buffer
          // small buffers are cheap, so increase with size of buffer source
          bitbltCost * (src.getWidth * src.getHeight) / (maxResolution._1 * maxResolution._2)
        case _ => .001 // bitblt a clean page to screen has a minimal cost
      }
      case _ => 0.0 // from screen is free
    }
  }

  @Callback(direct = true, doc = """function([dst: number, col: number, row: number, width: number, height: number, src: number, fromCol: number, fromRow: number]):boolean -- bitblt from buffer to screen. All parameters are optional. Writes to `dst` page in rectangle `x, y, width, height`, defaults to the bound screen and its viewport. Reads data from `src` page at `fx, fy`, default is the active page from position 1, 1""")
  def bitblt(context: Context, args: Arguments): Array[AnyRef] = {
    val dstIdx = args.optInteger(0, RESERVED_SCREEN_INDEX)
    screen(dstIdx, dst => {
      val col = args.optInteger(1, 1)
      val row = args.optInteger(2, 1)
      val w = args.optInteger(3, dst.getWidth)
      val h = args.optInteger(4, dst.getHeight)
      val srcIdx = args.optInteger(5, bufferIndex)
      screen(srcIdx, src => {
        val fromCol = args.optInteger(6, 1)
        val fromRow = args.optInteger(7, 1)

        var budgetCost: Double = determineBitbltBudgetCost(dst, src)
        val tierCredit: Double = ((tier + 1) * .5)
        val overBudget: Double = budgetCost - tierCredit

        if (overBudget > 0) {
          if (budgetExhausted) { // we've thrown once before
            if (overBudget > tierCredit) { // we need even more pause than just a single tierCredit
              val pauseNeeded = overBudget - tierCredit
              val seconds: Double = (pauseNeeded / tierCredit) / 20
              context.pause(seconds)
            }
            budgetCost = 0 // remove the rest of the budget cost at this point
          } else {
            budgetExhausted = true
            throw new LimitReachedException()
          }
        }
        budgetExhausted = false

        if (resolveInvokeCosts(dstIdx, context, budgetCost)) {
          if (dstIdx == srcIdx) {
            val tx = col - fromCol
            val ty = row - fromRow
            dst.copy(fromCol - 1, fromRow - 1, w, h, tx, ty)
            result(true)
          } else {
            // at least one of the two buffers is a gpu buffer
            GpuTextBuffer.bitblt(dst, col, row, w, h, src, fromRow, fromCol)
            result(true)
          }
        } else result((), "not enough energy")
      })
    })
  }

  @Callback(doc = """function(address:string[, reset:boolean=true]):boolean -- Binds the GPU to the screen with the specified address and resets screen settings if `reset` is true.""")
  def bind(context: Context, args: Arguments): Array[AnyRef] = {
    val address = args.checkString(0)
    val reset = args.optBoolean(1, default = true)
    node.network.node(address) match {
      case null => result((), "invalid address")
      case node: Node if node.host.isInstanceOf[TextBuffer] =>
        screenAddress = Option(address)
        screenInstance = Some(node.host.asInstanceOf[TextBuffer])
        screen(s => {
          if (reset) {
            val (gmw, gmh) = maxResolution
            val smw = s.getMaximumWidth
            val smh = s.getMaximumHeight
            s.setResolution(math.min(gmw, smw), math.min(gmh, smh))
            s.setColorDepth(ColorDepth(math.min(maxDepth.id, s.getMaximumColorDepth.id)))
            s.setForegroundColor(0xFFFFFF)
            s.setBackgroundColor(0x000000)
            s match {
              case oc: VideoRamRasterizer => oc.removeAllBuffers()
              case _ =>
            }
          }
          else context.pause(0) // To discourage outputting "in realtime" to multiple screens using one GPU.
          result(true)
        })
      case _ => result((), "not a screen")
    }
  }

  @Callback(direct = true, doc = """function():string -- Get the address of the screen the GPU is currently bound to.""")
  def getScreen(context: Context, args: Arguments): Array[AnyRef] = screen(RESERVED_SCREEN_INDEX, s => result(s.node.address))

  @Callback(direct = true, doc = """function():number, boolean -- Get the current background color and whether it's from the palette or not.""")
  def getBackground(context: Context, args: Arguments): Array[AnyRef] =
    screen(s => result(s.getBackgroundColor, s.isBackgroundFromPalette))

  @Callback(direct = true, doc = """function(value:number[, palette:boolean]):number, number or nil -- Sets the background color to the specified value. Optionally takes an explicit palette index. Returns the old value and if it was from the palette its palette index.""")
  def setBackground(context: Context, args: Arguments): Array[AnyRef] = {
    val color = args.checkInteger(0)
    if (bufferIndex == RESERVED_SCREEN_INDEX) {
      context.consumeCallBudget(setBackgroundCosts(tier))
    }
    screen(s => {
      val oldValue = s.getBackgroundColor
      val (oldColor, oldIndex) =
        if (s.isBackgroundFromPalette) {
          (s.getPaletteColor(oldValue), oldValue)
        }
        else {
          (oldValue, ())
        }
      s.setBackgroundColor(color, args.optBoolean(1, default = false))
      result(oldColor, oldIndex)
    })
  }

  @Callback(direct = true, doc = """function():number, boolean -- Get the current foreground color and whether it's from the palette or not.""")
  def getForeground(context: Context, args: Arguments): Array[AnyRef] =
    screen(s => result(s.getForegroundColor, s.isForegroundFromPalette))

  @Callback(direct = true, doc = """function(value:number[, palette:boolean]):number, number or nil -- Sets the foreground color to the specified value. Optionally takes an explicit palette index. Returns the old value and if it was from the palette its palette index.""")
  def setForeground(context: Context, args: Arguments): Array[AnyRef] = {
    val color = args.checkInteger(0)
    if (bufferIndex == RESERVED_SCREEN_INDEX) {
      context.consumeCallBudget(setForegroundCosts(tier))
    }
    screen(s => {
      val oldValue = s.getForegroundColor
      val (oldColor, oldIndex) =
        if (s.isForegroundFromPalette) {
          (s.getPaletteColor(oldValue), oldValue)
        }
        else {
          (oldValue, ())
        }
      s.setForegroundColor(color, args.optBoolean(1, default = false))
      result(oldColor, oldIndex)
    })
  }

  @Callback(direct = true, doc = """function(index:number):number -- Get the palette color at the specified palette index.""")
  def getPaletteColor(context: Context, args: Arguments): Array[AnyRef] = {
    val index = args.checkInteger(0)
    screen(s => try result(s.getPaletteColor(index)) catch {
      case _: ArrayIndexOutOfBoundsException => throw new IllegalArgumentException("invalid palette index")
    })
  }

  @Callback(direct = true, doc = """function(index:number, color:number):number -- Set the palette color at the specified palette index. Returns the previous value.""")
  def setPaletteColor(context: Context, args: Arguments): Array[AnyRef] = {
    val index = args.checkInteger(0)
    val color = args.checkInteger(1)
    if (bufferIndex == RESERVED_SCREEN_INDEX) {
      context.consumeCallBudget(setPaletteColorCosts(tier))
      context.pause(0.1)
    }
    screen(s => try {
      val oldColor = s.getPaletteColor(index)
      s.setPaletteColor(index, color)
      result(oldColor)
    }
    catch {
      case _: ArrayIndexOutOfBoundsException => throw new IllegalArgumentException("invalid palette index")
    })
  }

  @Callback(direct = true, doc = """function():number -- Returns the currently set color depth.""")
  def getDepth(context: Context, args: Arguments): Array[AnyRef] =
    screen(s => result(PackedColor.Depth.bits(s.getColorDepth)))

  @Callback(doc = """function(depth:number):number -- Set the color depth. Returns the previous value.""")
  def setDepth(context: Context, args: Arguments): Array[AnyRef] = {
    val depth = args.checkInteger(0)
    screen(s => {
      val oldDepth = s.getColorDepth
      depth match {
        case 1 => s.setColorDepth(ColorDepth.OneBit)
        case 4 if maxDepth.id >= ColorDepth.FourBit.id => s.setColorDepth(ColorDepth.FourBit)
        case 8 if maxDepth.id >= ColorDepth.EightBit.id => s.setColorDepth(ColorDepth.EightBit)
        case _ => throw new IllegalArgumentException("unsupported depth")
      }
      result(oldDepth)
    })
  }

  @Callback(direct = true, doc = """function():number -- Get the maximum supported color depth.""")
  def maxDepth(context: Context, args: Arguments): Array[AnyRef] =
    screen(s => result(PackedColor.Depth.bits(ColorDepth(math.min(maxDepth.id, s.getMaximumColorDepth.id)))))

  @Callback(direct = true, doc = """function():number, number -- Get the current screen resolution.""")
  def getResolution(context: Context, args: Arguments): Array[AnyRef] =
    screen(s => result(s.getWidth, s.getHeight))

  @Callback(doc = """function(width:number, height:number):boolean -- Set the screen resolution. Returns true if the resolution changed.""")
  def setResolution(context: Context, args: Arguments): Array[AnyRef] = {
    val w = args.checkInteger(0)
    val h = args.checkInteger(1)
    val (mw, mh) = maxResolution
    // Even though the buffer itself checks this again, we need this here for
    // the minimum of screen and GPU resolution.
    if (w < 1 || h < 1 || w > mw || h > mw || h * w > mw * mh)
      throw new IllegalArgumentException("unsupported resolution")
    screen(s => result(s.setResolution(w, h)))
  }

  @Callback(direct = true, doc = """function():number, number -- Get the maximum screen resolution.""")
  def maxResolution(context: Context, args: Arguments): Array[AnyRef] =
    screen(s => {
      val (gmw, gmh) = maxResolution
      val smw = s.getMaximumWidth
      val smh = s.getMaximumHeight
      result(math.min(gmw, smw), math.min(gmh, smh))
    })

  @Callback(direct = true, doc = """function():number, number -- Get the current viewport resolution.""")
  def getViewport(context: Context, args: Arguments): Array[AnyRef] =
    screen(s => result(s.getViewportWidth, s.getViewportHeight))

  @Callback(doc = """function(width:number, height:number):boolean -- Set the viewport resolution. Cannot exceed the screen resolution. Returns true if the resolution changed.""")
  def setViewport(context: Context, args: Arguments): Array[AnyRef] = {
    val w = args.checkInteger(0)
    val h = args.checkInteger(1)
    val (mw, mh) = maxResolution
    // Even though the buffer itself checks this again, we need this here for
    // the minimum of screen and GPU resolution.
    if (w < 1 || h < 1 || w > mw || h > mw || h * w > mw * mh)
      throw new IllegalArgumentException("unsupported viewport size")
    screen(s => {
      if (w > s.getWidth || h > s.getHeight)
        throw new IllegalArgumentException("unsupported viewport size")
      result(s.setViewport(w, h))
    })
  }

  @Callback(direct = true, doc = """function(x:number, y:number):string, number, number, number or nil, number or nil -- Get the value displayed on the screen at the specified index, as well as the foreground and background color. If the foreground or background is from the palette, returns the palette indices as fourth and fifth results, else nil, respectively.""")
  def get(context: Context, args: Arguments): Array[AnyRef] = {
    // maybe one day:
    //    if (bufferIndex != RESERVED_SCREEN_INDEX && args.count() == 0) {
    //      return screen {
    //        case ram: GpuTextBuffer => {
    //          val nbt = new NBTTagCompound
    //          ram.data.save(nbt)
    //          result(nbt)
    //        }
    //      }
    //    }
    val x = args.checkInteger(0) - 1
    val y = args.checkInteger(1) - 1
    screen(s => {
      val fgValue = s.getForegroundColor(x, y)
      val (fgColor, fgIndex) =
        if (s.isForegroundFromPalette(x, y)) {
          (s.getPaletteColor(fgValue), fgValue)
        }
        else {
          (fgValue, ())
        }

      val bgValue = s.getBackgroundColor(x, y)
      val (bgColor, bgIndex) =
        if (s.isBackgroundFromPalette(x, y)) {
          (s.getPaletteColor(bgValue), bgValue)
        }
        else {
          (bgValue, ())
        }

      result(s.get(x, y), fgColor, bgColor, fgIndex, bgIndex)
    })
  }

  @Callback(direct = true, doc = """function(x:number, y:number, value:string[, vertical:boolean]):boolean -- Plots a string value to the screen at the specified position. Optionally writes the string vertically.""")
  def set(context: Context, args: Arguments): Array[AnyRef] = {
    val x = args.checkInteger(0) - 1
    val y = args.checkInteger(1) - 1
    val value = args.checkString(2)
    val vertical = args.optBoolean(3, default = false)

    screen(s => {
      if (resolveInvokeCosts(bufferIndex, context, setCosts(tier))) {
        s.set(x, y, value, vertical)
        result(true)
      }
      else result((), "not enough energy")
    })
  }

  @Callback(direct = true, doc = """function(x:number, y:number, width:number, height:number, tx:number, ty:number):boolean -- Copies a portion of the screen from the specified location with the specified size by the specified translation.""")
  def copy(context: Context, args: Arguments): Array[AnyRef] = {
    val x = args.checkInteger(0) - 1
    val y = args.checkInteger(1) - 1
    val w = math.max(0, args.checkInteger(2))
    val h = math.max(0, args.checkInteger(3))
    val tx = args.checkInteger(4)
    val ty = args.checkInteger(5)
    screen(s => {
      if (resolveInvokeCosts(bufferIndex, context, copyCosts(tier))) {
        s.copy(x, y, w, h, tx, ty)
        result(true)
      }
      else result((), "not enough energy")
    })
  }

  @Callback(direct = true, doc = """function(x:number, y:number, width:number, height:number, char:string):boolean -- Fills a portion of the screen at the specified position with the specified size with the specified character.""")
  def fill(context: Context, args: Arguments): Array[AnyRef] = {
    val x = args.checkInteger(0) - 1
    val y = args.checkInteger(1) - 1
    val w = math.max(0, args.checkInteger(2))
    val h = math.max(0, args.checkInteger(3))
    val value = args.checkString(4)
    if (value.length == 1) screen(s => {
      if (resolveInvokeCosts(bufferIndex, context, fillCosts(tier))) {
        s.fill(x, y, w, h, value.charAt(0))
        result(true)
      }
      else {
        result((), "not enough energy")
      }
    })
    else throw new Exception("invalid fill value")
  }

  // ----------------------------------------------------------------------- //

  override def onMessage(message: Message): Unit = {
    super.onMessage(message)
    if (node.isNeighborOf(message.source)) {
      if (message.name == "computer.stopped" || message.name == "computer.started") {
        bufferIndex = RESERVED_SCREEN_INDEX
        removeAllBuffers()
      }
    }

    if (message.name == "computer.stopped" && node.isNeighborOf(message.source)) {
      screen(s => {
        val (gmw, gmh) = maxResolution
        val smw = s.getMaximumWidth
        val smh = s.getMaximumHeight
        s.setResolution(math.min(gmw, smw), math.min(gmh, smh))
        s.setColorDepth(ColorDepth(math.min(maxDepth.id, s.getMaximumColorDepth.id)))
        s.setForegroundColor(0xFFFFFF)
        val w = s.getWidth
        val h = s.getHeight
        message.source.host match {
          case machine: Machine if machine.lastError != null =>
            if (s.getColorDepth.id > ColorDepth.OneBit.id) s.setBackgroundColor(0x0000FF)
            else s.setBackgroundColor(0x000000)
            s.fill(0, 0, w, h, ' ')
            try {
              val wrapRegEx = s"(.{1,${math.max(1, w - 2)}})\\s".r
              val lines = wrapRegEx.replaceAllIn(machine.lastError.replace("\t", "  ") + "\n", m => Regex.quoteReplacement(m.group(1) + "\n")).linesIterator.toArray
              val firstRow = ((h - lines.length) / 2) max 2

              val message = "Unrecoverable Error"
              s.set((w - message.length) / 2, firstRow - 2, message, vertical = false)

              val maxLineLength = lines.map(_.length).max
              val col = ((w - maxLineLength) / 2) max 0
              for ((line, idx) <- lines.zipWithIndex) {
                val row = firstRow + idx
                s.set(col, row, line, vertical = false)
              }
            }
            catch {
              case t: Throwable => t.printStackTrace()
            }
          case _ =>
            s.setBackgroundColor(0x000000)
            s.fill(0, 0, w, h, ' ')
        }
        null // For screen()
      })
    }
  }

  override def onConnect(node: Node): Unit = {
    super.onConnect(node)
    if (screenInstance.isEmpty && screenAddress.fold(false)(_ == node.address)) {
      node.host match {
        case buffer: TextBuffer =>
          screenInstance = Some(buffer)
        case _ => // Not the screen node we're looking for.
      }
    }
  }

  override def onDisconnect(node: Node): Unit = {
    super.onDisconnect(node)
    if (node == this.node || screenAddress.contains(node.address)) {
      screenInstance = None
    }
  }

  // ----------------------------------------------------------------------- //

  private final val ScreenTag = "screen"
  private val BufferIndexTag: String = "bufferIndex"
  private val VideoRamTag: String = "videoRam"
  private final val NbtPages: String = "pages"
  private final val NbtPageIdx: String = "page_idx"
  private final val NbtPageData: String = "page_data"
  private val CompoundID = (new NBTTagCompound).getId

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)

    if (nbt.hasKey(ScreenTag)) {
      nbt.getString(ScreenTag) match {
        case screen: String if screen.nonEmpty => screenAddress = Some(screen)
        case _ => screenAddress = None
      }
      screenInstance = None
    }

    if (nbt.hasKey(BufferIndexTag)) {
      bufferIndex = nbt.getInteger(BufferIndexTag)
    }

    removeAllBuffers() // JUST in case
    if (nbt.hasKey(VideoRamTag)) {
      val videoRamNbt = nbt.getCompoundTag(VideoRamTag)
      val nbtPages = videoRamNbt.getTagList(NbtPages, CompoundID)
      for (i <- 0 until nbtPages.tagCount) {
        val nbtPage = nbtPages.getCompoundTagAt(i)
        val idx: Int = nbtPage.getInteger(NbtPageIdx)
        val data = nbtPage.getCompoundTag(NbtPageData)
        loadBuffer(node.address, idx, data, workspace)
      }
    }
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)

    if (screenAddress.isDefined) {
      nbt.setString(ScreenTag, screenAddress.get)
    }

    nbt.setInteger(BufferIndexTag, bufferIndex)

    val videoRamNbt = new NBTTagCompound
    val nbtPages = new NBTTagList

    val indexes = bufferIndexes()
    for (idx: Int <- indexes) {
      getBuffer(idx) match {
        case Some(page) =>
          val nbtPage = new NBTTagCompound
          nbtPage.setInteger(NbtPageIdx, idx)
          val data = new NBTTagCompound
          page.data.save(data)
          nbtPage.setTag(NbtPageData, data)
          nbtPages.appendTag(nbtPage)
        case _ => // ignore
      }
    }
    videoRamNbt.setTag(NbtPages, nbtPages)
    nbt.setTag(VideoRamTag, videoRamNbt)
  }
}
