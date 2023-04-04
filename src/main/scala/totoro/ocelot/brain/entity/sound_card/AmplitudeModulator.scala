package totoro.ocelot.brain.entity.sound_card

import totoro.ocelot.brain.nbt.NBTTagCompound

class AmplitudeModulator(val modulatorIndex: Int) extends Modulator {
  def this(nbt: NBTTagCompound) = this(nbt.getInteger("m"))

  def save(nbt: NBTTagCompound): Unit = {
    nbt.setInteger("m", modulatorIndex)
  }

  override def modulate(process: AudioProcess, channel: AudioChannel, value: Float): Float = {
    val modulator = process.channels(modulatorIndex)
    value * (1 + modulator.generate(process, isModulating = true))
  }
}
