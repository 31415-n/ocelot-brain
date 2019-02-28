package totoro.ocelot.brain.entity

import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.util.Persistable

/**
  * Represents a single usable object, which may be created
  * and then placed into the workspace, or into the inventory
  * of some other `Entity`.
  *
  * This may be a card, a computer case, a cable, etc.
  */
trait Entity extends Persistable {
  /**
    * Use this if the entity need some additional work to be set up
    */
  def initialize(): Unit = {}

  /**
    * This is used to decide for which components `update()` should
    * be called each tick.
    *
    * Return false here, if you do not need updates, to improve performance.
    */
  def needUpdate: Boolean = false

  /**
    * Called every tick.
    */
  def update(): Unit = {}

  /**
    * Called to properly destroy the entity and dispose all resources
    */
  def dispose(): Unit = {}

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    nbt.setString(Entity.TypeTag, this.getClass.getName)
  }
}

object Entity {
  val TypeTag = "type"
}
