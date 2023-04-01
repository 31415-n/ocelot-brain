package totoro.ocelot.brain.event

import totoro.ocelot.brain.entity.sound_card.Instruction

import java.nio.ByteBuffer

case class SoundCardAudioEvent(address: String,
                               data: ByteBuffer,
                               cleanData: Array[Array[Float]],
                               volume: Float,
                               instructions: Array[Instruction]) extends NodeEvent
