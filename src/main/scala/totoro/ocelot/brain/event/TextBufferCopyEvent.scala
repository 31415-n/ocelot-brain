package totoro.ocelot.brain.event

case class TextBufferCopyEvent(column: Int, row: Int, width: Int, height: Int,
                               horizontalTranslation: Int, verticalTranslation: Int) extends Event
