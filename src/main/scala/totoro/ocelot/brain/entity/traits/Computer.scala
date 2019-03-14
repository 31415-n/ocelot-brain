package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.machine.{Machine, MachineAPI}
import totoro.ocelot.brain.nbt.ExtendedNBT._
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.Node

trait Computer extends Environment with MachineHost with ComponentInventory {

  private lazy val _machine = MachineAPI.create(this)

  override def machine: Machine = _machine

  override def node: Node = machine.node

  // ----------------------------------------------------------------------- //

  override def onMachineConnect(node: Node): Unit = this.onConnect(node)

  override def onMachineDisconnect(node: Node): Unit = this.onDisconnect(node)

  override def componentSlot(address: String): Int =
    inventory.indexWhere {
      case env: Environment => env.node != null && env.node.address == address
    }

  // ----------------------------------------------------------------------- //

  override def update(): Unit = {
    // If we're not yet in a network we might have just been loaded from disk,
    // meaning there may be other tile entities that also have not re-joined
    // the network. We skip the update this round to allow other tile entities
    // to join the network, too, avoiding issues of missing nodes (e.g. in the
    // GPU which would otherwise loose track of its screen).
    if (isConnected) {
      machine.update()
    }

    super.update()
  }

  override def dispose(): Unit = {
    super.dispose()
    if (machine != null) {
      machine.stop()
    }
  }

  // ----------------------------------------------------------------------- //

  override def onEntityRemoved(entity: Entity) {
    super.onEntityRemoved(entity)
    entity match {
      case _: Processor => machine.stop()
    }
  }

  // ----------------------------------------------------------------------- //

  private final val ComputerTag = Settings.namespace + "computer"

  override def load(nbt: NBTTagCompound) {
    super.load(nbt)
    machine.load(nbt.getCompoundTag(ComputerTag))
  }

  override def save(nbt: NBTTagCompound) {
    super.save(nbt)
    if (machine != null) {
      nbt.setNewCompoundTag(ComputerTag, machine.save)
    }
  }

}
