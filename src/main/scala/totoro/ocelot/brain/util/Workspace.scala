package totoro.ocelot.brain.util

import java.util.UUID

import scala.util.Random

/**
  * A separated space with it's own internal time.
  * Can be put on pause.
  * All emulated computers belong to some workspace.
  */
class Workspace(val name: String = UUID.randomUUID().toString) {
  private var paused = false

  private var timeOffset: Int = 0
  private var ingameTimePaused = false
  private var ingameTimeSnapshot = 0

  private def getTotalWorldTime: Int =
    if (isIngameTimePaused) ingameTimeSnapshot
    else (System.currentTimeMillis() / 50 % 24000).toInt

  def getIngameTime: Int = (getTotalWorldTime + timeOffset) % 24000
  def rand: Random = Random

  def isPaused: Boolean = paused
  def setPaused(value: Boolean): Unit = {
    this.paused = value
  }

  def setIngameTime(ticks: Int): Unit = {
    timeOffset = ticks - getTotalWorldTime
  }
  def isIngameTimePaused: Boolean = ingameTimePaused
  def setIngameTimePaused(value: Boolean): Unit = {
    if (value) ingameTimeSnapshot = getTotalWorldTime
    else setIngameTime(getIngameTime)
    this.ingameTimePaused = value
  }
}

object Workspace {
  /**
    * General workspace that will used if not specified otherwise.
    */
  val Default = new Workspace()
}
