package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.nbt.ExtendedNBT._
import totoro.ocelot.brain.nbt.{NBT, NBTTagCompound}
import totoro.ocelot.brain.network._
import totoro.ocelot.brain.util.{Direction, MovingAverage}
import totoro.ocelot.brain.workspace.Workspace

import scala.collection.mutable

trait Hub extends Environment with SidedEnvironment with WorkspaceAware {
  implicit def ordering[A <: Plug]: Ordering[A] = Ordering.by(_.node.address)

  @Deprecated
  override def node: Node = null

  override def isConnected: Boolean = plugs.values.exists(plug =>
    plug != null &&
      plug.node != null &&
      plug.node.address != null &&
      plug.node.network != null)

  protected val plugs: Map[Direction.Value, Plug] = Direction.values.map(side => side -> createPlug(side)).toMap

  val queue: mutable.Queue[(Option[Direction.Value], Packet)] = mutable.Queue.empty[(Option[Direction.Value], Packet)]

  var maxQueueSize: Int = queueBaseSize

  var relayDelay: Int = relayBaseDelay

  var relayAmount: Int = relayBaseAmount

  var relayCooldown: Int = -1

  // 20 cycles
  val packetsPerCycleAvg = new MovingAverage(20)

  // ----------------------------------------------------------------------- //

  protected def queueBaseSize: Int = Settings.get.switchDefaultMaxQueueSize

  protected def queueSizePerUpgrade: Int = Settings.get.switchQueueSizeUpgrade

  protected def relayBaseDelay: Int = Settings.get.switchDefaultRelayDelay

  protected def relayDelayPerUpgrade: Double = Settings.get.switchRelayDelayUpgrade

  protected def relayBaseAmount: Int = Settings.get.switchDefaultRelayAmount

  protected def relayAmountPerUpgrade: Int = Settings.get.switchRelayAmountUpgrade

  // ----------------------------------------------------------------------- //

  override def sidedNode(side: Direction.Value): Node = if (side != null) plugs(side).node else null

  override def canConnect(side: Direction.Value): Boolean = side != null

  // ----------------------------------------------------------------------- //

  override val needUpdate = true

  override def update(): Unit = {
    super.update()
    if (relayCooldown > 0) {
      relayCooldown -= 1
    }
    else {
      relayCooldown = -1
      if (queue.nonEmpty) queue.synchronized {
        val packetsToRely = math.min(queue.size, relayAmount)
        packetsPerCycleAvg += packetsToRely
        for (_ <- 0 until packetsToRely) {
          val (sourceSide, packet) = queue.dequeue()
          relayPacket(sourceSide, packet)
        }
        if (queue.nonEmpty) {
          relayCooldown = relayDelay - 1
        }
      }
      else if (workspace.getIngameTime % relayDelay == 0) {
        packetsPerCycleAvg += 0
      }
    }
  }

  def tryEnqueuePacket(sourceSide: Option[Direction.Value], packet: Packet): Boolean = queue.synchronized {
    if (packet.ttl > 0 && queue.size < maxQueueSize) {
      queue += sourceSide -> packet.hop()
      if (relayCooldown < 0) {
        relayCooldown = relayDelay - 1
      }
      true
    }
    else false
  }

  protected def relayPacket(sourceSide: Option[Direction.Value], packet: Packet): Unit = {
    for (side <- Direction.values) {
      if (sourceSide.isEmpty || sourceSide.get != side) {
        val node = sidedNode(side)
        if (node != null) {
          node.sendToReachable("network.message", packet)
        }
      }
    }
  }

  // ----------------------------------------------------------------------- //

  private final val PlugsTag = "plugs"
  private final val QueueTag = "queue"
  private final val SideTag = "side"
  private final val RelayCooldownTag = "relayCooldown"

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)
    nbt.getTagList(PlugsTag, NBT.TAG_COMPOUND).toArray[NBTTagCompound].
      zipWithIndex.foreach {
      case (tag, index) => plugs(Direction(index)).node.load(tag)
    }
    nbt.getTagList(QueueTag, NBT.TAG_COMPOUND).foreach(
      (tag: NBTTagCompound) => {
        val side = tag.getDirection(SideTag)
        val packet = Network.newPacket(tag)
        queue += side -> packet
      })
    if (nbt.hasKey(RelayCooldownTag)) {
      relayCooldown = nbt.getInteger(RelayCooldownTag)
    }
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    nbt.setNewTagList(PlugsTag, (0 until Direction.values.size).map(index => {
      val plugNbt = new NBTTagCompound()
      val plug = plugs(Direction(index))
      if (plug.node != null)
        plug.node.save(plugNbt)
      plugNbt
    }))
    nbt.setNewTagList(QueueTag, queue.map {
      case (sourceSide, packet) =>
        val tag = new NBTTagCompound()
        tag.setDirection(SideTag, sourceSide)
        packet.save(tag)
        tag
    })
    if (relayCooldown > 0) {
      nbt.setInteger(RelayCooldownTag, relayCooldown)
    }
  }

  // ----------------------------------------------------------------------- //

  protected def createPlug(side: Direction.Value) = new Plug(side)
  
  protected class Plug(val side: Direction.Value) extends Environment {
    val node: Node = createNode(this)

    override def onMessage(message: Message): Unit = {
      if (isPrimary) {
        onPlugMessage(this, message)
      }
    }

    override def onConnect(node: Node): Unit = onPlugConnect(this, node)

    override def onDisconnect(node: Node): Unit = onPlugDisconnect(this, node)

    def isPrimary: Boolean = plugs.values.find(_.node.network == node.network).contains(this)

    def plugsInOtherNetworks: Iterable[Plug] = plugs.values.filter(_.node.network != node.network)
  }

  protected def onPlugConnect(plug: Plug, node: Node): Unit = {}

  protected def onPlugDisconnect(plug: Plug, node: Node): Unit = {}

  protected def onPlugMessage(plug: Plug, message: Message): Unit = {
    if (message.name == "network.message" && !plugs.values.exists(_.node == message.source)) message.data match {
      case Array(packet: Packet) => tryEnqueuePacket(Option(plug.side), packet)
      case _ =>
    }
  }

  protected def createNode(plug: Plug): Node = Network.newNode(plug, Visibility.Network, java.util.UUID.randomUUID().toString).create()
}
