package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Constants
import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{ComponentInventory, DeviceInfo, Entity, Environment}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.{Component, Network, Visibility}
import totoro.ocelot.brain.util.ResultWrapper.result
import totoro.ocelot.brain.workspace.Workspace

import scala.collection.mutable

/**
 * Adapter block для подключения к внешним блокам и компонентам
 * Имеет внутренние слоты для database и других расширений
 */
class Adapter extends Entity with Environment with DeviceInfo with ComponentInventory {
  override val node: Component = Network.newNode(this, Visibility.Network)
    .withComponent("adapter", Visibility.Network)
    .create()

  // Внутренний инвентарь для database и других компонентов
  final val getSizeInventory: Int = 1 // 1 слот для database

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Generic,
    DeviceAttribute.Description -> "External block interface",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "Adapter Block",
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo

  // Подключенные внешние компоненты (имитация)
  private val connectedComponents = mutable.Map[String, ExternalComponent]()

  @Callback(doc = """function():table -- Get list of connected components.""")
  def list(context: Context, args: Arguments): Array[AnyRef] = {
    val components = connectedComponents.map { case (address, component) =>
      address -> component.componentType
    }.toMap
    result(components)
  }

  @Callback(doc = """function(address:string):table -- Get component proxy.""")
  def proxy(context: Context, args: Arguments): Array[AnyRef] = {
    val address = args.checkString(0)
    connectedComponents.get(address) match {
      case Some(component) => result(component.getMethods)
      case None => result(null)
    }
  }

  // Получить database из слота
  def getDatabase: Option[Database] = {
    inventory(0).get match {
      case Some(entity: Database) => Some(entity)
      case _ => None
    }
  }

  // Подключить внешний компонент (для имитации)
  def connectComponent(componentType: String, methods: Map[String, Any]): String = {
    val address = java.util.UUID.randomUUID().toString.take(8)
    connectedComponents(address) = ExternalComponent(componentType, methods)
    
    // Уведомить сеть о новом компоненте
    node.sendToReachable("computer.signal", "component_added", address, componentType)
    
    address
  }

  // Отключить внешний компонент
  def disconnectComponent(address: String): Boolean = {
    connectedComponents.remove(address) match {
      case Some(_) =>
        node.sendToReachable("computer.signal", "component_removed", address)
        true
      case None => false
    }
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    // TODO: Сохранение инвентаря
  }

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)
    // TODO: Загрузка инвентаря
  }

  override def onConnect(node: totoro.ocelot.brain.network.Node): Unit = {
    super.onConnect(node)
    // При подключении к сети, регистрируем все подключенные компоненты
    for ((address, component) <- connectedComponents) {
      node.sendToReachable("computer.signal", "component_added", address, component.componentType)
    }
  }

  override def onDisconnect(node: totoro.ocelot.brain.network.Node): Unit = {
    super.onDisconnect(node)
    // При отключении от сети, уведомляем об удалении компонентов
    for ((address, _) <- connectedComponents) {
      node.sendToReachable("computer.signal", "component_removed", address)
    }
  }
}

/**
 * Представление внешнего компонента, подключенного через адаптер
 */
case class ExternalComponent(
  componentType: String,
  methods: Map[String, Any]
) {
  def getMethods: Map[String, Any] = methods
}
