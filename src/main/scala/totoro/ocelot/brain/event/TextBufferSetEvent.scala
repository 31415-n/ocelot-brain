package totoro.ocelot.brain.event

case class TextBufferSetEvent(address: String, x: Int, y: Int, value: String, vertical: Boolean) extends NodeEvent
