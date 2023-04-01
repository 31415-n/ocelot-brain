package totoro.ocelot.brain.entity.sound_card

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.nbt.NBTTagCompound

class AudioChannel {
  var generator: SignalGenerator = SignalGenerator.Square
  var frequency: Float = 0
  var offset: Float = 0
  var volume: Float = 1
  var isOpen: Boolean = false

  var isFmMod: Boolean = false
  var isAmMod: Boolean = false
  var amplitudeMod: Option[AmplitudeModulator] = None
  var frequencyMod: Option[FrequencyModulator] = None
  var envelope: Option[ADSREnvelope] = None

  def generate(process: AudioProcess, isModulating: Boolean = false): Float = {
    if (!isModulating && (isFmMod || isAmMod))
      return 0

    if (!isOpen && (envelope.isEmpty || envelope.get.isClosed))
      return 0

    var value = generator.generate(offset)

    if (frequencyMod.isDefined && !isFmMod && !isAmMod) {
      value = frequencyMod.get.modulate(process, this, value)
    } else {
      offset += frequency / Settings.get.soundCardSampleRate
    }

    if (offset > 1f) {
      offset %= 1f
      generator.nextPeriod()
    }

    if (amplitudeMod.isDefined && !isFmMod && !isAmMod)
      value = amplitudeMod.get.modulate(process, this, value)
    if (envelope.isDefined)
      value = envelope.get.modulate(process, this, value)

    value * volume
  }

  def load(nbt: NBTTagCompound): Unit = {
    generator = SignalGenerator.load(nbt.getCompoundTag("generator"))
    frequency = nbt.getFloat("frequency")
    offset = nbt.getFloat("offset")
    volume = nbt.getFloat("volume")
    isOpen = nbt.getBoolean("isOpen")
    isFmMod = nbt.getBoolean("isFmMod")
    isAmMod = nbt.getBoolean("isAmMod")
    if (nbt.hasKey("am")) amplitudeMod = Some(new AmplitudeModulator(nbt.getCompoundTag("am")))
    if (nbt.hasKey("fm")) frequencyMod = Some(new FrequencyModulator(nbt.getCompoundTag("fm")))
    if (nbt.hasKey("env")) envelope = Some(new ADSREnvelope(nbt.getCompoundTag("env")))
  }

  def save(nbt: NBTTagCompound): Unit = {
    val generatorTag = new NBTTagCompound
    generator.save(generatorTag)
    nbt.setTag("generator", generatorTag)

    nbt.setFloat("frequency", frequency)
    nbt.setFloat("offset", offset)
    nbt.setFloat("volume", volume)
    nbt.setBoolean("isOpen", isOpen)
    nbt.setBoolean("isFmMod", isFmMod)
    nbt.setBoolean("isAmMod", isAmMod)

    for (am <- amplitudeMod) {
      val amNBT = new NBTTagCompound
      am.save(amNBT)
      nbt.setTag("am", amNBT)
    }

    for (fm <- amplitudeMod) {
      val fmNBT = new NBTTagCompound
      fm.save(fmNBT)
      nbt.setTag("fm", fmNBT)
    }

    for (env <- envelope) {
      val envNBT = new NBTTagCompound
      env.save(envNBT)
      nbt.setTag("env", envNBT)
    }
  }
}
