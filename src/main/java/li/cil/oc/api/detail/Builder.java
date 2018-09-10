package li.cil.oc.api.detail;

import li.cil.oc.api.network.Component;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;

/**
 * Used for building {@link Node}s via {@link li.cil.oc.api.Network#newNode}.
 *
 * @param <T> the type of the node created by this builder.
 */
public interface Builder<T extends Node> {
    /**
     * Finalizes the construction of the node.
     * <p/>
     * This performs the actual creation of the node, initializes it to the
     * settings defined by the current builder and returns it.
     *
     * @return the final node.
     */
    T create();

    /**
     * Builder for basic nodes. These nodes merely allow network access and
     * take on no special role.
     */
    interface NodeBuilder extends Builder<Node> {
        /**
         * Makes the node a component.
         * <p/>
         * Nodes that are components can be accessed from computers, methods
         * declared in them marked using the {@link li.cil.oc.api.machine.Callback} annotation can
         * be invoked from computers that can see the component.
         *
         * @param name       the name of the component.
         * @param visibility the visibility of the component.
         * @return a builder for a node that is also a component.
         * @see li.cil.oc.api.network.Component
         */
        ComponentBuilder withComponent(String name, Visibility visibility);

        /**
         * Makes the node a component.
         * <p/>
         * Like {@link #withComponent(String, Visibility)}, but with a default
         * visibility set to the <em>reachability</em> of the node.
         *
         * @param name the name of the component.
         * @return a builder for a node that is also a component.
         * @see li.cil.oc.api.network.Component
         */
        ComponentBuilder withComponent(String name);
    }

    /**
     * Builder for component nodes. These node can be interacted with from
     * computers in the same network, that can <em>see</em> the component.
     */
    interface ComponentBuilder extends Builder<Component> { }
}
