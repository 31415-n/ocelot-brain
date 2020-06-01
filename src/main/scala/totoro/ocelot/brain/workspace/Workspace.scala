package totoro.ocelot.brain.workspace

import java.util.UUID

import totoro.ocelot.brain.entity.traits.{Entity, Environment, WorkspaceAware}
import totoro.ocelot.brain.nbt.ExtendedNBT._
import totoro.ocelot.brain.nbt.persistence.NBTPersistence
import totoro.ocelot.brain.nbt.{NBT, NBTBase, NBTTagCompound}
import totoro.ocelot.brain.network.Network
import totoro.ocelot.brain.util.Persistable

import scala.jdk.CollectionConverters._
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

  // Entities
  // ----------------------------------------------------------------------- //
  /**
    * This entity collection is used to manage the life cycle of entities,
    * and to save them when needed.
    */
  private val entities: mutable.ListBuffer[Entity] = mutable.ListBuffer.empty

  def add[T <: Entity](entity: T): T = {
    entities += entity
    entity match {
      case wa: WorkspaceAware => wa.workspace = this
      case _ =>
    }
    entity
  }

  def getEntitiesIter: Iterator[Entity] = entities.iterator

  def remove[T <: Entity](entity: T): T = {
    entities -= entity
    entity match {
      case wa: WorkspaceAware => if (wa.workspace == this) wa.workspace = null
      case _ =>
    }
    entity
  }

  /**
    * Update all entities of this workspace
    */
  def update(): Unit = {
    entities.foreach(entity => {
      if (entity.needUpdate) entity.update()
    })
    if (!ingameTimePaused) ingameTime += 1
  }

  def entityByAddress(address: String): Option[Entity] = {
    entities.find { case e: Environment => e.node.address == address }
  }

  // Persistence
  // ----------------------------------------------------------------------- //
  private def collectNetworks(): mutable.Set[Network] = {
    val networks: mutable.Set[Network] = mutable.Set.empty
    entities.foreach { case e: Environment =>
      if (e.node != null && e.node.network != null) networks += e.node.network
    }
    networks
  }

  private val TimeTag = "time"
  private val TimePausedTag = "time_paused"
  private val EdgesTag = "edges"
  private val EntitiesTag = "entities"

  override def save(nbt: NBTTagCompound): Unit = {
    // save global state
    nbt.setInteger(TimeTag, ingameTime)
    nbt.setBoolean(TimePausedTag, ingameTimePaused)

    // save network relations
    val nbtEdges: List[NBTBase] = collectNetworks().toList.flatMap(network => {
      network.save()
    })
    nbt.setTagList(EdgesTag, nbtEdges.asJava)

    // save entities
    val nbtEntities: ListBuffer[NBTBase] = entities.map(entity => {
      NBTPersistence.save(entity)
    })
    nbt.setTagList(EntitiesTag, nbtEntities.asJava)
  }

  override def load(nbt: NBTTagCompound): Unit = {
    ingameTime = nbt.getInteger(TimeTag)
    ingameTimePaused = nbt.getBoolean(TimePausedTag)

    // load entities
    entities.clear()
    nbt.getTagList(EntitiesTag, NBT.TAG_COMPOUND).foreach((nbt: NBTTagCompound) => {
      val entity = NBTPersistence.load(nbt).asInstanceOf[Entity]
      add(entity)
    })

    // load network relations
    nbt.getTagList(EdgesTag, NBT.TAG_COMPOUND).map((nbt: NBTTagCompound) => {
      val left = entityByAddress(nbt.getString(Network.LeftTag))
      val right = entityByAddress(nbt.getString(Network.RightTag))
      if (left.isDefined && right.isDefined) {
        left.get.asInstanceOf[Environment].connect(right.get.asInstanceOf[Environment])
      }
    })
  }
}
