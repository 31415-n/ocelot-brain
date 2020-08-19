package totoro.ocelot.brain.nbt.persistence

import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.util.Persistable
import totoro.ocelot.brain.workspace.Workspace

class PersistableString(var value: String) extends Persistable {
  def this() = this(null)

  private final val Tag = "value"

  override def save(nbt: NBTTagCompound): Unit = {
    if (value != null) nbt.setString(Tag, value)
  }

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    if (nbt.hasKey(Tag)) value = nbt.getString(Tag)
  }
}
