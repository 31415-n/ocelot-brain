package totoro.ocelot.brain.entity.ae2

import totoro.ocelot.brain.Constants
import totoro.ocelot.brain.entity.{DatabaseItemStack}
import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{DeviceInfo, Entity, Environment}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.{Component, Network, Visibility}
import totoro.ocelot.brain.util.ResultWrapper.result
import totoro.ocelot.brain.workspace.Workspace

import scala.collection.mutable

/**
 * ME Import Bus - импортирует предметы в ME сеть
 * Реализует OpenComputers ME Import Bus API
 */
class MEImportBus extends Entity with Environment with DeviceInfo {
  override val node: Component = Network.newNode(this, Visibility.Network)
    .withComponent("me_importbus", Visibility.Network)
    .create()

  // Конфигурация импорта для каждой стороны и слота
  private val importConfigurations = mutable.Map[(Int, Int), ImportConfiguration]()
  
  // Ссылка на ME сеть
  private var meNetwork: Option[MENetwork] = None

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Generic,
    DeviceAttribute.Description -> "ME Import Bus",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "ME Import Bus",
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
    result(0.0)
  }

  @Callback(doc = """function():number -- Get the average power usage of the network.""")
  def getAvgPowerUsage(context: Context, args: Arguments): Array[AnyRef] = {
    result(3.0) // Import Bus потребляет 3 AE/t
  }

  @Callback(doc = """function():number -- Get the idle power usage of the network.""")
  def getIdlePowerUsage(context: Context, args: Arguments): Array[AnyRef] = {
    result(1.0)
  }

  @Callback(doc = """function():number -- Get the maximum stored power in the network.""")
  def getMaxStoredPower(context: Context, args: Arguments): Array[AnyRef] = {
    result(0.0)
  }

  @Callback(doc = """function():number -- Get the stored power in the network.""")
  def getStoredPower(context: Context, args: Arguments): Array[AnyRef] = {
    result(0.0)
  }

  // ME Import Bus specific methods
  @Callback(doc = """function(side:number[, slot:number]):boolean -- Get the configuration of the import bus pointing in the specified direction.""")
  def getImportConfiguration(context: Context, args: Arguments): Array[AnyRef] = {
    val side = args.checkInteger(0)
    val slot = if (args.count() > 1) args.checkInteger(1) else 1
    
    if (side < 0 || side > 5) {
      throw new IllegalArgumentException("invalid side")
    }
    
    importConfigurations.get((side, slot)) match {
      case Some(config) => result(config.toTable)
      case None => result(Map.empty[String, Any])
    }
  }

  @Callback(doc = """function(side:number[, slot:number][, database:address, entry:number]):boolean -- Configure the import bus pointing in the specified direction to import item stacks matching the specified descriptor.""")
  def setImportConfiguration(context: Context, args: Arguments): Array[AnyRef] = {
    val side = args.checkInteger(0)
    val slot = if (args.count() > 1) args.checkInteger(1) else 1
    
    if (side < 0 || side > 5) {
      throw new IllegalArgumentException("invalid side")
    }
    
    if (args.count() >= 4) {
      val databaseAddress = args.checkString(2)
      val entry = args.checkInteger(3)
      
      val config = ImportConfiguration(
        databaseAddress = Some(databaseAddress),
        entry = Some(entry),
        itemStack = None // TODO: Получить из базы данных
      )
      
      importConfigurations((side, slot)) = config
      result(true)
    } else {
      // Очистить конфигурацию
      importConfigurations.remove((side, slot))
      result(true)
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
    // TODO: Сохранение конфигурации импорта
  }

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)
    // TODO: Загрузка конфигурации импорта
  }
}

/**
 * Конфигурация импорта для ME Import Bus
 */
case class ImportConfiguration(
  databaseAddress: Option[String] = None,
  entry: Option[Int] = None,
  itemStack: Option[DatabaseItemStack] = None
) {
  def toTable: Map[String, Any] = {
    val table = mutable.Map[String, Any]()
    
    databaseAddress.foreach(addr => table("database") = addr)
    entry.foreach(e => table("entry") = e)
    itemStack.foreach(stack => table.++=(stack.toTable))
    
    table.toMap
  }
}
