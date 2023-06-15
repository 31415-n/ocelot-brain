package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{DeviceInfo, Entity, Environment, TieredPersistable, WorkspaceAware}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.{Network, Node, Visibility}
import totoro.ocelot.brain.util.Tier.Tier
import totoro.ocelot.brain.workspace.Workspace
import totoro.ocelot.brain.{Constants, Settings}

//noinspection ScalaUnusedSymbol,ScalaWeakerAccess
class HologramProjector(override var tier: Tier) extends Entity with Environment with TieredPersistable
  with WorkspaceAware with DeviceInfo
{
  override val node: Node = Network.newNode(this, Visibility.Network)
    .withComponent("hologram")
    .create()

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Display,
    DeviceAttribute.Description -> "Holographic projector",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> s"VirtualViewer H1-${tier.num}",
    DeviceAttribute.Capacity -> (width * width * height).toString,
    DeviceAttribute.Width -> colors.length.toString
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo

  final val width = 3 * 16
  final val height = 2 * 16

  final val colorsByTier = Array(Array(0x00FF00), Array(0xFF0000, 0x00FF00, 0x0000FF))

  val colors: Array[Int] = colorsByTier(tier.id)

  // Layout is: first half is lower bit, second half is higher bit for the voxels in the cube.
  val volume = new Array[Int](width * width * 2)

  // true if the volume has changed and the mesh needs to be rebuilt
  var isDirty = false

  var scale = 1.0f

  var translationX = 0f
  var translationY = 0f
  var translationZ = 0f

  var rotationAngle = 0f
  var rotationX = 0f
  var rotationY = 0f
  var rotationZ = 0f
  var rotationSpeed = 0f
  var rotationSpeedX = 0f
  var rotationSpeedY = 0f
  var rotationSpeedZ = 0f

  def getColor(x: Int, y: Int, z: Int): Int = {
    val lbit = (volume(x + z * width) >>> y) & 1
    val hbit = (volume(x + z * width + width * width) >>> y) & 1
    lbit | (hbit << 1)
  }

  def setColor(x: Int, y: Int, z: Int, value: Int): Unit = {
    if ((value & 3) != getColor(x, y, z)) {
      val lbit = value & 1
      val hbit = (value >>> 1) & 1
      volume(x + z * width) = (volume(x + z * width) & ~(1 << y)) | (lbit << y)
      volume(x + z * width + width * width) = (volume(x + z * width + width * width) & ~(1 << y)) | (hbit << y)
      isDirty = true
    }
  }

  @Callback(doc = """function() -- Clears the hologram.""")
  def clear(context: Context, args: Arguments): Array[AnyRef] = {
    for (i <- volume.indices) volume(i) = 0
    isDirty = true
    null
  }

  @Callback(direct = true, doc = """function(x:number, y:number, z:number):number -- Returns the value for the specified voxel.""")
  def get(context: Context, args: Arguments): Array[AnyRef] = {
    val (x, y, z) = checkCoordinates(args)
    result(getColor(x, y, z))
  }

  @Callback(direct = true, limit = 256, doc = """function(x:number, y:number, z:number, value:number or boolean) -- Set the value for the specified voxel.""")
  def set(context: Context, args: Arguments): Array[AnyRef] = {
    val (x, y, z) = checkCoordinates(args)
    val value = checkColor(args, 3)
    setColor(x, y, z, value)
    null
  }

  @Callback(direct = true, limit = 128, doc = """function(x:number, z:number[, minY:number], maxY:number, value:number or boolean) -- Fills an interval of a column with the specified value.""")
  def fill(context: Context, args: Arguments): Array[AnyRef] = {
    val (x, _, z) = checkCoordinates(args, 0, -1, 1)
    val (minY, maxY, value) =
      if (args.count > 4)
        (math.min(32, math.max(1, args.checkInteger(2))), math.min(32, math.max(1, args.checkInteger(3))), checkColor(args, 4))
      else
        (1, math.min(32, math.max(1, args.checkInteger(2))), checkColor(args, 3))
    if (minY > maxY) throw new IllegalArgumentException("interval is empty")

    val mask = (0xFFFFFFFF >>> (31 - (maxY - minY))) << (minY - 1)
    val lbit = value & 1
    val hbit = (value >>> 1) & 1
    if (lbit == 0 || height == 0) volume(x + z * width) &= ~mask
    else volume(x + z * width) |= mask
    if (hbit == 0 || height == 0) volume(x + z * width + width * width) &= ~mask
    else volume(x + z * width + width * width) |= mask

    isDirty = true
    null
  }

  @Callback(doc = """function(data:string) -- Set the raw buffer to the specified byte array, where each byte represents a voxel color. Nesting is x,z,y.""")
  def setRaw(context: Context, args: Arguments): Array[AnyRef] = {
    val data = args.checkByteArray(0)
    for (x <- 0 until width; z <- 0 until width) {
      val offset = z * height + x * height * width
      if (data.length >= offset + height) {
        var lbit = 0
        var hbit = 0
        for (y <- (height - 1) to 0 by -1) {
          val color = data(offset + y)
          lbit |= (color & 1) << y
          hbit |= ((color & 3) >>> 1) << y
        }
        val index = x + z * width
        if (volume(index) != lbit || volume(index + width * width) != hbit) {
          volume(index) = lbit
          volume(index + width * width) = hbit
          isDirty = true
        }
      }
    }
    context.pause(Settings.get.hologramSetRawDelay)
    null
  }

  @Callback(doc = """function(x:number, z:number, sx:number, sz:number, tx:number, tz:number) -- Copies an area of columns by the specified translation.""")
  def copy(context: Context, args: Arguments): Array[AnyRef] = {
    val (x, _, z) = checkCoordinates(args, 0, -1, 1)
    val w = args.checkInteger(2)
    val h = args.checkInteger(3)
    val tx = args.checkInteger(4)
    val tz = args.checkInteger(5)

    // Anything to do at all?
    if (w <= 0 || h <= 0) return null
    if (tx == 0 && tz == 0) return null
    // Loop over the target rectangle, starting from the directions away from
    // the source rectangle and copy the data. This way we ensure we don't
    // overwrite anything we still need to copy.
    val (dx0, dx1) = (math.max(0, math.min(width - 1, x + tx + w - 1)), math.max(0, math.min(width, x + tx))) match {
      case dx if tx > 0 => dx
      case dx => dx.swap
    }
    val (dz0, dz1) = (math.max(0, math.min(width - 1, z + tz + h - 1)), math.max(0, math.min(width, z + tz))) match {
      case dz if tz > 0 => dz
      case dz => dz.swap
    }
    val (sx, sz) = (if (tx > 0) -1 else 1, if (tz > 0) -1 else 1)
    // Copy values to destination rectangle if there source is valid.
    for (nz <- dz0 to dz1 by sz) {
      nz - tz match {
        case oz if oz >= 0 && oz < width =>
          for (nx <- dx0 to dx1 by sx) {
            nx - tx match {
              case ox if ox >= 0 && ox < width =>
                volume(nz * width + nx) = volume(oz * width + ox)
                volume(nz * width + nx + width * width) = volume(oz * width + ox + width * width)
                isDirty = true
              case _ => /* Got no source column. */
            }
          }
        case _ => /* Got no source row. */
      }
    }

    // The reasoning here is: it'd take 18 ticks to do the whole are with fills,
    // so make this slightly more efficient (15 ticks - 0.75 seconds). Make it
    // 'free' if it's less than 0.25 seconds, i.e. for small copies.
    val area = (math.max(dx0, dx1) - math.min(dx0, dx1)) * (math.max(dz0, dz1) - math.min(dz0, dz1))
    val relativeArea = math.max(0, area / (width * width).toFloat - 0.25)
    context.pause(relativeArea)

    null
  }

  @Callback(direct = true, doc = """function():number -- Returns the render scale of the hologram.""")
  def getScale(context: Context, args: Arguments): Array[AnyRef] = {
    result(scale)
  }

  @Callback(doc = """function(value:number) -- Set the render scale. A larger scale consumes more energy.""")
  def setScale(context: Context, args: Arguments): Array[AnyRef] = {
    scale = math.max(0.333333, math.min(Settings.get.hologramMaxScaleByTier(tier.id), args.checkDouble(0))).toFloat
    null
  }

  @Callback(direct = true, doc = """function():number, number, number -- Returns the relative render projection offsets of the hologram.""")
  def getTranslation(context: Context, args: Arguments): Array[AnyRef] = {
    result(translationX, translationY, translationZ)
  }

  @Callback(doc = """function(tx:number, ty:number, tz:number) -- Sets the relative render projection offsets of the hologram.""")
  def setTranslation(context: Context, args: Arguments): Array[AnyRef] = {
    // Validate all axes before setting the values.
    val maxTranslation = Settings.get.hologramMaxTranslationByTier(tier.id)
    translationX = math.max(-maxTranslation, math.min(maxTranslation, args.checkDouble(0))).toFloat
    translationY = math.max(0, math.min(maxTranslation * 2, args.checkDouble(1))).toFloat
    translationZ = math.max(-maxTranslation, math.min(maxTranslation, args.checkDouble(2))).toFloat

    null
  }

  @Callback(direct = true, doc = """function():number -- The color depth supported by the hologram.""")
  def maxDepth(context: Context, args: Arguments): Array[AnyRef] = {
    result(tier.num)
  }

  @Callback(doc = """function(index:number):number -- Get the color defined for the specified value.""")
  def getPaletteColor(context: Context, args: Arguments): Array[AnyRef] = {
    val index = args.checkInteger(0)
    if (index < 1 || index > colors.length) throw new ArrayIndexOutOfBoundsException()
    result(colors(index - 1))
  }

  @Callback(doc = """function(index:number, value:number):number -- Set the color defined for the specified value.""")
  def setPaletteColor(context: Context, args: Arguments): Array[AnyRef] = {
    val index = args.checkInteger(0)
    if (index < 1 || index > colors.length) throw new ArrayIndexOutOfBoundsException()
    val value = args.checkInteger(1)
    val oldValue = colors(index - 1)
    colors(index - 1) = value
    result(oldValue)
  }

  @Callback(doc = """function(angle:number, x:number, y:number, z:number):boolean -- Set the base rotation of the displayed hologram.""")
  def setRotation(context: Context, args: Arguments): Array[AnyRef] = {
    if (tier.num >= 2) {
      val r = args.checkDouble(0) % 360
      val x = args.checkDouble(1)
      val y = args.checkDouble(2)
      val z = args.checkDouble(3)

      rotationAngle = r.toFloat
      rotationX = x.toFloat
      rotationY = y.toFloat
      rotationZ = z.toFloat

      result(true)
    }
    else result((), "not supported")
  }

  @Callback(doc = """function(speed:number, x:number, y:number, z:number):boolean -- Set the rotation speed of the displayed hologram.""")
  def setRotationSpeed(context: Context, args: Arguments): Array[AnyRef] = {
    if (tier.num >= 2) {
      val v = args.checkDouble(0) max -360 * 4 min 360 * 4
      val x = args.checkDouble(1)
      val y = args.checkDouble(2)
      val z = args.checkDouble(3)

      rotationSpeed = v.toFloat
      rotationSpeedX = x.toFloat
      rotationSpeedY = y.toFloat
      rotationSpeedZ = z.toFloat

      result(true)
    }
    else result((), "not supported")
  }

  @Callback(direct = true, doc = "function():number, number, number -- Get the dimension of the x,y,z axes.")
  def getDimensions(context: Context, args: Arguments): Array[AnyRef] = {
    result(width, height, width)
  }

  private def checkCoordinates(args: Arguments, idxX: Int = 0, idxY: Int = 1, idxZ: Int = 2): (Int, Int, Int) = {
    val x = if (idxX >= 0) args.checkInteger(idxX) - 1 else 0
    if (x < 0 || x >= width) throw new ArrayIndexOutOfBoundsException("x")
    val y = if (idxY >= 0) args.checkInteger(idxY) - 1 else 0
    if (y < 0 || y >= height) throw new ArrayIndexOutOfBoundsException("y")
    val z = if (idxZ >= 0) args.checkInteger(idxZ) - 1 else 0
    if (z < 0 || z >= width) throw new ArrayIndexOutOfBoundsException("z")
    (x, y, z)
  }

  private def checkColor(args: Arguments, index: Int): Int = {
    val value =
      if (args.isBoolean(index))
        if (args.checkBoolean(index)) 1 else 0
      else
        args.checkInteger(index)
    if (value < 0 || value > colors.length) throw new IllegalArgumentException("invalid value")
    value
  }

  private final val VolumeTag = "volume"
  private final val ColorsTag = "colors"
  private final val ScaleTag = "scale"
  private final val OffsetXTag = "offsetX"
  private final val OffsetYTag = "offsetY"
  private final val OffsetZTag = "offsetZ"
  private final val RotationAngleTag = "rotationAngle"
  private final val RotationXTag = "rotationX"
  private final val RotationYTag = "rotationY"
  private final val RotationZTag = "rotationZ"
  private final val RotationSpeedTag = "rotationSpeed"
  private final val RotationSpeedXTag = "rotationSpeedX"
  private final val RotationSpeedYTag = "rotationSpeedY"
  private final val RotationSpeedZTag = "rotationSpeedZ"

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    nbt.setIntArray(VolumeTag, volume)
    nbt.setIntArray(ColorsTag, colors)
    nbt.setFloat(ScaleTag, scale)
    nbt.setFloat(OffsetXTag, translationX)
    nbt.setFloat(OffsetYTag, translationY)
    nbt.setFloat(OffsetZTag, translationZ)
    nbt.setFloat(RotationAngleTag, rotationAngle)
    nbt.setFloat(RotationXTag, rotationX)
    nbt.setFloat(RotationYTag, rotationY)
    nbt.setFloat(RotationZTag, rotationZ)
    nbt.setFloat(RotationSpeedTag, rotationSpeed)
    nbt.setFloat(RotationSpeedXTag, rotationSpeedX)
    nbt.setFloat(RotationSpeedYTag, rotationSpeedY)
    nbt.setFloat(RotationSpeedZTag, rotationSpeedZ)
  }

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)
    nbt.getIntArray(VolumeTag).copyToArray(volume)
    nbt.getIntArray(ColorsTag).copyToArray(colors)
    scale = nbt.getFloat(ScaleTag)
    translationX = nbt.getFloat(OffsetXTag)
    translationY = nbt.getFloat(OffsetYTag)
    translationZ = nbt.getFloat(OffsetZTag)
    rotationAngle = nbt.getFloat(RotationAngleTag)
    rotationX = nbt.getFloat(RotationXTag)
    rotationY = nbt.getFloat(RotationYTag)
    rotationZ = nbt.getFloat(RotationZTag)
    rotationSpeed = nbt.getFloat(RotationSpeedTag)
    rotationSpeedX = nbt.getFloat(RotationSpeedXTag)
    rotationSpeedY = nbt.getFloat(RotationSpeedYTag)
    rotationSpeedZ = nbt.getFloat(RotationSpeedZTag)
  }
}
