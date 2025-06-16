package totoro.ocelot.brain.entity.ae2

import totoro.ocelot.brain.Constants
import totoro.ocelot.brain.entity.DatabaseItemStack
import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{ComponentInventory, DeviceInfo, Entity, Environment}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.{Component, Network, Visibility}
import totoro.ocelot.brain.util.ResultWrapper.result
import totoro.ocelot.brain.workspace.Workspace

import scala.collection.mutable

/**
 * ME Interface - интерфейс для взаимодействия с ME сетью
 * Реализует OpenComputers ME Interface API
 * Имеет 9 слотов для конфигурации предметов (как в настоящем AE2)
 */
class MEInterface extends Entity with Environment with DeviceInfo with ComponentInventory {
  override val node: Component = Network.newNode(this, Visibility.Network)
    .withComponent("me_interface", Visibility.Network)
    .create()

  // 9 слотов для конфигурации предметов (как в настоящем AE2)
  final val getSizeInventory: Int = 9
  
  // Конфигурация слотов интерфейса - какие предметы должны быть в каждом слоте
  private val slotConfigurations = mutable.Map[Int, InterfaceSlotConfig]()
  
  // Ссылка на ME сеть (будет устанавливаться при подключении к контроллеру)
  private var meNetwork: Option[MENetwork] = None

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Generic,
    DeviceAttribute.Description -> "ME Network Interface",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "ME Interface",
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo

  // Common Network API (делегируем к ME сети)
  @Callback(doc = """function():table -- Get a list of tables representing the available CPUs in the network.""")
  def getCpus(context: Context, args: Arguments): Array[AnyRef] = {
    meNetwork match {
      case Some(network) => result(network.getCpus())
      case None => result(Array.empty)
    }
  }

  @Callback(doc = """function([filter:table]):table -- Get a list of known item recipes.""")
  def getCraftables(context: Context, args: Arguments): Array[AnyRef] = {
    val filter = if (args.count() > 0) {
      import scala.jdk.CollectionConverters._
      Some(args.checkTable(0).asScala.toMap.asInstanceOf[Map[String, Any]])
    } else None
    meNetwork match {
      case Some(network) => result(network.getCraftables(filter))
      case None => result(Array.empty)
    }
  }

  @Callback(doc = """function([filter:table]):table -- Get a list of the stored items in the network.""")
  def getItemsInNetwork(context: Context, args: Arguments): Array[AnyRef] = {
    val filter = if (args.count() > 0) {
      import scala.jdk.CollectionConverters._
      Some(args.checkTable(0).asScala.toMap.asInstanceOf[Map[String, Any]])
    } else None
    meNetwork match {
      case Some(network) => result(network.getItemsInNetwork(filter))
      case None => result(Array.empty)
    }
  }

  @Callback(doc = """function([filter:table,] [dbAddress:string,] [startSlot:number,] [count:number]): bool -- Store items in the network.""")
  def store(context: Context, args: Arguments): Array[AnyRef] = {
    // TODO: Реализовать сохранение предметов в сеть через интерфейс
    result(false)
  }

  @Callback(doc = """function():table -- Get a list of the stored fluids in the network.""")
  def getFluidsInNetwork(context: Context, args: Arguments): Array[AnyRef] = {
    meNetwork match {
      case Some(network) => result(network.getFluidsInNetwork())
      case None => result(Array.empty)
    }
  }

  @Callback(doc = """function():number -- Get the average power injection into the network.""")
  def getAvgPowerInjection(context: Context, args: Arguments): Array[AnyRef] = {
    result(0.0) // Интерфейс не генерирует энергию
  }

  @Callback(doc = """function():number -- Get the average power usage of the network.""")
  def getAvgPowerUsage(context: Context, args: Arguments): Array[AnyRef] = {
    result(5.0) // Интерфейс потребляет 5 AE/t
  }

  @Callback(doc = """function():number -- Get the idle power usage of the network.""")
  def getIdlePowerUsage(context: Context, args: Arguments): Array[AnyRef] = {
    result(1.0) // Интерфейс в простое потребляет 1 AE/t
  }

  @Callback(doc = """function():number -- Get the maximum stored power in the network.""")
  def getMaxStoredPower(context: Context, args: Arguments): Array[AnyRef] = {
    result(0.0) // Интерфейс не хранит энергию
  }

  @Callback(doc = """function():number -- Get the stored power in the network.""")
  def getStoredPower(context: Context, args: Arguments): Array[AnyRef] = {
    result(0.0) // Интерфейс не хранит энергию
  }

  // ME Interface specific methods
  @Callback(doc = """function([slot:number]):table -- Get the configuration of the interface.""")
  def getInterfaceConfiguration(context: Context, args: Arguments): Array[AnyRef] = {
    val slot = if (args.count() > 0) args.checkInteger(0) else 1
    
    if (slot < 1 || slot > 9) {
      throw new IllegalArgumentException("invalid slot")
    }
    
    slotConfigurations.get(slot) match {
      case Some(config) => result(config.toTable)
      case None => result(Map.empty[String, Any])
    }
  }

  @Callback(doc = """function([slot:number][, database:address, entry:number[, size:number]]):boolean -- Configure the interface.""")
  def setInterfaceConfiguration(context: Context, args: Arguments): Array[AnyRef] = {
    val slot = if (args.count() > 0) args.checkInteger(0) else 1
    
    if (slot < 1 || slot > 9) {
      throw new IllegalArgumentException("invalid slot")
    }
    
    if (args.count() >= 3) {
      val databaseAddress = args.checkString(1)
      val entry = args.checkInteger(2)
      val size = if (args.count() > 3) args.checkInteger(3) else 1
      
      // TODO: Получить предмет из базы данных по адресу и записи
      val config = InterfaceSlotConfig(
        databaseAddress = Some(databaseAddress),
        entry = Some(entry),
        requestedSize = size,
        itemStack = None // TODO: Получить из базы данных
      )
      
      slotConfigurations(slot) = config
      result(true)
    } else {
      // Очистить конфигурацию слота
      slotConfigurations.remove(slot)
      result(true)
    }
  }

  // Методы для взаимодействия с ME сетью
  def insertItemToNetwork(itemStack: DatabaseItemStack, count: Int): Int = {
    meNetwork match {
      case Some(network) =>
        if (network.addItem(itemStack, count)) count else 0
      case None => 0
    }
  }

  def extractItemFromNetwork(itemStack: DatabaseItemStack, count: Int): Int = {
    meNetwork match {
      case Some(network) => network.removeItem(itemStack, count)
      case None => 0
    }
  }

  // Подключение к ME сети
  def connectToMENetwork(network: MENetwork): Unit = {
    meNetwork = Some(network)
  }

  def disconnectFromMENetwork(): Unit = {
    meNetwork = None
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    // TODO: Сохранение конфигурации слотов
  }

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)
    // TODO: Загрузка конфигурации слотов
  }
}

/**
 * Конфигурация слота ME Interface
 */
case class InterfaceSlotConfig(
  databaseAddress: Option[String] = None,
  entry: Option[Int] = None,
  requestedSize: Int = 1,
  itemStack: Option[DatabaseItemStack] = None
) {
  def toTable: Map[String, Any] = {
    val table = mutable.Map[String, Any](
      "size" -> requestedSize
    )
    
    databaseAddress.foreach(addr => table("database") = addr)
    entry.foreach(e => table("entry") = e)
    itemStack.foreach(stack => table.++=(stack.toTable))
    
    table.toMap
  }
}
