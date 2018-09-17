package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.Entity
import totoro.ocelot.brain.environment.traits.Environment
import totoro.ocelot.brain.nbt.ExtendedNBT._
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.{Message, Node}
import totoro.ocelot.brain.util.{Persistable, ResultWrapper}

trait EntityEnvironment extends Entity with Environment with WorldAware with Persistable {

  protected def isConnected: Boolean = node != null && node.address != null && node.network != null

  // ----------------------------------------------------------------------- //

  override def dispose() {
    super.dispose()
    Option(node).foreach(_.remove())
  }

  // ----------------------------------------------------------------------- //

  private final val NodeTag = Settings.namespace + "node"

  override def load(nbt: NBTTagCompound): Unit = {
    if (node != null && node.host == this) {
      node.load(nbt.getCompoundTag(NodeTag))
    }
  }

  override def save(nbt: NBTTagCompound): Unit = {
    if (node != null && node.host == this) {
      nbt.setNewCompoundTag(NodeTag, node.save)
    }
  }

  // ----------------------------------------------------------------------- //

  override def onMessage(message: Message) {}

  override def onConnect(node: Node) {}

  override def onDisconnect(node: Node) {}

  // ----------------------------------------------------------------------- //

  protected def result(args: Any*): Array[AnyRef] = ResultWrapper.result(args: _*)
}
