package totoro.ocelot.brain.entity.traits

import com.google.common.base.Charsets
import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.Packet

trait WakeMessageAware extends NetworkAware {
  protected var wakeMessage: Option[String] = None

  protected var wakeMessageFuzzy: Boolean = false

  @Callback(direct = true, doc = """function():string, boolean -- Get the current wake-up message.""")
  def getWakeMessage(context: Context, args: Arguments): Array[AnyRef] = result(wakeMessage.orNull, wakeMessageFuzzy)

  @Callback(doc = """function(message:string[, fuzzy:boolean]):string -- Set the wake-up message and whether to ignore additional data/parameters.""")
  def setWakeMessage(context: Context, args: Arguments): Array[AnyRef] = {
    val oldMessage = wakeMessage
    val oldFuzzy = wakeMessageFuzzy

    if (args.optAny(0, null) == null)
      wakeMessage = None
    else
      wakeMessage = Option(args.checkString(0))
    wakeMessageFuzzy = args.optBoolean(1, wakeMessageFuzzy)

    result(oldMessage.orNull, oldFuzzy)
  }

  protected def isPacketAccepted(packet: Packet, distance: Double): Boolean = true

  protected def receivePacket(packet: Packet, distance: Double) {
    if (packet.source != node.address && Option(packet.destination).forall(_ == node.address)) {
      if (isPacketAccepted(packet, distance)) {
        node.sendToReachable("computer.signal", Seq("modem_message", packet.source, Int.box(packet.port), Double.box(distance)) ++ packet.data: _*)
      }

      // Accept wake-up messages regardless of port because we close all ports
      // when our computer shuts down.
      val wakeup: Boolean = packet.data match {
        case Array(message: Array[Byte]) if wakeMessage.contains(new String(message, Charsets.UTF_8)) => true
        case Array(message: String) if wakeMessage.contains(message) => true
        case Array(message: Array[Byte], _*) if wakeMessageFuzzy && wakeMessage.contains(new String(message, Charsets.UTF_8)) => true
        case Array(message: String, _*) if wakeMessageFuzzy && wakeMessage.contains(message) => true
        case _ => false
      }
      if (wakeup) {
        node.sendToNeighbors("computer.start")
      }
    }
  }

  def loadWakeMessage(nbt: NBTTagCompound): Unit = {
    if (nbt.hasKey("wakeMessage")) {
      wakeMessage = Option(nbt.getString("wakeMessage"))
    }
    wakeMessageFuzzy = nbt.getBoolean("wakeMessageFuzzy")
  }

  def saveWakeMessage(nbt: NBTTagCompound): Unit = {
    wakeMessage.foreach(nbt.setString("wakeMessage", _))
    nbt.setBoolean("wakeMessageFuzzy", wakeMessageFuzzy)
  }
}
