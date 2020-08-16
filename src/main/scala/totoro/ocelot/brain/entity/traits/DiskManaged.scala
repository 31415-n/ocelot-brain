package totoro.ocelot.brain.entity.traits

import java.util.UUID

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.fs.{FileSystem, FileSystemAPI, FileSystemTrait, ReadWriteLabel}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.Node

/**
  * Basic trait for all managed-disk-like entities.
  */
trait DiskManaged extends Disk {
  protected var envLock = false // refactor later

  def fileSystem: FileSystem = {
    if (_fileSystem == null) _fileSystem = generateEnvironment(null)
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

  protected def generateEnvironment(address: String): FileSystem = {
    val finalAddress = if (address == null) UUID.randomUUID().toString else address
    var fs: FileSystemTrait = FileSystemAPI.fromSaveDirectory(finalAddress, capacity max 0, Settings.get.bufferChanges)
    if (isLocked) {
      fs = FileSystemAPI.asReadOnly(fs)
    }
    FileSystemAPI.asManagedEnvironment(finalAddress, fs, new ReadWriteLabel(finalAddress), speed)
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
        _fileSystem = generateEnvironment(fileSystem.node.address)
        // restore parameters
        _fileSystem.load(nbt)
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

  override def load(nbt: NBTTagCompound): Unit = {
    envLock = true
    super.load(nbt)
    if (nbt.hasKey(FileSystemTag)) {
      val nodeNbt = nbt.getCompoundTag(Environment.NodeTag)
      val address = nodeNbt.getString(Node.AddressTag)
      val fsNbt = nbt.getCompoundTag(FileSystemTag)
      _fileSystem = generateEnvironment(address)
      _fileSystem.load(fsNbt)
    }
    envLock = false
  }
}
