package totoro.ocelot.brain.entity.sound_card

trait Modulator {
  def modulate(process: AudioProcess, channel: AudioChannel, value: Float): Float
}
