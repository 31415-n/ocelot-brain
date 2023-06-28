package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.Node
import totoro.ocelot.brain.workspace.Workspace

/**
  * Takes care of properly connecting, updating and disconnecting
  * the components in the inventory
  */
trait ComponentInventory extends Entity with Environment with Inventory {
  private var isLoading = false

  override def initialize(): Unit = {
    super.initialize()
    connectComponents()
  }

  override def update(): Unit = {
    super.update()
    inventory.entities.foreach { _.update() }
  }

  override def dispose(): Unit = {
    super.dispose()
    disconnectComponents()
  }

  override def onEntityAdded(slot: Slot, entity: Entity): Unit = {
    super.onEntityAdded(slot, entity)

    entity match {
      // when loading, we don't want to connect components right away
      case environment: Environment =>
        if (!isLoading)
          connectItemNode(environment.node)
    }
  }

  override def onEntityRemoved(slot: Slot, entity: Entity): Unit = {
    entity match {
      case environment: Environment =>
        if (environment.node != null)
          environment.node.remove()
    }

    super.onEntityRemoved(slot, entity)
  }

  override def onConnect(node: Node): Unit = {
    super.onConnect(node)
    if (node == this.node) {
      connectComponents()
    }
  }

  override def onDisconnect(node: Node): Unit = {
    super.onDisconnect(node)
    if (node == this.node) {
      disconnectComponents()
    }
  }

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    isLoading = true
    super.load(nbt, workspace)
    isLoading = false
  }

  protected def connectComponents(): Unit = {
    inventory.entities.foreach {
      case environment: Environment =>
        connectItemNode(environment.node)
    }
  }

  protected def disconnectComponents(): Unit = {
    inventory.entities.foreach {
      case environment: Environment =>
        if (environment.node != null)
          environment.node.remove()
    }
  }

  protected def connectItemNode(node: Node): Unit = {
    if (this.node != null && node != null)
      this.node.connect(node)
  }
}
