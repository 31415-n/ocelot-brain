package li.cil.oc.common.tileentity.traits

import li.cil.oc.util.BlockPosition
import net.minecraft.nbt.NBTTagCompound

trait TileEntity extends net.minecraft.tileentity.TileEntity {
  def x: Int = 0

  def y: Int = 0

  def z: Int = 0

  def position = BlockPosition(x, y, z, getWorld)

  def isClient: Boolean = !isServer

  def isServer: Boolean = true

  // ----------------------------------------------------------------------- //

  def updateEntity() {}

  protected def initialize() {}

  def dispose() {}

  // ----------------------------------------------------------------------- //

  def readFromNBT(nbt: NBTTagCompound): Unit = {}

  def writeToNBT(nbt: NBTTagCompound): Unit = {}
}
