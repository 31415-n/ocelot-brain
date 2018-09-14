package li.cil.oc.common.component

import li.cil.oc.api.driver.DeviceInfo
import li.cil.oc.api.driver.DeviceInfo.{DeviceAttribute, DeviceClass}
import li.cil.oc.api.internal.TextBuffer.ColorDepth
import li.cil.oc.api.machine.{Arguments, Callback, Context}
import li.cil.oc.api.network.{EnvironmentHost, _}
import li.cil.oc.api.prefab.AbstractManagedEnvironment
import li.cil.oc.common._
import li.cil.oc.common.item.data.NodeData
import li.cil.oc.server.component.Keyboard
import li.cil.oc.util.PackedColor
import li.cil.oc.{Constants, Settings, api, util}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound

import scala.collection.convert.WrapAsJava._
import scala.collection.convert.WrapAsScala._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class TextBuffer(val host: EnvironmentHost) extends AbstractManagedEnvironment with api.internal.TextBuffer with DeviceInfo {
  override val node: Component = api.Network.newNode(this, Visibility.Network).
    withComponent("screen").
    create()

  private var maxResolution = Settings.screenResolutionsByTier(Tier.One)

  private var maxDepth = Settings.screenDepthsByTier(Tier.One)

  private var aspectRatio = (1.0, 1.0)

  private var precisionMode = false

  // For client side only.
  private var isRendering = true

  private var isDisplaying = true

  private var relativeLitArea = -1.0

  private val syncInterval = 100

  private var syncCooldown = syncInterval

  val proxy: TextBuffer.Proxy =
    new TextBuffer.ServerProxy(this)

  val data = new util.TextBuffer(maxResolution, PackedColor.Depth.format(maxDepth))

  var viewport: (Int, Int) = data.size

  def markInitialized(): Unit = {
    syncCooldown = -1 // Stop polling for init state.
    relativeLitArea = -1 // Recompute lit area, avoid screens blanking out until something changes.
  }

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Display,
    DeviceAttribute.Description -> "Text buffer",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "Text Screen V0",
    DeviceAttribute.Capacity -> (maxResolution._1 * maxResolution._2).toString,
    DeviceAttribute.Width -> Array("1", "4", "8").apply(maxDepth.ordinal())
  )

  override def getDeviceInfo: java.util.Map[String, String] = deviceInfo

  // ----------------------------------------------------------------------- //

  override val canUpdate = true

  override def update() {
    super.update()
    if (isDisplaying) {
      if (relativeLitArea < 0) {
        // The relative lit area is the number of pixels that are not blank
        // versus the number of pixels in the *current* resolution. This is
        // scaled to multi-block screens, since we only compute this for the
        // origin.
        val w = getViewportWidth
        val h = getViewportHeight
        var acc = 0f
        for (y <- 0 until h) {
          val line = data.buffer(y)
          val colors = data.color(y)
          for (x <- 0 until w) {
            val char = line(x)
            val color = colors(x)
            val bg = PackedColor.unpackBackground(color, data.format)
            val fg = PackedColor.unpackForeground(color, data.format)
            acc += (if (char == ' ') if (bg == 0) 0 else 1
            else if (char == 0x2588) if (fg == 0) 0 else 1
            else if (fg == 0 && bg == 0) 0 else 1)
          }
        }
        relativeLitArea = acc / (w * h).toDouble
      }
    }
  }

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

  override def setPowerState(value: Boolean) {
     isDisplaying = value
  }

  override def getPowerState: Boolean = isDisplaying

  override def setMaximumResolution(width: Int, height: Int) {
    if (width < 1) throw new IllegalArgumentException("width must be larger or equal to one")
    if (height < 1) throw new IllegalArgumentException("height must be larger or equal to one")
    maxResolution = (width, height)
    proxy.onBufferMaxResolutionChange(width, width)
  }

  override def getMaximumWidth: Int = maxResolution._1

  override def getMaximumHeight: Int = maxResolution._2

  override def setAspectRatio(width: Double, height: Double): Unit = this.synchronized(aspectRatio = (width, height))

  override def getAspectRatio: Double = aspectRatio._1 / aspectRatio._2

  override def setResolution(w: Int, h: Int): Boolean = {
    val (mw, mh) = maxResolution
    if (w < 1 || h < 1 || w > mw || h > mw || h * w > mw * mh)
      throw new IllegalArgumentException("unsupported resolution")
    // Always send to clients, their state might be dirty.
    proxy.onBufferResolutionChange(w, h)
    // Force set viewport to new resolution. This is partially for
    // backwards compatibility, and partially to enforce a valid one.
    val sizeChanged = data.size = (w, h)
    val viewportChanged = setViewport(w, h)
    if (sizeChanged || viewportChanged) {
      if (!viewportChanged && node != null) {
        node.sendToReachable("computer.signal", "screen_resized", Int.box(w), Int.box(h))
      }
      true
    }
    else false
  }

  override def getWidth: Int = data.width

  override def getHeight: Int = data.height

  override def setViewport(w: Int, h: Int): Boolean = {
    val (mw, mh) = data.size
    if (w < 1 || h < 1 || w > mw || h > mh)
      throw new IllegalArgumentException("unsupported viewport resolution")
    // Always send to clients, their state might be dirty.
    proxy.onBufferViewportResolutionChange(w, h)
    val (cw, ch) = viewport
    if (w != cw || h != ch) {
      viewport = (w, h)
      if (node != null) {
        node.sendToReachable("computer.signal", "screen_resized", Int.box(w), Int.box(h))
      }
      true
    }
    else false
  }

  override def getViewportWidth: Int = viewport._1

  override def getViewportHeight: Int = viewport._2

  override def setMaximumColorDepth(depth: api.internal.TextBuffer.ColorDepth): Unit = maxDepth = depth

  override def getMaximumColorDepth: ColorDepth = maxDepth

  override def setColorDepth(depth: api.internal.TextBuffer.ColorDepth): Boolean = {
    if (depth.ordinal > maxDepth.ordinal)
      throw new IllegalArgumentException("unsupported depth")
    // Always send to clients, their state might be dirty.
    proxy.onBufferDepthChange(depth)
    data.format = PackedColor.Depth.format(depth)
  }

  override def getColorDepth: ColorDepth = data.format.depth

  override def setPaletteColor(index: Int, color: Int): Unit = data.format match {
    case palette: PackedColor.MutablePaletteFormat =>
      palette(index) = color
      proxy.onBufferPaletteChange(index)
    case _ => throw new Exception("palette not available")
  }

  override def getPaletteColor(index: Int): Int = data.format match {
    case palette: PackedColor.MutablePaletteFormat => palette(index)
    case _ => throw new Exception("palette not available")
  }

  override def setForegroundColor(color: Int): Unit = setForegroundColor(color, isFromPalette = false)

  override def setForegroundColor(color: Int, isFromPalette: Boolean) {
    val value = PackedColor.Color(color, isFromPalette)
    if (data.foreground != value) {
      data.foreground = value
      proxy.onBufferColorChange()
    }
  }

  override def getForegroundColor: Int = data.foreground.value

  override def isForegroundFromPalette: Boolean = data.foreground.isPalette

  override def setBackgroundColor(color: Int): Unit = setBackgroundColor(color, isFromPalette = false)

  override def setBackgroundColor(color: Int, isFromPalette: Boolean) {
    val value = PackedColor.Color(color, isFromPalette)
    if (data.background != value) {
      data.background = value
      proxy.onBufferColorChange()
    }
  }

  override def getBackgroundColor: Int = data.background.value

  override def isBackgroundFromPalette: Boolean = data.background.isPalette

  def copy(col: Int, row: Int, w: Int, h: Int, tx: Int, ty: Int): Unit =
    if (data.copy(col, row, w, h, tx, ty))
      proxy.onBufferCopy(col, row, w, h, tx, ty)

  def fill(col: Int, row: Int, w: Int, h: Int, c: Char): Unit =
    if (data.fill(col, row, w, h, c))
      proxy.onBufferFill(col, row, w, h, c)

  def set(col: Int, row: Int, s: String, vertical: Boolean): Unit =
    if (col < data.width && (col >= 0 || -col < s.length)) {
      // Make sure the string isn't longer than it needs to be, in particular to
      // avoid sending too much data to our clients.
      val (x, y, truncated) =
        if (vertical) {
          if (row < 0) (col, 0, s.substring(-row))
          else (col, row, s.substring(0, math.min(s.length, data.height - row)))
        }
        else {
          if (col < 0) (0, row, s.substring(-col))
          else (col, row, s.substring(0, math.min(s.length, data.width - col)))
        }
      if (data.set(x, y, truncated, vertical))
        proxy.onBufferSet(x, row, truncated, vertical)
    }

  def get(col: Int, row: Int): Char = data.get(col, row)

  override def getForegroundColor(column: Int, row: Int): Int =
    if (isForegroundFromPalette(column, row)) {
      PackedColor.extractForeground(color(column, row))
    }
    else {
      PackedColor.unpackForeground(color(column, row), data.format)
    }

  override def isForegroundFromPalette(column: Int, row: Int): Boolean =
    data.format.isFromPalette(PackedColor.extractForeground(color(column, row)))

  override def getBackgroundColor(column: Int, row: Int): Int =
    if (isBackgroundFromPalette(column, row)) {
      PackedColor.extractBackground(color(column, row))
    }
    else {
      PackedColor.unpackBackground(color(column, row), data.format)
    }

  override def isBackgroundFromPalette(column: Int, row: Int): Boolean =
    data.format.isFromPalette(PackedColor.extractBackground(color(column, row)))

  override def rawSetText(col: Int, row: Int, text: Array[Array[Char]]): Unit = {
    for (y <- row until ((row + text.length) min data.height)) {
      val line = text(y - row)
      Array.copy(line, 0, data.buffer(y), col, line.length min data.width)
    }
    proxy.onBufferRawSetText(col, row, text)
  }

  override def rawSetBackground(col: Int, row: Int, color: Array[Array[Int]]): Unit = {
    for (y <- row until ((row + color.length) min data.height)) {
      val line = color(y - row)
      for (x <- col until ((col + line.length) min data.width)) {
        val packedBackground = data.color(row)(col) & 0x00FF
        val packedForeground = (data.format.deflate(PackedColor.Color(line(x - col))) << PackedColor.ForegroundShift) & 0xFF00
        data.color(row)(col) = (packedForeground | packedBackground).toShort
      }
    }
    // Better for bandwidth to send packed shorts here. Would need a special case for handling on client,
    // though, so let's be wasteful for once...
    proxy.onBufferRawSetBackground(col, row, color)
  }

  override def rawSetForeground(col: Int, row: Int, color: Array[Array[Int]]): Unit = {
    for (y <- row until ((row + color.length) min data.height)) {
      val line = color(y - row)
      for (x <- col until ((col + line.length) min data.width)) {
        val packedBackground = data.format.deflate(PackedColor.Color(line(x - col))) & 0x00FF
        val packedForeground = data.color(row)(col) & 0xFF00
        data.color(row)(col) = (packedForeground | packedBackground).toShort
      }
    }
    // Better for bandwidth to send packed shorts here. Would need a special case for handling on client,
    // though, so let's be wasteful for once...
    proxy.onBufferRawSetForeground(col, row, color)
  }

  private def color(column: Int, row: Int) = {
    if (column < 0 || column >= getWidth || row < 0 || row >= getHeight)
      throw new IndexOutOfBoundsException()
    else data.color(row)(column)
  }

  override def renderText(): Boolean = relativeLitArea != 0 && proxy.render()

  override def setRenderingEnabled(enabled: Boolean): Unit = isRendering = enabled

  override def isRenderingEnabled: Boolean = isRendering

  override def keyDown(character: Char, code: Int, player: EntityPlayer): Unit =
    proxy.keyDown(character, code, player)

  override def keyUp(character: Char, code: Int, player: EntityPlayer): Unit =
    proxy.keyUp(character, code, player)

  override def clipboard(value: String, player: EntityPlayer): Unit =
    proxy.clipboard(value, player)

  override def mouseDown(x: Double, y: Double, button: Int, player: EntityPlayer): Unit =
    proxy.mouseDown(x, y, button, player)

  override def mouseDrag(x: Double, y: Double, button: Int, player: EntityPlayer): Unit =
    proxy.mouseDrag(x, y, button, player)

  override def mouseUp(x: Double, y: Double, button: Int, player: EntityPlayer): Unit =
    proxy.mouseUp(x, y, button, player)

  override def mouseScroll(x: Double, y: Double, delta: Int, player: EntityPlayer): Unit =
    proxy.mouseScroll(x, y, delta, player)

  // ----------------------------------------------------------------------- //

  override def onConnect(node: Node) {
    super.onConnect(node)
  }

  override def onDisconnect(node: Node) {
    super.onDisconnect(node)
  }

  // ----------------------------------------------------------------------- //

  private final val IsOnTag = Settings.namespace + "isOn"
  private final val MaxWidthTag = Settings.namespace + "maxWidth"
  private final val MaxHeightTag = Settings.namespace + "maxHeight"
  private final val PreciseTag = Settings.namespace + "precise"
  private final val ViewportWidthTag = Settings.namespace + "viewportWidth"
  private final val ViewportHeightTag = Settings.namespace + "viewportHeight"

  override def load(nbt: NBTTagCompound) {
    super.load(nbt)

    if (nbt.hasKey(NodeData.BufferTag)) {
      data.load(nbt.getCompoundTag(NodeData.BufferTag))
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

  // Null check for Waila (and other mods that may call this client side).
  override def save(nbt: NBTTagCompound): Unit = if (node != null) {
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
        case computer: tileentity.traits.Computer if !computer.machine.isPaused =>
          computer.machine.pause(0.1)
        case _ =>
      }
    }

    nbt.setBoolean(IsOnTag, isDisplaying)
    nbt.setInteger(MaxWidthTag, maxResolution._1)
    nbt.setInteger(MaxHeightTag, maxResolution._2)
    nbt.setBoolean(PreciseTag, precisionMode)
    nbt.setInteger(ViewportWidthTag, viewport._1)
    nbt.setInteger(ViewportHeightTag, viewport._2)
  }
}

object TextBuffer {
  var clientBuffers: ListBuffer[TextBuffer] = mutable.ListBuffer.empty[TextBuffer]

  abstract class Proxy {
    def owner: TextBuffer

    var dirty = false

    var nodeAddress = ""

    def markDirty() {
      dirty = true
    }

    def render() = false

    def onBufferColorChange(): Unit

    def onBufferCopy(col: Int, row: Int, w: Int, h: Int, tx: Int, ty: Int) {
      owner.relativeLitArea = -1
    }

    def onBufferDepthChange(depth: api.internal.TextBuffer.ColorDepth): Unit

    def onBufferFill(col: Int, row: Int, w: Int, h: Int, c: Char) {
      owner.relativeLitArea = -1
    }

    def onBufferPaletteChange(index: Int): Unit

    def onBufferResolutionChange(w: Int, h: Int) {
      owner.relativeLitArea = -1
    }

    def onBufferViewportResolutionChange(w: Int, h: Int) {
      owner.relativeLitArea = -1
    }

    def onBufferMaxResolutionChange(w: Int, h: Int) {
    }

    def onBufferSet(col: Int, row: Int, s: String, vertical: Boolean) {
      owner.relativeLitArea = -1
    }

    def onBufferRawSetText(col: Int, row: Int, text: Array[Array[Char]]) {
      owner.relativeLitArea = -1
    }

    def onBufferRawSetBackground(col: Int, row: Int, color: Array[Array[Int]]) {
      owner.relativeLitArea = -1
    }

    def onBufferRawSetForeground(col: Int, row: Int, color: Array[Array[Int]]) {
      owner.relativeLitArea = -1
    }

    def keyDown(character: Char, code: Int, player: EntityPlayer): Unit

    def keyUp(character: Char, code: Int, player: EntityPlayer): Unit

    def clipboard(value: String, player: EntityPlayer): Unit

    def mouseDown(x: Double, y: Double, button: Int, player: EntityPlayer): Unit

    def mouseDrag(x: Double, y: Double, button: Int, player: EntityPlayer): Unit

    def mouseUp(x: Double, y: Double, button: Int, player: EntityPlayer): Unit

    def mouseScroll(x: Double, y: Double, delta: Int, player: EntityPlayer): Unit
  }

  class ServerProxy(val owner: TextBuffer) extends Proxy {
    override def render(): Boolean = {
      true
    }

    override def onBufferColorChange() {
      markDirty()
    }

    override def onBufferCopy(col: Int, row: Int, w: Int, h: Int, tx: Int, ty: Int) {
      super.onBufferCopy(col, row, w, h, tx, ty)
      markDirty()
    }

    override def onBufferDepthChange(depth: api.internal.TextBuffer.ColorDepth) {
      markDirty()
    }

    override def onBufferFill(col: Int, row: Int, w: Int, h: Int, c: Char) {
      super.onBufferFill(col, row, w, h, c)
      markDirty()
    }

    override def onBufferPaletteChange(index: Int) {
      markDirty()
    }

    override def onBufferResolutionChange(w: Int, h: Int) {
      super.onBufferResolutionChange(w, h)
      markDirty()
    }

    override def onBufferViewportResolutionChange(w: Int, h: Int) {
      super.onBufferViewportResolutionChange(w, h)
      markDirty()
    }

    override def onBufferSet(col: Int, row: Int, s: String, vertical: Boolean) {
      super.onBufferSet(col, row, s, vertical)
      markDirty()
    }

    override def onBufferMaxResolutionChange(w: Int, h: Int) {
      super.onBufferMaxResolutionChange(w, h)
      markDirty()
    }

    override def onBufferRawSetText(col: Int, row: Int, text: Array[Array[Char]]) {
      super.onBufferRawSetText(col, row, text)
      markDirty()
    }

    override def onBufferRawSetBackground(col: Int, row: Int, color: Array[Array[Int]]) {
      super.onBufferRawSetBackground(col, row, color)
      markDirty()
    }

    override def onBufferRawSetForeground(col: Int, row: Int, color: Array[Array[Int]]) {
      super.onBufferRawSetForeground(col, row, color)
      markDirty()
    }

    override def keyDown(character: Char, code: Int, player: EntityPlayer) {
      sendToKeyboards("keyboard.keyDown", player, Char.box(character), Int.box(code))
    }

    override def keyUp(character: Char, code: Int, player: EntityPlayer) {
      sendToKeyboards("keyboard.keyUp", player, Char.box(character), Int.box(code))
    }

    override def clipboard(value: String, player: EntityPlayer) {
      sendToKeyboards("keyboard.clipboard", player, value)
    }

    override def mouseDown(x: Double, y: Double, button: Int, player: EntityPlayer) {
      sendMouseEvent(player, "touch", x, y, button)
    }

    override def mouseDrag(x: Double, y: Double, button: Int, player: EntityPlayer) {
      sendMouseEvent(player, "drag", x, y, button)
    }

    override def mouseUp(x: Double, y: Double, button: Int, player: EntityPlayer) {
      sendMouseEvent(player, "drop", x, y, button)
    }

    override def mouseScroll(x: Double, y: Double, delta: Int, player: EntityPlayer) {
      sendMouseEvent(player, "scroll", x, y, delta)
    }

    private def sendMouseEvent(player: EntityPlayer, name: String, x: Double, y: Double, data: Int): Unit = {
      val args = mutable.ArrayBuffer.empty[AnyRef]

      args += player
      args += name
      if (owner.precisionMode) {
        args += Double.box(x)
        args += Double.box(y)
      }
      else {
        args += Int.box(x.toInt + 1)
        args += Int.box(y.toInt + 1)
      }
      args += Int.box(data)
      if (Settings.get.inputUsername) {
        args += player.getName
      }

      owner.node.sendToReachable("computer.checked_signal", args: _*)
    }

    private def sendToKeyboards(name: String, values: AnyRef*) {
      owner.node.sendToNeighbors(name, values: _*)
    }
  }
}
