package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.traits.DeviceInfo
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network._
import totoro.ocelot.brain.{Constants, Settings}

import scala.collection.convert.WrapAsScala._

class LinkedCard extends Environment with QuantumNetwork.QuantumNode with DeviceInfo {
  override val node: Node = Network.newNode(this, Visibility.Network).
    withComponent("tunnel", Visibility.Neighbors).
    create()

  var tunnel: String = "creative"

  // ----------------------------------------------------------------------- //

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Network,
    DeviceAttribute.Description -> "Quantumnet controller",
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
    // Cast to iterable to use Scala's toArray instead of the Arguments' one (which converts byte arrays to Strings).
    val packet = Network.newPacket(node.address, null, 0, args.asInstanceOf[java.lang.Iterable[AnyRef]].toArray)
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

  def receivePacket(packet: Packet) {
    val distance = 0
    node.sendToReachable("computer.signal", Seq("modem_message", packet.source, Int.box(packet.port), Double.box(distance)) ++ packet.data: _*)
  }

  // ----------------------------------------------------------------------- //

  override def onConnect(node: Node) {
    super.onConnect(node)
    if (node == this.node) {
      QuantumNetwork.add(this)
    }
  }

  override def onDisconnect(node: Node) {
    super.onDisconnect(node)
    if (node == this.node) {
      QuantumNetwork.remove(this)
    }
  }

  // ----------------------------------------------------------------------- //

  private final val TunnelTag = Settings.namespace + "tunnel"

  override def load(nbt: NBTTagCompound) {
    super.load(nbt)
    if (nbt.hasKey(TunnelTag)) {
      tunnel = nbt.getString(TunnelTag)
    }
  }

  override def save(nbt: NBTTagCompound) {
    super.save(nbt)
    nbt.setString(TunnelTag, tunnel)
  }
}
