package totoro.ocelot.brain.event

import java.nio.ByteBuffer

case class SoundCardAudioEvent(address: String, data: ByteBuffer, cleanData: Array[Float], volume: Float) extends NodeEvent
