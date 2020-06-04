package totoro.ocelot.brain.event

import totoro.ocelot.brain.entity.Relay

case class RelayActivityEvent(relay: Relay) extends Event
