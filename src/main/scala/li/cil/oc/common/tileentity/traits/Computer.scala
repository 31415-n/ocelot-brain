package li.cil.oc.common.tileentity.traits

import java.lang
import java.util

import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.api.machine.Machine
import li.cil.oc.api.network.Node
import li.cil.oc.integration.opencomputers.DriverRedstoneCard
import li.cil.oc.util.ExtendedNBT._
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

import scala.collection.convert.WrapAsJava._

trait Computer extends Environment with ComponentInventory with RedstoneAware
  with api.machine.MachineHost with StateAware {

  private lazy val _machine = api.Machine.create(this)

  override def machine: Machine = _machine

  override def node: Node = machine.node

  // ----------------------------------------------------------------------- //

  def isRunning: Boolean = machine.isRunning

  override def getCurrentState: util.EnumSet[api.util.StateAware.State] = {
    if (isRunning) util.EnumSet.of(api.util.StateAware.State.IsWorking)
    else util.EnumSet.noneOf(classOf[api.util.StateAware.State])
  }

  // ----------------------------------------------------------------------- //

  override def internalComponents(): lang.Iterable[ItemStack] = (0 until getSizeInventory).collect {
    case slot if !getStackInSlot(slot).isEmpty && isComponentSlot(slot, getStackInSlot(slot)) => getStackInSlot(slot)
  }

  override def onMachineConnect(node: api.network.Node): Unit = this.onConnect(node)

  override def onMachineDisconnect(node: api.network.Node): Unit = this.onDisconnect(node)

  def hasRedstoneCard: Boolean = items.exists {
    case item if !item.isEmpty => machine.isRunning && DriverRedstoneCard.worksWith(item, getClass)
    case _ => false
  }

  // ----------------------------------------------------------------------- //

  override def updateEntity(): Unit = {
    // If we're not yet in a network we might have just been loaded from disk,
    // meaning there may be other tile entities that also have not re-joined
    // the network. We skip the update this round to allow other tile entities
    // to join the network, too, avoiding issues of missing nodes (e.g. in the
    // GPU which would otherwise loose track of its screen).
    if (isConnected) {
      updateComputer()
      updateComponents()
    }

    super.updateEntity()
  }

  protected def updateComputer(): Unit = {
    machine.update()
  }

  override def dispose(): Unit = {
    super.dispose()
    if (machine != null) {
      machine.stop()
    }
  }

  // ----------------------------------------------------------------------- //

  private final val ComputerTag = Settings.namespace + "computer"

  override def readFromNBT(nbt: NBTTagCompound) {
    super.readFromNBT(nbt)
    machine.load(nbt.getCompoundTag(ComputerTag))
    _isOutputEnabled = hasRedstoneCard
  }

  override def writeToNBT(nbt: NBTTagCompound) {
    super.writeToNBT(nbt)
    if (machine != null) {
      nbt.setNewCompoundTag(ComputerTag, machine.save)
    }
  }

  // ----------------------------------------------------------------------- //

  override def isUsableByPlayer(player: EntityPlayer): Boolean =
    machine.canInteract(player.getName)

  override protected def onRedstoneInputChanged(args: RedstoneChangedEventArgs) {
    super.onRedstoneInputChanged(args)
    machine.node.sendToNeighbors("redstone.changed", args)
  }
}
