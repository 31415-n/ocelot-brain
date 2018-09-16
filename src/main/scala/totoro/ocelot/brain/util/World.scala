package totoro.ocelot.brain.util

import scala.util.Random

/**
  * Represents Ocelot workspace environment in which the emulation is running
  */
class World {
  def getWorldTime: Long = getTotalWorldTime / 50 % 24000
  def getTotalWorldTime: Long = System.currentTimeMillis()
  def isPaused = false
  def rand: Random.type = Random
}

object World {
  val Default = new World()
}
