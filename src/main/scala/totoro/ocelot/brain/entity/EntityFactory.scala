package totoro.ocelot.brain.entity

import totoro.ocelot.brain.nbt.NBTTagCompound

import scala.collection.mutable

/**
  * Used to load [[Entity]] objects from NBT tags.
  * The available Entity types need to be registered first.
  */
object EntityFactory {

  private val entities: mutable.HashMap[String, Class[_ <: Entity]] = mutable.HashMap.empty

  /**
    * Register new `Entity` type
    */
  def add(entity: Class[_ <: Entity]): Unit = {
    entities(entity.getSimpleName) = entity
  }

  /**
    * Unserialize an `Entity` instance from NBT tag
    */
  def from(nbt: NBTTagCompound): Option[Entity] =
    if (nbt.hasKey(Entity.TypeTag)) {
      val typeId = nbt.getString(Entity.TypeTag)
      if (entities.contains(typeId)) {
        val entity = entities(typeId).newInstance()
        entity.load(nbt)
        Some(entity)
      } else None
    } else None
}
