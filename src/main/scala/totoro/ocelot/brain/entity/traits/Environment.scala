package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.{Component, Message, Network, Node}
import totoro.ocelot.brain.util.{Direction, Persistable}
import totoro.ocelot.brain.workspace.Workspace

/**
  * The environment of a node.
  *
  * To get some more control over which sides of your entity may connect to a
  * network, see `SidedEnvironment`.
  *
  * When an entity implements this interface a good way of connecting and
  * disconnecting is the following pattern:
  *
  * {{{
  * void update() {
  *   super.update()
  *   if (node != null && node.network == null) {
  *     api.Network.joinOrCreateNetwork(this);
  *   }
  * }
  *
  * void invalidate() {
  *   super.invalidate()
  *   if (node != null) node.remove()
  * }
  * }}}
  *
  * Item environments are always managed, so you will always have to provide a
  * driver for items that should interact with the component network.
  *
  * To interact with environments from user code you will have to do two things:
  *
  * - Make the [[Environment]].node a [[Component]] and ensure
  * its [[Component]].visibility is set to a value where it can
  * be seen by computers in the network.
  * - Annotate methods in the environment as [[totoro.ocelot.brain.entity.machine.Callback]]s.
  */
trait Environment extends Persistable with LifeCycle {
  /**
    * The node this environment wraps.
    *
    * The node is the environments gateway to the component network, and thus
    * its preferred way to interact with other components in the same network.
    *
    * @return the node this environment wraps.
    */
  def node: Node

  /**
    * Connects the node of specified environment to the node of this environment.
    *
    * This is a shortcut for `node.network.connect(this.node, other.node)`.
    *
    * If this environment is not in a network, i.e. `node.network` is `null`,
    * this will throw an exception.
    *
    * @param environment the environment to connect it's node to this environment's node.
    * @throws NullPointerException if `network` is `null`.
    */
  def connect(environment: Environment): Unit = {
    if (node.network == null) new Network(node)
    node.network.connect(this, environment)
  }

  def connect(sidedEnvironment: SidedEnvironment, side: Direction.Value): Unit =
    if (sidedEnvironment.canConnect(side)) {
      if (node.network == null) new Network(node)
      node.network.connect(node, sidedEnvironment.sidedNode(side))
    }

  /**
    * Connects the node of specified environment to the node of this environment.
    *
    * This is a shortcut for `node.network.connect(this.node, node)`.
    *
    * If this environment is not in a network, i.e. `node.network` is `null`,
    * this will throw an exception.
    *
    * @param node the node to connect to this environment's node.
    * @throws NullPointerException if `network` is `null`.
    */
  def connect(node: Node): Unit = {
    if (this.node.network == null) new Network(node)
    this.node.network.connect(this.node, node)
  }

  /**
    * Disconnects the node of specified environment from the node of this environment.
    *
    * This is a shortcut for `node.network.disconnect(this.node, other.node)`.
    *
    * If this environment is not in a network, i.e. `node.network` is `null`,
    * this will throw an exception.
    *
    * @param environment the environment to disconnect it's node from this environment's node.
    * @throws NullPointerException if `network` is `null`.
    */
  def disconnect(environment: Environment): Unit =
    if (node.network != null)
      node.network.disconnect(this, environment)

  def disconnect(sidedEnvironment: SidedEnvironment, side: Direction.Value): Unit =
    if (sidedEnvironment.canConnect(side)) {
      if (node.network != null)
        node.network.disconnect(node, sidedEnvironment.sidedNode(side))
    }

  /**
    * Disconnects the node of specified environment from the node of this environment.
    *
    * This is a shortcut for `node.network.disconnect(this.node, node)`.
    *
    * If this environment is not in a network, i.e. `node.network` is `null`,
    * this will throw an exception.
    *
    * @param node the node to disconnect from this environment's node.
    * @throws NullPointerException if `network` is `null`.
    */
  def disconnect(node: Node): Unit =
    if (node.network != null)
      this.node.network.disconnect(this.node, node)

  /**
    * This is called when a node is added to a network.
    *
    * This is also called for the node itself, if it was added to the network.
    *
    * At this point the node's network is never `null` and you can use
    * it to query it for other nodes. Use this to perform initialization logic,
    * such as building lists of nodes of a certain type in the network.
    *
    * For example, if node A is added to a network with nodes B and C, these
    * calls are made:
    * - A.onConnect(A)
    * - A.onConnect(B)
    * - A.onConnect(C)
    * - B.onConnect(A)
    * - C.onConnect(A)
    */
  def onConnect(node: Node): Unit = {}

  /**
    * This is called when a node is removed from the network.
    *
    * This is also called for the node itself, when it has been removed from
    * its network. Note that this is called on the node that is being removed
    * ''only once'' with the node itself as the parameter.
    *
    * At this point the node's network is no longer available (`null`).
    * Use this to perform clean-up logic such as removing references to the
    * removed node.
    *
    * For example, if node A is removed from a network with nodes A, B and C,
    * these calls are made:
    * - A.onDisconnect(A)
    * - B.onDisconnect(A)
    * - C.onDisconnect(A)
    */
  def onDisconnect(node: Node): Unit = {}

  /**
    * This is the generic message handler.
    *
    * It is called whenever this environments [[Node]] receives a message
    * that was sent via one of the `send` methods in the [[Network]]
    * or the `Node` itself.
    *
    * @param message the message to handle.
    */
  def onMessage(message: Message): Unit = {}

  /**
    * Returns `true` if the current environment is attached to a network
    */
  def isConnected: Boolean = node != null && node.address != null && node.network != null

  // ----------------------------------------------------------------------- //

  override def initialize(): Unit = {
    super.initialize()

    // NOTE: OpenComputers schedules newly created tile entities for joining/creating a new network.
    // This code is subtly different:
    // - our environments (as far as I know) are not limited to what would be tile entities in OC
    // - there's no deferred execution
    // The only place in Ocelot where this method is called is Workspace (whenever an entity is added to it).
    // Since workspaces only hold "tile entities", the first point shouldn't cause any problems.
    // And the other one? No idea.
    Network.joinOrCreateNetwork(this)
  }

  override def dispose(): Unit = {
    if (node != null) node.remove()
    super.dispose()
  }

  // ----------------------------------------------------------------------- //

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)
    if (node != null)
      node.load(nbt.getCompoundTag(Environment.NodeTag))
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    if (node != null) {
      val nodeTag = new NBTTagCompound
      node.save(nodeTag)
      nbt.setTag(Environment.NodeTag, nodeTag)
    }
  }
}

object Environment {
  final val NodeTag = "node"
}
