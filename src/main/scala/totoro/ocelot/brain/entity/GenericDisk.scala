package totoro.ocelot.brain.entity

import java.util.UUID

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.fs.{FileSystemAPI, Label}
import totoro.ocelot.brain.network.Node

class GenericDisk(label: Label, capacity: Int, platterCount: Int, var managed: Boolean = true, val speed: Int) extends Environment {

  private var environment: Environment = _

  override def node: Node =
    if (environment != null) environment.node
    else null

  def address: String =
    if (node != null) environment.node.address
    else null

  /**
    * This method generates new environment based on
    * `managed` field and the Class of this disk
    */
  def initEnvironment(): Unit = {
    val oldAddress = address
    environment = if (managed) {
      val fs = FileSystemAPI.fromSaveDirectory(address, capacity max 0, Settings.get.bufferChanges)
      FileSystemAPI.asManagedEnvironment(fs, label, speed)
    } else {
      new Drive(capacity max 0, platterCount, label, speed)
    }
    if (environment != null && node != null) {
      val newAddress = if (oldAddress == null || oldAddress.isEmpty)
        UUID.randomUUID().toString
      else oldAddress
      node.address = newAddress
    }
  }

  initEnvironment()
}
