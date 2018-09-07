package li.cil.oc.common.tileentity.traits

import net.minecraft.nbt.NBTTagCompound

trait TileEntity extends net.minecraft.tileentity.TileEntity {

  protected def initialize() {}

  def updateEntity() {}

  def dispose() {}

  // ----------------------------------------------------------------------- //

  def readFromNBT(nbt: NBTTagCompound): Unit = {}

  def writeToNBT(nbt: NBTTagCompound): Unit = {}
}
