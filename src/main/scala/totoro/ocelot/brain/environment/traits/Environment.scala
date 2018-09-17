package totoro.ocelot.brain.environment.traits

import totoro.ocelot.brain.network.{Component, Message, Network, Node}

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
  * - Annotate methods in the environment as [[totoro.ocelot.brain.machine.Callback]]s.
  */
trait Environment {
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
}
