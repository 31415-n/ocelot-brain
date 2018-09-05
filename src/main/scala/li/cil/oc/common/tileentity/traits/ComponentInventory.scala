package li.cil.oc.common.tileentity.traits

import li.cil.oc.api.network.Node
import li.cil.oc.common.inventory
import net.minecraft.nbt.NBTTagCompound

trait ComponentInventory extends Environment with Inventory with inventory.ComponentInventory {
  override def host: ComponentInventory = this

  override protected def initialize(): Unit = {
    super.initialize()
    // TODO: ?
    connectComponents()
  }

  override def dispose(): Unit = {
    super.dispose()
    // TODO: ?
    disconnectComponents()
  }

  override def onConnect(node: Node) {
    super.onConnect(node)
    if (node == this.node) {
      connectComponents()
    }
  }

  override def onDisconnect(node: Node) {
    super.onDisconnect(node)
    if (node == this.node) {
      disconnectComponents()
    }
  }

  override def writeToNBT(nbt: NBTTagCompound) {
    connectComponents()
    super.writeToNBT(nbt)
    save(nbt)
  }

  override def readFromNBT(nbt: NBTTagCompound) {
    super.readFromNBT(nbt)
    load(nbt)
    connectComponents()
  }
}
