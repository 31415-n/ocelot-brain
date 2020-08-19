package totoro.ocelot.brain.util

import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.workspace.Workspace

/**
  * An object that can be persisted to an NBT tag and restored back from it.
  */
trait Persistable {
  /**
    * Restores a previous state of the object from the specified NBT tag.
    *
    * @param nbt the tag to read the state from.
    */
  def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {}

  /**
    * Saves the current state of the object into the specified NBT tag.
    *
    * This should write the state in such a way that it can be restored when
    * `load` is called with that tag.
    *
    * @param nbt the tag to save the state to.
    */
  def save(nbt: NBTTagCompound): Unit = {}
}
