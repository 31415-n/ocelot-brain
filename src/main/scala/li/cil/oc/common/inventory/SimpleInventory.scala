package li.cil.oc.common.inventory

import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack

trait SimpleInventory extends IInventory {
  override def getInventoryStackLimit = 64

  // Items required in a slot before it's set to null (for ghost stacks).
  def getInventoryStackRequired = 1

  override def decrStackSize(slot: Int, amount: Int): ItemStack = {
    if (slot >= 0 && slot < getSizeInventory) {
      (getStackInSlot(slot) match {
        case stack: ItemStack =>
          val result = stack.splitStack(amount)
          result
        case _ => ItemStack.EMPTY
      }) match {
        case stack: ItemStack if stack.getCount > 0 => stack
        case _ => ItemStack.EMPTY
      }
    }
    else ItemStack.EMPTY
  }

  override def removeStackFromSlot(slot: Int): ItemStack = {
    if (slot >= 0 && slot < getSizeInventory) {
      val stack = getStackInSlot(slot)
      setInventorySlotContents(slot, ItemStack.EMPTY)
      stack
    }
    else ItemStack.EMPTY
  }

  override def clear(): Unit = {
    for (slot <- 0 until getSizeInventory) {
      setInventorySlotContents(slot, ItemStack.EMPTY)
    }
  }
}
