package totoro.ocelot.brain.entity.traits

/**
 * Use this interface on environments provided by drivers for items that can
 * be installed in a server rack.
 *
 * The provided environment can be used for updating the part in its installed
 * state. The nodes provided by the getters in this interface are used to
 * access nodes provided by the environment (e.g. multiple "interfacing"
 * nodes for a switch), and connect the nodes to the corresponding buses as
 * defined by the rack's configuration.
 *
 * Note: mountables may implement the `ComponentHost` interface and
 * `IInventory`. In this case, if they contain a redstone card and have
 * a state of `State.IsWorking` the rack will visually connect to
 * redstone, for example. Same goes for abstract bus cards, and potentially
 * more things in the future.
 *
 * Furthermore, implementing `Analyzable` will allow specifying more
 * information when the analyzer is used on the mountable while it's in a rack.
 */
trait RackMountable extends Environment with StateAware {
//  /**
//   * Returns some data describing the state of the mountable.
//   * <br>
//   * This is called on the server side to synchronize data to the client after
//   * the rack's {@link li.cil.oc.api.internal.Rack# markChanged ( int )}
//   * method has been called for the slot this mountable is in. It will there
//   * be passed on with the render event to allow state specific rendering of
//   * the mountable in the rack.
//   *
//   * @return the data to synchronize to the clients.
//   */
//  def getData: NBTTagCompound

  /**
   * The number of connectables exposed by the environment.
   *
   * Node that only the first three will ever be used.
   */
  def getConnectableCount: Int

  /**
   * Returns the node at the specified index.
   */
  def getConnectableAt(index: Int): RackBusConnectable
}