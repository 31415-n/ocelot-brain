package totoro.ocelot.brain.event

case class TextBufferBitBltEvent(address: String, x: Int, y: Int, width: Int, height: Int, id: Int, fromX: Int, fromY: Int) extends Event
