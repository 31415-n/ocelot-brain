package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Constants
import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{DeviceInfo, Entity, Environment, Tiered}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.{Component, Network, Visibility}
import totoro.ocelot.brain.util.ResultWrapper.result
import totoro.ocelot.brain.util.Tier
import totoro.ocelot.brain.util.Tier.Tier
import totoro.ocelot.brain.workspace.Workspace

import scala.collection.mutable

/**
 * Database upgrade для хранения и сравнения метаданных предметов
 * Реализует OpenComputers database API
 */
class Database(override val tier: Tier) extends Entity with Environment with DeviceInfo with Tiered {
  override val node: Component = Network.newNode(this, Visibility.Network)
    .withComponent("database", Visibility.Network)
    .create()

  // Хранилище предметов в слотах базы данных
  private val slots = mutable.Map[Int, DatabaseItemStack]()
  private val maxSlots = tier match {
    case Tier.One => 9
    case Tier.Two => 18
    case Tier.Three => 27
  }

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Generic,
    DeviceAttribute.Description -> "Item metadata storage",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> s"Database (${tier.label})",
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo

  @Callback(doc = """function(slot:number):table -- Get the representation of the item stack stored in the specified slot.""")
  def get(context: Context, args: Arguments): Array[AnyRef] = {
    val slot = args.checkInteger(0)
    if (slot < 1 || slot > maxSlots) {
      throw new IllegalArgumentException("invalid slot")
    }
    
    slots.get(slot) match {
      case Some(itemStack) => result(itemStack.toTable)
      case None => result(null)
    }
  }

  @Callback(doc = """function(slot:number):string -- Computes a hash value for the item stack in the specified slot.""")
  def computeHash(context: Context, args: Arguments): Array[AnyRef] = {
    val slot = args.checkInteger(0)
    if (slot < 1 || slot > maxSlots) {
      throw new IllegalArgumentException("invalid slot")
    }
    
    slots.get(slot) match {
      case Some(itemStack) => result(itemStack.computeHash())
      case None => result("")
    }
  }

  @Callback(doc = """function(hash:string):number -- Get the index of an item stack with the specified hash.""")
  def indexOf(context: Context, args: Arguments): Array[AnyRef] = {
    val hash = args.checkString(0)
    
    for ((slot, itemStack) <- slots) {
      if (itemStack.computeHash() == hash) {
        return result(slot)
      }
    }
    
    result(-1)
  }

  @Callback(doc = """function(slot:number):boolean -- Clears the specified slot.""")
  def clear(context: Context, args: Arguments): Array[AnyRef] = {
    val slot = args.checkInteger(0)
    if (slot < 1 || slot > maxSlots) {
      throw new IllegalArgumentException("invalid slot")
    }
    
    val hadItem = slots.contains(slot)
    slots.remove(slot)
    result(hadItem)
  }

  @Callback(doc = """function(fromSlot:number, toSlot:number[, address:string]):boolean -- Copies an entry to another slot.""")
  def copy(context: Context, args: Arguments): Array[AnyRef] = {
    val fromSlot = args.checkInteger(0)
    val toSlot = args.checkInteger(1)
    val targetAddress = if (args.count() > 2) Some(args.checkString(2)) else None
    
    if (fromSlot < 1 || fromSlot > maxSlots || toSlot < 1 || toSlot > maxSlots) {
      throw new IllegalArgumentException("invalid slot")
    }
    
    slots.get(fromSlot) match {
      case Some(itemStack) =>
        targetAddress match {
          case Some(address) =>
            // TODO: Копирование в другую базу данных по адресу
            result(false)
          case None =>
            // Копирование в этой же базе данных
            val hadItem = slots.contains(toSlot)
            slots(toSlot) = itemStack.copy()
            result(hadItem)
        }
      case None => result(false)
    }
  }

  @Callback(doc = """function(address:string):number -- Copies the data stored in this database to another database.""")
  def clone(context: Context, args: Arguments): Array[AnyRef] = {
    val targetAddress = args.checkString(0)
    // TODO: Реализовать клонирование в другую базу данных
    result(0)
  }

  // Внутренние методы для работы с слотами
  def setSlot(slot: Int, itemStack: Option[DatabaseItemStack]): Boolean = {
    if (slot < 1 || slot > maxSlots) return false

    itemStack match {
      case Some(stack) =>
        slots(slot) = stack
        true
      case None =>
        slots.remove(slot).isDefined
    }
  }

  def getSlot(slot: Int): Option[DatabaseItemStack] = {
    if (slot < 1 || slot > maxSlots) None
    else slots.get(slot)
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    // TODO: Сохранение содержимого слотов в NBT
  }

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)
    // TODO: Загрузка содержимого слотов из NBT
  }
}

/**
 * Представление предмета в базе данных
 */
case class DatabaseItemStack(
  name: String,
  label: Option[String] = None,
  damage: Int = 0,
  maxDamage: Int = 0,
  size: Int = 1,
  maxSize: Int = 64,
  hasTag: Boolean = false,
  nbt: Option[Map[String, Any]] = None
) {
  def toTable: Map[String, Any] = {
    val table = mutable.Map[String, Any](
      "name" -> name,
      "damage" -> damage,
      "maxDamage" -> maxDamage,
      "size" -> size,
      "maxSize" -> maxSize,
      "hasTag" -> hasTag
    )

    label.foreach(l => table("label") = l)
    nbt.foreach(n => table("tag") = n)

    table.toMap
  }

  def computeHash(): String = {
    // Простая реализация хеширования на основе основных свойств предмета
    val hashInput = s"$name:$damage:$maxDamage:${hasTag}:${nbt.getOrElse("")}"
    hashInput.hashCode.toString
  }
}
