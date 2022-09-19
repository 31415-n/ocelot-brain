package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.{Ocelot, Settings}
import totoro.ocelot.brain.entity.fs.{FileSystem, FileSystemAPI, FileSystemTrait, ReadWriteLabel}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.Node
import totoro.ocelot.brain.workspace.Workspace

import java.nio.file.{Files, Path, Paths}
import java.util.UUID

/**
  * Basic trait for all managed-disk-like entities.
  */
trait DiskManaged extends Disk with WorkspaceAware {
  protected var envLock = false // refactor later

  protected var _address: Option[String] = None

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

  protected var _fileSystem: FileSystem = _

  private var _realPath: Path = _
  def realPath: Path = _realPath
  def realPath_=(value: Path): Unit = {
    _realPath = value

    _fileSystem = generateEnvironment()
  }

  protected def generateEnvironment(): FileSystem = {
    if (_address.isEmpty)
      _address = Option(UUID.randomUUID().toString)

    if (realPath != null && Files.exists(realPath)) {
      Ocelot.log.info(s"Real path was set to ${realPath.toString}")
    }
    else {
      _realPath = workspace.path.resolve(_address.get)

      Ocelot.log.info(s"Real path was (re)initialized to ${realPath.toString}")
    }

    var fs: FileSystemTrait = FileSystemAPI.fromDirectory(realPath.toFile, capacity max 0, Settings.get.bufferChanges)

    if (isLocked) {
      fs = FileSystemAPI.asReadOnly(fs)
    }

    FileSystemAPI.asManagedEnvironment(_address.get, fs, new ReadWriteLabel(_address.get), speed, activityType.orNull)
  }

  // ----------------------------------------------------------------------- //

  override def onLockChange(oldLockInfo: String): Unit = {
    super.onLockChange(oldLockInfo)
    // do no touch the file system without need
    if (isLocked(oldLockInfo) != isLocked) {
      if (_fileSystem != null) {
        // save changes
        val nbt = new NBTTagCompound()
        fileSystem.save(nbt)
        // regenerate filesystem instance
        _fileSystem = generateEnvironment()
        // restore parameters
        _fileSystem.load(nbt, workspace)
      }
    }
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
      nbt.setString(RealPathTag, realPath.toString)
    }
  }

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    envLock = true

    super.load(nbt, workspace)

    this.workspace = workspace

    if (nbt.hasKey(FileSystemTag)) {
      val nodeNbt = nbt.getCompoundTag(Environment.NodeTag)
      val fsNbt = nbt.getCompoundTag(FileSystemTag)

      _address = Option(nodeNbt.getString(Node.AddressTag))

      // GenerateEnvironment will be called in setter
      realPath = if (nbt.hasKey(RealPathTag)) Paths.get(nbt.getString(RealPathTag)) else workspace.path

      _fileSystem.load(fsNbt, workspace)
    }

    envLock = false
  }
}
