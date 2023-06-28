package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Constants
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{Computer, DeviceInfo, Entity, Environment, RackBusConnectable, StateAware, TieredPersistable}
import totoro.ocelot.brain.network.{Message, Node}
import totoro.ocelot.brain.util.Tier.Tier

import scala.collection.immutable.HashSet

class Server(override var tier: Tier)
  extends Computer
  with Entity
  with DeviceInfo
  with TieredPersistable
  with traits.Server
{
  private final lazy val deviceInfo = Map[String, String](
    DeviceAttribute.Class -> DeviceClass.System,
    DeviceAttribute.Description -> "Server",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "Blader",
    DeviceAttribute.Capacity -> inventory.size.toString
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo

  // ----------------------------------------------------------------------- //
  // Environment

  override def onConnect(node: Node): Unit = {
    if (node == this.node) {
      connectComponents()
    }
  }

  override def onDisconnect(node: Node): Unit = {
    if (node == this.node) {
      disconnectComponents()
    }
  }

  override def onMessage(message: Message): Unit = {
  }

  // ----------------------------------------------------------------------- //
  // MachineHost

  override def componentSlot(address: String): Int = inventory.iterator.indexWhere(slot => {
    slot.get match {
      case Some(entity: Entity with Environment) =>
       entity.node != null && entity.node.address == address
      case _ => false
    }
  })

  override def onMachineConnect(node: Node): Unit = onConnect(node)

  override def onMachineDisconnect(node: Node): Unit = onDisconnect(node)

  // ----------------------------------------------------------------------- //
  // RackMountable

  override def getConnectableCount: Int = inventory.count(slot => {
    slot.get match {
      case Some(_: RackBusConnectable) => true
      case _ => false
    }
  })

  override def getConnectableAt(index: Int): RackBusConnectable = {
    inventory.iterator.zipWithIndex.foreach { case (slot, i) =>
      slot.get match {
        case Some(busConnectable: RackBusConnectable) =>
          if (i == index)
            busConnectable
        case _ =>
      }
    }

    throw new Exception("unable to obtain connectable")
  }

  // ----------------------------------------------------------------------- //
  // StateAware

  override def getCurrentState: Set[StateAware.State.Value] = {
    if (machine.isRunning) HashSet(StateAware.State.IsWorking)
    else HashSet[StateAware.State.Value]()
  }
}
