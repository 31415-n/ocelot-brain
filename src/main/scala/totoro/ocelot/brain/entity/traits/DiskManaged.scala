package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.fs.{FileSystem, FileSystemAPI, FileSystemTrait, ReadWriteLabel}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.Node
import totoro.ocelot.brain.workspace.Workspace

import java.nio.file.{Files, InvalidPathException, Path, Paths}
import java.util.UUID
import scala.util.Try

/**
  * Basic trait for all managed-disk-like entities.
  */
trait DiskManaged extends Disk with WorkspaceAware {
  protected var envLock = false // refactor later

  protected var address: Option[String] = None

  protected var _fileSystem: FileSystem = _
  def fileSystem: FileSystem = {
    if (_fileSystem == null)
      _fileSystem = generateEnvironment()

    _fileSystem
  }

  override def node: Node =
    if (envLock) {
      if (_fileSystem != null) _fileSystem.node
      else null
    } else {
      if (fileSystem != null) fileSystem.node
      else null
    }

  // ----------------------------------------------------------------------- //

  def defaultRealPath: Path = workspace.path.resolve(address.get)

  private var _customRealPath: Option[Path] = None
  def customRealPath: Option[Path] = _customRealPath
  def customRealPath_=(value: Option[Path]): Unit = {
    _customRealPath = value
    _fileSystem = generateEnvironment()
  }

  protected def generateEnvironment(): FileSystem = {
    if (address.isEmpty)
      address = Option(UUID.randomUUID().toString)

    // Restoring default path if component was just inserted to slot (without NBT data loading)
    // or if user has changed/deleted/renamed previously set path
    val realPath = if (customRealPath.isEmpty || !Files.exists(customRealPath.get) || !Files.isDirectory(customRealPath.get)) defaultRealPath else customRealPath.get

    var fileSystemTrait: FileSystemTrait = FileSystemAPI.fromDirectory(
      realPath.toFile,
      capacity max 0,
      Settings.get.bufferChanges
    )

    if (isLocked)
      fileSystemTrait = FileSystemAPI.asReadOnly(fileSystemTrait)

    FileSystemAPI.asManagedEnvironment(
      address.get, fileSystemTrait, new ReadWriteLabel(address.get), speed, activityType.orNull)
  }

  // ----------------------------------------------------------------------- //

  def saveToNbtAndLoad(): Unit = {
    if (_fileSystem == null)
      return

    // save changes
    val nbt = new NBTTagCompound()
    fileSystem.save(nbt)
    // regenerate filesystem instance
    _fileSystem = generateEnvironment()
    // restore parameters
    _fileSystem.load(nbt, workspace)
  }

  override def onLockChange(oldLockInfo: String): Unit = {
    super.onLockChange(oldLockInfo)

    // do no touch the file system without need
    if (isLocked(oldLockInfo) != isLocked)
        saveToNbtAndLoad()
  }

  // ----------------------------------------------------------------------- //

  private val FileSystemTag = "fs"
  private val RealPathTag = "rp"

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)

    if (_fileSystem != null) {
      val fsNbt = new NBTTagCompound
      _fileSystem.save(fsNbt)
      nbt.setTag(FileSystemTag, fsNbt)

      // Custom real path
      if (customRealPath.isDefined)
        nbt.setString(RealPathTag, customRealPath.get.toString)
      else
        nbt.removeTag(RealPathTag)
    }
  }

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    envLock = true

    super.load(nbt, workspace)

    this.workspace = workspace

    if (nbt.hasKey(FileSystemTag)) {
      val nodeNbt = nbt.getCompoundTag(Environment.NodeTag)
      val fsNbt = nbt.getCompoundTag(FileSystemTag)

      address = Option(nodeNbt.getString(Node.AddressTag))
      customRealPath = if (nbt.hasKey(RealPathTag)) Some(Paths.get(nbt.getString(RealPathTag))) else None

      _fileSystem.load(fsNbt, workspace)
    }

    envLock = false
  }
}
