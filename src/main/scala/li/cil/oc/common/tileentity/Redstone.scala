package li.cil.oc.common.tileentity

import li.cil.oc.{Settings, api}
import li.cil.oc.api.network.{Component, Node, Visibility}
import li.cil.oc.common.tileentity.traits.{RedstoneAware, RedstoneChangedEventArgs}
import li.cil.oc.server.component
import li.cil.oc.server.component.RedstoneVanilla
import li.cil.oc.util.ExtendedNBT._
import net.minecraft.nbt.NBTTagCompound

class Redstone extends traits.Environment with RedstoneAware {
  val instance: RedstoneVanilla =
    new component.Redstone.Vanilla(this)
  instance.wakeNeighborsOnly = false
  val node: Component = instance.node
  val dummyNode: Node = if (node != null) {
    node.setVisibility(Visibility.Network)
    _isOutputEnabled = true
    api.Network.newNode(this, Visibility.None).create()
  }
  else null

  // ----------------------------------------------------------------------- //

  private final val RedstoneTag = Settings.namespace + "redstone"

  override def readFromNBT(nbt: NBTTagCompound) {
    super.readFromNBT(nbt)
    instance.load(nbt.getCompoundTag(RedstoneTag))
  }

  override def writeToNBT(nbt: NBTTagCompound) {
    super.writeToNBT(nbt)
    nbt.setNewCompoundTag(RedstoneTag, instance.save)
  }

  // ----------------------------------------------------------------------- //

  override protected def onRedstoneInputChanged(args: RedstoneChangedEventArgs) {
    super.onRedstoneInputChanged(args)
    if (node != null && node.network != null) {
      node.connect(dummyNode)
      dummyNode.sendToNeighbors("redstone.changed", args)
    }
  }
}
