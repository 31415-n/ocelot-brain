package li.cil.oc.common.tileentity.traits

import li.cil.oc.Settings
import li.cil.oc.api.network
import li.cil.oc.api.network.SidedEnvironment
import li.cil.oc.common.EventHandler
import li.cil.oc.util.ExtendedNBT._
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.world.World

trait Environment extends TileEntity with network.Environment with network.EnvironmentHost {
  override def world: World = getWorld

  protected def isConnected: Boolean = node != null && node.address != null && node.network != null

  // ----------------------------------------------------------------------- //

  override protected def initialize() {
    super.initialize()
    EventHandler.scheduleServer(this)
  }

  override def updateEntity() {
    super.updateEntity()
  }

  override def dispose() {
    super.dispose()
    Option(node).foreach(_.remove)
    this match {
      case sidedEnvironment: SidedEnvironment => for (side <- EnumFacing.values) {
        Option(sidedEnvironment.sidedNode(side)).foreach(_.remove())
      }
      case _ =>
    }
  }

  // ----------------------------------------------------------------------- //

  private final val NodeTag = Settings.namespace + "node"

  override def readFromNBT(nbt: NBTTagCompound): Unit = {
    super.readFromNBT(nbt)
    if (node != null && node.host == this) {
      node.load(nbt.getCompoundTag(NodeTag))
    }
  }

  override def writeToNBT(nbt: NBTTagCompound): Unit = {
    super.writeToNBT(nbt)
    if (node != null && node.host == this) {
      nbt.setNewCompoundTag(NodeTag, node.save)
    }
  }

  // ----------------------------------------------------------------------- //

  override def onMessage(message: network.Message) {}

  override def onConnect(node: network.Node) {}

  override def onDisconnect(node: network.Node) {}

  // ----------------------------------------------------------------------- //

  protected def result(args: Any*): Array[AnyRef] = li.cil.oc.util.ResultWrapper.result(args: _*)
}
