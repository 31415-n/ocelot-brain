package net.minecraft.world

import scala.util.Random

/**
  * Represents Ocelot workspace environment in which emulation is running
  */

class World {
  def getWorldTime: Long = System.currentTimeMillis()
  def getTotalWorldTime: Long = System.currentTimeMillis()
  def isPaused = false
  def rand: Random.type = Random
}
