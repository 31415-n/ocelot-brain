package totoro.ocelot.brain.event

import totoro.ocelot.brain.entity.sound_card.Instruction

import java.nio.ByteBuffer

case class SelfDestructingCardBoomEvent(address: String) extends NodeEvent
