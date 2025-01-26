package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.{ComponentInventory, Entity, Hub, Tiered}
import totoro.ocelot.brain.event.{EventBus, RelayActivityEvent}
import totoro.ocelot.brain.nbt.ExtendedNBT._
import totoro.ocelot.brain.nbt.{NBT, NBTTagCompound}
import totoro.ocelot.brain.network._
import totoro.ocelot.brain.util.Tier.Tier
import totoro.ocelot.brain.util.{Direction, Tier}
import totoro.ocelot.brain.workspace.Workspace

class Relay extends Hub with ComponentInventory with Entity with WirelessEndpoint with QuantumNetwork.QuantumNode {
  val cpuSlot: Slot = inventory.slot(0)
  val memorySlot: Slot = inventory.slot(1)
  val hddSlot: Slot = inventory.slot(2)
  val networkCardSlot: Slot = inventory.slot(3)

  private var wirelessTier: Option[Tier] = None // from Tier.One to Tier.Two

  protected var strength: Double = maxWirelessRange

  private var isRepeater = true

  private def isWirelessEnabled: Boolean = wirelessTier.exists(_ >= Tier.One)

  def maxWirelessRange: Double = wirelessTier.map(_.id).map(Settings.get.maxWirelessRange).getOrElse(0)

  private var isLinkedEnabled = false

  protected var _tunnel = "creative"

  override def tunnel: String = _tunnel

  protected val componentNodes: Array[Component] = Array.fill(6)(Network.newNode(this, Visibility.Network).
    withComponent("relay").
    create())

  private var lastMessage = 0L

  private def onSwitchActivity(): Unit = {
    val now = System.currentTimeMillis()
    if (now - lastMessage >= (relayDelay - 1) * 50) {
      lastMessage = now
      EventBus.send(RelayActivityEvent(this))
    }
  }

  // ----------------------------------------------------------------------- //

  @Callback(direct = true, doc = """function():number -- Get the signal strength (range) used when relaying messages.""")
  def getStrength(context: Context, args: Arguments): Array[AnyRef] = synchronized(result(strength))

  @Callback(doc = """function(strength:number):number -- Set the signal strength (range) used when relaying messages.""")
  def setStrength(context: Context, args: Arguments): Array[AnyRef] = synchronized {
    strength = math.max(0, math.min(args.checkDouble(0), maxWirelessRange))
    result(strength)
  }

  @Callback(direct = true, doc = """function():boolean -- Get whether the access point currently acts as a repeater (resend received wireless packets wirelessly).""")
  def isRepeater(context: Context, args: Arguments): Array[AnyRef] = synchronized(result(isRepeater))

  @Callback(doc = """function(enabled:boolean):boolean -- Set whether the access point should act as a repeater.""")
  def setRepeater(context: Context, args: Arguments): Array[AnyRef] = synchronized {
    isRepeater = args.checkBoolean(0)
    result(isRepeater)
  }

  // ----------------------------------------------------------------------- //

  override def receivePacket(packet: Packet, source: WirelessEndpoint): Unit = {
    if (isWirelessEnabled) {
      tryEnqueuePacket(None, packet)
    }
  }

  override def receivePacket(packet: Packet): Unit = {
    if (isLinkedEnabled) {
      tryEnqueuePacket(None, packet)
    }
  }

  override protected def relayPacket(sourceSide: Option[Direction.Value], packet: Packet): Unit = {
    super.relayPacket(sourceSide, packet)

    if (isWirelessEnabled && strength > 0 && (sourceSide.isDefined || isRepeater)) {
      Network.sendWirelessPacket(this, strength, packet)
    }

    if (isLinkedEnabled && sourceSide.isDefined) {
      val endpoints = QuantumNetwork.getEndpoints(_tunnel).filter(_ != this)
      for (endpoint <- endpoints) {
        endpoint.receivePacket(packet)
      }
    }

    onSwitchActivity()
  }

  // ----------------------------------------------------------------------- //

  override protected def onPlugConnect(plug: Plug, node: Node): Unit = {
    super.onPlugConnect(plug, node)
    if (node == plug.node) {
      Network.joinWirelessNetwork(this)
    }
    if (plug.isPrimary)
      plug.node.connect(componentNodes(plug.side.id))
    else
      componentNodes(plug.side.id).remove()
  }

  override protected def onPlugDisconnect(plug: Plug, node: Node): Unit = {
    super.onPlugDisconnect(plug, node)
    if (node == plug.node) {
      Network.leaveWirelessNetwork(this)
    }
    if (plug.isPrimary && node != plug.node)
      plug.node.connect(componentNodes(plug.side.id))
    else
      componentNodes(plug.side.id).remove()
  }

  // ----------------------------------------------------------------------- //

  override def onEntityAdded(slot: Slot, entity: Entity): Unit = {
    super.onEntityAdded(slot, entity)
    updateLimits(slot, entity)
  }

  private def updateLimits(slot: Slot, entity: Entity): Unit = {
    slot match {
      case `cpuSlot` =>
        relayDelay = (relayBaseDelay - (entity.asInstanceOf[Tiered].tier.num * relayDelayPerUpgrade).toInt).max(1)

      case `memorySlot` =>
        relayAmount = (relayBaseAmount + (entity match {
          case mem: Memory => (mem.memoryTier.id + 1) * relayAmountPerUpgrade
          case t: Tiered => t.tier.num * (relayAmountPerUpgrade * 2)
        })).max(1)

      case `hddSlot` =>
        maxQueueSize = (queueBaseSize + entity.asInstanceOf[Tiered].tier.num * queueSizePerUpgrade).max(1)

      case `networkCardSlot` => entity match {
        case w: WirelessNetworkCard =>
          wirelessTier = Some(w.tier)

        case l: LinkedCard =>
          _tunnel = l.tunnel
          isLinkedEnabled = true
          QuantumNetwork.add(this)
      }
    }
  }

  override def onEntityRemoved(slot: Slot, entity: Entity): Unit = {
    super.onEntityRemoved(slot, entity)

    slot match {
      case `cpuSlot` => relayDelay = relayBaseDelay
      case `memorySlot` => relayAmount = relayBaseAmount
      case `hddSlot` => maxQueueSize = queueBaseSize
      case `networkCardSlot` =>
        wirelessTier = None
        isLinkedEnabled = false
        QuantumNetwork.remove(this)
    }
  }

  // ----------------------------------------------------------------------- //

  private final val StrengthTag = "strength"
  private final val IsRepeaterTag = "isRepeater"
  private final val ComponentNodesTag = "componentNodes"

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)

    for (slot <- inventory; entity <- slot.get) {
      updateLimits(slot, entity)
    }

    if (nbt.hasKey(StrengthTag)) {
      strength = nbt.getDouble(StrengthTag) max 0 min maxWirelessRange
    }
    if (nbt.hasKey(IsRepeaterTag)) {
      isRepeater = nbt.getBoolean(IsRepeaterTag)
    }
    nbt.getTagList(ComponentNodesTag, NBT.TAG_COMPOUND).toArray[NBTTagCompound].
      zipWithIndex.foreach {
      case (tag, index) => componentNodes(index).load(tag)
    }
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)

    nbt.setDouble(StrengthTag, strength)
    nbt.setBoolean(IsRepeaterTag, isRepeater)
    nbt.setNewTagList(ComponentNodesTag, componentNodes.map {
      case node: Node =>
        val tag = new NBTTagCompound()
        node.save(tag)
        tag
      case _ => new NBTTagCompound()
    })
  }
}
