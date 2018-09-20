package totoro.ocelot.brain.network

import totoro.ocelot.brain.machine._
import totoro.ocelot.brain.nbt.NBTTagCompound

/**
  * Components are nodes that can be addressed computers via drivers.
  *
  * Components therefore form a sub-network in the overall network, and some
  * special rules apply to them. For one, components specify an additional
  * kind of visibility. Component visibility may have to differ from real
  * network reachability in some cases, such as network cards (which have to
  * be able to communicate across the whole network, but computers should only
  * "see" the cards installed directly in them).
  *
  * Unlike the [[Node]]'s network reachability, this is a dynamic value and
  * can be changed at any time. For example, this is used to hide multi-block
  * screen parts that are not the origin from computers in the network.
  *
  * The method responsible for dispatching network messages from computers also
  * only allows sending messages to components that the computer can see,
  * according to the component's visibility. Therefore nodes won't receive
  * messages from computer's that should not be able to see them.
  */
trait Component extends Node {
  /**
    * The name of the node.
    *
    * This should be the type name of the component represented by the node,
    * since this is what is returned from `component.type`. As such it
    * is to be expected that there be multiple nodes with the same name, but
    * that those nodes all have the same underlying type (i.e. there can be
    * multiple "filesystem" nodes, but they should all behave the same way).
    */
  def name: String

  /**
    * Get the visibility of this component.
    */
  def visibility: Visibility.Value = _visibility

  private lazy val callbacks = Callbacks(host)

  private lazy val hosts = callbacks.map {
    case (method, _) => method -> Some(host)
  }

  private var _visibility = Visibility.None

  /**
    * Set the visibility of this component.
    *
    * Note that this cannot be higher / more visible than the reachability of
    * the node. Trying to set it to a higher value will generate an exception.
    *
    * @throws java.lang.IllegalArgumentException if the specified value is
    *                                            more visible than the node's
    *                                            reachability.
    */
  def setVisibility(value: Visibility.Value): Unit = {
    if (value.id > reachability.id) throw new IllegalArgumentException("Trying to set computer visibility to '" + value + "' on a '" + name +
      "' node with reachability '" + reachability + "'. It will be limited to the node's reachability.")
    if (network != null) _visibility match {
      case Visibility.Neighbors => value match {
        case Visibility.Network => addTo(reachableNodes)
        case Visibility.None => removeFrom(neighbors)
        case _ =>
      }
      case Visibility.Network => value match {
        case Visibility.Neighbors =>
          val neighborSet = neighbors.toSet
          removeFrom(reachableNodes.filterNot(neighborSet.contains))
        case Visibility.None => removeFrom(reachableNodes)
        case _ =>
      }
      case Visibility.None => value match {
        case Visibility.Neighbors => addTo(neighbors)
        case Visibility.Network => addTo(reachableNodes)
        case _ =>
      }
    }
    _visibility = value
  }

  /**
    * Tests whether this component can be seen by the specified node,
    * usually representing a computer in the network.
    *
    * '''Important''': this will always return `true` if the node is
    * not currently in a network.
    *
    * @param other the computer node to check for.
    * @return true if the computer can see this node; false otherwise.
    */
  def canBeSeenFrom(other: Node): Boolean = visibility match {
    case Visibility.None => false
    case Visibility.Network => canBeReachedFrom(other)
    case Visibility.Neighbors => isNeighborOf(other)
  }

  private def addTo(nodes: Iterable[Node]): Unit = nodes.foreach(_.host match {
    case machine: Machine => machine.addComponent(this)
    case _ =>
  })

  private def removeFrom(nodes: Iterable[Node]): Unit = nodes.foreach(_.host match {
    case machine: Machine => machine.removeComponent(this)
    case _ =>
  })

  // ----------------------------------------------------------------------- //

  /**
    * The list of names of methods exposed by this component.
    *
    * This does not return the callback annotations directly, because those
    * may not contain the method's name (as it defaults to the name of the
    * annotated method).
    *
    * The returned collection is read-only.
    */
  def methods: Set[String] = callbacks.keySet

  /**
    * Get the annotation information of a method.
    *
    * This is needed for custom architecture implementations that need to know
    * if a callback is direct or not, for example.
    *
    * @param method the method to the the info for.
    * @return the annotation of the specified method or `null`.
    */
  def annotation(method: String): Callback =
    callbacks.get(method) match {
      case Some(_) => callbacks(method).annotation
      case _ => throw new NoSuchMethodException()
    }

  /**
    * Tries to call a function with the specified name on this component.
    *
    * The name of the method must be one of the names in `methods()`.
    * The returned array may be `null` if there is no return value.
    *
    * @param method    the name of the method to call.
    * @param context   the context from which the method is called, usually the
    *                  instance of the computer running the script that made
    *                  the call.
    * @param arguments the arguments passed to the method.
    * @return the list of results, or `null` if there is no result.
    * @throws NoSuchMethodException if there is no method with that name.
    */
  def invoke(method: String, context: Context, arguments: AnyRef*): Array[AnyRef] = callbacks.get(method) match {
    case Some(callback) => hosts(method) match {
      case Some(environment) => Registry.convert(callback(environment, context, new Arguments(Seq(arguments: _*))))
      case _ => throw new NoSuchMethodException()
    }
    case _ => throw new NoSuchMethodException()
  }

  // ----------------------------------------------------------------------- //

  override def load(nbt: NBTTagCompound) {
    super.load(nbt)
    if (nbt.hasKey(Node.VisibilityTag)) _visibility = Visibility(nbt.getInteger(Node.VisibilityTag))
  }

  override def save(nbt: NBTTagCompound) {
    super.save(nbt)
    nbt.setInteger(Node.VisibilityTag, _visibility.id)
  }

  override def toString: String = super.toString + s"@$name"
}
