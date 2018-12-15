package totoro.ocelot.brain.event

case class TextBufferFillEvent(address: String, column: Int, row: Int,
                               width: Int, height: Int, value: Char) extends Event
