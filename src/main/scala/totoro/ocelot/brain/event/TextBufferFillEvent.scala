package totoro.ocelot.brain.event

case class TextBufferFillEvent(address: String, x: Int, y: Int,
                               width: Int, height: Int, codePoint: Int) extends NodeEvent
