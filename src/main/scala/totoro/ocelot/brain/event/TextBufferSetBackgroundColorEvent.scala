package totoro.ocelot.brain.event

case class TextBufferSetBackgroundColorEvent(color: Int, isFromPalette: Boolean) extends Event
