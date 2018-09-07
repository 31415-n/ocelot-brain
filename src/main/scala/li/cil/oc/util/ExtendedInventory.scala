package li.cil.oc.util

import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack

import scala.collection.mutable
import scala.language.implicitConversions

object ExtendedInventory {

  implicit def extendedInventory(inventory: IInventory): ExtendedInventory = new ExtendedInventory(inventory)

  class ExtendedInventory(val inventory: IInventory) extends mutable.IndexedSeq[ItemStack] {
    override def length: Int = inventory.getSizeInventory

    override def update(idx: Int, elem: ItemStack): Unit = inventory.setInventorySlotContents(idx, elem)

    override def apply(idx: Int): ItemStack = inventory.getStackInSlot(idx)
  }
}
