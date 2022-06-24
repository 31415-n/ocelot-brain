package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.fs.{FileSystem, FileSystemAPI, FileSystemTrait, ReadWriteLabel}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.Node
import totoro.ocelot.brain.workspace.Workspace

import java.util.UUID

/**
  * Basic trait for all managed-disk-like entities.
  */
trait DiskManaged extends Disk with WorkspaceAware {
  protected var envLock = false // refactor later

  protected var _address: Option[String] = None

  def fileSystem: FileSystem = {
    if (_fileSystem == null) _fileSystem = generateEnvironment()
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

  protected def generateEnvironment(): FileSystem = {
    if (_address.isEmpty) _address = Option(UUID.randomUUID().toString)
    var fs: FileSystemTrait = FileSystemAPI.fromSaveDirectory(workspace.path, _address.get, capacity max 0, Settings.get.bufferChanges)
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

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    if (_fileSystem != null) {
      val fsNbt = new NBTTagCompound
      _fileSystem.save(fsNbt)
      nbt.setTag(FileSystemTag, fsNbt)
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
      _fileSystem = generateEnvironment()
      _fileSystem.load(fsNbt, workspace)
    }
    envLock = false
  }
}
