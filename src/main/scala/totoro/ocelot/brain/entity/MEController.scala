package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Constants
import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{DeviceInfo, Entity, Environment}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.{Component, Network, Visibility}
import totoro.ocelot.brain.util.ResultWrapper.result
import totoro.ocelot.brain.workspace.Workspace

import scala.collection.mutable

/**
 * ME Controller - центральный блок ME сети
 * Реализует OpenComputers ME Controller API
 */
class MEController extends Entity with Environment with DeviceInfo {
  override val node: Component = Network.newNode(this, Visibility.Network)
    .withComponent("me_controller", Visibility.Network)
    .create()

  // Имитация ME сети
  private val meNetwork = new MENetwork()
  
  // Энергетические параметры
  private var storedPower: Double = 0.0
  private var maxStoredPower: Double = 8000.0
  private var avgPowerInjection: Double = 0.0
  private var avgPowerUsage: Double = 20.0
  private var idlePowerUsage: Double = 20.0

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Generic,
    DeviceAttribute.Description -> "ME Network Controller",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "ME Controller",
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo

  // Common Network API
  @Callback(doc = """function():table -- Get a list of tables representing the available CPUs in the network.""")
  def getCpus(context: Context, args: Arguments): Array[AnyRef] = {
    result(meNetwork.getCpus())
  }

  @Callback(doc = """function([filter:table]):table -- Get a list of known item recipes.""")
  def getCraftables(context: Context, args: Arguments): Array[AnyRef] = {
    val filter = if (args.count() > 0) {
      import scala.jdk.CollectionConverters._
      Some(args.checkTable(0).asScala.toMap.asInstanceOf[Map[String, Any]])
    } else None
    result(meNetwork.getCraftables(filter))
  }

  @Callback(doc = """function([filter:table]):table -- Get a list of the stored items in the network.""")
  def getItemsInNetwork(context: Context, args: Arguments): Array[AnyRef] = {
    val filter = if (args.count() > 0) {
      import scala.jdk.CollectionConverters._
      Some(args.checkTable(0).asScala.toMap.asInstanceOf[Map[String, Any]])
    } else None
    result(meNetwork.getItemsInNetwork(filter))
  }

  @Callback(doc = """function([filter:table,] [dbAddress:string,] [startSlot:number,] [count:number]): bool -- Store items in the network.""")
  def store(context: Context, args: Arguments): Array[AnyRef] = {
    // TODO: Реализовать сохранение предметов в сеть
    result(false)
  }

  @Callback(doc = """function():table -- Get a list of the stored fluids in the network.""")
  def getFluidsInNetwork(context: Context, args: Arguments): Array[AnyRef] = {
    result(meNetwork.getFluidsInNetwork())
  }

  @Callback(doc = """function():number -- Get the average power injection into the network.""")
  def getAvgPowerInjection(context: Context, args: Arguments): Array[AnyRef] = {
    result(avgPowerInjection)
  }

  @Callback(doc = """function():number -- Get the average power usage of the network.""")
  def getAvgPowerUsage(context: Context, args: Arguments): Array[AnyRef] = {
    result(avgPowerUsage)
  }

  @Callback(doc = """function():number -- Get the idle power usage of the network.""")
  def getIdlePowerUsage(context: Context, args: Arguments): Array[AnyRef] = {
    result(idlePowerUsage)
  }

  @Callback(doc = """function():number -- Get the maximum stored power in the network.""")
  def getMaxStoredPower(context: Context, args: Arguments): Array[AnyRef] = {
    result(maxStoredPower)
  }

  @Callback(doc = """function():number -- Get the stored power in the network.""")
  def getStoredPower(context: Context, args: Arguments): Array[AnyRef] = {
    result(storedPower)
  }

  // ME Controller specific methods
  @Callback(doc = """function():number -- Returns the amount of stored energy on the connected side.""")
  def getEnergyStored(context: Context, args: Arguments): Array[AnyRef] = {
    result(storedPower)
  }

  @Callback(doc = """function():number -- Returns the maximum amount of stored energy on the connected side.""")
  def getMaxEnergyStored(context: Context, args: Arguments): Array[AnyRef] = {
    result(maxStoredPower)
  }

  @Callback(doc = """function():number -- Returns whether this component can have energy extracted from the connected side.""")
  def canExtract(context: Context, args: Arguments): Array[AnyRef] = {
    result(false)
  }

  @Callback(doc = """function():number -- Returns whether this component can receive energy on the connected side.""")
  def canReceive(context: Context, args: Arguments): Array[AnyRef] = {
    result(true)
  }

  // Методы для управления ME сетью
  def getMENetwork: MENetwork = meNetwork

  def addStoredItem(itemStack: DatabaseItemStack, count: Int): Boolean = {
    meNetwork.addItem(itemStack, count)
  }

  def removeStoredItem(itemStack: DatabaseItemStack, count: Int): Int = {
    meNetwork.removeItem(itemStack, count)
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    nbt.setDouble("storedPower", storedPower)
    nbt.setDouble("maxStoredPower", maxStoredPower)
    // TODO: Сохранение состояния ME сети
  }

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)
    storedPower = nbt.getDouble("storedPower")
    maxStoredPower = nbt.getDouble("maxStoredPower")
    // TODO: Загрузка состояния ME сети
  }
}

/**
 * Имитация ME сети для хранения предметов и управления крафтингом
 */
class MENetwork {
  private val storedItems = mutable.Map[String, StoredItem]()
  private val storedFluids = mutable.Map[String, StoredFluid]()
  private val craftingCpus = mutable.ArrayBuffer[CraftingCPU]()

  def getCpus(): Array[Map[String, Any]] = {
    craftingCpus.map(_.toMap).toArray
  }

  def getCraftables(filter: Option[Map[String, Any]]): Array[Map[String, Any]] = {
    // TODO: Реализовать получение доступных рецептов
    Array.empty
  }

  def getItemsInNetwork(filter: Option[Map[String, Any]]): Array[Map[String, Any]] = {
    storedItems.values.map(_.toMap).toArray
  }

  def getFluidsInNetwork(): Array[Map[String, Any]] = {
    storedFluids.values.map(_.toMap).toArray
  }

  def addItem(itemStack: DatabaseItemStack, count: Int): Boolean = {
    val key = itemStack.computeHash()
    storedItems.get(key) match {
      case Some(stored) =>
        storedItems(key) = stored.copy(size = stored.size + count)
      case None =>
        storedItems(key) = StoredItem(itemStack, count)
    }
    true
  }

  def removeItem(itemStack: DatabaseItemStack, count: Int): Int = {
    val key = itemStack.computeHash()
    storedItems.get(key) match {
      case Some(stored) if stored.size >= count =>
        val newSize = stored.size - count
        if (newSize > 0) {
          storedItems(key) = stored.copy(size = newSize)
        } else {
          storedItems.remove(key)
        }
        count
      case Some(stored) =>
        val available = stored.size
        storedItems.remove(key)
        available
      case None => 0
    }
  }
}

case class StoredItem(itemStack: DatabaseItemStack, size: Int) {
  def toMap: Map[String, Any] = {
    itemStack.toTable ++ Map("size" -> size)
  }
}

case class StoredFluid(name: String, amount: Int, label: Option[String] = None) {
  def toMap: Map[String, Any] = {
    val map = mutable.Map[String, Any](
      "name" -> name,
      "amount" -> amount
    )
    label.foreach(l => map("label") = l)
    map.toMap
  }
}

case class CraftingCPU(name: String, busy: Boolean = false, storage: Int = 0) {
  def toMap: Map[String, Any] = Map(
    "name" -> name,
    "busy" -> busy,
    "storage" -> storage
  )
}
