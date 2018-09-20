package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.entity.{Entity, Environment}
import totoro.ocelot.brain.network.Node

/**
  * Takes care of properly connecting, updating and disconnecting
  * the components in the inventory
  */
trait ComponentInventory extends Inventory with Environment with Entity {
  override def initialize(): Unit = {
    super.initialize()
    connectComponents()
  }

  override def update(): Unit = {
    super.update()
    inventory.foreach { _.update() }
  }

  override def dispose(): Unit = {
    super.dispose()
    disconnectComponents()
  }

  override def onEntityAdded(entity: Entity): Unit = {
    super.onEntityAdded(entity)
    entity match {
      case environment: Environment =>
        node.connect(environment.node)
    }
  }

  override def onEntityRemoved(entity: Entity): Unit = {
    entity match {
      case environment: Environment =>
        environment.node.remove()
    }
    super.onEntityRemoved(entity)
  }

  override def onConnect(node: Node) {
    super.onConnect(node)
    if (node == this.node) {
      connectComponents()
    }
  }

  override def onDisconnect(node: Node) {
    super.onDisconnect(node)
    if (node == this.node) {
      disconnectComponents()
    }
  }

  private def connectComponents(): Unit = {
    inventory.foreach {
      case environment: Environment => node.connect(environment.node)
    }
  }

  private def disconnectComponents(): Unit = {
    inventory.foreach {
      case environment: Environment => environment.node.remove()
    }
  }
}
