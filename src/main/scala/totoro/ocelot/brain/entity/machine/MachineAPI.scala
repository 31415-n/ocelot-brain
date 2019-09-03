package totoro.ocelot.brain.entity.machine

import java.util.concurrent.ScheduledExecutorService

import totoro.ocelot.brain.entity.traits.MachineHost
import totoro.ocelot.brain.machine.luaj.LuaJLuaArchitecture
import totoro.ocelot.brain.util.ThreadPoolFactory
import totoro.ocelot.brain.Settings

import scala.collection.mutable

object MachineAPI {
  // Keep registration order, to allow deterministic iteration of the architectures.
  val checked: mutable.HashMap[Class[_ <: Architecture], String] = mutable.HashMap.empty

  /**
    * Register an architecture that can be used to create new machines.
    *
    * Note that although registration is optional, it is strongly recommended
    * to allow `architectures()` to be useful.
    *
    * @param architecture the architecture to register.
    * @throws IllegalArgumentException if the specified architecture is invalid.
    */
  def add(architecture: Class[_ <: Architecture], name: String) {
    if (!checked.contains(architecture)) {
      try
        architecture.getConstructor(classOf[Machine])
      catch {
        case t: Throwable => throw new IllegalArgumentException("Architecture does not have required constructor.", t)
      }
      checked(architecture) = name
    }
  }

  /**
    * A list of all ''registered'' architectures.
    *
    * Note that registration is optional, although automatic when calling
    * `create(MachineHost)` with a not yet
    * registered architecture. What this means is that unless a mod providing
    * a custom architecture also registers it, you may not see it in this list
    * until it also created a new machine using that architecture.
    */
  def architectures: Iterable[Class[_ <: Architecture]] = checked.keys

  def defaultArchitecture: Class[_ <: Architecture] =
    if (Settings.get.forceLuaJ)
      classOf[LuaJLuaArchitecture]
    else architectures.headOption.orNull

  /**
    * Get the name of the specified architecture.
    *
    * @param architecture the architecture to get the name for.
    * @return the name of the specified architecture.
    */
  def getArchitectureName(architecture: Class[_ <: Architecture]): String =
    checked.getOrElse(architecture, architecture.getSimpleName)

  /**
    * Creates a new machine for the specified host.
    *
    * You are responsible for calling update and save / load functions on the
    * machine for it to work correctly.
    *
    * @param host the owner object of the machine, providing context.
    * @return the newly created machine.
    * @throws IllegalArgumentException if the specified architecture is invalid.
    */
  def create(host: MachineHost) = new Machine(host)

  /** Possible states of the computer, and in particular its executor. */
  private[machine] object State extends Enumeration {
    /** The computer is not running right now and there is no Lua state. */
    val Stopped: State.Value = Value("Stopped")

    /** Booting up, doing the first run to initialize the kernel and libs. */
    val Starting: State.Value = Value("Starting")

    /** Computer is currently rebooting. */
    val Restarting: State.Value = Value("Restarting")

    /** The computer is currently shutting down. */
    val Stopping: State.Value = Value("Stopping")

    /** The computer is paused and waiting for the game to resume. */
    val Paused: State.Value = Value("Paused")

    /** The computer executor is waiting for a synchronized call to be made. */
    val SynchronizedCall: State.Value = Value("SynchronizedCall")

    /** The computer should resume with the result of a synchronized call. */
    val SynchronizedReturn: State.Value = Value("SynchronizedReturn")

    /** The computer will resume as soon as possible. */
    val Yielded: State.Value = Value("Yielded")

    /** The computer is yielding for a longer amount of time. */
    val Sleeping: State.Value = Value("Sleeping")

    /** The computer is up and running, executing Lua code. */
    val Running: State.Value = Value("Running")
  }

  /** Signals are messages sent to the Lua state from Java asynchronously. */
  private[machine] class Signal(val name: String, val args: Array[AnyRef]) extends GenericSignal {
    def convert() = new Signal(name, Registry.convert(args))
  }

  val threadPool: ScheduledExecutorService = ThreadPoolFactory.create("Computer", Settings.get.threads)
}
