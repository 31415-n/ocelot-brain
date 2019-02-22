package totoro.ocelot.brain.workspace

import java.util.UUID

import totoro.ocelot.brain.network.Network

import scala.collection.mutable
import scala.util.Random

/**
  * A separated spacetime plane with networks and entities.
  * Can be put on pause. Can be serialized to a NBT tag.
  */
class Workspace(val name: String = UUID.randomUUID().toString) {

  // Time-related aspects
  // ----------------------------------------------------------------------- //
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

  // Networks
  // ----------------------------------------------------------------------- //
  private val networks: mutable.ListBuffer[Network] = mutable.ListBuffer.empty

  val DefaultNetwork = new Network()
  addNetwork(DefaultNetwork)

  def addNetwork(network: Network): Unit = {
    networks += network
    network.workspace = this
  }

  def getNetworksIter: Iterator[Network] = {
    networks.iterator
  }

  def removeNetwork(network: Network): Unit = {
    networks -= network
    network.workspace = null
  }
}

object Workspace {
  /**
    * General workspace that will used if not specified otherwise.
    */
  val Default = new Workspace()
}
