package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{DeviceInfo, Entity, Environment, RackBusConnectable, Tiered, WakeMessageAware}
import totoro.ocelot.brain.event.EventBus
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network._
import totoro.ocelot.brain.util.Tier
import totoro.ocelot.brain.util.Tier.Tier
import totoro.ocelot.brain.workspace.Workspace
import totoro.ocelot.brain.{Constants, Settings}

import scala.collection.mutable

class NetworkCard
  extends Entity
    with Environment
    with RackBusConnectable
    with WakeMessageAware
    with DeviceInfo
    with Tiered {

  override val node: Component =
    Network.newNode(this, Visibility.Network).withComponent("modem", Visibility.Neighbors).create()

  val openPorts = mutable.Set.empty[Int]

  // wired network card is the 1st in the max ports list (before both wireless cards)
  def maxOpenPorts: Int = Settings.get.maxOpenPorts(tier.id)

  // ----------------------------------------------------------------------- //

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Network,
    DeviceAttribute.Description -> "Ethernet controller",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "Zinkel E-4",
    DeviceAttribute.Version -> "4.0",
    DeviceAttribute.Capacity -> Settings.get.maxNetworkPacketSize.toString,
    DeviceAttribute.Size -> maxOpenPorts.toString,
    DeviceAttribute.Width -> Settings.get.maxNetworkPacketParts.toString,
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo

  override def tier: Tier = Tier.One

  // ----------------------------------------------------------------------- //

  @Callback(doc = """function(port:number):boolean -- Opens the specified port. Returns true if the port was opened.""")
  def open(context: Context, args: Arguments): Array[AnyRef] = {
    val port = checkPort(args.checkInteger(0))
    if (openPorts.contains(port)) result(false)
    else if (openPorts.size >= maxOpenPorts) {
      throw new java.io.IOException("too many open ports")
    }
    else result(openPorts.add(port))
  }

  @Callback(doc = """function([port:number]):boolean -- Closes the specified port (default: all ports). Returns true if ports were closed.""")
  def close(context: Context, args: Arguments): Array[AnyRef] = {
    if (args.count == 0) {
      val closed = openPorts.nonEmpty
      openPorts.clear()
      result(closed)
    } else {
      val port = checkPort(args.checkInteger(0))
      result(openPorts.remove(port))
    }
  }

  @Callback(direct = true, doc = """function(port:number):boolean -- Whether the specified port is open.""")
  def isOpen(context: Context, args: Arguments): Array[AnyRef] = {
    val port = checkPort(args.checkInteger(0))
    result(openPorts.contains(port))
  }

  @Callback(direct = true, doc = """function():boolean -- Whether this card has wireless networking capability.""")
  def isWireless(context: Context, args: Arguments): Array[AnyRef] = result(false)

  @Callback(direct = true, doc = """function():boolean -- Whether this card has wired networking capability.""")
  def isWired(context: Context, args: Arguments): Array[AnyRef] = result(true)

  @Callback(doc = """function(address:string, port:number, data...) -- Sends the specified data to the specified target.""")
  def send(context: Context, args: Arguments): Array[AnyRef] = {
    val address = args.checkString(0)
    val port = checkPort(args.checkInteger(1))
    val packet = Network.newPacket(node.address, address, port, args.drop(2).toArray)
    doSend(packet)
    networkActivity()
    result(true)
  }

  @Callback(doc = """function(port:number, data...) -- Broadcasts the specified data on the specified port.""")
  def broadcast(context: Context, args: Arguments): Array[AnyRef] = {
    val port = checkPort(args.checkInteger(0))
    val packet = Network.newPacket(node.address, null, port, args.drop(1).toArray)
    doBroadcast(packet)
    networkActivity()
    result(true)
  }

  protected def doSend(packet: Packet): Unit = node.sendToReachable("network.message", packet)

  protected def doBroadcast(packet: Packet): Unit = node.sendToReachable("network.message", packet)

  // ----------------------------------------------------------------------- //

  override def onDisconnect(node: Node): Unit = {
    super.onDisconnect(node)
    if (node == this.node) {
      openPorts.clear()
    }
  }

  override def onMessage(message: Message): Unit = {
    super.onMessage(message)
    if ((message.name == "computer.stopped" || message.name == "computer.started") && node.isNeighborOf(message.source))
      openPorts.clear()
    if (message.name == "network.message") message.data match {
      case Array(packet: Packet) => receivePacket(packet)
      case _ =>
    }
  }

  override protected def isPacketAccepted(packet: Packet, distance: Double): Boolean = {
    if (super.isPacketAccepted(packet, distance)) {
      if (openPorts.contains(packet.port)) {
        networkActivity()
        return true
      }
    }
    false
  }

  override def receivePacket(packet: Packet): Unit = receivePacket(packet, 0)

  // ----------------------------------------------------------------------- //

  private final val OpenPortsTag = "openPorts"
  private final val WakeMessageTag = "wakeMessage"
  private final val WakeMessageFuzzyTag = "wakeMessageFuzzy"

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)
    assert(openPorts.isEmpty)
    openPorts ++= nbt.getIntArray(OpenPortsTag)
    loadWakeMessage(nbt)
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    nbt.setIntArray(OpenPortsTag, openPorts.toArray)
    wakeMessage.foreach(nbt.setString(WakeMessageTag, _))
    nbt.setBoolean(WakeMessageFuzzyTag, wakeMessageFuzzy)
  }

  // ----------------------------------------------------------------------- //

  protected def checkPort(port: Int): Int =
    if (port < 1 || port > 0xFFFF) throw new IllegalArgumentException("invalid port number")
    else port

  private def networkActivity(): Unit = {
    EventBus.sendNetworkActivity(node)
  }
}
