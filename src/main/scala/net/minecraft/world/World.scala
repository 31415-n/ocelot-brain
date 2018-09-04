package net.minecraft.world

/**
  * Represents Ocelot workspace environment in which emulation is running
  */

class World {
  def getWorldTime: Long = System.currentTimeMillis()
  def getTotalWorldTime: Long = System.currentTimeMillis()
  def isPaused = false
}
