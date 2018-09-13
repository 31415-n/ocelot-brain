package totoro.ocelot.brain.environment

import totoro.ocelot.brain.util.Persistable

/**
  * This kind of environment is managed by either a compatible inventory, such
  * as a computer or floppy drive, or by an adapter block or similar.
  *
  * This means its update and save/load methods will be called by their logical
  * container. This is required for item environments, and for block
  * environments that cannot be directly integrated into a block's tile entity,
  * for example because you have no direct control over the block (e.g. what we
  * do with the command block).
  */
trait ManagedEnvironment extends Environment with Persistable {
  /**
    * This is used to decide whether to put a component in the list of
    * components that need updating, i.e. for which `update()` should
    * be called each tick.
    *
    * Return false here, if you do not need updates, to improve performance.
    */
  def canUpdate: Boolean

  /**
    * This is called by the host of this managed environment once per tick.
    */
  def update(): Unit
}
