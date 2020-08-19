package totoro.ocelot.brain.nbt.persistence

import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.util.Persistable
import totoro.ocelot.brain.workspace.Workspace

class PersistableInteger(var value: Int) extends Persistable {
  def this() = this(0)

  private final val Tag = "value"

  override def save(nbt: NBTTagCompound): Unit = {
    nbt.setInteger(Tag, value)
  }

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    if (nbt.hasKey(Tag)) value = nbt.getInteger(Tag)
  }
}
