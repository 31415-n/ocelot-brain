package li.cil.oc.common.tileentity.traits

import li.cil.oc.common.inventory
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

trait Inventory extends TileEntity with inventory.Inventory {
  private lazy val inventory = Array.fill[ItemStack](getSizeInventory)(ItemStack.EMPTY)

  def items: Array[ItemStack] = inventory

  // ----------------------------------------------------------------------- //

  override def readFromNBTForServer(nbt: NBTTagCompound) {
    super.readFromNBTForServer(nbt)
    load(nbt)
  }

  override def writeToNBTForServer(nbt: NBTTagCompound) {
    super.writeToNBTForServer(nbt)
    save(nbt)
  }

  // ----------------------------------------------------------------------- //

  override def isUsableByPlayer(player: EntityPlayer) = true
}
