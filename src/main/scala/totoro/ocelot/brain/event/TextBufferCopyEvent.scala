package totoro.ocelot.brain.event

case class TextBufferCopyEvent(address: String, column: Int, row: Int, width: Int, height: Int,
                               horizontalTranslation: Int, verticalTranslation: Int) extends Event
