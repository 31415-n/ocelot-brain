package totoro.ocelot.brain.machine

/**
  * A single signal that was queued on a machine.
  *
  * This interface is not intended to be implemented, it only serves as a return
  * type for [[Machine]].popSignal().
  */
trait Signal {
  /**
    * The name of the signal.
    */
  def name: String

  /**
    * The list of arguments for the signal.
    */
  def args: Array[AnyRef]
}
