package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.entity.traits.Entity.{CustomDataTag, EntityIdTag}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.nbt.persistence.NBTPersistence
import totoro.ocelot.brain.util.Persistable
import totoro.ocelot.brain.workspace.Workspace

import java.util.UUID

/**
  * Represents a single persistable object, which may be created
  * and then placed into the workspace, or into the inventory
  * of some other `Entity`.
  *
  * This may be a card, a computer case, a cable, etc.
  */
trait Entity extends Persistable with LifeCycle {
  private var _entityId: UUID = UUID.randomUUID()

  def entityId: UUID = _entityId

  protected var customData: Persistable = _

  def setCustomData(p: Persistable): Unit = {
    customData = p
  }

  def getCustomData: Persistable = customData

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)

    nbt.setString(EntityIdTag, entityId.toString)

    if (customData != null) {
      nbt.setTag(CustomDataTag, NBTPersistence.save(customData))
    }
  }

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)

    _entityId = Option.when(nbt.hasKey(EntityIdTag))(UUID.fromString(nbt.getString(EntityIdTag)))
      .getOrElse(UUID.randomUUID())

    if (nbt.hasKey(CustomDataTag)) {
      setCustomData(NBTPersistence.load(nbt.getCompoundTag(CustomDataTag), workspace))
    }
  }
}

object Entity {
  private val CustomDataTag = "custom_data"
  private val EntityIdTag = "entity_id"
}
