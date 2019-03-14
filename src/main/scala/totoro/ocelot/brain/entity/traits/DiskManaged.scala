package totoro.ocelot.brain.entity.traits

import java.util.UUID

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.fs.FileSystemAPI
import totoro.ocelot.brain.network.Node

/**
  * Basic trait for all managed-disk-like entities.
  */
trait DiskManaged extends Disk {
  def address: String

  def environment: Environment = {
    if (_environment == null) _environment = generateEnvironment()
    _environment
  }

  override def node: Node =
    if (environment != null) environment.node
    else null


  // ----------------------------------------------------------------------- //

  protected var _environment: Environment = _

  protected def generateEnvironment(): Environment = {
    val finalAddress = if (address == null) UUID.randomUUID().toString else address
    val fs = FileSystemAPI.fromSaveDirectory(finalAddress, capacity max 0, Settings.get.bufferChanges)
    FileSystemAPI.asManagedEnvironment(fs, label, speed)
  }
}
