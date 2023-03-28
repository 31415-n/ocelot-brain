package totoro.ocelot.brain.entity.sound_card

import totoro.ocelot.brain.entity.sound_card.SignalGenerator._
import totoro.ocelot.brain.nbt.NBTTagCompound

import scala.collection.mutable

sealed abstract class SignalGenerator {
  def generate(offset: Float): Float

  def nextPeriod(): Unit = {}

  def save(nbt: NBTTagCompound): Unit = {
    nbt.setByte("t", this match {
      case _: LFSR => 0
      case _: Noise => 1
      case Square => 2
      case Sine => 3
      case Triangle => 4
      case Sawtooth => 5
    })
  }
}

object SignalGenerator {
  val modes: Map[AnyRef, AnyRef] = {
    val map = new mutable.HashMap[AnyRef, AnyRef]

    def put[K, V](k: K, v: V) = map.put(k.asInstanceOf[AnyRef], v.asInstanceOf[AnyRef])

    put("noise", -1)
    put(-1, "noise")
    put("square", 1)
    put(1, "square")
    put("sine", 2)
    put(2, "sine")
    put("triangle", 3)
    put(3, "triangle")
    put("sawtooth", 4)
    put(4, "sawtooth")

    map.toMap
  }

  def fromMode(id: Int): Option[SignalGenerator] = id match {
    case -1 => Some(new Noise)
    case 1 => Some(Square)
    case 2 => Some(Sine)
    case 3 => Some(Triangle)
    case 4 => Some(Sawtooth)
  }

  def load(nbt: NBTTagCompound): SignalGenerator = {
    nbt.getByte("t") match {
      case 0 => new LFSR(nbt)
      case 1 => new Noise(nbt)
      case 2 => Square
      case 3 => Sine
      case 4 => Triangle
      case 5 => Sawtooth
    }
  }

  class LFSR(var value: Int, var mask: Int) extends SignalGenerator {
    private var sample: Int = 1

    def this(nbt: NBTTagCompound) = this(nbt.getInteger("v"), nbt.getInteger("m"))

    override def save(nbt: NBTTagCompound): Unit = {
      super.save(nbt)
      nbt.setInteger("v", value)
      nbt.setInteger("m", mask)
      nbt.setInteger("s", sample)
    }

    override def generate(offset: Float): Float = {
      sample
    }

    override def nextPeriod(): Unit = {
      if ((value & 1) != 0) {
        value = (value >>> 1) ^ mask
        sample = 1
      } else {
        value >>>= 1
        sample = -1
      }
    }
  }

  class Noise extends SignalGenerator {
    def this(nbt: NBTTagCompound) = {
      this()
      value = nbt.getFloat("v")
    }

    override def save(nbt: NBTTagCompound): Unit = {
      super.save(nbt)
      nbt.setFloat("v", value)
    }

    private var value = math.random().toFloat

    override def generate(offset: Float): Float = {
      value
    }

    override def nextPeriod(): Unit = {
      value = math.random().toFloat
    }
  }

  object Square extends SignalGenerator {
    override def generate(offset: Float): Float = math.sin(2 * math.Pi * offset).sign.toFloat
  }

  object Sine extends SignalGenerator {
    override def generate(offset: Float): Float = math.sin(2 * math.Pi * offset).toFloat
  }

  object Triangle extends SignalGenerator {
    override def generate(offset: Float): Float = 1f - 4f * math.abs(offset - 0.5f)
  }

  object Sawtooth extends SignalGenerator {
    override def generate(offset: Float): Float = 2 * offset - 1
  }
}