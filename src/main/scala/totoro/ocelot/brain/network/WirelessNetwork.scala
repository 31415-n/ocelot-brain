package totoro.ocelot.brain.network

import scala.collection.mutable

object WirelessNetwork {
  val _endpoints: mutable.HashSet[WirelessEndpoint] = mutable.HashSet.empty[WirelessEndpoint]

  def add(endpoint: WirelessEndpoint): Unit = _endpoints += endpoint

  def remove(endpoint: WirelessEndpoint): Unit = _endpoints -= endpoint

  def endpoints: mutable.HashSet[WirelessEndpoint] = _endpoints
}
