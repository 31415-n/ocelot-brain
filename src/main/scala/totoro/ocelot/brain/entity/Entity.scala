package totoro.ocelot.brain.entity

/**
  * Represents a single usable object, which may be created
  * abd then placed into the workspace, or into the inventory
  * of some other `Entity`.
  *
  * This may be a component, a computer case, a cable, etc.
  */
trait Entity {
  def initialize(): Unit = {}
  def update(): Unit = {}
  def dispose(): Unit = {}
}
