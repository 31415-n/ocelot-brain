package totoro.ocelot.brain.workspace

import java.util.UUID

import totoro.ocelot.brain.entity.traits.Persistable
import totoro.ocelot.brain.nbt.ExtendedNBT._
import totoro.ocelot.brain.nbt.{NBT, NBTBase, NBTTagCompound}
import totoro.ocelot.brain.network.Network

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Random

/**
  * A separated spacetime plane with networks and entities.
  * Can be put on pause. Can be serialized to a NBT tag.
  */
class Workspace(val name: String = UUID.randomUUID().toString) extends Persistable {

  private val random = new Random(System.currentTimeMillis())
  def rand: Random = random

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

  // Persistence
  // ----------------------------------------------------------------------- //
  private val TimeTag = "time"
  private val TimePausedTag = "time_paused"
  private val NetworksTag = "networks"

  override def save(nbt: NBTTagCompound): Unit = {
    nbt.setInteger(TimeTag, ingameTime)
    nbt.setBoolean(TimePausedTag, ingameTimePaused)

    val nbtNetworks: ListBuffer[NBTBase] = networks.map(network => {
      val nbt = new NBTTagCompound()
      network.save(nbt)
      nbt
    })
    nbt.setTagList(NetworksTag, nbtNetworks.asJava)
  }

  override def load(nbt: NBTTagCompound): Unit = {
    ingameTime = nbt.getInteger(TimeTag)
    ingameTimePaused = nbt.getBoolean(TimePausedTag)

    networks.clear()
    networks ++= nbt.getTagList(NetworksTag, NBT.TAG_COMPOUND).map((nbt: NBTBase) => {
      val network = new Network()
      network.load(nbt.asInstanceOf[NBTTagCompound])
      network
    })
  }
}
