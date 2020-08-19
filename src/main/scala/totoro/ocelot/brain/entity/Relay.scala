package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.{Entity, Hub}
import totoro.ocelot.brain.event.{EventBus, RelayActivityEvent}
import totoro.ocelot.brain.nbt.ExtendedNBT._
import totoro.ocelot.brain.nbt.{NBT, NBTTagCompound}
import totoro.ocelot.brain.network._
import totoro.ocelot.brain.util.{Direction, Tier}

class Relay extends Hub with Entity with WirelessEndpoint with QuantumNetwork.QuantumNode {
  protected var cpuTier: Int = Tier.None // from Tier.None to Tier.Three
  protected var memoryTier: Int = Tier.None // from Tier.None to Tier.Six
  protected var hddTier: Int = Tier.None // from Tier.None to Tier.Three

  protected var wirelessTier: Int = Tier.None // from Tier.None to Tier.Two
  protected var isLinkedEnabled = false

  protected var strength: Double = maxWirelessRange

  protected var isRepeater = true

  def isWirelessEnabled: Boolean = wirelessTier >= Tier.One

  def maxWirelessRange: Double = if (wirelessTier == Tier.One || wirelessTier == Tier.Two)
    Settings.get.maxWirelessRange(wirelessTier) else 0

  protected var _tunnel = "creative"

  override def tunnel: String = _tunnel

  protected val componentNodes: Array[Component] = Array.fill(6)(Network.newNode(this, Visibility.Network).
    withComponent("relay").
    create())

  var lastMessage = 0L

  def onSwitchActivity(): Unit = {
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
    strength = math.max(args.checkDouble(0), math.min(0, maxWirelessRange))
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

  override protected def onPlugConnect(plug: Plug, node: Node) {
    super.onPlugConnect(plug, node)
    if (node == plug.node) {
      Network.joinWirelessNetwork(this)
    }
    if (plug.isPrimary)
      plug.node.connect(componentNodes(plug.side.id))
    else
      componentNodes(plug.side.id).remove()
  }

  override protected def onPlugDisconnect(plug: Plug, node: Node) {
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

  def setCpuTier(tier: Int): Unit = {
    if (tier >= Tier.One && tier <= Tier.Three) {
      cpuTier = tier
      updateLimits()
    }
  }

  def setMemoryTier(tier: Int): Unit = {
    if (tier >= Tier.One && tier <= Tier.Six) {
      memoryTier = tier
      updateLimits()
    }
  }

  def setHddTier(tier: Int): Unit = {
    if (tier >= Tier.One && tier <= Tier.Three) {
      hddTier = tier
      updateLimits()
    }
  }

  def setWirelessTier(tier: Int): Unit = {
    if (tier >= Tier.One && tier <= Tier.Two) {
      wirelessTier = tier
      isLinkedEnabled = false
      updateLimits()
    }
  }

  def setLinkedTunnel(tunnel: String): Unit = {
    if (tunnel != null) {
      this._tunnel = tunnel
      isLinkedEnabled = true
      wirelessTier = Tier.None
    } else {
      isLinkedEnabled = false
    }
    updateLimits()
  }

  private def updateLimits() {
    if (cpuTier > Tier.None && cpuTier <= Tier.Three)
      relayDelay = math.max(1, relayBaseDelay - ((cpuTier + 1) * relayDelayPerUpgrade).toInt)
    else relayDelay = relayBaseDelay

    if (memoryTier > Tier.None && memoryTier <= Tier.Six)
      relayAmount = math.max(1, relayBaseAmount + (memoryTier + 1) * relayAmountPerUpgrade)
    else relayAmount = relayBaseAmount

    if (hddTier > Tier.None && hddTier <= Tier.Three)
      maxQueueSize = math.max(1, queueBaseSize + (hddTier + 1) * queueSizePerUpgrade)
    else maxQueueSize = queueBaseSize

    if (isLinkedEnabled) QuantumNetwork.add(this)
    else QuantumNetwork.remove(this)
  }

  // ----------------------------------------------------------------------- //

  private final val StrengthTag = "strength"
  private final val IsRepeaterTag = "isRepeater"
  private final val ComponentNodesTag = "componentNodes"

  private final val CpuTierTag = "cpuTier"
  private final val MemoryTierTag = "memoryTier"
  private final val HDDTierTag = "hddTier"
  private final val WirelessTierTag = "wirelessTier"
  private final val TunnelTag = "tunnel"
  private final val IsLinkedEnabledTag = "isLinkedEnabled"

  override def load(nbt: NBTTagCompound): Unit = {
    super.load(nbt)

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

    if (nbt.hasKey(CpuTierTag)) cpuTier = nbt.getInteger(CpuTierTag)
    if (nbt.hasKey(MemoryTierTag)) memoryTier = nbt.getInteger(MemoryTierTag)
    if (nbt.hasKey(HDDTierTag)) hddTier = nbt.getInteger(HDDTierTag)
    if (nbt.hasKey(WirelessTierTag)) wirelessTier = nbt.getInteger(WirelessTierTag)
    if (nbt.hasKey(TunnelTag)) _tunnel = nbt.getString(TunnelTag)
    if (nbt.hasKey(IsLinkedEnabledTag)) isLinkedEnabled = nbt.getBoolean(IsLinkedEnabledTag)

    updateLimits()
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

    nbt.setInteger(CpuTierTag, cpuTier)
    nbt.setInteger(MemoryTierTag, memoryTier)
    nbt.setInteger(HDDTierTag, hddTier)
    nbt.setInteger(WirelessTierTag, wirelessTier)
    nbt.setString(TunnelTag, _tunnel)
    nbt.setBoolean(IsLinkedEnabledTag, isLinkedEnabled)
  }
}
