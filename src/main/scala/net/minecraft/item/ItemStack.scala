package net.minecraft.item

import net.minecraft.nbt.NBTTagCompound

class ItemStack(item: Item, amount: Int, damage: Int = 0) {
  var nbt: NBTTagCompound = _

  def setTagCompound(tag: NBTTagCompound): Unit = nbt = tag
  def getTagCompound: NBTTagCompound = nbt

  def writeToNBT(nbt: NBTTagCompound): Unit = _
}
