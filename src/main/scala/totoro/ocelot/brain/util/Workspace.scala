package totoro.ocelot.brain.util

import scala.util.Random

/**
  * A separated space with it's own internal time.
  * Can be put on pause.
  * All emulated computers belong to some workspace.
  */
class Workspace {
  def getWorldTime: Long = getTotalWorldTime / 50 % 24000
  def getTotalWorldTime: Long = System.currentTimeMillis()
  def isPaused = false
  def rand: Random = Random
}

object Workspace {
  /**
    * General workspace that will used if not specified otherwise.
    */
  val Default = new Workspace()
}
