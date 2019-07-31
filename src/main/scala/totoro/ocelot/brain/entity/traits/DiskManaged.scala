package totoro.ocelot.brain.entity.traits

import java.util.UUID

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.FileSystem
import totoro.ocelot.brain.entity.fs.{FileSystemAPI, FileSystemTrait}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.Node

/**
  * Basic trait for all managed-disk-like entities.
  */
trait DiskManaged extends Disk {
  def address: String

  def fileSystem: FileSystem = {
    if (_fileSystem == null) _fileSystem = generateEnvironment()
    _fileSystem
  }

  override def node: Node =
    if (fileSystem != null) fileSystem.node
    else null


  // ----------------------------------------------------------------------- //

  protected var _fileSystem: FileSystem = _

  protected def generateEnvironment(): FileSystem = {
    val finalAddress = if (address == null) UUID.randomUUID().toString else address
    var fs: FileSystemTrait = FileSystemAPI.fromSaveDirectory(finalAddress, capacity max 0, Settings.get.bufferChanges)
    if (isLocked) {
      fs = FileSystemAPI.asReadOnly(fs)
    }
    FileSystemAPI.asManagedEnvironment(fs, label, speed)
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
        _fileSystem.load(nbt)
      }
    }
  }
}
