package totoro.ocelot.brain.entity

import totoro.ocelot.brain.{Constants, Settings}
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network._
import totoro.ocelot.brain.util.Tier

import scala.language.implicitConversions

abstract class WirelessNetworkCard extends NetworkCard with WirelessEndpoint {
  override val node: Component = Network.newNode(this, Visibility.Network).
    withComponent("modem", Visibility.Neighbors).
    create()

  protected def maxWirelessRange: Double

  protected def shouldSendWiredTraffic: Boolean

  var strength: Double = maxWirelessRange

  def receivePacket(packet: Packet, source: WirelessEndpoint) {
    receivePacket(packet, 1)
  }

  // ----------------------------------------------------------------------- //

  @Callback(direct = true, doc = """function():number -- Get the signal strength (range) used when sending messages.""")
  def getStrength(context: Context, args: Arguments): Array[AnyRef] = result(strength)

  @Callback(doc = """function(strength:number):number -- Set the signal strength (range) used when sending messages.""")
  def setStrength(context: Context, args: Arguments): Array[AnyRef] = {
    strength = math.max(0, math.min(args.checkDouble(0), maxWirelessRange))
    result(strength)
  }

  override def isWireless(context: Context, args: Arguments): Array[AnyRef] = result(true)

  override def isWired(context: Context, args: Arguments): Array[AnyRef] = result(shouldSendWiredTraffic)

  override protected def doSend(packet: Packet) {
    if (strength > 0) {
      Network.sendWirelessPacket(this, strength, packet)
    }
    if (shouldSendWiredTraffic)
      super.doSend(packet)
  }

  override protected def doBroadcast(packet: Packet) {
    if (strength > 0) {
      Network.sendWirelessPacket(this, strength, packet)
    }
    if (shouldSendWiredTraffic)
      super.doBroadcast(packet)
  }

  // ----------------------------------------------------------------------- //

  override val needUpdate = true

  override def onConnect(node: Node) {
    super.onConnect(node)
    if (node == this.node) {
      Network.joinWirelessNetwork(this)
    }
  }

  override def onDisconnect(node: Node) {
    super.onDisconnect(node)
    if (node == this.node) {
      Network.leaveWirelessNetwork(this)
    }
  }

  // ----------------------------------------------------------------------- //

  private final val StrengthTag = "strength"

  override def load(nbt: NBTTagCompound) {
    super.load(nbt)
    if (nbt.hasKey(StrengthTag)) {
      strength = nbt.getDouble(StrengthTag) max 0 min maxWirelessRange
    }
  }

  override def save(nbt: NBTTagCompound) {
    super.save(nbt)
    nbt.setDouble(StrengthTag, strength)
  }
}

object WirelessNetworkCard {
  class Tier1 extends WirelessNetworkCard {
    override protected def maxWirelessRange: Double = Settings.get.maxWirelessRange(Tier.One)

    // wired network card is before wireless cards in max port list
    override protected def maxOpenPorts = Settings.get.maxOpenPorts(Tier.One + 1)

    override protected def shouldSendWiredTraffic = false

    // ----------------------------------------------------------------------- //

    private final lazy val deviceInfo = Map(
      DeviceAttribute.Class -> DeviceClass.Network,
      DeviceAttribute.Description -> "Wireless ethernet controller",
      DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
      DeviceAttribute.Product -> "Zinkel W-4.1",
      DeviceAttribute.Version -> "4.1",
      DeviceAttribute.Capacity -> Settings.get.maxNetworkPacketSize.toString,
      DeviceAttribute.Size -> maxOpenPorts.toString,
      DeviceAttribute.Width -> maxWirelessRange.toString
    )

    override def getDeviceInfo: Map[String, String] = deviceInfo
  }

  class Tier2 extends Tier1 {
    override protected def maxWirelessRange: Double = Settings.get.maxWirelessRange(Tier.Two)

    // wired network card is before wireless cards in max port list
    override protected def maxOpenPorts = Settings.get.maxOpenPorts(Tier.Two + 1)

    override protected def shouldSendWiredTraffic = true

    // ----------------------------------------------------------------------- //

    private final lazy val deviceInfo = Map(
      DeviceAttribute.Class -> DeviceClass.Network,
      DeviceAttribute.Description -> "Wireless ethernet controller",
      DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
      DeviceAttribute.Product -> "Zinkel W-4.2",
      DeviceAttribute.Version -> "4.2",
      DeviceAttribute.Capacity -> Settings.get.maxNetworkPacketSize.toString,
      DeviceAttribute.Size -> maxOpenPorts.toString,
      DeviceAttribute.Width -> maxWirelessRange.toString
    )

    override def getDeviceInfo: Map[String, String] = deviceInfo
  }
}
