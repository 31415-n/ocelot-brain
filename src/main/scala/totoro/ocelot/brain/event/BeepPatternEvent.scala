package totoro.ocelot.brain.event

case class BeepPatternEvent(address: String, pattern: String) extends NodeEvent
