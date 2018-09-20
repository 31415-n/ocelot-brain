package totoro.ocelot.brain.entity.traits

object StateAware {

  /**
    * Possible work states.
    */
  object State extends Enumeration {
    type State = Value

    val None,

    /**
      * Indicates that some work can be performed,
      * but that the current state is being idle.
      */
    CanWork,

    /**
      * Indicates that some work is currently being performed.
      */
    IsWorking = Value
  }

}

/**
  * Implemented on machines that have an "working" state.
  */
trait StateAware {
  /**
    * Get the current work state.
    *
    * An empty set indicates that no work can be performed.
    *
    * @return the current state.
    */
  def getCurrentState: Set[StateAware.State.Value]
}
