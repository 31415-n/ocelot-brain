package totoro.ocelot.brain.machine

import totoro.ocelot.brain.network.Node

/**
  * This is used to provide some context to [[Callback]]s, i.e. the
  * computer from which the callback was called.
  */
trait Context {
  /**
    * The node through which the computer is attached to the component network.
    */
  def node: Node

  /**
    * Tests whether a player is allowed to use the computer.
    *
    * If enabled in the server's configuration, computers can be owned by
    * players. This means that only players that are in a computer's user list
    * may interact with it, i.e. only players in the user list may:
    *
    * - Trigger input via a keyboard.
    * - Change the computer's inventory.
    * - Break the computer block.
    * 
    * There are three exceptions to this rule:
    *
    * - Operators are ''always'' allowed the above actions.
    * - If the user list is ''empty'' then ''all'' players are
    * allowed the above actions.
    * - In single player mode the player is always allowed the above
    * actions.
    *
    * Use this to check whether you should signal something to the computer,
    * for example. Note that for signals triggered via network messages there
    * is a `computer.checked_signal` message, that expects an
    * `User` as the first argument and performs this check
    * before pushing the signal.
    *
    * @param player the name of the player to check for.
    * @return whether the player with the specified name may use the computer.
    */
  def canInteract(player: String): Boolean

  /**
    * Whether the computer is currently in a running state, i.e. it is neither
    * paused, stopping or stopped.
    *
    * The computer thread may or may not be running while the computer is in
    * this state. The computer will accept signals while in this state.
    */
  def isRunning: Boolean

  /**
    * Whether the computer is currently in a paused state.
    *
    * The computer thread is not running while the computer is in this state.
    * The computer will accept signals while in this state.
    */
  def isPaused: Boolean

  /**
    * Starts the computer.
    *
    * The computer will enter a ''starting'' state, in which it will start
    * accepting signals. The computer will start executing in the next server
    * tick.
    *
    * If this is called while the computer is in a paused state it will set the
    * remaining pause time to zero, but it will ''not'' immediately resume
    * the computer. The computer will continue with what it did before it was
    * paused in the next server tick.
    *
    * If this is called while the computer is in a non-paused and non-stopped
    * state it will do nothing and return `false`.
    *
    * @return `true` if the computer switched to a running state.
    */
  def start(): Boolean

  /**
    * Pauses the computer for the specified duration.
    *
    * If this is called from a ''direct'' callback the computer will only
    * pause after the current task has completed, possibly leading to no pause
    * at all. If this is called from a ''non-direct'' callback the
    * computer will be paused for the specified duration before the call
    * returns. Use this to add artificial delays, e.g. for expensive or
    * powerful operations (say, scanning blocks surrounding a computer).
    *
    * '''Important''': if this is called from the ''server thread'' while
    * the executor thread is running this will ''block'' until the
    * computer finishes its current task. The pause will be applied after that.
    * This is usually a bad thing to do, since it may lag the game, but can be
    * handy to synchronize the computer thread to the server thread. For
    * example, this is used when saving screens, which are controlled mostly
    * via direct callbacks.
    * '''However''', if the computer is already in a paused state
    * and the call would not lead to a longer pause this will immediately
    * return `false`, ''without'' blocking.
    *
    * Note that the computer still accepts signals while in paused state, so
    * it is generally better to avoid long pauses, to avoid a signal queue
    * overflow, which would lead to some signals being dropped.
    *
    * Also note that the time left to spend paused is stored in game ticks, so
    * the time resolution is actually quite limited.
    *
    * If this is called while the computer is in a paused, stopping or stopped
    * state this will do nothing and return `false`.
    *
    * @param seconds the number of seconds to pause the computer for.
    * @return `true` if the computer switched to the paused state.
    */
  def pause(seconds: Double): Boolean

  /**
    * Stops the computer.
    *
    * The computer will enter a ''stopping'' state, in which it will not
    * accept new signals. It will be fully stopped in the next server tick. It
    * is not possible to return to a running state from a stopping state. If
    * start is called while in a stopping state the computer will be rebooted.
    *
    * If this is called from a callback, the callback will still finish, but
    * its result will be discarded. If this is called from the server thread
    * while the executor thread is running the computer in the background, it
    * will finish its current work and the computer will be stopped in some
    * future server tick after it has completed.
    *
    * If this is called while the computer is in a stopping or stopped state
    * this will do nothing and return `false`.
    *
    * @return `true` if the computer switched to the stopping state.
    */
  def stop(): Boolean

  /**
    * This method allows dynamic costs for direct calls.
    *
    * It will update the budget for direct calls in the current context, and
    * throw a [[LimitReachedException]] that should ''not'' be caught
    * by the callback function. It will be handled in the calling code and
    * take care of switching states as necessary.
    *
    * Call this from a method with `@Callback(direct = true)` and
    * no `limit` set to use dynamic costs. If a limit is set, it will
    * always be deduced from the budget in addition to this.
    *
    * When called from a non-direct / synchronous callback this does nothing.
    *
    * @param callCost the cost of the direct call being performed.
    */
  def consumeCallBudget(callCost: Double): Unit

  /**
    * Push a signal into the computer.
    *
    * Signals are processed sequentially by the computer, and are queued in a
    * queue with limited length. If the queue is full and the signal could not
    * be pushed this will return `false`.
    *
    * Note that only a limited amount of types is supported for arguments:
    *
    * - `null` and Scala's `Unit` and `None` (all appear
    * as `nil` on the Lua side, for example)
    * - Boolean values.
    * - Numeric types (byte, short, int, long, float, double).
    * - Strings.
    * - Byte arrays (which appear as strings on the Lua side, e.g.).
    * - Maps if and only if both keys and values are strings.
    * - NBTTagCompounds.
    *
    * If an unsupported type is specified the method will enqueue nothing
    * instead, resulting in a `nil` on the Lua side, e.g., and log a
    * warning.
    *
    * @param name the name of the signal to push.
    * @param args additional arguments to pass along with the signal.
    * @return `true` if the signal was queued; `false` otherwise.
    */
  def signal(name: String, args: AnyRef*): Boolean
}
