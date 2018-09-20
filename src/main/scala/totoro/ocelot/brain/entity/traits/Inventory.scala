package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.Ocelot
import totoro.ocelot.brain.entity.{Entity, EntityFactory}
import totoro.ocelot.brain.nbt.ExtendedNBT._
import totoro.ocelot.brain.nbt.{NBT, NBTTagCompound}
import totoro.ocelot.brain.util.Persistable

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Describes an entity with internal container, which can hold other entities.
  * Such as computer case.
  */
trait Inventory extends Persistable {
  /**
    * Returns the collection of entites indide of the container
    */
  val inventory: mutable.ListBuffer[Entity] = ListBuffer.empty

  /**
    * Put entity in the inventory
    */
  def add(entity: Entity): Boolean = {
    if (!inventory.contains(entity)) {
      inventory += entity
      onEntityAdded(entity)
      true
    } else false
  }

  /**
    * Remove an entity from inventory
    */
  def remove(entity: Entity): Boolean = {
    if (inventory.contains(entity)) {
      inventory -= entity
      onEntityRemoved(entity)
      true
    } else false
  }

  def clear(): Boolean = {
    if (inventory.nonEmpty) {
      inventory.clear()
      true
    } else false
  }

  /**
    * Is called any time new entity is added to the inventory
    */
  def onEntityAdded(entity: Entity): Unit = {}

  /**
    * Is called any time new entity is removed from the inventory
    */
  def onEntityRemoved(entity: Entity): Unit = {}

  // ----------------------------------------------------------------------- //

  private final val InventoryTag = "inventory"

  override def load(nbt: NBTTagCompound) {
    nbt.getTagList(InventoryTag, NBT.TAG_COMPOUND).foreach { nbt: NBTTagCompound =>
      EntityFactory.from(nbt) match {
        case Some(entity) =>
          onEntityAdded(entity)
          inventory += entity
        case _ =>
          Ocelot.log.error("Some problems parsing an entity from NBT tag: " + nbt)
      }
    }
  }

  override def save(nbt: NBTTagCompound) {
    nbt.setNewTagList(InventoryTag,
      inventory.map { entity =>
        val nbt = new NBTTagCompound()
        entity.save(nbt)
        nbt
      }
    )
  }
}
