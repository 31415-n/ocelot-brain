package totoro.ocelot.brain.entity

import totoro.ocelot.brain.environment.Environment
import totoro.ocelot.brain.machine.Machine
import totoro.ocelot.brain.network.Node
import totoro.ocelot.brain.util.WorldAware

/**
  * This interface has to be implemented by 'hosts' of machine instances.
  *
  * It provides some context for the machine, in particular which world it is
  * running in, to allow querying the time of day, for example.
  */
trait MachineHost extends WorldAware {
  /**
    * The machine currently hosted.
    */
  def machine: Machine

  /**
    * List of all components that are built into this machine directly.
    *
    * This is used to find CPUs, component buses and memory.
    */
  def internalComponents: Iterable[Entity]

  /**
    * Get the slot a component with the specified address is in.
    *
    * This is intended to allow determining the slot of ''item''
    * components sitting in computers. For other components this returns
    * negative values.
    *
    * @param address the address of the component to get the slot for.
    * @return the index of the slot the component is in.
    */
  def componentSlot(address: String): Int

  /**
    * This is called on the owner when the machine's
    * [[Environment]].onConnect(Node)
    * method gets called. This can be useful for reacting to network events
    * when the owner does not have its own node (for example, computer cases
    * expose their machine's node as their own node). This callback allows it
    * to connect its components (graphics cards and the like) when it is
    * connected to a node network (when added to the world, for example).
    *
    * @param node the node that was connected to the network.
    */
  def onMachineConnect(node: Node): Unit

  /**
    * Like `onMachineConnect(Node)`, except that this is called whenever
    * the machine's [[Environment]].onDisconnect(Node)
    * method is called.
    *
    * @param node the node that was disconnected from the network.
    */
  def onMachineDisconnect(node: Node): Unit
}
