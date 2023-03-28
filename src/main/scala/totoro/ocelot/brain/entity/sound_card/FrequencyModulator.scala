package totoro.ocelot.brain.entity.sound_card

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.nbt.NBTTagCompound

class FrequencyModulator(val modulatorIndex: Int, val depth: Float) extends Modulator {
  def this(nbt: NBTTagCompound) = this(nbt.getInteger("m"), nbt.getFloat("d"))

  def save(nbt: NBTTagCompound): Unit = {
    nbt.setInteger("m", modulatorIndex)
    nbt.setFloat("d", depth)
  }

  override def modulate(process: AudioProcess, channel: AudioChannel, value: Float): Float = {
    val modulator = process.channels(modulatorIndex)
    val deviation = modulator.generate(process, isModulating = true) * depth * 100
    channel.offset += (channel.frequency + deviation) / Settings.get.soundCardSampleRate
    value
  }
}
