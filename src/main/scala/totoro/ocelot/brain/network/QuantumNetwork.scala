package totoro.ocelot.brain.network

import scala.collection.mutable

// Just because the name is so fancy!
object QuantumNetwork {
  private val tunnels = mutable.Map.empty[String, mutable.WeakHashMap[QuantumNode, Unit]]

  def add(card: QuantumNode): Unit = {
    tunnels.getOrElseUpdate(card.tunnel, mutable.WeakHashMap.empty).put(card, ())
  }

  def remove(card: QuantumNode): Unit = {
    tunnels.get(card.tunnel).foreach(_.remove(card))
  }

  def getEndpoints(tunnel: String): Iterable[QuantumNode] =
    tunnels.get(tunnel).fold(Iterable.empty[QuantumNode])(_.keys)

  trait QuantumNode {
    def tunnel: String

    def receivePacket(packet: Packet): Unit
  }
}
