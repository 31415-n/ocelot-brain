package totoro.ocelot.brain.workspace

import totoro.ocelot.brain.entity.tape.StorageManager
import totoro.ocelot.brain.entity.traits.{Entity, Environment, SidedEnvironment, WorkspaceAware}
import totoro.ocelot.brain.nbt.ExtendedNBT._
import totoro.ocelot.brain.nbt.persistence.NBTPersistence
import totoro.ocelot.brain.nbt.{NBT, NBTBase, NBTTagCompound}
import totoro.ocelot.brain.network.Node
import totoro.ocelot.brain.util.Direction

import java.nio.file.Path
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._
import scala.util.Random

/**
  * A separated spacetime plane with networks and entities.
  * Can be put on pause. Can be serialized to a NBT tag.
 *  Keeps a `path` to save files (for things that cannot be serialized to NBT, like hard drives).
  */
class Workspace(var path: Path) {
  private val random = new Random(System.currentTimeMillis())
  def rand: Random = random

  val tapeStorage = new StorageManager(this)

  // Internal emulator time in Minecraft ticks
  // ----------------------------------------------------------------------- //
  private var ingameTime: Int = 0
  private var ingameTimePaused: Boolean = false

  private var lastTickNanoTime: Long = System.nanoTime()

  /**
    * @return workspace time in ticks
    */
  def getIngameTime: Int = ingameTime

  def setIngameTime(ticks: Int): Unit = {
    ingameTime = ticks
    lastTickNanoTime = System.nanoTime()
  }

  def getLastTickNanoTime: Long = lastTickNanoTime

  /**
    * Usually the internal workspace time gets updates by one tick
    * for every call of `workspace.update()`.
    * But you can "pause" the time, and then it will remain the same, regardless of `update()` calls.
    */
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
    entity.initialize()
    entity match {
      case wa: WorkspaceAware => wa.workspace = this
      case _ =>
    }
    entity
  }

  def getEntitiesIter: Iterator[Entity] = entities.iterator

  def remove[T <: Entity](entity: T): T = {
    // We: 1. dispose 2. remove from the list 3. unset the workspace.
    // What order should these three things really be in?
    // ...Let's hope nothing requires a specific order.
    entity.dispose()
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
    for (entity <- entities if entity.needUpdate) {
      entity.update()
    }

    if (!ingameTimePaused) {
      ingameTime += 1
      lastTickNanoTime = System.nanoTime()
    }
  }

  def entityByAddress(address: String): Option[Entity] = {
    entities.find {
      case se: SidedEnvironment => Direction.values.unsorted
        .exists(dir => se.sidedNode(dir) != null && se.sidedNode(dir).address == address)
      case e: Environment => e.node.address == address
    }
  }

  def nodeByAddress(address: String): Option[Node] = {
    for (entity <- entities) {
      entity match {
        case se: SidedEnvironment =>
          val result = Direction.values.unsorted
            .find(dir => se.sidedNode(dir) != null && se.sidedNode(dir).address == address).map(dir => se.sidedNode(dir))
          if (result.nonEmpty) return result
        case e: Environment if e.node.address == address => return Some(e.node)
        case _ =>
      }
    }
    None
  }

  // Persistence
  // ----------------------------------------------------------------------- //
  private val LeftTag = "left"
  private val RightTag = "right"

  private def buildEdgeNbt(address1: String, address2: String): NBTTagCompound = {
    val edgeNbt = new NBTTagCompound()
    edgeNbt.setString(LeftTag, address1)
    edgeNbt.setString(RightTag, address2)
    edgeNbt
  }

  private def collectEdges(): ListBuffer[NBTBase] = {
    entities.flatMap {
      case s: SidedEnvironment =>
        Direction.values.unsorted.flatMap { d: Direction.Value =>
          val node = s.sidedNode(d)
          if (node != null) node.neighbors.map(n => buildEdgeNbt(node.address, n.address))
          else Seq.empty
        }
      case e: Environment =>
        e.node.neighbors.map(n => buildEdgeNbt(e.node.address, n.address))
    }
  }

  private val TimeTag = "time"
  private val TimePausedTag = "time_paused"
  private val EdgesTag = "edges"
  private val EntitiesTag = "entities"

  def save(nbt: NBTTagCompound): Unit = {
    // save global state
    nbt.setInteger(TimeTag, ingameTime)
    nbt.setBoolean(TimePausedTag, ingameTimePaused)
    lastTickNanoTime = System.nanoTime()

    // save entities
    val nbtEntities: ListBuffer[NBTBase] = entities.map(entity => {
      NBTPersistence.save(entity)
    })
    nbt.setTagList(EntitiesTag, nbtEntities.asJava)

    // save network relations
    val nbtEdges: ListBuffer[NBTBase] = collectEdges()
    nbt.setTagList(EdgesTag, nbtEdges.asJava)
  }

  def load(nbt: NBTTagCompound): Unit = {
    ingameTime = nbt.getInteger(TimeTag)
    ingameTimePaused = nbt.getBoolean(TimePausedTag)

    // load entities
    entities.foreach(_.dispose())
    entities.clear()
    nbt.getTagList(EntitiesTag, NBT.TAG_COMPOUND).foreach((nbt: NBTTagCompound) => {
      val entity = NBTPersistence.load(nbt, this).asInstanceOf[Entity]
      add(entity)
    })

    // load network relations
    nbt.getTagList(EdgesTag, NBT.TAG_COMPOUND).map((nbt: NBTTagCompound) => {
      val left = nodeByAddress(nbt.getString(LeftTag))
      val right = nodeByAddress(nbt.getString(RightTag))
      if (left.isDefined && right.isDefined) {
        if (!left.get.isNeighborOf(right.get))
          left.get.connect(right.get)
      }
    })
  }
}
