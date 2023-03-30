package totoro.ocelot.brain.entity.sound_card

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.nbt.{NBT, NBTBase, NBTTagCompound}
import totoro.ocelot.brain.util.Persistable
import totoro.ocelot.brain.workspace.Workspace

import scala.jdk.CollectionConverters.SeqHasAsJava

class AudioProcess extends Persistable {
  var delay: Int = 0
  var error: Float = 0
  var channels: Array[AudioChannel] = Array.ofDim[AudioChannel](Settings.get.soundCardChannelCount).map(_ => new AudioChannel)

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    delay = nbt.getInteger("delay")
    error = nbt.getFloat("error")
    val list = nbt.getTagList("channels", NBT.TAG_COMPOUND)
    for (i <- 0 until list.tagCount().min(Settings.get.soundCardChannelCount)) {
      channels(i) = new AudioChannel
      channels(i).load(list.getCompoundTagAt(i), workspace)
    }
  }

  override def save(nbt: NBTTagCompound): Unit = {
    nbt.setInteger("delay", delay)
    nbt.setFloat("error", error)
    nbt.setTagList("channels", channels.map(ch => {
      val nbt = new NBTTagCompound
      ch.save(nbt)
      nbt: NBTBase
    }).toList.asJava)
  }
}