package totoro.ocelot.brain.network

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object WirelessNetwork {
  val _endpoints: ArrayBuffer[WirelessEndpoint] = mutable.ArrayBuffer.empty[WirelessEndpoint]

  def add(endpoint: WirelessEndpoint): Unit = _endpoints += endpoint

  def remove(endpoint: WirelessEndpoint): Unit = _endpoints -= endpoint

  def endpoints: ArrayBuffer[WirelessEndpoint] = _endpoints
}
