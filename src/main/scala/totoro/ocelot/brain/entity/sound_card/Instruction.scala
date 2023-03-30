package totoro.ocelot.brain.entity.sound_card

import totoro.ocelot.brain.entity.sound_card.Instruction._
import totoro.ocelot.brain.nbt.NBTTagCompound

sealed abstract class Instruction {
  def execute(process: AudioProcess): Unit

  def save(nbt: NBTTagCompound): Unit = {
    nbt.setByte("t", this match {
      case _: Delay => 0
      case _: Open => 1
      case _: Close => 2
      case _: SetGenerator => 3
      case _: SetFM => 4
      case _: ResetFM => 5
      case _: SetAM => 6
      case _: ResetAM => 7
      case _: SetEnvelope => 8
      case _: ResetEnvelope => 9
      case _: SetVolume => 10
      case _: SetFrequency => 11
    })
  }
}

object Instruction {
  def load(nbt: NBTTagCompound): Instruction = {
    nbt.getByte("t") match {
      case 0 => new Delay(nbt)
      case 1 => new Open(nbt)
      case 2 => new Close(nbt)
      case 3 => new SetGenerator(nbt)
      case 4 => new SetFM(nbt)
      case 5 => new ResetFM(nbt)
      case 6 => new SetAM(nbt)
      case 7 => new ResetAM(nbt)
      case 8 => new SetEnvelope(nbt)
      case 9 => new ResetEnvelope(nbt)
      case 10 => new SetVolume(nbt)
      case 11 => new SetFrequency(nbt)
    }
  }

  class Delay(delay: Int) extends Instruction {
    def this(nbt: NBTTagCompound) = this(nbt.getInteger("d"))

    override def save(nbt: NBTTagCompound): Unit = {
      super.save(nbt)
      nbt.setInteger("d", delay)
    }

    override def execute(process: AudioProcess): Unit = {
      process.delay = delay
    }
  }

  sealed abstract class ChannelSpecific(channelIndex: Int) extends Instruction {
    override def save(nbt: NBTTagCompound): Unit = {
      super.save(nbt)
      nbt.setInteger("c", channelIndex)
    }

    def execute(process: AudioProcess, channel: AudioChannel): Unit

    override def execute(process: AudioProcess): Unit = {
      execute(process, process.channels(channelIndex))
    }
  }

  class Open(channelIndex: Int) extends ChannelSpecific(channelIndex) {
    def this(nbt: NBTTagCompound) = this(nbt.getInteger("c"))

    override def execute(process: AudioProcess, channel: AudioChannel): Unit = {
      channel.isOpen = true
      for (env <- channel.envelope)
        env.trigger()
    }
  }

  class Close(channelIndex: Int) extends ChannelSpecific(channelIndex) {
    def this(nbt: NBTTagCompound) = this(nbt.getInteger("c"))

    override def execute(process: AudioProcess, channel: AudioChannel): Unit = {
      channel.isOpen = false
    }
  }

  class SetGenerator(channelIndex: Int, generator: SignalGenerator) extends ChannelSpecific(channelIndex) {
    def this(nbt: NBTTagCompound) = this(nbt.getInteger("c"), SignalGenerator.load(nbt.getCompoundTag("g")))

    override def save(nbt: NBTTagCompound): Unit = {
      super.save(nbt)
      val generatorNBT = new NBTTagCompound
      generator.save(generatorNBT)
      nbt.setTag("g", generatorNBT)
    }

    override def execute(process: AudioProcess, channel: AudioChannel): Unit = {
      channel.generator = generator
    }
  }

  class SetFM(channelIndex: Int, fm: FrequencyModulator) extends ChannelSpecific(channelIndex) {
    def this(nbt: NBTTagCompound) = this(nbt.getInteger("c"), new FrequencyModulator(nbt.getCompoundTag("fm")))

    override def save(nbt: NBTTagCompound): Unit = {
      super.save(nbt)
      val fmNBT = new NBTTagCompound
      fm.save(fmNBT)
      nbt.setTag("fm", fmNBT)
    }

    override def execute(process: AudioProcess, channel: AudioChannel): Unit = {
      if (channel.isFmMod || channel.isAmMod)
        return

      // FIXME: this is a Computronics bug; consider if 2 or more channels are modulated by the same carrier
      for (old <- channel.frequencyMod)
        process.channels(old.modulatorIndex).isFmMod = false

      val modulator = process.channels(fm.modulatorIndex)
      modulator.isFmMod = true
      channel.frequencyMod = Some(fm)
    }
  }

  class ResetFM(channelIndex: Int) extends ChannelSpecific(channelIndex) {
    def this(nbt: NBTTagCompound) = this(nbt.getInteger("c"))

    override def execute(process: AudioProcess, channel: AudioChannel): Unit = {
      // same bug
      for (old <- channel.frequencyMod)
        process.channels(old.modulatorIndex).isFmMod = false

      channel.frequencyMod = None
    }
  }

  class SetAM(channelIndex: Int, am: AmplitudeModulator) extends ChannelSpecific(channelIndex) {
    def this(nbt: NBTTagCompound) = this(nbt.getInteger("c"), new AmplitudeModulator(nbt.getCompoundTag("am")))

    override def save(nbt: NBTTagCompound): Unit = {
      super.save(nbt)
      val amNBT = new NBTTagCompound
      am.save(amNBT)
      nbt.setTag("fm", amNBT)
    }

    override def execute(process: AudioProcess, channel: AudioChannel): Unit = {
      if (channel.isFmMod || channel.isAmMod)
        return

      // same bug
      for (old <- channel.amplitudeMod)
        process.channels(old.modulatorIndex).isAmMod = false

      val modulator = process.channels(am.modulatorIndex)
      modulator.isAmMod = true
      channel.amplitudeMod = Some(new AmplitudeModulator(am.modulatorIndex))
    }
  }

  class ResetAM(channelIndex: Int) extends ChannelSpecific(channelIndex) {
    def this(nbt: NBTTagCompound) = this(nbt.getInteger("c"))

    override def execute(process: AudioProcess, channel: AudioChannel): Unit = {
      // same bug
      for (old <- channel.amplitudeMod)
        process.channels(old.modulatorIndex).isAmMod = false

      channel.amplitudeMod = None
    }
  }

  class SetEnvelope(channelIndex: Int, envelope: ADSREnvelope) extends ChannelSpecific(channelIndex) {
    def this(nbt: NBTTagCompound) = this(nbt.getInteger("c"), new ADSREnvelope(nbt.getCompoundTag("e")))

    override def save(nbt: NBTTagCompound): Unit = {
      super.save(nbt)
      val envelopeNBT = new NBTTagCompound
      envelope.save(envelopeNBT)
      nbt.setTag("e", envelopeNBT)
    }

    override def execute(process: AudioProcess, channel: AudioChannel): Unit = {
      for (oldEnvelope <- channel.envelope) {
        envelope.phase = oldEnvelope.phase
        envelope.factor = oldEnvelope.factor
      }
      channel.envelope = Some(envelope)
    }
  }

  class ResetEnvelope(channelIndex: Int) extends ChannelSpecific(channelIndex) {
    def this(nbt: NBTTagCompound) = this(nbt.getInteger("c"))

    override def execute(process: AudioProcess, channel: AudioChannel): Unit = {
      channel.envelope = None
    }
  }

  class SetVolume(channelIndex: Int, volume: Float) extends ChannelSpecific(channelIndex) {
    def this(nbt: NBTTagCompound) = this(nbt.getInteger("c"), nbt.getFloat("v"))

    override def save(nbt: NBTTagCompound): Unit = {
      super.save(nbt)
      nbt.setFloat("v", volume)
    }

    override def execute(process: AudioProcess, channel: AudioChannel): Unit = {
      channel.volume = volume.min(1).max(0)
    }
  }

  class SetFrequency(channelIndex: Int, frequency: Float) extends ChannelSpecific(channelIndex) {
    def this(nbt: NBTTagCompound) = this(nbt.getInteger("c"), nbt.getFloat("f"))

    override def save(nbt: NBTTagCompound): Unit = {
      super.save(nbt)
      nbt.setFloat("f", frequency)
    }

    override def execute(process: AudioProcess, channel: AudioChannel): Unit = {
      channel.frequency = frequency
    }
  }
}