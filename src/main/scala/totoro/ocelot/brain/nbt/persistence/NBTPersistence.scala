package totoro.ocelot.brain.nbt.persistence

import totoro.ocelot.brain.entity.traits.Tiered
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.util.Persistable

import scala.collection.mutable

/**
  * This object contains methods to serialize custom Persistable objects
  * to NBT tags and back.
  * Serialized data will look like this:
  * `
  * {
  *   type: xxx.xxx.Class,
  *   data: {
  *     <serialized Persistable object>
  *   },
  *   ...
  * }
  * `
  * The root object may be a freshly created NBT with nothing else,
  * of some existing object - this does not matter as long as there
  * are no field name clashes.
  */

object NBTPersistence {
  val TypeTag = "type"
  val DataTag = "data"

  private val constructors: mutable.HashMap[String, InstanceConstructor] = mutable.HashMap.empty

  def registerConstructor(className: String, constructor: InstanceConstructor): Unit = {
    constructors(className) = constructor
  }

  def unregisterConstructor(className: String): Unit = {
    constructors.remove(className)
  }

  def save(persistable: Persistable, nbt: NBTTagCompound): Unit = {
    nbt.setString(TypeTag, persistable.getClass.getName)
    val data = new NBTTagCompound()
    persistable.save(data)
    nbt.setTag(DataTag, data)
  }

  def save(persistable: Persistable): NBTTagCompound = {
    val nbt = new NBTTagCompound()
    save(persistable, nbt)
    nbt
  }

  def load(nbt: NBTTagCompound): Persistable = {
    val className = nbt.getString(TypeTag)
    val persistable = if (constructors.contains(className)) {
      constructors(className).construct(nbt, className)
    } else {
      val clazz = Class.forName(className)
      val constructor = clazz.getConstructor()
      constructor.newInstance().asInstanceOf[Persistable]
    }
    load(nbt, persistable)
  }

  def load(nbt: NBTTagCompound, persistable: Persistable): Persistable = {
    persistable.load(nbt.getCompoundTag(DataTag))
    persistable
  }

  trait InstanceConstructor {
    def construct(nbt: NBTTagCompound, className: String): Persistable
  }

  class TieredConstructor extends InstanceConstructor {
    override def construct(nbt: NBTTagCompound, className: String): Persistable = {
      val clazz = Class.forName(className)
      val constructor = clazz.getConstructors()(0)
      val tier: Int = nbt.getInteger(Tiered.TierTag)
      constructor.newInstance(tier.asInstanceOf[Object]).asInstanceOf[Persistable]
    }
  }
}
