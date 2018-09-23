package totoro.ocelot.brain.event

case class TextBufferSetForegroundColorEvent(color: Int, isFromPalette: Boolean) extends Event
