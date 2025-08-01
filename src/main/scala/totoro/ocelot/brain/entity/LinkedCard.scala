package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{DeviceInfo, Entity, Environment, Tiered, WakeMessageAware}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network._
import totoro.ocelot.brain.util.Tier
import totoro.ocelot.brain.util.Tier.Tier
import totoro.ocelot.brain.workspace.Workspace
import totoro.ocelot.brain.{Constants, Settings}

class LinkedCard
  extends Entity
    with Environment
    with QuantumNetwork.QuantumNode
    with WakeMessageAware
    with DeviceInfo
    with Tiered {

  override val node: Node = Network.newNode(this, Visibility.Network).
    withComponent("tunnel", Visibility.Neighbors).
    create()

  override def tier: Tier = Tier.Three

  var _tunnel: String = "creative"

  def tunnel: String = _tunnel

  def tunnel_=(value: String): Unit = {
    QuantumNetwork.remove(this)
    _tunnel = value
    QuantumNetwork.add(this)
  }

  // ----------------------------------------------------------------------- //

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Network,
    DeviceAttribute.Description -> "QuantumNet controller",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "AetherComm V",
    DeviceAttribute.Capacity -> Settings.get.maxNetworkPacketSize.toString,
    DeviceAttribute.Width -> Settings.get.maxNetworkPacketParts.toString
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo

  // ----------------------------------------------------------------------- //

  @Callback(doc = """function(data...) -- Sends the specified data to the card this one is linked to.""")
  def send(context: Context, args: Arguments): Array[AnyRef] = {
    val endpoints = QuantumNetwork.getEndpoints(tunnel).filter(_ != this)
    // Arguments.toArray converts byte arrays to Strings. The use of iterator() avoids that.
    val packet = Network.newPacket(node.address, null, 0, args.iterator().toArray)
    for (endpoint <- endpoints) {
      endpoint.receivePacket(packet)
    }
    result(true)
  }

  @Callback(direct = true, doc = """function():number -- Gets the maximum packet size (config setting).""")
  def maxPacketSize(context: Context, args: Arguments): Array[AnyRef] = result(Settings.get.maxNetworkPacketSize)

  @Callback(direct = true, doc = """function():string -- Gets this link card's shared channel address""")
  def getChannel(context: Context, args: Arguments): Array[AnyRef] = {
    result(this.tunnel)
  }

  def receivePacket(packet: Packet): Unit = receivePacket(packet, 0)

  // ----------------------------------------------------------------------- //

  override def onConnect(node: Node): Unit = {
    super.onConnect(node)
    if (node == this.node) {
      QuantumNetwork.add(this)
    }
  }

  override def onDisconnect(node: Node): Unit = {
    super.onDisconnect(node)
    if (node == this.node) {
      QuantumNetwork.remove(this)
    }
  }

  // ----------------------------------------------------------------------- //

  private final val TunnelTag = "tunnel"

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)
    if (nbt.hasKey(TunnelTag)) {
      tunnel = nbt.getString(TunnelTag)
    }
    loadWakeMessage(nbt)
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    nbt.setString(TunnelTag, tunnel)
    saveWakeMessage(nbt)
  }
}
