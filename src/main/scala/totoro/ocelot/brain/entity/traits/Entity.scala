package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.nbt.NBTTagCompound

/**
  * Represents a single persistable object, which may be created
  * and then placed into the workspace, or into the inventory
  * of some other `Entity`.
  *
  * This may be a card, a computer case, a cable, etc.
  */
trait Entity extends Persistable with LifeCycle {
  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    nbt.setString(Entity.TypeTag, this.getClass.getName)
  }
}

object Entity {
  val TypeTag = "type"
}
