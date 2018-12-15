package totoro.ocelot.brain.event

case class TextBufferCopyEvent(address: String, x: Int, y: Int, width: Int, height: Int,
                               horizontalTranslation: Int, verticalTranslation: Int) extends Event
