package totoro.ocelot.brain.network

import totoro.ocelot.brain.network.Visibility.Visibility

/**
  * Used for building [[Node]]s via [[Network]].newNode.
  */
object Builder {

  /**
    * Builder for basic nodes. These nodes merely allow network access and
    * take on no special role.
    */
  trait NodeBuilder extends Builder[Node] {
    /**
      * Makes the node a component.
      *
      * Nodes that are components can be accessed from computers, methods
      * declared in them marked using the [[totoro.ocelot.brain.machine.Callback]] annotation can
      * be invoked from computers that can see the component.
      *
      * @param name       the name of the component.
      * @param visibility the visibility of the component.
      * @return a builder for a node that is also a component.
      * @see [[Component]]
      */
    def withComponent(name: String, visibility: Visibility): Builder.ComponentBuilder

    /**
      * Makes the node a component.
      *
      * Like `withComponent(String, Visibility)`, but with a default
      * visibility set to the ''reachability'' of the node.
      *
      * @param name the name of the component.
      * @return a builder for a node that is also a component.
      * @see [[Component]]
      */
    def withComponent(name: String): Builder.ComponentBuilder
  }

  /**
    * Builder for component nodes. These node can be interacted with from
    * computers in the same network, that can ''see'' the component.
    */
  trait ComponentBuilder extends Builder[Component] {}

}

/**
  * @tparam T the type of the node created by this builder.
  */
trait Builder[T <: Node] {
  /**
    * Finalizes the construction of the node.
    * <p/>
    * This performs the actual creation of the node, initializes it to the
    * settings defined by the current builder and returns it.
    *
    * @return the final node.
    */
  def create(): T
}
