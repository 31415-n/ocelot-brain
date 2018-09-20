package totoro.ocelot.brain.network

/**
  * Interface for wireless endpoints that can be registered with the internal
  * wireless network registry.
  *
  * These can be added to the wireless network via the `Network` API, to
  * allow them to receive packets like wireless network cards and access points
  * do (and handle or forward them as they see fit).
  *
  * If the position of the endpoint can change, it must be updated manually via
  * `Network.updateWirelessNetwork(WirelessEndpoint)`.
  */
trait WirelessEndpoint {
  /**
    * Makes the endpoint receive a single packet.
    *
    * @param packet the packet to receive.
    * @param sender the endpoint that sent the message. This is not
    *               necessarily the original sender of the packet, just
    *               the last point it went through, such as an access
    *               point, for example.
    */
  def receivePacket(packet: Packet, sender: WirelessEndpoint): Unit
}
