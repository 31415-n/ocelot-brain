package totoro.ocelot.brain.entity.sound_card

import totoro.ocelot.brain.nbt.NBTTagCompound

trait Modulator {
  def modulate(process: AudioProcess, channel: AudioChannel, value: Float): Float
}
