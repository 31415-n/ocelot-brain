package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.{Entity, Hub}
import totoro.ocelot.brain.event.{EventBus, RelayActivityEvent}
import totoro.ocelot.brain.nbt.ExtendedNBT._
import totoro.ocelot.brain.nbt.{NBT, NBTTagCompound}
import totoro.ocelot.brain.network._
import totoro.ocelot.brain.util.Tier.Tier
import totoro.ocelot.brain.util.{Direction, Tier}
import totoro.ocelot.brain.workspace.Workspace

class Relay extends Hub with Entity with WirelessEndpoint with QuantumNetwork.QuantumNode {
  protected var _cpuTier: Option[Tier] = None // from Tier.One to Tier.Three
  protected var _memoryTier: Option[Tier] = None // from Tier.One to Tier.Six
  protected var _hddTier: Option[Tier] = None // from Tier.One to Tier.Three

  protected var _wirelessTier: Option[Tier] = None // from Tier.One to Tier.Two
  protected var isLinkedEnabled = false

  protected var strength: Double = maxWirelessRange

  protected var isRepeater = true

  def isWirelessEnabled: Boolean = _wirelessTier.exists(_ >= Tier.One)

  def maxWirelessRange: Double = _wirelessTier.map(_.id).map(Settings.get.maxWirelessRange).getOrElse(0)

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

  def cpuTier: Option[Tier] = _cpuTier

  def cpuTier_=(tier: Option[Tier]): Unit = {
    _cpuTier = tier
    updateLimits()
  }

  def memoryTier: Option[Tier] = _memoryTier

  def memoryTier_=(tier: Option[Tier]): Unit = {
    _memoryTier = tier
    updateLimits()
  }

  def hddTier: Option[Tier] = _hddTier

  def hddTier_=(tier: Option[Tier]): Unit = {
    _hddTier = tier
    updateLimits()
  }

  def wirelessTier: Option[Tier] = _wirelessTier

  def wirelessTier_=(tier: Option[Tier]): Unit = {
    _wirelessTier = tier
    isLinkedEnabled = tier.isEmpty
    updateLimits()
  }

  def tunnel_=(tunnel: String): Unit = {
    this.tunnel = Some(tunnel)
  }

  def tunnel_=(tunnel: Option[String]): Unit = {
    tunnel match {
      case Some(tunnel) =>
        _tunnel = tunnel
        isLinkedEnabled = true
        _wirelessTier = None

      case None =>
        isLinkedEnabled = false
    }

    updateLimits()
  }

  private def updateLimits(): Unit = {
    relayDelay = _cpuTier match {
      case Some(cpuTier) => (relayBaseDelay - (cpuTier.num * relayDelayPerUpgrade).toInt).max(1)
      case None => relayBaseDelay
    }

    relayAmount = _memoryTier match {
      case Some(memoryTier) => (relayBaseAmount + memoryTier.num * relayAmountPerUpgrade).max(1)
      case None => relayBaseAmount
    }

    maxQueueSize = _hddTier match {
      case Some(hddTier) => (queueBaseSize + hddTier.num * queueSizePerUpgrade).max(1)
      case None => queueBaseSize
    }

    if (isLinkedEnabled) {
      QuantumNetwork.add(this)
    } else {
      QuantumNetwork.remove(this)
    }
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

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)

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

    def readTier(key: String): Option[Tier] =
      if (nbt.hasKey(key)) {
        val tierId = nbt.getInteger(key)

        // previously Tier.None
        if (tierId == -1) None else Some(Tier(tierId))
      } else None

    _cpuTier = readTier(CpuTierTag)
    _memoryTier = readTier(MemoryTierTag)
    _hddTier = readTier(HDDTierTag)
    _wirelessTier = readTier(WirelessTierTag)

    if (nbt.hasKey(TunnelTag)) {
      _tunnel = nbt.getString(TunnelTag)
    }

    if (nbt.hasKey(IsLinkedEnabledTag)) {
      isLinkedEnabled = nbt.getBoolean(IsLinkedEnabledTag)
    }

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

    _cpuTier.foreach(cpuTier => nbt.setInteger(CpuTierTag, cpuTier.id))
    _memoryTier.foreach(memoryTier => nbt.setInteger(MemoryTierTag, memoryTier.id))
    _hddTier.foreach(hddTier => nbt.setInteger(HDDTierTag, hddTier.id))
    _wirelessTier.foreach(wirelessTier => nbt.setInteger(WirelessTierTag, wirelessTier.id))
    nbt.setString(TunnelTag, _tunnel)
    nbt.setBoolean(IsLinkedEnabledTag, isLinkedEnabled)
  }
}
