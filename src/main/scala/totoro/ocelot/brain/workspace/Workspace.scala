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

  def rand: Random = Random

  // Internal emulator time in Minecraft ticks
  // ----------------------------------------------------------------------- //
  private var ingameTime: Int = 0
  private var ingameTimePaused: Boolean = false

  def getIngameTime: Int = ingameTime

  def setIngameTime(ticks: Int): Unit = {
    ingameTime = ticks
  }

  def isIngameTimePaused: Boolean = ingameTimePaused

  def setIngameTimePaused(paused: Boolean): Unit = {
    ingameTimePaused = paused
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

  /**
    * Update all entities in all networks
    */
  def update(): Unit = {
    networks.foreach(network => {
      network.nodes.foreach(node => {
        if (node.host.needUpdate) node.host.update()
      })
    })

    if (!ingameTimePaused) ingameTime += 1
  }
}

object Workspace {
  /**
    * General workspace that will used if not specified otherwise.
    */
  val Default = new Workspace()
}
