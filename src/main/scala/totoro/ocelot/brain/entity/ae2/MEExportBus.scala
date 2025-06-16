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
 * ME Export Bus - экспортирует предметы из ME сети
 * Реализует OpenComputers ME Export Bus API
 */
class MEExportBus extends Entity with Environment with DeviceInfo {
  override val node: Component = Network.newNode(this, Visibility.Network)
    .withComponent("me_exportbus", Visibility.Network)
    .create()

  // Конфигурация экспорта для каждой стороны и слота
  private val exportConfigurations = mutable.Map[(Int, Int), ExportConfiguration]()
  
  // Ссылка на ME сеть
  private var meNetwork: Option[MENetwork] = None

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Generic,
    DeviceAttribute.Description -> "ME Export Bus",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "ME Export Bus",
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
    result(3.0) // Export Bus потребляет 3 AE/t
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

  // ME Export Bus specific methods
  @Callback(doc = """function(side:number[, slot:number]):boolean -- Get the configuration of the export bus pointing in the specified direction.""")
  def getExportConfiguration(context: Context, args: Arguments): Array[AnyRef] = {
    val side = args.checkInteger(0)
    val slot = if (args.count() > 1) args.checkInteger(1) else 1
    
    if (side < 0 || side > 5) {
      throw new IllegalArgumentException("invalid side")
    }
    
    exportConfigurations.get((side, slot)) match {
      case Some(config) => result(config.toTable)
      case None => result(Map.empty[String, Any])
    }
  }

  @Callback(doc = """function(side:number[, slot:number][, database:address, entry:number[, size:number]]):boolean -- Configure the export bus pointing in the specified direction to export item stacks matching the specified descriptor.""")
  def setExportConfiguration(context: Context, args: Arguments): Array[AnyRef] = {
    val side = args.checkInteger(0)
    val slot = if (args.count() > 1) args.checkInteger(1) else 1
    
    if (side < 0 || side > 5) {
      throw new IllegalArgumentException("invalid side")
    }
    
    if (args.count() >= 4) {
      val databaseAddress = args.checkString(2)
      val entry = args.checkInteger(3)
      val size = if (args.count() > 4) args.checkInteger(4) else 1
      
      val config = ExportConfiguration(
        databaseAddress = Some(databaseAddress),
        entry = Some(entry),
        requestedSize = size,
        itemStack = None // TODO: Получить из базы данных
      )
      
      exportConfigurations((side, slot)) = config
      result(true)
    } else {
      // Очистить конфигурацию
      exportConfigurations.remove((side, slot))
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
    // TODO: Сохранение конфигурации экспорта
  }

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)
    // TODO: Загрузка конфигурации экспорта
  }
}

/**
 * Конфигурация экспорта для ME Export Bus
 */
case class ExportConfiguration(
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
