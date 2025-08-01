package totoro.ocelot.brain.entity.fs

import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.result
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{DeviceInfo, DiskActivityAware, Environment}
import totoro.ocelot.brain.event.EventBus
import totoro.ocelot.brain.event.FileSystemActivityType.ActivityType
import totoro.ocelot.brain.nbt.ExtendedNBT._
import totoro.ocelot.brain.nbt.{NBT, NBTTagCompound, NBTTagIntArray, NBTTagList}
import totoro.ocelot.brain.network._
import totoro.ocelot.brain.workspace.Workspace
import totoro.ocelot.brain.{Constants, Settings}

import java.io.{FileNotFoundException, IOException}
import scala.collection.mutable

class FileSystem(val fileSystem: FileSystemTrait, var label: Label, val speed: Int, val activityType: Option[ActivityType])
  extends Environment with DeviceInfo {

  def this(address: String, fileSystem: FileSystemTrait, label: Label, speed: Int, activityType: Option[ActivityType]) = {
    this(fileSystem, label, speed, activityType)
    node.address = address
  }

  override val node: Component = Network.newNode(this, Visibility.Network).
    withComponent("filesystem", Visibility.Neighbors).
    create()

  private val owners = mutable.Map.empty[String, mutable.Set[Int]]

  final val readCosts = Array(1.0 / 1, 1.0 / 4, 1.0 / 7, 1.0 / 10, 1.0 / 13, 1.0 / 15)
  final val seekCosts = Array(1.0 / 1, 1.0 / 4, 1.0 / 7, 1.0 / 10, 1.0 / 13, 1.0 / 15)
  final val writeCosts = Array(1.0 / 1, 1.0 / 2, 1.0 / 3, 1.0 / 4, 1.0 / 5, 1.0 / 6)

  // ----------------------------------------------------------------------- //

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Volume,
    DeviceAttribute.Description -> "Filesystem",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "Catfish 14.1",
    DeviceAttribute.Capacity -> (fileSystem.spaceTotal * 1.024).toInt.toString,
    DeviceAttribute.Size -> fileSystem.spaceTotal.toString,
    DeviceAttribute.Clock -> (((2000 / readCosts(speed)).toInt / 100).toString + "/" +
      ((2000 / seekCosts(speed)).toInt / 100).toString + "/" + ((2000 / writeCosts(speed)).toInt / 100).toString)
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo

  // ----------------------------------------------------------------------- //

  @Callback(direct = true, doc = """function():string -- Get the current label of the drive.""")
  def getLabel(context: Context, args: Arguments): Array[AnyRef] = fileSystem.synchronized {
    if (label != null) result(label.getLabel) else null
  }

  @Callback(doc = """function(value:string):string -- Sets the label of the drive. Returns the new value, which may be truncated.""")
  def setLabel(context: Context, args: Arguments): Array[AnyRef] = fileSystem.synchronized {
    if (label == null) throw new Exception("drive does not support labeling")
    if (args.checkAny(0) == null) label.setLabel(null)
    else label.setLabel(args.checkString(0))
    result(label.getLabel)
  }

  @Callback(direct = true, doc = """function():boolean -- Returns whether the file system is read-only.""")
  def isReadOnly(context: Context, args: Arguments): Array[AnyRef] = fileSystem.synchronized {
    result(fileSystem.isReadOnly)
  }

  @Callback(direct = true, doc = """function():number -- The overall capacity of the file system, in bytes.""")
  def spaceTotal(context: Context, args: Arguments): Array[AnyRef] = fileSystem.synchronized {
    val space = fileSystem.spaceTotal
    if (space < 0) result(Double.PositiveInfinity)
    else result(space)
  }

  @Callback(direct = true, doc = """function():number -- The currently used capacity of the file system, in bytes.""")
  def spaceUsed(context: Context, args: Arguments): Array[AnyRef] = fileSystem.synchronized {
    result(fileSystem.spaceUsed)
  }

  @Callback(direct = true, doc = """function(path:string):boolean -- Returns whether an object exists at the specified absolute path in the file system.""")
  def exists(context: Context, args: Arguments): Array[AnyRef] = fileSystem.synchronized {
    diskActivity()
    result(fileSystem.exists(clean(args.checkString(0))))
  }

  @Callback(direct = true, doc = """function(path:string):number -- Returns the size of the object at the specified absolute path in the file system.""")
  def size(context: Context, args: Arguments): Array[AnyRef] = fileSystem.synchronized {
    diskActivity()
    result(fileSystem.size(clean(args.checkString(0))))
  }

  @Callback(direct = true, doc = """function(path:string):boolean -- Returns whether the object at the specified absolute path in the file system is a directory.""")
  def isDirectory(context: Context, args: Arguments): Array[AnyRef] = fileSystem.synchronized {
    diskActivity()
    result(fileSystem.isDirectory(clean(args.checkString(0))))
  }

  @Callback(direct = true, doc = """function(path:string):number -- Returns the (real world) timestamp of when the object at the specified absolute path in the file system was modified.""")
  def lastModified(context: Context, args: Arguments): Array[AnyRef] = fileSystem.synchronized {
    diskActivity()
    result(fileSystem.lastModified(clean(args.checkString(0))))
  }

  @Callback(doc = """function(path:string):table -- Returns a list of names of objects in the directory at the specified absolute path in the file system.""")
  def list(context: Context, args: Arguments): Array[AnyRef] = fileSystem.synchronized {
    Option(fileSystem.list(clean(args.checkString(0)))) match {
      case Some(list) =>
        diskActivity()
        Array(list)
      case _ => null
    }
  }

  @Callback(doc = """function(path:string):boolean -- Creates a directory at the specified absolute path in the file system. Creates parent directories, if necessary.""")
  def makeDirectory(context: Context, args: Arguments): Array[AnyRef] = fileSystem.synchronized {
    def recurse(path: String): Boolean = !fileSystem.exists(path) && (fileSystem.makeDirectory(path) ||
      (recurse(path.split("/").dropRight(1).mkString("/")) && fileSystem.makeDirectory(path)))

    val success = recurse(clean(args.checkString(0)))
    diskActivity()
    result(success)
  }

  @Callback(doc = """function(path:string):boolean -- Removes the object at the specified absolute path in the file system.""")
  def remove(context: Context, args: Arguments): Array[AnyRef] = fileSystem.synchronized {
    def recurse(parent: String): Boolean = (!fileSystem.isDirectory(parent) ||
      fileSystem.list(parent).forall(child => recurse(parent + "/" + child))) && fileSystem.delete(parent)

    val success = recurse(clean(args.checkString(0)))
    diskActivity()
    result(success)
  }

  @Callback(doc = """function(from:string, to:string):boolean -- Renames/moves an object from the first specified absolute path in the file system to the second.""")
  def rename(context: Context, args: Arguments): Array[AnyRef] = fileSystem.synchronized {
    val success = fileSystem.rename(clean(args.checkString(0)), clean(args.checkString(1)))
    diskActivity()
    result(success)
  }

  @Callback(direct = true, doc = """function(handle:userdata) -- Closes an open file descriptor with the specified handle.""")
  def close(context: Context, args: Arguments): Array[AnyRef] = fileSystem.synchronized {
    close(context, checkHandle(args, 0))
    null
  }

  @Callback(direct = true, limit = 4, doc = """function(path:string[, mode:string='r']):userdata -- Opens a new file descriptor and returns its handle.""")
  def open(context: Context, args: Arguments): Array[AnyRef] = fileSystem.synchronized {
    if (owners.get(context.node.address).fold(false)(_.size >= Settings.get.maxHandles)) {
      throw new IOException("too many open handles")
    }
    val path = args.checkString(0)
    val mode = args.optString(1, "r")
    val handle = fileSystem.open(clean(path), parseMode(mode))
    if (handle > 0) {
      owners.getOrElseUpdate(context.node.address, mutable.Set.empty[Int]) += handle
    }
    diskActivity()
    result(new HandleValue(node.address, handle))
  }

  @Callback(direct = true, limit = 15, doc = """function(handle:userdata, count:number):string or nil -- Reads up to the specified amount of data from an open file descriptor with the specified handle. Returns nil when EOF is reached.""")
  def read(context: Context, args: Arguments): Array[AnyRef] = fileSystem.synchronized {
    context.consumeCallBudget(readCosts(speed))
    val handle = checkHandle(args, 0)
    val n = math.min(Settings.get.maxReadBuffer, math.max(0, args.checkInteger(1)))
    checkOwner(context.node.address, handle)
    Option(fileSystem.getHandle(handle)) match {
      case Some(file) =>
        // Limit size of read buffer to avoid crazy allocations.
        val buffer = new Array[Byte](n)
        val read = file.read(buffer)
        if (read >= 0) {
          val bytes =
            if (read == buffer.length)
              buffer
            else {
              val bytes = new Array[Byte](read)
              Array.copy(buffer, 0, bytes, 0, read)
              bytes
            }
          diskActivity()
          result(bytes)
        }
        else {
          result(())
        }
      case _ => throw new IOException("bad file descriptor")
    }
  }

  @Callback(direct = true, doc = """function(handle:userdata, whence:string, offset:number):number -- Seeks in an open file descriptor with the specified handle. Returns the new pointer position.""")
  def seek(context: Context, args: Arguments): Array[AnyRef] = fileSystem.synchronized {
    context.consumeCallBudget(seekCosts(speed))
    val handle = checkHandle(args, 0)
    val whence = args.checkString(1)
    val offset = args.checkInteger(2)
    checkOwner(context.node.address, handle)
    Option(fileSystem.getHandle(handle)) match {
      case Some(file) =>
        whence match {
          case "cur" => file.seek(file.position + offset)
          case "set" => file.seek(offset)
          case "end" => file.seek(file.length + offset)
          case _ => throw new IllegalArgumentException("invalid mode")
        }
        result(file.position)
      case _ => throw new IOException("bad file descriptor")
    }
  }

  @Callback(direct = true, doc = """function(handle:userdata, value:string):boolean -- Writes the specified data to an open file descriptor with the specified handle.""")
  def write(context: Context, args: Arguments): Array[AnyRef] = fileSystem.synchronized {
    context.consumeCallBudget(writeCosts(speed))
    val handle = checkHandle(args, 0)
    val value = args.checkByteArray(1)
    checkOwner(context.node.address, handle)
    Option(fileSystem.getHandle(handle)) match {
      case Some(file) =>
        file.write(value)
        diskActivity()
        result(true)
      case _ => throw new IOException("bad file descriptor")
    }
  }

  // ----------------------------------------------------------------------- //

  def checkHandle(args: Arguments, index: Int): Int = {
    if (args.isInteger(index)) {
      args.checkInteger(index)
    } else if (args.isTable(index)) {
      args.checkTable(index).get("handle") match {
        case handle: Number => handle.intValue()
        case _ => throw new IOException("bad file descriptor")
      }
    } else args.checkAny(index) match {
      case handle: HandleValue => handle.handle
      case _ => throw new IOException("bad file descriptor")
    }
  }

  def close(context: Context, handle: Int): Unit = {
    Option(fileSystem.getHandle(handle)) match {
      case Some(file) =>
        owners.get(context.node.address) match {
          case Some(set) if set.remove(handle) => file.close()
          case _ => throw new IOException("bad file descriptor")
        }
      case _ => throw new IOException("bad file descriptor")
    }
  }

  // ----------------------------------------------------------------------- //

  override def onMessage(message: Message): Unit = fileSystem.synchronized {
    super.onMessage(message)
    if (message.name == "computer.stopped" || message.name == "computer.started") {
      owners.get(message.source.address) match {
        case Some(set) =>
          set.foreach(handle => Option(fileSystem.getHandle(handle)) match {
            case Some(file) => file.close()
            case _ => // Invalid handle... huh.
          })
          set.clear()
        case _ => // Computer had no open files.
      }
    }
  }

  private var container: Environment with DiskActivityAware = _

  override def onConnect(node: Node): Unit = {
    node.host match {
      case x: DiskActivityAware =>
        if (node.isNeighborOf(this.node))
          container = x
      case _ =>
    }
  }

  override def onDisconnect(node: Node): Unit = fileSystem.synchronized {
    super.onDisconnect(node)
    if (node == this.node) {
      fileSystem.close()
    }
    else if (owners.contains(node.address)) {
      for (handle <- owners(node.address)) {
        Option(fileSystem.getHandle(handle)) match {
          case Some(file) => file.close()
          case _ =>
        }
      }
      owners.remove(node.address)
    }
    if (container != null) {
      if (node.host == container) container = null
    }
  }

  // ----------------------------------------------------------------------- //

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)

    nbt.getTagList("owners", NBT.TAG_COMPOUND).foreach((ownerNbt: NBTTagCompound) => {
      val address = ownerNbt.getString("address")
      if (address != "") {
        owners += address -> mutable.HashSet.from(ownerNbt.getIntArray("handles"))
      }
    })

    if (label != null) {
      label.load(nbt, workspace)
    }
    fileSystem.load(nbt.getCompoundTag("fs"), workspace)
  }

  override def save(nbt: NBTTagCompound): Unit = fileSystem.synchronized {
    super.save(nbt)

    if (label != null) {
      label.save(nbt)
    }

    val ownersNbt = new NBTTagList()
    for ((address, handles) <- owners) {
      val ownerNbt = new NBTTagCompound()
      ownerNbt.setString(Node.AddressTag, address)
      ownerNbt.setTag("handles", new NBTTagIntArray(handles.toArray))
      ownersNbt.appendTag(ownerNbt)
    }
    nbt.setTag("owners", ownersNbt)

    nbt.setNewCompoundTag("fs", fileSystem.save)
  }

  // ----------------------------------------------------------------------- //

  private def clean(path: String) = {
    val result = com.google.common.io.Files.simplifyPath(path)
    if (result.startsWith("../") || result == "..") throw new FileNotFoundException(path)
    if (result == "/" || result == ".") ""
    else result
  }

  private def parseMode(value: String): Mode.Value = {
    if (("r" == value) || ("rb" == value)) return Mode.Read
    if (("w" == value) || ("wb" == value)) return Mode.Write
    if (("a" == value) || ("ab" == value)) return Mode.Append
    throw new IllegalArgumentException("unsupported mode")
  }

  private def checkOwner(owner: String, handle: Int): Unit =
    if (!owners.contains(owner) || !owners(owner).contains(handle))
      throw new IOException("bad file descriptor")

  private def diskActivity(): Unit = {
    activityType match {
      case Some(activityType) =>
        EventBus.sendDiskActivity(node, activityType)

        if (container != null)
          container.resetLastDiskAccess()
      case _ =>
    }
  }
}
