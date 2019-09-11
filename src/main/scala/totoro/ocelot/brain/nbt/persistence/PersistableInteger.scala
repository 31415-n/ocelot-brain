package totoro.ocelot.brain.nbt.persistence

import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.util.Persistable

class PersistableInteger(var value: Int) extends Persistable {
  private final val Tag = "value"

  override def save(nbt: NBTTagCompound): Unit = {
    if (value != null) nbt.setInteger(Tag, value)
  }

  override def load(nbt: NBTTagCompound): Unit = {
    if (nbt.hasKey(Tag)) value = nbt.getInteger(Tag)
  }
}
