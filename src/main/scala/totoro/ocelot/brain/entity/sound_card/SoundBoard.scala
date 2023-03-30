package totoro.ocelot.brain.entity.sound_card

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.event.{EventBus, SoundCardAudioEvent}
import totoro.ocelot.brain.nbt.{NBT, NBTBase, NBTTagCompound, NBTTagList}
import totoro.ocelot.brain.util.Persistable
import totoro.ocelot.brain.util.ResultWrapper.result
import totoro.ocelot.brain.workspace.Workspace

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util
import scala.jdk.CollectionConverters.{IteratorHasAsScala, SeqHasAsJava}

class SoundBoard extends Persistable {
  val process = new AudioProcess

  private val buildBuffer: util.ArrayDeque[Instruction] = new util.ArrayDeque[Instruction]
  private val nextBuffer: util.ArrayDeque[Instruction] = new util.ArrayDeque[Instruction]
  private var buildDelay = 0
  private var nextDelay = 0
  private var soundVolume = 1f
  private var timeout = System.currentTimeMillis

  def setTotalVolume(volume: Double): Unit = {
    soundVolume = (volume.min(1).max(0) * 127).toInt.toFloat / 127
  }

  def checkChannel(channel: Int): Int = {
    if (channel < 1 || channel > process.channels.length)
      throw new IllegalArgumentException("invalid channel: " + channel)
    channel - 1
  }

  def tryAdd(inst: Instruction): Array[AnyRef] = {
    buildBuffer.synchronized {
      if (buildBuffer.size >= Settings.get.soundCardQueueSize)
        return result(false, "too many instructions")
      buildBuffer.add(inst)
    }
    result(true)
  }

  def setWave(ch: Int, mode: Int): Array[AnyRef] = {
    val channel = checkChannel(ch)
    SignalGenerator.fromMode(mode) match {
      case Some(v) =>
        tryAdd(new Instruction.SetGenerator(channel, v))
      case None =>
        throw new IllegalArgumentException(f"invalid mode: $mode")
    }
  }

  def delay(duration: Int): Array[AnyRef] = {
    if (duration < 0 || duration > Settings.get.soundCardMaxDelay)
      throw new IllegalArgumentException(s"invalid duration. must be between 0 and ${Settings.get.soundCardMaxDelay}")
    if (buildDelay + duration > Settings.get.soundCardMaxDelay)
      return result(false, "too many delays in queue")
    buildDelay += duration
    tryAdd(new Instruction.Delay(duration))
  }

  def clear(): Unit = {
    buildBuffer.synchronized {
      buildBuffer.clear()
    }
    buildDelay = 0
  }

  def startProcess(): Array[AnyRef] = {
    buildBuffer.synchronized {
      if (nextBuffer.isEmpty) {
        if (buildBuffer.size == 0)
          return result(true)

        nextBuffer.synchronized {
          nextBuffer.addAll(new util.ArrayDeque[Instruction](buildBuffer))
        }

        nextDelay = buildDelay
        buildBuffer.clear()
        buildDelay = 0
        if (System.currentTimeMillis > timeout) {
          timeout = System.currentTimeMillis
        }

        result(true)
      } else {
        result(false, System.currentTimeMillis - timeout)
      }
    }
  }

  def update(address: String): Unit = {
    if (nextBuffer != null && !nextBuffer.isEmpty && System.currentTimeMillis >= timeout - 100) {
      val clone = nextBuffer.synchronized {
        val clone = nextBuffer.clone
        timeout = timeout + nextDelay
        nextBuffer.clear()
        clone
      }

      sendSound(address, clone)
    }
  }

  private def sendSound(address: String, buffer: util.Queue[Instruction]): Unit = {
    val sampleRate = Settings.get.soundCardSampleRate
    val data = new ByteArrayOutputStream

    while (!buffer.isEmpty || process.delay > 0) {
      if (process.delay > 0) {
        val sampleCount = process.delay * sampleRate / 1000
        for (_ <- 0 until sampleCount) {
          var sample = 0f
          for (channel <- process.channels) {
            val x = channel.generate(process)
            sample += x
          }

          val value = sample.min(1).max(-1) * 127 + process.error
          process.error = value - value.floor

          data.write(value.floor.toByte ^ 0x80)
        }
        process.delay = 0
      } else {
        val inst = buffer.poll
        inst.execute(process)
      }
    }

    if (data.size > 0) {
      val buf = ByteBuffer.allocateDirect(data.size())
      buf.put(data.toByteArray)
      buf.flip()
      EventBus.send(SoundCardAudioEvent(address, buf, soundVolume))
    }
  }

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    process.load(nbt.getCompoundTag("process"), workspace)

    buildBuffer.synchronized {
      buildBuffer.clear()
      buildBuffer.addAll(loadInstrBuffer(nbt.getTagList("buildBuffer", NBT.TAG_COMPOUND)))
    }

    nextBuffer.synchronized {
      nextBuffer.clear()
      nextBuffer.addAll(loadInstrBuffer(nbt.getTagList("nextBuffer", NBT.TAG_COMPOUND)))
    }

    buildDelay = nbt.getInteger("buildDelay")
    nextDelay = nbt.getInteger("nextDelay")
    soundVolume = nbt.getFloat("soundVolume")
    timeout = nbt.getLong("timeout")
  }

  override def save(nbt: NBTTagCompound): Unit = {
    val processNBT = new NBTTagCompound
    process.save(processNBT)
    nbt.setTag("process", processNBT)

    nbt.setTagList("buildBuffer", saveInstrBuffer(buildBuffer))
    nbt.setTagList("nextBuffer", saveInstrBuffer(nextBuffer))
    nbt.setInteger("buildDelay", buildDelay)
    nbt.setInteger("nextDelay", nextDelay)
    nbt.setFloat("soundVolume", soundVolume)
    nbt.setLong("timeout", timeout)
  }

  private def loadInstrBuffer(nbt: NBTTagList): util.ArrayDeque[Instruction] = {
    val buffer = new util.ArrayDeque[Instruction](nbt.tagCount())
    for (i <- 0 until nbt.tagCount()) {
      buffer.push(Instruction.load(nbt.getCompoundTagAt(i)))
    }
    buffer
  }

  private def saveInstrBuffer(buffer: util.ArrayDeque[Instruction]): util.List[NBTBase] = {
    buffer.synchronized {
      buffer.iterator().asScala.map(instr => {
        val nbt = new NBTTagCompound
        instr.save(nbt)
        nbt: NBTBase
      }).toList.asJava
    }
  }
}