package totoro.ocelot.brain.event

case class TextBufferSetResolutionEvent(address: String, width: Int, height: Int) extends NodeEvent
