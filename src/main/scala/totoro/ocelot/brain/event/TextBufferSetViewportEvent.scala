package totoro.ocelot.brain.event

case class TextBufferSetViewportEvent(address: String, width: Int, height: Int) extends NodeEvent
