package li.cil.oc.common.tileentity.traits

import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.api.network._
import li.cil.oc.common.tileentity.traits
import li.cil.oc.util.ExtendedNBT._
import li.cil.oc.util.MovingAverage
import net.minecraft.nbt.{NBT, NBTTagCompound}
import net.minecraft.util.EnumFacing

import scala.collection.mutable

trait Hub extends traits.Environment with SidedEnvironment {
  override def node: Node = null

  override protected def isConnected: Boolean = plugs.exists(plug =>
    plug != null &&
    plug.node != null &&
    plug.node.address != null &&
    plug.node.network != null)

  protected val plugs: Array[Plug] = EnumFacing.values.map(side => createPlug(side))

  val queue: mutable.Queue[(Option[EnumFacing], Packet)] = mutable.Queue.empty[(Option[EnumFacing], Packet)]

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

  override def canConnect(side: EnumFacing): Boolean = side != null

  override def sidedNode(side: EnumFacing): Node = if (side != null) plugs(side.ordinal).node else null

  // ----------------------------------------------------------------------- //

  override def updateEntity() {
    super.updateEntity()
    if (relayCooldown > 0) {
      relayCooldown -= 1
    }
    else {
      relayCooldown = -1
      if (queue.nonEmpty) queue.synchronized {
        packetsPerCycleAvg += queue.size
        for (_ <- 0 until math.min(queue.size, relayAmount)) {
          val (sourceSide, packet) = queue.dequeue()
          relayPacket(sourceSide, packet)
        }
        if (queue.nonEmpty) {
          relayCooldown = relayDelay - 1
        }
      }
      else if (getWorld.getTotalWorldTime % relayDelay == 0) {
        packetsPerCycleAvg += 0
      }
    }
  }

  def tryEnqueuePacket(sourceSide: Option[EnumFacing], packet: Packet): Boolean = queue.synchronized {
    if (packet.ttl > 0 && queue.size < maxQueueSize) {
      queue += sourceSide -> packet.hop()
      if (relayCooldown < 0) {
        relayCooldown = relayDelay - 1
      }
      true
    }
    else false
  }

  protected def relayPacket(sourceSide: Option[EnumFacing], packet: Packet) {
    for (side <- EnumFacing.values if Option(side) != sourceSide && sidedNode(side) != null) {
      sidedNode(side).sendToReachable("network.message", packet)
    }
  }

  // ----------------------------------------------------------------------- //

  private final val PlugsTag = Settings.namespace + "plugs"
  private final val QueueTag = Settings.namespace + "queue"
  private final val SideTag = "side"
  private final val RelayCooldownTag = Settings.namespace + "relayCooldown"

  override def readFromNBT(nbt: NBTTagCompound) {
    super.readFromNBT(nbt)
    nbt.getTagList(PlugsTag, NBT.TAG_COMPOUND).toArray[NBTTagCompound].
      zipWithIndex.foreach {
      case (tag, index) => plugs(index).node.load(tag)
    }
    nbt.getTagList(QueueTag, NBT.TAG_COMPOUND).foreach(
      (tag: NBTTagCompound) => {
        val side = tag.getDirection(SideTag)
        val packet = api.Network.newPacket(tag)
        queue += side -> packet
      })
    if (nbt.hasKey(RelayCooldownTag)) {
      relayCooldown = nbt.getInteger(RelayCooldownTag)
    }
  }

  override def writeToNBT(nbt: NBTTagCompound): Unit = queue.synchronized {
    super.writeToNBT(nbt)
    nbt.setNewTagList(PlugsTag, plugs.map(plug => {
      val plugNbt = new NBTTagCompound()
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

  protected def createPlug(side: EnumFacing) = new Plug(side)

  protected class Plug(val side: EnumFacing) extends api.network.Environment {
    val node: Node = createNode(this)

    override def onMessage(message: Message) {
      if (isPrimary) {
        onPlugMessage(this, message)
      }
    }

    override def onConnect(node: Node): Unit = onPlugConnect(this, node)

    override def onDisconnect(node: Node): Unit = onPlugDisconnect(this, node)

    def isPrimary: Boolean = plugs(plugs.indexWhere(_.node.network == node.network)) == this

    def plugsInOtherNetworks: Array[Plug] = plugs.filter(_.node.network != node.network)
  }

  protected def onPlugConnect(plug: Plug, node: Node) {}

  protected def onPlugDisconnect(plug: Plug, node: Node) {}

  protected def onPlugMessage(plug: Plug, message: Message) {
    if (message.name == "network.message" && !plugs.exists(_.node == message.source)) message.data match {
      case Array(packet: Packet) => tryEnqueuePacket(Option(plug.side), packet)
      case _ =>
    }
  }

  protected def createNode(plug: Plug): Node = api.Network.newNode(plug, Visibility.Network).create()
}
