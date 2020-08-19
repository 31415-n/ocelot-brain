package totoro.ocelot.brain.entity.traits

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File}
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

import com.google.common.io.Files
import totoro.ocelot.brain.entity.fs.Label
import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.event.EventBus
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.{Component, Network, Visibility}
import totoro.ocelot.brain.workspace.Workspace
import totoro.ocelot.brain.{Ocelot, Settings}

/**
  * Basic trait for all unmanaged-disk-like entities.
  */
trait DiskUnmanaged extends Disk with WorkspaceAware {
  def label: Label

  override val node: Component = Network.newNode(this, Visibility.Network).
    withComponent("drive", Visibility.Neighbors).
    create()

  protected def savePath = new File(workspace.path.toFile, node.address + ".bin")

  /**
    * Number of physical platters to pretend a disk has in unmanaged mode. This
    * controls seek times, in how it emulates sectors overlapping (thus sharing
    * a common head position for access).
    */
  def platterCount: Int

  def capacity: Int

  def speed: Int

  def setAddress(newAddress: String): Unit = {
    if (node.network != null)
      node.network.remap(node, newAddress)
    else
      node.address = newAddress
  }

  // ----------------------------------------------------------------------- //

  protected final val sectorSize = 512

  protected def data = new Array[Byte](capacity)

  protected def sectorCount: Int = capacity / sectorSize

  protected def sectorsPerPlatter: Int = sectorCount / platterCount

  protected var headPos = 0

  final val readSectorCosts = Array(1.0 / 10, 1.0 / 20, 1.0 / 30, 1.0 / 40, 1.0 / 50, 1.0 / 60)
  final val writeSectorCosts = Array(1.0 / 5, 1.0 / 10, 1.0 / 15, 1.0 / 20, 1.0 / 25, 1.0 / 30)
  final val readByteCosts = Array(1.0 / 48, 1.0 / 64, 1.0 / 80, 1.0 / 96, 1.0 / 112, 1.0 / 128)
  final val writeByteCosts = Array(1.0 / 24, 1.0 / 32, 1.0 / 40, 1.0 / 48, 1.0 / 56, 1.0 / 64)

  // ----------------------------------------------------------------------- //

  @Callback(direct = true, doc = """function():string -- Get the current label of the drive.""")
  def getLabel(context: Context, args: Arguments): Array[AnyRef] = this.synchronized {
    if (label != null) result(label.getLabel) else null
  }

  @Callback(doc = """function(value:string):string -- Sets the label of the drive. Returns the new value, which may be truncated.""")
  def setLabel(context: Context, args: Arguments): Array[AnyRef] = this.synchronized {
    if (isLocked) throw new Exception("drive is read only")
    if (label == null) throw new Exception("drive does not support labeling")
    if (args.checkAny(0) == null) label.setLabel(null)
    else label.setLabel(args.checkString(0))
    result(label.getLabel)
  }

  @Callback(direct = true, doc = """function():number -- Returns the total capacity of the drive, in bytes.""")
  def getCapacity(context: Context, args: Arguments): Array[AnyRef] = result(capacity)

  @Callback(direct = true, doc = """function():number -- Returns the size of a single sector on the drive, in bytes.""")
  def getSectorSize(context: Context, args: Arguments): Array[AnyRef] = result(sectorSize)

  @Callback(direct = true, doc = """function():number -- Returns the number of platters in the drive.""")
  def getPlatterCount(context: Context, args: Arguments): Array[AnyRef] = result(platterCount)

  @Callback(direct = true, doc = """function(sector:number):string -- Read the current contents of the specified sector.""")
  def readSector(context: Context, args: Arguments): Array[AnyRef] = this.synchronized {
    context.consumeCallBudget(readSectorCosts(speed))
    val sector = moveToSector(context, checkSector(args, 0))
    diskActivity()
    val sectorData = new Array[Byte](sectorSize)
    Array.copy(data, sectorOffset(sector), sectorData, 0, sectorSize)
    result(sectorData)
  }

  @Callback(direct = true, doc = """function(sector:number, value:string) -- Write the specified contents to the specified sector.""")
  def writeSector(context: Context, args: Arguments): Array[AnyRef] = this.synchronized {
    if (isLocked) throw new Exception("drive is read only")
    context.consumeCallBudget(writeSectorCosts(speed))
    val sectorData = args.checkByteArray(1)
    val sector = moveToSector(context, checkSector(args, 0))
    diskActivity()
    Array.copy(sectorData, 0, data, sectorOffset(sector), math.min(sectorSize, sectorData.length))
    null
  }

  @Callback(direct = true, doc = """function(offset:number):number -- Read a single byte at the specified offset.""")
  def readByte(context: Context, args: Arguments): Array[AnyRef] = this.synchronized {
    context.consumeCallBudget(readByteCosts(speed))
    val offset = args.checkInteger(0) - 1
    moveToSector(context, checkSector(offset))
    diskActivity()
    result(data(offset))
  }

  @Callback(direct = true, doc = """function(offset:number, value:number) -- Write a single byte to the specified offset.""")
  def writeByte(context: Context, args: Arguments): Array[AnyRef] = this.synchronized {
    if (isLocked) throw new Exception("drive is read only")
    context.consumeCallBudget(writeByteCosts(speed))
    val offset = args.checkInteger(0) - 1
    val value = args.checkInteger(1).toByte
    moveToSector(context, checkSector(offset))
    diskActivity()
    data(offset) = value
    null
  }

  // ----------------------------------------------------------------------- //

  protected def validateSector(sector: Int): Int = {
    if (sector < 0 || sector >= sectorCount)
      throw new IllegalArgumentException("invalid offset, not in a usable sector")
    sector
  }

  protected def checkSector(offset: Int): Int = validateSector(offsetSector(offset))

  protected def checkSector(args: Arguments, n: Int): Int = validateSector(args.checkInteger(n) - 1)

  protected def moveToSector(context: Context, sector: Int): Int = {
    val newHeadPos = sectorToHeadPos(sector)
    if (headPos != newHeadPos) {
      val delta = math.abs(headPos - newHeadPos)
      if (delta > Settings.get.sectorSeekThreshold) context.pause(Settings.get.sectorSeekTime)
      headPos = newHeadPos
    }
    sector
  }

  protected def sectorToHeadPos(sector: Int): Int = sector % sectorsPerPlatter

  protected def sectorOffset(sector: Int): Int = sector * sectorSize

  protected def offsetSector(offset: Int): Int = offset / sectorSize

  // ----------------------------------------------------------------------- //

  private final val HeadPosTag = "headPos"

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = this.synchronized {
    super.load(nbt, workspace)

    if (node.address != null) try {
      val path = savePath
      if (path.exists()) {
        val bin = new ByteArrayInputStream(Files.toByteArray(path))
        val zin = new GZIPInputStream(bin)
        var offset = 0
        var read = 0
        while (read >= 0 && offset < data.length) {
          read = zin.read(data, offset, data.length - offset)
          offset += read
        }
      }
    }
    catch {
      case t: Throwable => Ocelot.log.warn(s"Failed loading drive contents for '${node.address}'.", t)
    }

    headPos = nbt.getInteger(HeadPosTag) max 0 min sectorToHeadPos(sectorCount)

    if (label != null) {
      label.load(nbt, workspace)
    }
  }

  override def save(nbt: NBTTagCompound): Unit = this.synchronized {
    super.save(nbt)

    if (node.address != null) try {
      val path = savePath
      path.getParentFile.mkdirs()
      val bos = new ByteArrayOutputStream()
      val zos = new GZIPOutputStream(bos)
      zos.write(data)
      zos.close()
      Files.write(bos.toByteArray, path)
    }
    catch {
      case t: Throwable => Ocelot.log.warn(s"Failed saving drive contents for '${node.address}'.", t)
    }

    nbt.setInteger(HeadPosTag, headPos)

    if (label != null) {
      label.save(nbt)
    }
  }

  // ----------------------------------------------------------------------- //

  private def diskActivity(): Unit = {
    EventBus.sendDiskActivity(node)
  }
}
