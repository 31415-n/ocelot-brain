package net.minecraft.tileentity

import net.minecraft.world.World

object TileEntity {
  val world = new World()
}

class TileEntity {
  private var world: World = TileEntity.world

  def setWorld(world: World): Unit = this.world = world
  def getWorld: World = world
}
