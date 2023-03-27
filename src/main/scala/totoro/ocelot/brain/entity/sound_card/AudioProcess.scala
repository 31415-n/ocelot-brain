package totoro.ocelot.brain.entity.sound_card

import totoro.ocelot.brain.Settings

class AudioProcess {
  var delay: Int = 0
  var error: Float = 0
  val channels: Array[AudioChannel] = Array.ofDim[AudioChannel](Settings.get.soundCardChannelCount).map(_ => new AudioChannel)
}
