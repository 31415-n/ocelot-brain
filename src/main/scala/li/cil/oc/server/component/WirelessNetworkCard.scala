package li.cil.oc.server.component

import java.util

import li.cil.oc.{Constants, Settings, api}
import li.cil.oc.api.Network
import li.cil.oc.api.driver.DeviceInfo.{DeviceAttribute, DeviceClass}
import li.cil.oc.api.machine.{Arguments, Callback, Context}
import li.cil.oc.api.network.{EnvironmentHost, _}
import li.cil.oc.common.Tier
import net.minecraft.nbt.NBTTagCompound

import scala.collection.convert.WrapAsJava._
import scala.language.implicitConversions

abstract class WirelessNetworkCard(host: EnvironmentHost) extends NetworkCard(host) with WirelessEndpoint {
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
      api.Network.sendWirelessPacket(this, strength, packet)
    }
    if (shouldSendWiredTraffic)
      super.doSend(packet)
  }

  override protected def doBroadcast(packet: Packet) {
    if (strength > 0) {
      api.Network.sendWirelessPacket(this, strength, packet)
    }
    if (shouldSendWiredTraffic)
      super.doBroadcast(packet)
  }

  // ----------------------------------------------------------------------- //

  override val canUpdate = true

  override def onConnect(node: Node) {
    super.onConnect(node)
    if (node == this.node) {
      api.Network.joinWirelessNetwork(this)
    }
  }

  override def onDisconnect(node: Node) {
    super.onDisconnect(node)
    if (node == this.node) {
      api.Network.leaveWirelessNetwork(this)
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
  class Tier1(host: EnvironmentHost) extends WirelessNetworkCard(host) {
    override protected def maxWirelessRange: Double = Settings.get.maxWirelessRange(Tier.One)
    
    // wired network card is before wireless cards in max port list
    override protected def maxOpenPorts = Settings.get.maxOpenPorts(Tier.One + 1)
    
    override protected def shouldSendWiredTraffic = false

    // ----------------------------------------------------------------------- //

    private final lazy val deviceInfo = Map(
      DeviceAttribute.Class -> DeviceClass.Network,
      DeviceAttribute.Description -> "Wireless ethernet controller",
      DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
      DeviceAttribute.Product -> "39i110 (LPPW-01)",
      DeviceAttribute.Version -> "1.0",
      DeviceAttribute.Capacity -> Settings.get.maxNetworkPacketSize.toString,
      DeviceAttribute.Size -> maxOpenPorts.toString,
      DeviceAttribute.Width -> maxWirelessRange.toString
    )

    override def getDeviceInfo: util.Map[String, String] = deviceInfo
  }
  
  class Tier2(host: EnvironmentHost) extends Tier1(host) {
    override protected def maxWirelessRange: Double = Settings.get.maxWirelessRange(Tier.Two)
    
    // wired network card is before wireless cards in max port list
    override protected def maxOpenPorts = Settings.get.maxOpenPorts(Tier.Two + 1)
    
    override protected def shouldSendWiredTraffic = true

    // ----------------------------------------------------------------------- //

    private final lazy val deviceInfo = Map(
      DeviceAttribute.Class -> DeviceClass.Network,
      DeviceAttribute.Description -> "Wireless ethernet controller",
      DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
      DeviceAttribute.Product -> "62i230 (MPW-01)",
      DeviceAttribute.Version -> "2.0",
      DeviceAttribute.Capacity -> Settings.get.maxNetworkPacketSize.toString,
      DeviceAttribute.Size -> maxOpenPorts.toString,
      DeviceAttribute.Width -> maxWirelessRange.toString
    )
    
    override def getDeviceInfo: util.Map[String, String] = deviceInfo
  }
}
