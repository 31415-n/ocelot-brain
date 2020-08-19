package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.nbt.persistence.NBTPersistence
import totoro.ocelot.brain.util.Persistable
import totoro.ocelot.brain.workspace.Workspace

/**
  * Represents a single persistable object, which may be created
  * and then placed into the workspace, or into the inventory
  * of some other `Entity`.
  *
  * This may be a card, a computer case, a cable, etc.
  */
trait Entity extends Persistable with LifeCycle {
  protected var customData: Persistable = _

  def setCustomData(p: Persistable): Unit = {
    customData = p
  }

  def getCustomData: Persistable = customData

  private val CustomDataTag = "custom_data"

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    if (customData != null) {
      nbt.setTag(CustomDataTag, NBTPersistence.save(customData))
    }
  }

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)
    if (nbt.hasKey(CustomDataTag)) {
      setCustomData(NBTPersistence.load(nbt.getCompoundTag(CustomDataTag), workspace))
    }
  }
}
