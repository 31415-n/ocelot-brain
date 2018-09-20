package totoro.ocelot.brain.machine

/**
  * Used by the Machine to determine the result of a call to
  * [[Architecture]].runThreaded(boolean).
  *
  * Do not implement this interface, only use the predefined internal classes.
  */
object ExecutionResult {

  /**
    * Indicates the machine may sleep for the specified number of ticks. This
    * is merely considered a suggestion. If signals are in the queue or are
    * pushed to the queue while sleeping, the sleep will be interrupted and
    * [[Architecture]].runThreaded(boolean) will be called so that the next
    * signal is pushed.
    *
    * @param ticks The number of ticks to sleep.
    */
  final class Sleep(val ticks: Int) extends ExecutionResult

  /**
    * Indicates tha the computer should shutdown or reboot.
    *
    * @param reboot Whether to reboot. If false the computer will stop.
    */
  final class Shutdown(val reboot: Boolean) extends ExecutionResult

  /**
    * Indicates that a synchronized call should be performed. The architecture
    * is expected to be in a state that allows the next call to be to
    * [[Architecture]].runSynchronized() instead of
    * [[Architecture]].runThreaded(boolean). This is used to perform calls
    * from the server's main thread, to avoid threading issues when interacting
    * with other objects in the world.
    */
  final class SynchronizedCall extends ExecutionResult

  /**
    * Indicates that an error occurred and the computer should crash.
    *
    * @param message The error message.
    */
  final class Error(val message: String) extends ExecutionResult

}

abstract class ExecutionResult
