package net.minecraft.tileentity

import net.minecraft.world.World

class TileEntity {
  private var world: World = _

  def setWorld(world: World): Unit = this.world = world
  def getWorld: World = world
}
