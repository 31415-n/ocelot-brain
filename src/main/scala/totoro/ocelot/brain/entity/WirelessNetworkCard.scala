package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network._
import totoro.ocelot.brain.util.Tier
import totoro.ocelot.brain.util.Tier.Tier
import totoro.ocelot.brain.workspace.Workspace
import totoro.ocelot.brain.{Constants, Settings}

import scala.language.implicitConversions

abstract class WirelessNetworkCard extends NetworkCard with WirelessEndpoint {
  override val node: Component = Network.newNode(this, Visibility.Network).
    withComponent("modem", Visibility.Neighbors).
    create()

  protected def shouldSendWiredTraffic: Boolean

  protected def maxWirelessRange: Double = Settings.get.maxWirelessRange(tier.id)

  // the wired network card precedes wireless cards in max port list
  override def maxOpenPorts: Int = Settings.get.maxOpenPorts(tier.id + 1)

  var strength: Double = maxWirelessRange

  def receivePacket(packet: Packet, source: WirelessEndpoint): Unit = {
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

  override protected def doSend(packet: Packet): Unit = {
    if (strength > 0) {
      Network.sendWirelessPacket(this, strength, packet)
    }
    if (shouldSendWiredTraffic)
      super.doSend(packet)
  }

  override protected def doBroadcast(packet: Packet): Unit = {
    if (strength > 0) {
      Network.sendWirelessPacket(this, strength, packet)
    }
    if (shouldSendWiredTraffic)
      super.doBroadcast(packet)
  }

  // ----------------------------------------------------------------------- //

  override val needUpdate = true

  override def onConnect(node: Node): Unit = {
    super.onConnect(node)
    if (node == this.node) {
      Network.joinWirelessNetwork(this)
    }
  }

  override def onDisconnect(node: Node): Unit = {
    super.onDisconnect(node)
    if (node == this.node) {
      Network.leaveWirelessNetwork(this)
    }
  }

  // ----------------------------------------------------------------------- //

  private final val StrengthTag = "strength"

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)
    if (nbt.hasKey(StrengthTag)) {
      strength = nbt.getDouble(StrengthTag) max 0 min maxWirelessRange
    }
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    nbt.setDouble(StrengthTag, strength)
  }
}

object WirelessNetworkCard {
  class Tier1 extends WirelessNetworkCard {
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

    override def tier: Tier = Tier.One

    override protected def isPacketAccepted(packet: Packet, distance: Double): Boolean = {
      if (distance <= maxWirelessRange && (distance > 0 || shouldSendWiredTraffic)) {
        super.isPacketAccepted(packet, distance)
      } else {
        false
      }
    }
  }

  class Tier2 extends Tier1 {
    override def tier: Tier = Tier.Two

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
