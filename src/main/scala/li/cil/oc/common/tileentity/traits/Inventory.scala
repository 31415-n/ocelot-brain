package li.cil.oc.common.tileentity.traits

import li.cil.oc.common.inventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

trait Inventory extends TileEntity with inventory.Inventory {
  private lazy val inventory = Array.fill[ItemStack](getSizeInventory)(ItemStack.EMPTY)

  def items: Array[ItemStack] = inventory

  // ----------------------------------------------------------------------- //

  override def readFromNBT(nbt: NBTTagCompound) {
    super.readFromNBT(nbt)
    load(nbt)
  }

  override def writeToNBT(nbt: NBTTagCompound) {
    super.writeToNBT(nbt)
    save(nbt)
  }
}
