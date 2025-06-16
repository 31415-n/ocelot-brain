package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Constants
import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{ComponentInventory, DeviceInfo, Entity, Environment}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.{Component, Network, Visibility}
import totoro.ocelot.brain.util.ResultWrapper.result
import totoro.ocelot.brain.workspace.Workspace

/**
 * Crystal Chest - специальный сундук с расширенными возможностями
 * Используется для обмена предметами между игроками и программами
 */
class CrystalChest extends Entity with Environment with DeviceInfo with ComponentInventory {
  override val node: Component = Network.newNode(this, Visibility.Network)
    .withComponent("crystal_chest", Visibility.Network)
    .create()

  // Инвентарь Crystal Chest (обычно больше обычного сундука)
  final val getSizeInventory: Int = 108 // 12x9 слотов

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Generic,
    DeviceAttribute.Description -> "Crystal Storage Chest",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "Crystal Chest",
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo

  @Callback(doc = """function():number -- Get the size of the chest inventory.""")
  def getInventorySize(context: Context, args: Arguments): Array[AnyRef] = {
    result(getSizeInventory)
  }

  @Callback(doc = """function(slot:number):table -- Get the item stack in the specified slot.""")
  def getStackInSlot(context: Context, args: Arguments): Array[AnyRef] = {
    val slot = args.checkInteger(0)
    if (slot < 1 || slot > getSizeInventory) {
      throw new IllegalArgumentException("invalid slot")
    }

    inventory(slot - 1).get match {
      case Some(stack: DatabaseItemStack) => result(stack.toTable)
      case _ => result(null)
    }
  }

  @Callback(doc = """function():table -- Get all non-empty stacks in the chest.""")
  def getAllStacks(context: Context, args: Arguments): Array[AnyRef] = {
    val stacks = for {
      i <- 0 until getSizeInventory
      slot = inventory(i)
      if slot.get.isDefined
    } yield (i + 1) -> (slot.get.get match {
      case itemStack: DatabaseItemStack => itemStack.toTable
      case _ => Map.empty[String, Any]
    })

    result(stacks.toMap)
  }

  @Callback(doc = """function(slot:number, count:number):number -- Extract items from the specified slot.""")
  def extractItem(context: Context, args: Arguments): Array[AnyRef] = {
    val slot = args.checkInteger(0)
    val count = args.checkInteger(1)

    if (slot < 1 || slot > getSizeInventory) {
      throw new IllegalArgumentException("invalid slot")
    }

    // TODO: Реализовать извлечение предметов
    result(0)
  }

  @Callback(doc = """function(slot:number, item:table, count:number):number -- Insert items into the specified slot.""")
  def insertItem(context: Context, args: Arguments): Array[AnyRef] = {
    val slot = args.checkInteger(0)
    val itemTable = args.checkTable(1)
    val count = args.checkInteger(2)

    if (slot < 1 || slot > getSizeInventory) {
      throw new IllegalArgumentException("invalid slot")
    }

    // TODO: Реализовать вставку предметов
    result(0)
  }

  @Callback(doc = """function(fromSlot:number, toSlot:number, count:number):number -- Move items between slots.""")
  def moveItem(context: Context, args: Arguments): Array[AnyRef] = {
    val fromSlot = args.checkInteger(0)
    val toSlot = args.checkInteger(1)
    val count = args.checkInteger(2)

    if (fromSlot < 1 || fromSlot > getSizeInventory ||
        toSlot < 1 || toSlot > getSizeInventory) {
      throw new IllegalArgumentException("invalid slot")
    }

    // TODO: Реализовать перемещение предметов
    result(0)
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    // TODO: Сохранение инвентаря
  }

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)
    // TODO: Загрузка инвентаря
  }
}
