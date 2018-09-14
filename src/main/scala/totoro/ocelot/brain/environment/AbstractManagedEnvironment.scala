package totoro.ocelot.brain.environment

import totoro.ocelot.brain.environment.traits.ManagedEnvironment
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.{Message, Node}

/**
  * Simple base implementation of the [[ManagedEnvironment]] interface, so
  * unused methods don't clutter the implementing class.
  */
object AbstractManagedEnvironment {
  val NodeTag = "node"
}

abstract class AbstractManagedEnvironment extends ManagedEnvironment {
  // Should be initialized using setNode(api.Network.newNode()). See TileEntityEnvironment.
  private var _node: Node = _

  override def node: Node = _node

  protected def setNode(value: Node): Unit = {
    _node = value
  }

  override def canUpdate = false

  override def update(): Unit = {}

  override def onConnect(node: Node): Unit = {}

  override def onDisconnect(node: Node): Unit = {}

  override def onMessage(message: Message): Unit = {}

  override def load(nbt: NBTTagCompound): Unit = {
    if (node != null) node.load(nbt.getCompoundTag(AbstractManagedEnvironment.NodeTag))
  }

  override def save(nbt: NBTTagCompound): Unit = {
    if (node != null) {
      val nodeTag = new NBTTagCompound
      node.save(nodeTag)
      nbt.setTag(AbstractManagedEnvironment.NodeTag, nodeTag)
    }
  }
}
