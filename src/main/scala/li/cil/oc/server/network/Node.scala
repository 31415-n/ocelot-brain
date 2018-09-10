package li.cil.oc.server.network

import com.google.common.base.Strings
import li.cil.oc.{OpenComputers, api}
import li.cil.oc.api.network.{Environment, Visibility, Node => ImmutableNode}
import net.minecraft.nbt.NBTTagCompound

import scala.collection.convert.WrapAsJava._
import scala.collection.convert.WrapAsScala._

trait Node extends ImmutableNode {
  def host: Environment

  def reachability: Visibility

  final var address: String = _

  final var network: api.network.Network = _

  def canBeReachedFrom(other: ImmutableNode): Boolean = reachability match {
    case Visibility.None => false
    case Visibility.Neighbors => isNeighborOf(other)
    case Visibility.Network => isInSameNetwork(other)
  }

  def isNeighborOf(other: ImmutableNode): Boolean =
    isInSameNetwork(other) && network.neighbors(this).exists(_ == other)

  def reachableNodes: java.lang.Iterable[ImmutableNode] =
    if (network == null) Iterable.empty[ImmutableNode].toSeq
    else network.nodes(this)

  def neighbors: java.lang.Iterable[ImmutableNode] =
    if (network == null) Iterable.empty[ImmutableNode].toSeq
    else network.neighbors(this)

  def connect(node: ImmutableNode): Unit = network.connect(this, node)

  def disconnect(node: ImmutableNode): Unit =
    if (network != null && isInSameNetwork(node)) network.disconnect(this, node)

  def remove(): Unit = if (network != null) network.remove(this)

  private def isInSameNetwork(other: ImmutableNode) = network != null && other != null && network == other.network

  // ----------------------------------------------------------------------- //

  def onConnect(node: ImmutableNode) {
    try {
      host.onConnect(node)
    } catch {
      case e: Throwable => OpenComputers.log.warn(s"A component of type '${host.getClass.getName}' threw an error while being connected to the component network.", e)
    }
  }

  def onDisconnect(node: ImmutableNode) {
    try {
      host.onDisconnect(node)
    } catch {
      case e: Throwable => OpenComputers.log.warn(s"A component of type '${host.getClass.getName}' threw an error while being disconnected from the component network.", e)
    }
  }

  // ----------------------------------------------------------------------- //

  def load(nbt: NBTTagCompound): Unit = {
    if (nbt.hasKey("address")) {
      val newAddress = nbt.getString("address")
      if (!Strings.isNullOrEmpty(newAddress) && newAddress != address) network match {
        case wrapper: Network.Wrapper => wrapper.network.remap(this, newAddress)
        case _ => address = newAddress
      }
    }
  }

  def save(nbt: NBTTagCompound): Unit = {
    if (address != null) {
      nbt.setString("address", address)
    }
  }

  override def toString = s"Node($address, $host)"
}
