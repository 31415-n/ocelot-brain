package totoro.ocelot.brain.event

case class TextBufferFillEvent(column: Int, row: Int, width: Int, height: Int, value: Char) extends Event
