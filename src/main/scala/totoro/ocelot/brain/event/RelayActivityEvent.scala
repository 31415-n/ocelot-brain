package totoro.ocelot.brain.event

import totoro.ocelot.brain.entity.Relay
import totoro.ocelot.brain.util.Direction

case class RelayActivityEvent(relay: Relay) extends NodeEvent {
  override def address: String = relay.sidedNode(Direction.North).address
}
