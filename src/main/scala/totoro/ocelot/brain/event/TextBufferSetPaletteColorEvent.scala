package totoro.ocelot.brain.event

case class TextBufferSetPaletteColorEvent(address: String, index: Int, color: Int) extends Event
