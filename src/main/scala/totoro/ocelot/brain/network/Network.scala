package totoro.ocelot.brain.network

import totoro.ocelot.brain.entity.Environment
import totoro.ocelot.brain.entity.traits.WorkspaceAware
import totoro.ocelot.brain.nbt._
import totoro.ocelot.brain.network.Visibility.Visibility
import totoro.ocelot.brain.util.Persistable
import totoro.ocelot.brain.{Ocelot, Settings}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

// Looking at this again after some time, the similarity to const in C++ is somewhat uncanny.
class Network private(private val data: mutable.Map[String, Network.Vertex]) extends WorkspaceAware with Persistable {

  def this() = {
    this(mutable.Map[String, Network.Vertex]())
  }

  def this(node: Node) = {
    this()
    addNew(node)
    node.onConnect(node)
  }

  data.values.foreach(node => {
    node.data.network = this
  })

  // Called by nodes when they want to change address from loading.
  def remap(remappedNode: Node, newAddress: String) {
    data.get(remappedNode.address) match {
      case Some(node) =>
        val neighbors = node.edges.map(_.other(node))
        node.data.remove()
        node.data.address = newAddress
        while (data.contains(node.data.address)) {
          node.data.address = java.util.UUID.randomUUID().toString
        }
        if (neighbors.isEmpty)
          addNew(node.data)
        else
          neighbors.foreach(_.data.connect(node.data))
      case _ => throw new AssertionError("Node believes it belongs to a network it doesn't.")
    }
  }

  // ----------------------------------------------------------------------- //

  def connect(environment: Environment): Boolean = {
    connect(environment.node)
  }

  def connect(node: Node): Boolean = {
    if (!nodes.exists(_ == node)) {
      addNew(node)
      node.onConnect(node)
      true
    } else false
  }

  def connect(envA: Environment, envB: Environment): Boolean = {
    connect(envA.node, envB.node)
  }

  def connect(nodeA: Node, nodeB: Node): Boolean = {
    if (nodeA == null) throw new NullPointerException("nodeA")
    if (nodeB == null) throw new NullPointerException("nodeB")

    if (nodeA == nodeB) throw new IllegalArgumentException(
      "Cannot connect a node to itself.")

    val containsA = contains(nodeA)
    val containsB = contains(nodeB)

    if (!containsA && !containsB) throw new IllegalArgumentException(
      "At least one of the nodes must already be in this network.")

    lazy val oldNodeA = node(nodeA)
    lazy val oldNodeB = node(nodeB)

    if (containsA && containsB) {
      // Both nodes already exist in the network but there is a new connection.
      // This can happen if a new node sequentially connects to multiple nodes
      // in an existing network, e.g. in a setup like so:
      // O O   Where O is an old node, and N is the new Node. It would connect
      // O N   to the node above and left to it (in no particular order).
      if (!oldNodeA.edges.exists(_.isBetween(oldNodeA, oldNodeB))) {
        assert(!oldNodeB.edges.exists(_.isBetween(oldNodeA, oldNodeB)))
        Network.Edge(oldNodeA, oldNodeB)
        if (oldNodeA.data.reachability == Visibility.Neighbors)
          oldNodeB.data.onConnect(oldNodeA.data)
        if (oldNodeB.data.reachability == Visibility.Neighbors)
          oldNodeA.data.onConnect(oldNodeB.data)
        true
      }
      else false // That connection already exists.
    }
    else if (containsA) add(oldNodeA, nodeB)
    else add(oldNodeB, nodeA)
  }

  def disconnect(envA: Environment, envB: Environment): Boolean = {
    disconnect(envA.node, envB.node)
  }

  def disconnect(nodeA: Node, nodeB: Node): Boolean = {
    if (nodeA == nodeB) throw new IllegalArgumentException(
      "Cannot disconnect a node from itself.")

    val containsA = contains(nodeA)
    val containsB = contains(nodeB)

    if (!containsA || !containsB) throw new IllegalArgumentException(
      "Both of the nodes must be in this network.")

    def oldNodeA = node(nodeA)

    def oldNodeB = node(nodeB)

    oldNodeA.edges.find(_.isBetween(oldNodeA, oldNodeB)) match {
      case Some(edge) =>
        handleSplit(edge.remove())
        if (edge.left.data.reachability == Visibility.Neighbors)
          edge.right.data.onDisconnect(edge.left.data)
        if (edge.right.data.reachability == Visibility.Neighbors)
          edge.left.data.onDisconnect(edge.right.data)
        true
      case _ => false // That connection doesn't exists.
    }
  }

  def remove(node: Node): Boolean = {
    data.remove(node.address) match {
      case Some(entry) =>
        node.network = null
        val subGraphs = entry.remove()
        val targets = Iterable(node) ++ (entry.data.reachability match {
          case Visibility.None => Iterable.empty[Node]
          case Visibility.Neighbors => entry.edges.map(_.other(entry).data)
          case Visibility.Network => subGraphs.flatMap(_.values.map(_.data))
        })
        handleSplit(subGraphs)
        targets.foreach(_.onDisconnect(node))
        true
      case _ => false
    }
  }

  // ----------------------------------------------------------------------- //

  def node(address: String): Node = {
    data.get(address) match {
      case Some(node) => node.data
      case _ => null
    }
  }

  def nodes: Iterable[Node] = data.values.map(_.data)

  def reachableNodes(reference: Node): Iterable[Node] = {
    val referenceNeighbors = neighbors(reference).toSet
    nodes.filter(node => node != reference && (node.reachability == Visibility.Network ||
      (node.reachability == Visibility.Neighbors && referenceNeighbors.contains(node))))
  }

  def reachingNodes(reference: Node): Iterable[Node] = {
    if (reference.reachability == Visibility.Network) nodes.filter(node => node != reference)
    else if (reference.reachability == Visibility.Neighbors) {
      val referenceNeighbors = neighbors(reference).toSet
      nodes.filter(node => node != reference && referenceNeighbors.contains(node))
    } else Iterable.empty
  }

  def neighbors(node: Node): Iterable[Node] = {
    data.get(node.address) match {
      case Some(n) if n.data == node => n.edges.map(_.other(n).data)
      case _ => throw new IllegalArgumentException("Node must be in this network.")
    }
  }

  // ----------------------------------------------------------------------- //

  def sendToAddress(source: Node, target: String, name: String, args: Any*): Unit = {
    if (source.network != this)
      throw new IllegalArgumentException("Source node must be in this network.")
    data.get(target) match {
      case Some(node) if node.data.canBeReachedFrom(source) =>
        send(source, Iterable(node.data), name, args: _*)
      case _ =>
    }
  }

  def sendToNeighbors(source: Node, name: String, args: Any*): Unit = {
    if (source.network != this)
      throw new IllegalArgumentException("Source node must be in this network.")
    send(source, neighbors(source).filter(_.reachability != Visibility.None), name, args: _*)
  }

  def sendToReachable(source: Node, name: String, args: Any*): Unit = {
    if (source.network != this)
      throw new IllegalArgumentException("Source node must be in this network.")
    send(source, reachableNodes(source), name, args: _*)
  }

  def sendToVisible(source: Node, name: String, args: Any*): Unit = {
    if (source.network != this)
      throw new IllegalArgumentException("Source node must be in this network.")
    send(source, reachableNodes(source) collect {
      case component: Component if component.canBeSeenFrom(source) => component
    }, name, args: _*)
  }

  // ----------------------------------------------------------------------- //

  private def contains(node: Node) = node.network == this && data.contains(node.address)

  private def node(node: Node) = data(node.address)

  private def addNew(node: Node) = {
    val newNode = new Network.Vertex(node)
    if (node.address == null || data.contains(node.address))
      node.address = java.util.UUID.randomUUID().toString
    data += node.address -> newNode
    node.network = this
    newNode
  }

  private def add(oldNode: Network.Vertex, addedNode: Node): Boolean = {
    // Queue onConnect calls to avoid side effects from callbacks.
    val connects = mutable.Buffer.empty[(Node, Iterable[Node])]
    // Check if the other node is new or if we have to merge networks.
    if (addedNode.network == null) {
      val newNode = addNew(addedNode)
      Network.Edge(oldNode, newNode)
      addedNode.reachability match {
        case Visibility.None =>
          connects += ((addedNode, Iterable(addedNode)))
        case Visibility.Neighbors =>
          connects += ((addedNode, Iterable(addedNode) ++ neighbors(addedNode)))
          reachingNodes(addedNode).foreach(node => connects += ((node, Iterable(addedNode))))
        case Visibility.Network =>
          // Explicitly send to the added node itself first.
          connects += ((addedNode, Iterable(addedNode) ++ nodes.filter(_ != addedNode)))
          reachingNodes(addedNode).foreach(node => connects += ((node, Iterable(addedNode))))
      }
    }
    else {
      val otherNetwork = addedNode.network

      // If the other network contains nodes with addresses used in our local
      // network we'll have to re-assign those... since dynamically handling
      // changes to one's address is not expected of nodes / hosts, we have to
      // remove and reconnect the nodes. This is a pretty shitty solution, and
      // may break things slightly here and there (e.g. if this is the node of
      // a running machine the computer will most likely crash), but it should
      // never happen in normal operation anyway. It *can* happen when NBT
      // editing stuff or using mods to clone blocks (e.g. WorldEdit).
      val duplicates = otherNetwork.data.filter(entry => data.contains(entry._1)).values.toArray
      val otherNetworkAfterReaddress = if (duplicates.isEmpty) {
        otherNetwork
      } else {
        duplicates.foreach(vertex => {
          val node = vertex.data
          val neighbors = vertex.edges.map(_.other(vertex).data).toArray

          var newAddress = ""
          do {
            newAddress = java.util.UUID.randomUUID().toString
          } while (data.contains(newAddress) || otherNetwork.data.contains(newAddress))

          // This may lead to splits, which is the whole reason we have to
          // check the network of the other nodes after the readdressing.
          node.remove()
          node.address = newAddress
          new Network(node)

          if (node.address == newAddress) {
            neighbors.filter(_.network != null).foreach(_.connect(node))
          } else {
            Ocelot.log.error("I can't see this happening any other way than someone directly setting node addresses, " +
              "which they shouldn't. So yeah. Shit'll be borked. Deal with it.")
            node.remove() // well screw you then
          }
        })

        duplicates.head.data.network
      }

      // The address change can theoretically cause the node to be kicked from
      // its old network (via onConnect callbacks), so we make sure it's still
      // in the same network. If it isn't we start over.
      if (addedNode.network != null && addedNode.network == otherNetworkAfterReaddress) {
        if (addedNode.reachability == Visibility.Neighbors)
          connects += ((addedNode, Iterable(oldNode.data)))
        if (oldNode.data.reachability == Visibility.Neighbors)
          connects += ((oldNode.data, Iterable(addedNode)))

        val oldNodes = nodes
        val newNodes = otherNetworkAfterReaddress.nodes
        val oldVisibleNodes = oldNodes.filter(_.reachability == Visibility.Network)
        val newVisibleNodes = newNodes.filter(_.reachability == Visibility.Network)

        newVisibleNodes.foreach(node => connects += ((node, oldNodes)))
        oldVisibleNodes.foreach(node => connects += ((node, newNodes)))

        data ++= otherNetworkAfterReaddress.data
        otherNetworkAfterReaddress.data.values.foreach(node => {
          node.data.network = this
        })
        otherNetworkAfterReaddress.data.clear()

        Network.Edge(oldNode, node(addedNode))
      }
      else add(oldNode, addedNode)
    }

    for ((node, nodes) <- connects) nodes.foreach(_.onConnect(node))

    true
  }

  private def handleSplit(subGraphs: Seq[mutable.Map[String, Network.Vertex]]): Unit =
    if (subGraphs.size > 1) {
      val nodes = subGraphs.map(_.values.map(_.data))
      val visibleNodes = nodes.map(_.filter(_.reachability == Visibility.Network))

      data.clear()
      data ++= subGraphs.head
      subGraphs.tail.foreach(new Network(_))

      for (indexA <- subGraphs.indices) {
        val nodesA = nodes(indexA)
        val visibleNodesA = visibleNodes(indexA)
        for (indexB <- (indexA + 1) until subGraphs.length) {
          val nodesB = nodes(indexB)
          val visibleNodesB = visibleNodes(indexB)
          visibleNodesA.foreach(node => nodesB.foreach(_.onDisconnect(node)))
          visibleNodesB.foreach(node => nodesA.foreach(_.onDisconnect(node)))
        }
      }
    }

  private def send(source: Node, targets: Iterable[Node], name: String, args: Any*) {
    val message = new Message(source, name, Array(args: _*))
    targets.foreach(_.host.onMessage(message))
  }

  // Persistence
  // ----------------------------------------------------------------------- //

  override def save(nbt: NBTTagCompound): Unit = {

  }

  override def load(nbt: NBTTagCompound): Unit = {

  }
}


object Network {
  /**
    * Makes a wireless endpoint join the wireless network defined by the mod.
    *
    * OpenComputers tracks endpoints to which to send wireless packets sent
    * via the `sendWirelessPacket(WirelessEndpoint, double, Packet)`
    * method. The packets will ''only'' be sent to endpoints registered
    * with the network.
    *
    * '''Important''': when your endpoint is removed from the world,
    * ''you must ensure it is also removed from the network''!
    *
    * @param endpoint the endpoint to register with the network.
    */
  def joinWirelessNetwork(endpoint: WirelessEndpoint): Unit = {
    WirelessNetwork.add(endpoint)
  }

  /**
    * Removes a wireless endpoint from the wireless network.
    *
    * This must be called when an endpoint becomes invalid, otherwise it will
    * remain in the network!
    *
    * Calling this for an endpoint that was not added before does nothing.
    *
    * @param endpoint the endpoint to remove from the wireless network.
    */
  def leaveWirelessNetwork(endpoint: WirelessEndpoint): Unit = {
    WirelessNetwork.remove(endpoint)
  }

  /**
    * Sends a packet via the wireless network.
    *
    * This will look for all other registered wireless endpoints in range of
    * the sender and submit the packets to them. Whether another end point is
    * reached depends on the distance and potential obstacles between the
    * sender and the receiver (harder blocks require a stronger signal to be
    * penetrated).
    *
    * @param source   the endpoint that is sending the message.
    * @param strength the signal strength with which to send the packet.
    * @param packet   the packet to send.
    */
  def sendWirelessPacket(source: WirelessEndpoint, strength: Double, packet: Packet): Unit = {
    for (endpoint <- WirelessNetwork.endpoints) {
      endpoint.receivePacket(packet, source)
    }
  }

  /**
    * Factory function for creating new nodes.
    *
    * Use this to create a node for your environment. This
    * will return a builder that can be used to further specialize the node,
    * making it a component node (for callbacks).
    *
    * Example use:
    * {{{
    * class YourThing extends Environment {
    *   private Component node_ = Network.newNode(this, Visibility.Network).
    *       withComponent("your_thing").
    *       create();
    *
    *   public Node node() { return node_; }
    *
    *   // ...
    * }
    * }}}
    *
    * Note that the ''reachability'' specified here is the general
    * availability of the created node to other nodes in the network. Special
    * rules apply to components, which have a ''visibility'' that is used
    * to control how they can be reached from computers. For example, network
    * cards have a ''reachability'' of `Visibility.Network`, to
    * allow them to communicate with each other, but a ''visibility'' of
    * `Visibility.Neighbors` to avoid other computers in the network
    * to see the card (i.e. only the user programs running on the computer the
    * card installed in can see interact with it).
    *
    * @param host         the environment the node is created for.
    * @param reachability the reachability of the node.
    * @return a new node builder.
    */
  def newNode(host: Environment, reachability: Visibility) = new NodeBuilder(host, reachability)

  /**
    * Creates a new network packet as it would be sent or received by a
    * network card.
    *
    * These packets can be forwarded by switches and access points. For wired
    * transmission they must be sent over a node's send method, with the
    * message name being `network.message`.
    *
    * @param source      the address of the sending node.
    * @param destination the address of the destination, or `null`
    *                    for a broadcast.
    * @param port        the port to send the packet to.
    * @param data        the payload of the packet.
    * @return the new packet.
    */
  def newPacket(source: String, destination: String, port: Int, data: Array[AnyRef]): Packet = {
    val packet = new Packet(source, destination, port, data)
    // We do the size check here instead of in the constructor of the packet
    // itself to avoid errors when loading packets.
    if (packet.size > Settings.get.maxNetworkPacketSize) throw new IllegalArgumentException("packet too big (max " + Settings.get.maxNetworkPacketSize + ")")
    packet
  }

  /**
    * Re-creates a network packet from a previously stored state.
    *
    * @param nbt the tag to load the packet from.
    * @return the loaded packet.
    */
  def newPacket(nbt: NBTTagCompound): Packet = {
    val source = nbt.getString("source")
    val destination =
      if (nbt.hasKey("dest")) null
      else nbt.getString("dest")
    val port = nbt.getInteger("port")
    val ttl = nbt.getInteger("ttl")
    val data = (for (i <- 0 until nbt.getInteger("dataLength")) yield {
      if (nbt.hasKey("data" + i)) nbt.getTag("data" + i) match {
        case tag: NBTTagByte => Boolean.box(tag.getByte == 1)
        case tag: NBTTagInt => Int.box(tag.getInt)
        case tag: NBTTagDouble => Double.box(tag.getDouble)
        case tag: NBTTagString => tag.getString: AnyRef
        case tag: NBTTagByteArray => tag.getByteArray
      }
      else null
    }).toArray
    new Packet(source, destination, port, data, ttl)
  }

  class NodeBuilder(val _host: Environment, val _reachability: Visibility) extends Builder.NodeBuilder {

    def withComponent(name: String, visibility: Visibility) =
      new Network.ComponentBuilder(_host, _reachability, name, visibility)

    def withComponent(name: String): Builder.ComponentBuilder = withComponent(name, _reachability)

    def create(): Node = new Node {
      val host: Environment = _host
      val reachability: Visibility = _reachability
    }
  }

  class ComponentBuilder(val _host: Environment, val _reachability: Visibility, val _name: String, val _visibility: Visibility)
    extends Builder.ComponentBuilder {

    def create(): Component = new Component {
      val host: Environment = _host
      val reachability: Visibility = _reachability
      val name: String = _name
      setVisibility(_visibility)
    }
  }

  // ----------------------------------------------------------------------- //

  private class Vertex(val data: Node) {
    val edges: ArrayBuffer[Edge] = ArrayBuffer.empty[Edge]

    def remove(): Seq[mutable.Map[String, Vertex]] = {
      edges.foreach(edge => edge.other(this).edges -= edge)
      searchGraphs(edges.map(_.other(this)))
    }

    override def toString = s"$data [${edges.length}]"
  }

  private case class Edge(left: Vertex, right: Vertex) {
    left.edges += this
    right.edges += this

    def other(side: Vertex): Vertex = if (side == left) right else left

    def isBetween(a: Vertex, b: Vertex): Boolean = (a == left && b == right) || (b == left && a == right)

    def remove(): Seq[mutable.Map[String, Vertex]] = {
      left.edges -= this
      right.edges -= this
      searchGraphs(List(left, right))
    }
  }

  private def searchGraphs(seeds: Seq[Vertex]) = {
    val seen = mutable.Set.empty[Vertex]
    seeds.map(seed => {
      if (seen.contains(seed)) None
      else {
        val addressed = mutable.Map.empty[String, Vertex]
        val queue = mutable.Queue(seed)
        while (queue.nonEmpty) {
          val node = queue.dequeue()
          seen += node
          addressed += node.data.address -> node
          queue ++= node.edges.map(_.other(node)).filter(n => !seen.contains(n) && !queue.contains(n))
        }
        Some(addressed)
      }
    }) filter (_.nonEmpty) map (_.get)
  }
}
