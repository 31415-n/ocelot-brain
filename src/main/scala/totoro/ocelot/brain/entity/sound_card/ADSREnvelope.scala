package totoro.ocelot.brain.entity.sound_card

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.sound_card.ADSREnvelope.Phase
import totoro.ocelot.brain.nbt.NBTTagCompound

class ADSREnvelope(val attack: Int, val decay: Int, val sustain: Float, val release: Int)
  extends Modulator
{
  private var attackSpeed = 0f
  private var decaySpeed = 0f
  private var releaseSpeed = 0f
  private var initialPhase = Phase.Attack
  private var initialFactor = sustain

  var phase: Phase.Value = Phase.Closed
  var factor: Float = 0f
  precompute()

  def this(nbt: NBTTagCompound) = {
    this(nbt.getInteger("a"), nbt.getInteger("d"), nbt.getFloat("s"), nbt.getInteger("r"))
    this.phase = Phase.values.find(_.id == nbt.getByte("p")).get
    this.factor = nbt.getFloat("f")
  }

  def save(nbt: NBTTagCompound): Unit = {
    nbt.setInteger("a", attack)
    nbt.setInteger("d", decay)
    nbt.setFloat("s", sustain)
    nbt.setInteger("r", release)
    nbt.setByte("p", phase.id.toByte)
    nbt.setFloat("f", factor)
  }

  private def precompute(): Unit = {
    val _attack = attack.max(0)
    val _decay = decay.max(0)
    val _sustain = sustain.max(0).min(1)
    val _release = release.max(0)

    val sampleRate = Settings.get.soundCardSampleRate
    attackSpeed = 1000f / (_attack * sampleRate)

    if (_attack == 0f) {
      if (_decay == 0f) {
        initialPhase = Phase.Sustain
        initialFactor = _sustain
      } else {
        initialPhase = Phase.Decay
        initialFactor = 1f
      }
    } else {
      initialPhase = Phase.Attack
      initialFactor = 0f
    }

    decaySpeed = if (_decay == 0f) Float.PositiveInfinity else ((1f - _sustain) * 1000f) / (_decay * sampleRate)
    releaseSpeed = if (_release == 0f) Float.PositiveInfinity else (_sustain * 1000f) / (_release * sampleRate)
  }

  def trigger(): Unit = {
    phase = initialPhase
    factor = initialFactor
  }

  def isClosed: Boolean = phase == Phase.Closed

  override def modulate(process: AudioProcess, channel: AudioChannel, value: Float): Float = {
    if (phase == Phase.Closed)
      return 0f

    if (!channel.isOpen)
      phase = Phase.Release

    phase match {
      case Phase.Attack =>
        factor += attackSpeed
        if (factor >= 1f) {
          factor = 1f
          phase = Phase.Decay
        }
      case Phase.Decay =>
        factor -= decaySpeed
        if (factor <= sustain) {
          factor = sustain
          phase = Phase.Sustain
        }
      case Phase.Release =>
        factor -= releaseSpeed
        if (factor <= 0f) {
          phase = Phase.Closed
          factor = 0f
        }
      case _ =>
      // do nothing
    }

    value * factor
  }
}

object ADSREnvelope {
  object Phase extends Enumeration {
    val Closed, Attack, Decay, Sustain, Release = Value
  }
}