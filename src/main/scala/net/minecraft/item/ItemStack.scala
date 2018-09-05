package net.minecraft.item

import net.minecraft.nbt.{NBT, NBTTagCompound}

object ItemStack {
  def EMPTY = new ItemStack(null, 0)
}

class ItemStack(private var item: Item, private var amount: Int = 1, private var damage: Int = 0) {

  var nbt: NBTTagCompound = _

  def load(nbt: NBTTagCompound): Unit = {
    item = Item.REGISTRY(nbt.getString("id"))
    amount = nbt.getByte("Count")
    damage = nbt.getShort("Damage")

    if (nbt.hasKeyOfType("tag", NBT.TAG_COMPOUND)) {
      this.nbt = nbt.getCompoundTag("tag").copy()
    }
  }

  def this() {
    this(null, 1, 0)
  }

  def this(nbt: NBTTagCompound) {
    this()
    load(nbt)
  }

  def setTagCompound(tag: NBTTagCompound): Unit = nbt = tag
  def hasTagCompound: Boolean = nbt != null
  def getTagCompound: NBTTagCompound = nbt

  def getItem: Item = item
  def getItemDamage: Int = damage

  def writeToNBT(nbt: NBTTagCompound): Unit = { }

  def getCount: Int = amount
  def setCount(count: Int): Unit = amount = count

  def splitStack(take: Int): ItemStack = {
    val canTake = Math.min(take, amount)
    amount -= canTake
    new ItemStack(item, canTake)
  }

  def isEmpty: Boolean = amount <= 0

  def isItemEqual(stack: ItemStack): Boolean = item.equals(stack.item)
}
