package totoro.ocelot.brain.event

case class TextBufferSetEvent(x: Int, y: Int, value: String, vertical: Boolean) extends Event
