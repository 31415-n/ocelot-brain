package totoro.ocelot.brain.entity.traits

trait LifeCycle {
  /**
    * Use this if the entity need some additional work to be set up
    */
  def initialize(): Unit = {}

  /**
    * This is used to decide for which components `update()` should
    * be called each tick.
    *
    * Return false here, if you do not need updates, to improve performance.
    */
  def needUpdate: Boolean = false

  /**
    * Called every tick.
    */
  def update(): Unit = {}

  /**
    * Called to properly destroy the entity and dispose all resources
    */
  def dispose(): Unit = {}
}
