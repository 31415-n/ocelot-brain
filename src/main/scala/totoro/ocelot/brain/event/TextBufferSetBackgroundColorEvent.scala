package totoro.ocelot.brain.event

case class TextBufferSetBackgroundColorEvent(address: String, color: Int) extends Event
