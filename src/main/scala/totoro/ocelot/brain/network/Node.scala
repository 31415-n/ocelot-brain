package totoro.ocelot.brain.network

import com.google.common.base.Strings
import totoro.ocelot.brain.Ocelot
import totoro.ocelot.brain.environment.traits.Environment
import totoro.ocelot.brain.nbt.NBTTagCompound

/**
  * A single node in a [[Network]].
  *
  * All nodes in a network have a unique address; the network will generate a
  * unique address and assign it to new nodes.
  *
  * '''Important''': you must not create your own implementations of this interface.
  * Use the factory methods in the network API to create new node instances
  * and store them in your environment.
  *
  * @see [[Component]]
  */
trait Node {
  /**
    * The environment hosting this node.
    */
  def host: Environment

  /**
    * The reachability of this node.
    *
    * This is used by the network to control which system messages to deliver
    * to which nodes. This value should not change over the lifetime of a node.
    *
    * It furthermore determines what is returned by the [[Network]]'s
    * `neighbors` and `nodes` functions.
    *
    * Note that this has no effect on the ''real'' reachability of a node;
    * it is only used to filter to which nodes to send connect, disconnect and
    * reconnect messages. If addressed directly, the node will still receive
    * that message even if it comes from a node that should not be able to see
    * it. Therefore nodes should still verify themselves that they want to
    * accept a message from the message's source.
    *
    * A different matter is a [[Component]]'s `visibility`, which is
    * checked before delivering messages a computer tries to send.
    */
  def reachability: Visibility.Value

  /**
    * The address of the node, so that it can be found in the network.
    *
    * This is used by the network manager when a node is added to a network to
    * assign it a unique address, if it doesn't already have one. Nodes must not
    * use custom addresses, only those assigned by the network. The only option
    * they have is to *not* have an address, which can be useful for "dummy"
    * nodes, such as cables. In that case they may ignore the address being set.
    */
  final var address: String = _

  /**
    * The network this node is currently in.
    *
    * Note that valid nodes should never return `None` here. When created a node
    * should immediately be added to a network, after being removed from its
    * network a node should be considered invalid.
    *
    * This will always be set automatically by the network manager. Do not
    * change this value and do not return anything that it wasn't set to.
    */
  final var network: Network = _

  /**
    * Checks whether this node can be reached from the specified node.
    *
    * @param other the node to check for.
    * @return whether this node can be reached from the specified node.
    */
  def canBeReachedFrom(other: Node): Boolean = reachability match {
    case Visibility.None => false
    case Visibility.Neighbors => isNeighborOf(other)
    case Visibility.Network => isInSameNetwork(other)
  }

  /**
    * Checks whether this node is a neighbor of the specified node.
    *
    * @param other the node to check for.
    * @return whether this node is directly connected to the other node.
    */
  def isNeighborOf(other: Node): Boolean =
    isInSameNetwork(other) && network.neighbors(this).exists(_ == other)

  /**
    * Get the list of nodes reachable from this node, based on their
    * `reachability()`.
    *
    * This is a shortcut for `node.network.reachableNodes(node)`.
    *
    * If this node is not in a network, i.e. `network` is `null`,
    * this returns an empty list.
    *
    * @return the list of nodes reachable from this node.
    */
  def reachableNodes: Iterable[Node] =
    if (network == null) Iterable.empty[Node].toSeq
    else network.reachableNodes(this)

  /**
    * Get the list of neighbor nodes, i.e. nodes directly connected to this
    * node.
    *
    * This is a shortcut for `node.network.neighbors(node)`.
    *
    * If this node is not in a network, i.e. `network` is `null`,
    * this returns an empty list.
    *
    * @return the list of nodes directly connected to this node.
    */
  def neighbors: Iterable[Node] =
    if (network == null) Iterable.empty[Node].toSeq
    else network.neighbors(this)

  /**
    * Connects the specified node to this node.
    *
    * This is a shortcut for `node.network.connect(node, other)`.
    *
    * If this node is not in a network, i.e. `network` is `null`,
    * this will throw an exception.
    *
    * @param node the node to connect to this node.
    * @throws NullPointerException if `network` is `null`.
    */
  def connect(node: Node): Unit = network.connect(this, node)

  /**
    * Disconnects the specified node from this node.
    *
    * This is a shortcut for `node.network.disconnect(node, other)`.
    *
    * If this node is not in a network, i.e. `network` is `null`,
    * this will do nothing.
    *
    * @param node the node to connect to this node.
    * @throws NullPointerException if `network` is `null`.
    */
  def disconnect(node: Node): Unit =
    if (network != null && isInSameNetwork(node)) network.disconnect(this, node)

  /**
    * Removes this node from its network.
    *
    * This is a shortcut for `node.network.remove(node)`.
    *
    * If this node is not in a network, i.e. `network` is `null`,
    * this will do nothing.
    */
  def remove(): Unit = if (network != null) network.remove(this)

  private def isInSameNetwork(other: Node) = network != null && other != null && network == other.network

  // ----------------------------------------------------------------------- //

  def onConnect(node: Node) {
    try
      host.onConnect(node)
    catch {
      case e: Throwable => Ocelot.log.warn(s"A component of type '${host.getClass.getName}' threw an error while being connected to the component network.", e)
    }
  }

  def onDisconnect(node: Node) {
    try
      host.onDisconnect(node)
    catch {
      case e: Throwable => Ocelot.log.warn(s"A component of type '${host.getClass.getName}' threw an error while being disconnected from the component network.", e)
    }
  }

  // ----------------------------------------------------------------------- //

  /**
    * Send a message to a node with the specified address.
    *
    * This is a shortcut for `node.network.sendToAddress(node, ...)`.
    *
    * If this node is not in a network, i.e. `network` is `null`,
    * this will do nothing.
    *
    * @param target the address of the node to send the message to.
    * @param name   the name of the message.
    * @param data   the data to pass along with the message.
    */
  def sendToAddress(target: String, name: String, data: Any*): Unit =
    if (network != null) network.sendToAddress(this, target, name, data)

  /**
    * Send a message to all neighbors of this node.
    *
    * This is a shortcut for `node.network.sendToNeighbors(node, ...)`.
    *
    * If this node is not in a network, i.e. `network` is `null`,
    * this will do nothing.
    *
    * @param name the name of the message.
    * @param data the data to pass along with the message.
    */
  def sendToNeighbors(name: String, data: Any*): Unit =
    if (network != null) network.sendToNeighbors(this, name, data)

  /**
    * Send a message to all nodes reachable from this node.
    *
    * This is a shortcut for `node.network.sendToReachable(node, ...)`.
    *
    * If this node is not in a network, i.e. `network` is `null`,
    * this will do nothing.
    *
    * @param name the name of the message.
    * @param data the data to pass along with the message.
    */
  def sendToReachable(name: String, data: Any*): Unit =
    if (network != null) network.sendToReachable(this, name, data)

  /**
    * Send a message to all nodes visible from this node.
    *
    * This is a shortcut for `node.network.sendToVisible(node, ...)`.
    *
    * If this node is not in a network, i.e. `network` is `null`,
    * this will do nothing.
    *
    * @param name the name of the message.
    * @param data the data to pass along with the message.
    */
  def sendToVisible(name: String, data: Any*): Unit =
    if (network != null) network.sendToVisible(this, name, data)

  // ----------------------------------------------------------------------- //

  final val NodeTag = "node"
  final val AddressTag = "address"
  final val BufferTag = "buffer"
  final val VisibilityTag = "visibility"

  def load(nbt: NBTTagCompound): Unit = {
    if (nbt.hasKey(AddressTag)) {
      val newAddress = nbt.getString(AddressTag)
      if (!Strings.isNullOrEmpty(newAddress) && newAddress != address)
        network.remap(this, newAddress)
    }
  }

  def save(nbt: NBTTagCompound): Unit = {
    if (address != null) {
      nbt.setString(AddressTag, address)
    }
  }

  // ----------------------------------------------------------------------- //

  override def toString = s"Node($address, $host)"
}
