package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.network.Packet

/**
 * Use this interface on environments that may receive network messages from a
 * bus in a rack.
 *
 * Specifically, this is checked on environments in servers installed in racks.
 * The server will collect the first three environments of components in it
 * implement this interface, and provide their nodes to the rack via the
 * [[RackMountable.getConnectableAt]] method. This in turn will allow them
 * to be 'connected' to the buses, so that they can receive network messages
 * arriving on the respective side of the rack.
 */
trait RackBusConnectable extends Environment  {
  /**
   * Called to inject a network packet that arrived on the bus this
   * environment is connected to in the hosting rack.
   *
   * @param packet the packet to handle.
   */
  def receivePacket(packet: Packet): Unit
}