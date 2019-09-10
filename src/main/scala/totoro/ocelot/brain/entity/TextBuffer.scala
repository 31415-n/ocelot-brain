package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{DeviceInfo, Environment, Tiered}
import totoro.ocelot.brain.event._
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.{Component, Network, Node, Visibility}
import totoro.ocelot.brain.user.User
import totoro.ocelot.brain.util.{ColorDepth, GenericTextBuffer, PackedColor, Tier}
import totoro.ocelot.brain.{Constants, Settings}

import scala.collection.mutable

/**
  * This trait implements functionality for displaying and manipulating
  * text, like screens and robots.
  */
class TextBuffer(var bufferTier: Int = Tier.One) extends Environment with DeviceInfo with Tiered {
  override val node: Component =  Network.newNode(this, Visibility.Network).
    withComponent("screen").
    create()

  private var maxResolution = Settings.screenResolutionsByTier(bufferTier)

  private var maxDepth = Settings.screenDepthsByTier(bufferTier)

  private var aspectRatio = (1.0, 1.0)

  protected var precisionMode = false

  private var isDisplaying = true

  val data = new GenericTextBuffer(maxResolution, PackedColor.Depth.format(maxDepth))

  var viewport: (Int, Int) = data.size

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Display,
    DeviceAttribute.Description -> "Text buffer",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "Matrix V0",
    DeviceAttribute.Capacity -> (maxResolution._1 * maxResolution._2).toString,
    DeviceAttribute.Width -> Array("1", "4", "8").apply(maxDepth.id)
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo

  override def tier: Int = bufferTier
  override def tier_=(value: Int): Unit = bufferTier = value

  // ----------------------------------------------------------------------- //

  @Callback(direct = true, doc = """function():boolean -- Returns whether the screen is currently on.""")
  def isOn(computer: Context, args: Arguments): Array[AnyRef] = result(isDisplaying)

  @Callback(doc = """function():boolean -- Turns the screen on. Returns true if it was off.""")
  def turnOn(computer: Context, args: Arguments): Array[AnyRef] = {
    val oldPowerState = isDisplaying
    setPowerState(value = true)
    result(isDisplaying != oldPowerState, isDisplaying)
  }

  @Callback(doc = """function():boolean -- Turns off the screen. Returns true if it was on.""")
  def turnOff(computer: Context, args: Arguments): Array[AnyRef] = {
    val oldPowerState = isDisplaying
    setPowerState(value = false)
    result(isDisplaying != oldPowerState, isDisplaying)
  }

  @Callback(direct = true, doc = """function():number, number -- The aspect ratio of the screen. For multi-block screens this is the number of blocks, horizontal and vertical.""")
  def getAspectRatio(context: Context, args: Arguments): Array[AnyRef] = this.synchronized {
    result(aspectRatio._1, aspectRatio._2)
  }

  @Callback(doc = """function():table -- The list of keyboards attached to the screen.""")
  def getKeyboards(context: Context, args: Arguments): Array[AnyRef] = {
    context.pause(0.25)
    Array(node.neighbors.filter(_.host.isInstanceOf[Keyboard]).map(_.address).toArray)
  }

  @Callback(direct = true, doc = """function():boolean -- Returns whether the screen is in high precision mode (sub-pixel mouse event positions).""")
  def isPrecise(computer: Context, args: Arguments): Array[AnyRef] = result(precisionMode)

  @Callback(doc = """function(enabled:boolean):boolean -- Set whether to use high precision mode (sub-pixel mouse event positions).""")
  def setPrecise(computer: Context, args: Arguments): Array[AnyRef] = {
    // Available for T3 screens only... easiest way to check for us is to
    // base it off of the maximum color depth.
    if (maxDepth == Settings.screenDepthsByTier(Tier.Three)) {
      val oldValue = precisionMode
      precisionMode = args.checkBoolean(0)
      result(oldValue)
    }
    else result(Unit, "unsupported operation")
  }

  // ----------------------------------------------------------------------- //

  /**
    * Set whether the buffer is powered on.
    *
    * For example, screens can be powered on and off by sending a redstone
    * pulse into them, in addition to their component API.
    *
    * @param value whether the buffer should be on or not.
    * @see `getPowerState()`
    */
  def setPowerState(value: Boolean) {
    isDisplaying = value
  }

  /**
    * Get the current power state.
    *
    * @return whether the buffer is powered on.
    * @see `setPowerState(boolean)`
    */
  def getPowerState: Boolean = isDisplaying

  /**
    * Sets the maximum resolution supported by this buffer.
    *
    * @param width  the maximum horizontal resolution, in characters.
    * @param height the maximum vertical resolution, in characters.
    */
  def setMaximumResolution(width: Int, height: Int) {
    if (width < 1) throw new IllegalArgumentException("width must be larger or equal to one")
    if (height < 1) throw new IllegalArgumentException("height must be larger or equal to one")
    maxResolution = (width, height)
  }

  /**
    * Get the maximum horizontal size of the buffer.
    */
  def getMaximumWidth: Int = maxResolution._1

  /**
    * Get the maximum vertical size of the buffer.
    */
  def getMaximumHeight: Int = maxResolution._2

  /**
    * Set the 'aspect ratio' of the buffer.
    *
    * Not to be confused with the maximum resolution of the buffer, this
    * refers to the 'physical' size of the buffer's container. For multi-
    * block screens, for example, this is the number of horizontal and
    * vertical blocks.
    *
    * @param width  the horizontal size of the physical representation.
    * @param height the vertical size of the physical representation.
    */
  def setAspectRatio(width: Double, height: Double): Unit = this.synchronized(aspectRatio = (width, height))

  /**
    * Get the aspect ratio of the buffer.
    *
    * Note that this is in fact `width / height`.
    *
    * @see #setAspectRatio(double, double)
    */
  def getAspectRatio: Double = aspectRatio._1 / aspectRatio._2

  /**
    * Set the buffer's active resolution.
    *
    * @param width  the horizontal resolution.
    * @param height the vertical resolution.
    * @return `true` if the resolution changed.
    */
  def setResolution(width: Int, height: Int): Boolean = {
    val (mw, mh) = maxResolution
    if (width < 1 || height < 1 || width > mw || height > mw || height * width > mw * mh)
      throw new IllegalArgumentException("unsupported resolution")
    // Send to clients
    EventBus.send(TextBufferSetResolutionEvent(this.node.address, width, height))
    // Force set viewport to new resolution. This is partially for
    // backwards compatibility, and partially to enforce a valid one.
    val sizeChanged = data.size = (width, height)
    val viewportChanged = setViewport(width, height)
    if (sizeChanged || viewportChanged) {
      if (!viewportChanged && node != null) node.sendToReachable("computer.signal", "screen_resized", Int.box(width), Int.box(height))
      true
    }
    else false
  }

  /**
    * Get the current horizontal resolution.
    *
    * @see `setResolution(int, int)`
    */
  def getWidth: Int = data.width

  /**
    * Get the current vertical resolution.
    *
    * @see `setResolution(int, int)`
    */
  def getHeight: Int = data.height

  /**
    * Set the buffer's active viewport resolution.
    *
    * This cannot exceed the current buffer resolution.
    *
    * @param width  the horizontal resolution.
    * @param height the vertical resolution.
    * @return `true` if the resolution changed.
    * @see `setResolution(int, int)`
    */
  def setViewport(width: Int, height: Int): Boolean = {
    val (mw, mh) = data.size
    if (width < 1 || height < 1 || width > mw || height > mh)
      throw new IllegalArgumentException("unsupported viewport resolution")
    EventBus.send(TextBufferSetViewportEvent(this.node.address, width, height))
    val (cw, ch) = viewport
    if (width != cw || height != ch) {
      viewport = (width, height)
      if (node != null) node.sendToReachable("computer.signal", "screen_resized", Int.box(width), Int.box(height))
      true
    }
    else false
  }

  /**
    * Get the current horizontal viewport resolution.
    *
    * @see `setViewport(int, int)`
    */
  def getViewportWidth: Int = viewport._1

  /**
    * Get the current vertical viewport resolution.
    *
    * @see `setViewport(int, int)`
    */
  def getViewportHeight: Int = viewport._2

  /**
    * Sets the maximum color depth supported by this buffer.
    *
    * Note that this is the ''maximum'' supported depth, lower depths
    * will be supported, too. So when setting this to four bit, one bit will
    * be supported, too. When setting this to eight bit, four and one bit
    * will be supported, also.
    *
    * @param depth the maximum color depth of the buffer.
    */
  def setMaximumColorDepth(depth: ColorDepth.Value): Unit = maxDepth = depth

  /**
    * Get the maximum color depth supported.
    */
  def getMaximumColorDepth: ColorDepth.Value = maxDepth

  /**
    * Set the active color depth for this buffer.
    *
    * @param depth the new color depth.
    * @return `true` if the color depth changed.
    */
  def setColorDepth(depth: ColorDepth.Value): Boolean = {
    if (depth.id > maxDepth.id)
      throw new IllegalArgumentException("unsupported depth")
    EventBus.send(TextBufferSetColorDepthEvent(this.node.address, depth.id))
    data.format = PackedColor.Depth.format(depth)
  }

  /**
    * Get the active color depth of this buffer.
    */
  def getColorDepth: ColorDepth.Value = data.format.depth

  /**
    * Set the color of the active color palette at the specified index.
    *
    * This will error if the current depth does not have a palette (one bit).
    *
    * @param index the index at which to set the color.
    * @param color the color to set for the specified index.
    */
  def setPaletteColor(index: Int, color: Int): Unit = data.format match {
    case palette: PackedColor.MutablePaletteFormat =>
      palette(index) = color
      EventBus.send(TextBufferSetPaletteColorEvent(this.node.address, index, color))
    case _ => throw new Exception("palette not available")
  }

  /**
    * Get the color in the active color palette at the specified index.
    *
    * This will error if the current depth does not have a palette (one bit).
    *
    * @param index the index at which to get the color.
    * @return the color in the active palette at the specified index.
    */
  def getPaletteColor(index: Int): Int = data.format match {
    case palette: PackedColor.MutablePaletteFormat => palette(index)
    case _ => throw new Exception("palette not available")
  }

  /**
    * Set the active foreground color, not using a palette.
    *
    * @param color the new foreground color.
    * @see `setForegroundColor(int, boolean)`
    */
  def setForegroundColor(color: Int): Unit = setForegroundColor(color, isFromPalette = false)

  /**
    * Set the active foreground color.
    *
    * If the value is not from the palette, the actually stored value may
    * differ from the specified one, as it is converted to the buffer's
    * current color depth.
    *
    * For palette-only color formats (four bit) the best fit from the palette
    * is chosen, if the value is not from the palette.
    *
    * @param color         the color or palette index.
    * @param isFromPalette `true` if `color` specifies a palette index.
    */
  def setForegroundColor(color: Int, isFromPalette: Boolean) {
    val value = PackedColor.Color(color, isFromPalette)
    if (data.foreground != value) {
      data.foreground = value
      EventBus.send(TextBufferSetForegroundColorEvent(this.node.address, data.format.inflate(data.format.deflate(value) & 0xFF)))
    }
  }

  /**
    * The active foreground color.
    */
  def getForegroundColor: Int = data.foreground.value

  /**
    * `true` if the foreground color is from the color palette, meaning
    * the value returned from `getForegroundColor()` is the color
    * palette index.
    */
  def isForegroundFromPalette: Boolean = data.foreground.isPalette

  /**
    * Set the active background color, not using a palette.
    *
    * @param color the new background color.
    * @see `setBackgroundColor(int, boolean)`
    */
  def setBackgroundColor(color: Int): Unit = setBackgroundColor(color, isFromPalette = false)

  /**
    * Set the active background color.
    *
    * If the value is not from the palette, the actually stored value may
    * differ from the specified one, as it is converted to the buffer's
    * current color depth.
    *
    * For palette-only color formats (four bit) the best fit from the palette
    * is chosen, if the value is not from the palette.
    *
    * @param color         the color or palette index.
    * @param isFromPalette `true` if `color` specifies a palette index.
    */
  def setBackgroundColor(color: Int, isFromPalette: Boolean) {
    val value = PackedColor.Color(color, isFromPalette)
    if (data.background != value) {
      data.background = value
      EventBus.send(TextBufferSetBackgroundColorEvent(this.node.address, data.format.inflate(data.format.deflate(value) & 0xFF)))
    }
  }

  /**
    * The active background color.
    */
  def getBackgroundColor: Int = data.background.value

  /**
    * `true` if the background color is from the color palette, meaning
    * the value returned from `getBackgroundColor()` is the color
    * palette index.
    */
  def isBackgroundFromPalette: Boolean = data.background.isPalette

  /**
    * Copy a portion of the text buffer.
    *
    * This will copy the area's text and colors.
    *
    * @param column                the starting horizontal index of the area to copy.
    * @param row                   the starting vertical index of the area to copy.
    * @param width                 the width of the area to copy.
    * @param height                the height of the area to copy.
    * @param horizontalTranslation the horizontal offset, relative to the starting column to copy the are to.
    * @param verticalTranslation   the vertical offset, relative to the starting row to copy the are to.
    */
  def copy(column: Int, row: Int, width: Int, height: Int, horizontalTranslation: Int, verticalTranslation: Int): Unit =
    if (data.copy(column, row, width, height, horizontalTranslation, verticalTranslation))
      EventBus.send(TextBufferCopyEvent(this.node.address, column, row, width, height, horizontalTranslation, verticalTranslation))

  /**
    * Fill a portion of the text buffer.
    *
    * This will set the area's colors to the currently active ones.
    *
    * @param column the starting horizontal index of the area to fill.
    * @param row    the starting vertical index of the area to fill.
    * @param width  the width of the area to fill.
    * @param height the height of the area to fill.
    * @param value  the character to fill the area with.
    */
  def fill(column: Int, row: Int, width: Int, height: Int, value: Char): Unit =
    if (data.fill(column, row, width, height, value))
      EventBus.send(TextBufferFillEvent(this.node.address, column, row, width, height, value))

  /**
    * Write a string into the text buffer.
    *
    * This will apply the currently active colors to the changed area.
    *
    * @param column   the starting horizontal index to write at.
    * @param row      the starting vertical index to write at.
    * @param value    the string to write.
    * @param vertical `true` if the string should be written vertically instead of horizontally.
    */
  def set(column: Int, row: Int, value: String, vertical: Boolean): Unit =
    if (column < data.width && (column >= 0 || -column < value.length)) {
      // Make sure the string isn't longer than it needs to be, in particular to
      // avoid sending too much data to our clients.
      val (x, y, truncated) =
      if (vertical) if (row < 0) (column, 0, value.substring(-row))
      else (column, row, value.substring(0, math.min(value.length, data.height - row)))
      else if (column < 0) (0, row, value.substring(-column))
      else (column, row, value.substring(0, math.min(value.length, data.width - column)))
      if (data.set(x, y, truncated, vertical))
        EventBus.send(TextBufferSetEvent(this.node.address, x, y, truncated, vertical))
    }

  /**
    * Get the character in the text buffer at the specified location.
    *
    * @param column the horizontal index.
    * @param row    the vertical index.
    * @return the character at that index.
    */
  def get(column: Int, row: Int): Char = data.get(column, row)

  /**
    * Get the foreground color of the text buffer at the specified location.
    *
    * '''Important''': this may be a palette index.
    *
    * @param column the horizontal index.
    * @param row    the vertical index.
    * @return the foreground color at that index.
    */
  def getForegroundColor(column: Int, row: Int): Int =
    if (isForegroundFromPalette(column, row)) PackedColor.extractForeground(color(column, row))
    else PackedColor.unpackForeground(color(column, row), data.format)

  /**
    * Whether the foreground color of the text buffer at the specified
    * location if from the color palette.
    *
    * @param column the horizontal index.
    * @param row    the vertical index.
    * @return whether the foreground at that index is from the palette.
    */
  def isForegroundFromPalette(column: Int, row: Int): Boolean =
    data.format.isFromPalette(PackedColor.extractForeground(color(column, row)))

  /**
    * Get the background color of the text buffer at the specified location.
    *
    * '''Important''': this may be a palette index.
    *
    * @param column the horizontal index.
    * @param row    the vertical index.
    * @return the background color at that index.
    */
  def getBackgroundColor(column: Int, row: Int): Int =
    if (isBackgroundFromPalette(column, row)) PackedColor.extractBackground(color(column, row))
    else PackedColor.unpackBackground(color(column, row), data.format)

  /**
    * Whether the background color of the text buffer at the specified
    * location if from the color palette.
    *
    * @param column the horizontal index.
    * @param row    the vertical index.
    * @return whether the background at that index is from the palette.
    */
  def isBackgroundFromPalette(column: Int, row: Int): Boolean =
    data.format.isFromPalette(PackedColor.extractBackground(color(column, row)))

  /**
    * Overwrites a portion of the text in raw mode.
    *
    * This will copy the given char array into the buffer, starting at the
    * specified column and row. The array is expected to be indexed row-
    * first, i.e. the first dimension is the vertical axis, the second
    * the horizontal.
    *
    * '''Important''': this performs no checks as to whether something
    * actually changed. It will always send the changed patch to clients.
    * It will also not crop the specified array to the actually used range.
    * In other words, this is not intended to be exposed as-is to user code,
    * it should always be called with validated, and, as necessary, cropped
    * values.
    *
    * @param column the horizontal index.
    * @param row    the vertical index.
    * @param text   the text to write.
    */
  def rawSetText(column: Int, row: Int, text: Array[Array[Char]]): Unit = {
    for (y <- row until ((row + text.length) min data.height)) {
      val line = text(y - row)
      Array.copy(line, 0, data.buffer(y), column, line.length min data.width)
    }
  }

  /**
    * Overwrites a portion of the foreground color information in raw mode.
    *
    * This will convert the specified RGB data (in `0xRRGGBB` format)
    * to the internal, packed representation and copy it into the buffer,
    * starting at the specified column and row. The array is expected to be
    * indexed row-first, i.e. the first dimension is the vertical axis, the
    * second the horizontal.
    *
    * '''Important''': this performs no checks as to whether something
    * actually changed. It will always send the changed patch to clients.
    * It will also not crop the specified array to the actually used range.
    * In other words, this is not intended to be exposed as-is to user code,
    * it should always be called with validated, and, as necessary, cropped
    * values.
    *
    * @param column the horizontal index.
    * @param row    the vertical index.
    * @param color  the foreground color data to write.
    */
  def rawSetForeground(column: Int, row: Int, color: Array[Array[Int]]): Unit = {
    for (y <- row until ((row + color.length) min data.height)) {
      val line = color(y - row)
      for (x <- column until ((column + line.length) min data.width)) {
        val packedBackground = data.format.deflate(PackedColor.Color(line(x - column))) & 0x00FF
        val packedForeground = data.color(row)(column) & 0xFF00
        data.color(row)(column) = (packedForeground | packedBackground).toShort
      }
    }
  }

  /**
    * Overwrites a portion of the background color information in raw mode.
    *
    * This will convert the specified RGB data (in `0xRRGGBB` format)
    * to the internal, packed representation and copy it into the buffer,
    * starting at the specified column and row. The array is expected to be
    * indexed row-first, i.e. the first dimension is the vertical axis, the
    * second the horizontal.
    *
    * '''Important''': this performs no checks as to whether something
    * actually changed. It will always send the changed patch to clients.
    * It will also not crop the specified array to the actually used range.
    * In other words, this is not intended to be exposed as-is to user code,
    * it should always be called with validated, and, as necessary, cropped
    * values.
    *
    * @param column the horizontal index.
    * @param row    the vertical index.
    * @param color  the background color data to write.
    */
  def rawSetBackground(column: Int, row: Int, color: Array[Array[Int]]): Unit = {
    for (y <- row until ((row + color.length) min data.height)) {
      val line = color(y - row)
      for (x <- column until ((column + line.length) min data.width)) {
        val packedBackground = data.color(row)(column) & 0x00FF
        val packedForeground = (data.format.deflate(PackedColor.Color(line(x - column))) << PackedColor.ForegroundShift) & 0xFF00
        data.color(row)(column) = (packedForeground | packedBackground).toShort
      }
    }
  }

  private def color(column: Int, row: Int) = {
    if (column < 0 || column >= getWidth || row < 0 || row >= getHeight)
      throw new IndexOutOfBoundsException()
    else data.color(row)(column)
  }

  /**
    * Signals a key down event for the buffer.
    *
    * This will trigger a message that will be picked up by
    * keyboards, which will then cause a signal in attached machines.
    *
    * @param character the character of the pressed key.
    * @param code      the key code of the pressed key.
    * @param player    the player that pressed the key. Pass `null` on the client side.
    */
  def keyDown(character: Char, code: Int, player: User) {
    sendToKeyboards("keyboard.keyDown", player, Char.box(character), Int.box(code))
  }

  /**
    * Signals a key up event for the buffer.
    *
    * This will trigger a message that will be picked up by
    * keyboards, which will then cause a signal in attached machines.
    *
    * @param character the character of the released key.
    * @param code      the key code of the released key.
    * @param player    the player that released the key. Pass `null` on the client side.
    */
  def keyUp(character: Char, code: Int, player: User) {
    sendToKeyboards("keyboard.keyUp", player, Char.box(character), Int.box(code))
  }

  /**
    * Signals a clipboard paste event for the buffer.
    *
    * This will trigger a message that will be picked up by
    * keyboards, which will then cause a signal in attached machines.
    *
    * @param value  the text that was pasted.
    * @param player the player that pasted the text. Pass `null` on the client side.
    */
  def clipboard(value: String, player: User) {
    sendToKeyboards("keyboard.clipboard", player, value)
  }

  /**
    * Signals a mouse button down event for the buffer.
    *
    * This will cause a signal in attached machines.
    *
    * @param x      the horizontal coordinate of the mouse, in characters.
    * @param y      the vertical coordinate of the mouse, in characters.
    * @param button the button of the mouse that was pressed.
    * @param player the player that pressed the mouse button. Pass `null` on the client side.
    */
  def mouseDown(x: Double, y: Double, button: Int, player: User) {
    sendMouseEvent(player, "touch", x, y, button)
  }

  /**
    * Signals a mouse drag event for the buffer.
    *
    * This will cause a signal in attached machines.
    *
    * @param x      the horizontal coordinate of the mouse, in characters.
    * @param y      the vertical coordinate of the mouse, in characters.
    * @param button the button of the mouse that is pressed.
    * @param player the player that moved the mouse. Pass `null` on the client side.
    */
  def mouseDrag(x: Double, y: Double, button: Int, player: User) {
    sendMouseEvent(player, "drag", x, y, button)
  }

  /**
    * Signals a mouse button release event for the buffer.
    *
    * This will cause a signal in attached machines.
    *
    * @param x      the horizontal coordinate of the mouse, in characters.
    * @param y      the vertical coordinate of the mouse, in characters.
    * @param button the button of the mouse that was released.
    * @param player the player that released the mouse button. Pass `null` on the client side.
    */
  def mouseUp(x: Double, y: Double, button: Int, player: User) {
    sendMouseEvent(player, "drop", x, y, button)
  }

  /**
    * Signals a mouse wheel scroll event for the buffer.
    *
    * This will cause a signal in attached machines.
    *
    * @param x      the horizontal coordinate of the mouse, in characters.
    * @param y      the vertical coordinate of the mouse, in characters.
    * @param delta  indicates the direction of the mouse scroll.
    * @param player the player that scrolled the mouse wheel. Pass `null` on the client side.
    */
  def mouseScroll(x: Double, y: Double, delta: Int, player: User) {
    sendMouseEvent(player, "scroll", x, y, delta)
  }

  private def sendMouseEvent(player: User, name: String, x: Double, y: Double, data: Int): Unit = {
    val args = mutable.ArrayBuffer.empty[AnyRef]

    args += player
    args += name
    if (precisionMode) {
      args += Double.box(x)
      args += Double.box(y)
    }
    else {
      args += Int.box(x.toInt + 1)
      args += Int.box(y.toInt + 1)
    }
    args += Int.box(data)
    if (Settings.get.inputUsername) {
      args += player.nickname
    }

    node.sendToReachable("computer.checked_signal", args: _*)
  }

  private def sendToKeyboards(name: String, values: AnyRef*) {
    node.sendToNeighbors(name, values: _*)
  }

  // ----------------------------------------------------------------------- //

  private final val DataTag = "data"
  private final val IsOnTag = "isOn"
  private final val MaxWidthTag = "maxWidth"
  private final val MaxHeightTag = "maxHeight"
  private final val PreciseTag = "precise"
  private final val ViewportWidthTag = "viewportWidth"
  private final val ViewportHeightTag = "viewportHeight"

  override def load(nbt: NBTTagCompound) {
    super.load(nbt)

    if (nbt.hasKey(Node.BufferTag)) {
      data.load(nbt.getCompoundTag(Node.BufferTag))
    }

    if (nbt.hasKey(IsOnTag)) {
      isDisplaying = nbt.getBoolean(IsOnTag)
    }
    if (nbt.hasKey(MaxWidthTag) && nbt.hasKey(MaxHeightTag)) {
      val maxWidth = nbt.getInteger(MaxWidthTag)
      val maxHeight = nbt.getInteger(MaxHeightTag)
      maxResolution = (maxWidth, maxHeight)
    }
    precisionMode = nbt.getBoolean(PreciseTag)

    if (nbt.hasKey(ViewportWidthTag)) {
      val vpw = nbt.getInteger(ViewportWidthTag)
      val vph = nbt.getInteger(ViewportHeightTag)
      viewport = (vpw min data.width max 1, vph min data.height max 1)
    } else {
      viewport = data.size
    }
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    // Happy thread synchronization hack! Here's the problem: GPUs allow direct
    // calls for modifying screens to give a more responsive experience. This
    // causes the following problem: when saving, if the screen is saved first,
    // then the executor runs in parallel and changes the screen *before* the
    // server thread begins saving that computer, the saved computer will think
    // it changed the screen, although the saved screen wasn't. To avoid that we
    // wait for all computers the screen is connected to to finish their current
    // execution and pausing them (which will make them resume in the next tick
    // when their update() runs).
    if (node.network != null) {
      for (node <- node.network.nodes) node.host match {
        case computer: traits.Computer if !computer.machine.isPaused =>
          computer.machine.pause(0.1)
        case _ =>
      }
    }

    val dataNbt = new NBTTagCompound()
    data.save(dataNbt)
    nbt.setTag(DataTag, dataNbt)

    nbt.setBoolean(IsOnTag, isDisplaying)
    nbt.setInteger(MaxWidthTag, maxResolution._1)
    nbt.setInteger(MaxHeightTag, maxResolution._2)
    nbt.setBoolean(PreciseTag, precisionMode)
    nbt.setInteger(ViewportWidthTag, viewport._1)
    nbt.setInteger(ViewportHeightTag, viewport._2)
  }
}
