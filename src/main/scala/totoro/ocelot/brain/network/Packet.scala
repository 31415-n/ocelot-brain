package totoro.ocelot.brain.network

import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.{Ocelot, Settings}

/**
  * These packets represent messages sent using a network card or wireless
  * network card, and can be relayed by the switch and access point blocks.
  *
  * These will be sent as the payload of `network.message` messages.
  *
  * '''Important''' do ''not'' implement this interface. Use the factory
  * methods in [[Network]] instead.
  *
  * @param source      the address of the ''original'' sender of this packet.
  * @param destination the address of the destination of the packet. This is
  *                    `null` for broadcast packets.
  * @param port        the port this packet is being sent to.
  * @param data        the payload of the packet. This will usually only contain simple types,
  *                    to allow persisting the packet.
  * @param ttl         the remaining 'time to live' for this packet. When a packet with a TTL of
  *                    zero is received it will not be relayed by switches and access points. It
  *                    will however still be received by a network card.
  */
class Packet(var source: String, var destination: String, var port: Int, var data: Array[AnyRef], var ttl: Int = 5) {
  /**
    * The size of the packet's payload.
    *
    * This is computed based on the types in the data array, but is only defined
    * for primitive types, i.e. null, boolean, integer, boolean byte array and
    * string. All other types do ''not'' contribute to the packet's size.
    */
  val size: Int = Option(data).fold(0)(values => {
    if (values.length > Settings.get.maxNetworkPacketParts) throw new IllegalArgumentException("packet has too many parts")
    values.length * 2 + values.foldLeft(0)((acc, arg) => {
      acc + (arg match {
        case null | Unit | None => 1
        case _: java.lang.Boolean => 1
        case _: java.lang.Byte => 2 /* FIXME: Bytes are currently sent as shorts */
        case _: java.lang.Short => 2
        case _: java.lang.Integer => 4
        case _: java.lang.Long => 8
        case _: java.lang.Float => 4
        case _: java.lang.Double => 8
        case value: java.lang.String => value.length max 1
        case value: Array[Byte] => value.length max 1
        case value => throw new IllegalArgumentException(s"unsupported data type: $value (${value.getClass.getCanonicalName})")
      })
    })
  })

  /**
    * Generates a copy of the packet, with a reduced time to live.
    *
    * This is called by switches and access points to generate relayed packets.
    *
    * @return a copy of this packet with a reduced TTL.
    */
  def hop() = new Packet(source, destination, port, data, ttl - 1)

  /**
    * Saves the packet's data to the specified compound tag.
    *
    * Restore a packet saved like this using the factory method in the
    * [[Network]] class.
    */
  def save(nbt: NBTTagCompound): Unit = {
    nbt.setString("source", source)
    if (destination != null && destination.nonEmpty) nbt.setString("dest", destination)
    nbt.setInteger("port", port)
    nbt.setInteger("ttl", ttl)
    nbt.setInteger("dataLength", data.length)
    for (i <- data.indices) data(i) match {
      case null | Unit | None =>
      case value: java.lang.Boolean => nbt.setBoolean("data" + i, value)
      case value: java.lang.Byte => nbt.setShort("data" + i, value.shortValue)
      case value: java.lang.Short => nbt.setShort("data" + i, value)
      case value: java.lang.Integer => nbt.setInteger("data" + i, value)
      case value: java.lang.Long => nbt.setLong("data" + i, value)
      case value: java.lang.Float => nbt.setFloat("data" + i, value)
      case value: java.lang.Double => nbt.setDouble("data" + i, value)
      case value: java.lang.String => nbt.setString("data" + i, value)
      case value: Array[Byte] => nbt.setByteArray("data" + i, value)
      case value => Ocelot.log.warn("Unexpected type while saving network packet: " + value.getClass.getName)
    }
  }

  override def toString = s"{source = $source, destination = $destination, port = $port, data = [${data.mkString(", ")}}]}"
}
