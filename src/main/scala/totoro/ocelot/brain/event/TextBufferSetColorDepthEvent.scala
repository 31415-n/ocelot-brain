package totoro.ocelot.brain.event

case class TextBufferSetColorDepthEvent(address: String, depth: Int) extends NodeEvent
