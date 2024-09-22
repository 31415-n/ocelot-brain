package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.nbt.NBTTagCompound

/**
 * This interface is implemented by the rack tile entity.
 *
 * It particularly allows [[RackMountable]]s installed in the rack to flag
 * themselves as having changed, so their data gets resent to clients.
 *
 * Server racks <em>do not</em> serve as environment for the computer nodes of
 * servers. That's what the [[Server]]s are for,
 * which are mountables that can be placed in the rack.
 *
 * Another purpose is to allow identifying tile entities as racks via the API,
 * i.e. without having to link against internal classes. This also means that
 * <em>you should not implement this</em>.
 */
trait Rack {
  /**
   * Determine the index of the specified mountable.
   *
   * @param mountable the mountable in this rack to get the index of.
   * @return the index in the rack, or <tt>-1</tt> if it's not in the rack.
   */
  def indexOfMountable(mountable: RackMountable): Int

  /**
   * The mountable in the specified slot.
   * <br>
   * This can be <tt>null</tt>, for example when there is no mountable installed
   * in that slot.
   *
   * @param slot the slot in which to get the mountable.
   * @return the mountable currently hosted in the specified slot.
   */
  def getMountable(slot: Int): RackMountable

  /**
   * Get the last data state provided by the mountable in the specified slot.
   * <br>
   * This is also available on the client. This may be <tt>null</tt>.
   *
   * @param slot the slot of the mountable to get the data for.
   * @return the data of the mountable in that slot, or <tt>null</tt>.
   */
  def getMountableData(slot: Int): NBTTagCompound

  /**
   * Mark the mountable in the specified slot as changed.
   * <br>
   * This will cause the mountable's {@link RackMountable# getData ( )} method
   * to be called in the next tick and the updated data to be sent to the
   * clients, where it can be used for state based rendering of the mountable
   * for example.
   *
   * @param slot the slot of the mountable to queue for updating.
   */
  def markChanged(slot: Int): Unit
}
