package totoro.ocelot.brain.event

import totoro.ocelot.brain.entity.tape.AudioPacketDfpwm

case class TapeDriveAudioEvent(address: String, pkt: AudioPacketDfpwm) extends NodeEvent
