package totoro.ocelot.brain.event

import java.nio.ByteBuffer

case class SoundCardAudioEvent(address: String, data: ByteBuffer, volume: Float) extends NodeEvent
