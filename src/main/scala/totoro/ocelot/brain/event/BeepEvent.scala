package totoro.ocelot.brain.event

case class BeepEvent(address: String, frequency: Short, duration: Short) extends Event
