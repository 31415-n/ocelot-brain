package totoro.ocelot.brain.event

case class TextBufferSetForegroundColorEvent(address: String, color: Int) extends Event
