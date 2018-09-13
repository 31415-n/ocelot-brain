package totoro.ocelot.brain.machine

import totoro.ocelot.brain.entity.MachineHost

import scala.collection.mutable

import scala.collection.convert.WrapAsJava._

import java.util

object MachineAPI {
  // Keep registration order, to allow deterministic iteration of the architectures.
  val checked: mutable.LinkedHashSet[Class[_ <: Architecture]] = mutable.LinkedHashSet.empty[Class[_ <: Architecture]]

  /**
    * Register an architecture that can be used to create new machines.
    *
    * Note that although registration is optional, it is strongly recommended
    * to allow `architectures()` to be useful.
    *
    * @param architecture the architecture to register.
    * @throws IllegalArgumentException if the specified architecture is invalid.
    */
  def add(architecture: Class[_ <: Architecture]) {
    if (!checked.contains(architecture)) {
      try
        architecture.getConstructor(classOf[Machine])

      catch {
        case t: Throwable => throw new IllegalArgumentException("Architecture does not have required constructor.", t)
      }
      checked += architecture
    }
  }

  def architectures: util.List[Class[_ <: Architecture]] = checked.toSeq

  def getArchitectureName(architecture: Class[_ <: Architecture]): String =
    architecture.getAnnotation(classOf[Architecture.Name]) match {
      case annotation: Architecture.Name => annotation.value
      case _ => architecture.getSimpleName
    }

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
  private[machine] class Signal(val name: String, val args: Array[AnyRef]) extends machine.Signal {
    def convert() = new Signal(name, Registry.convert(args))
  }

  private val threadPool = ThreadPoolFactory.create("Computer", Settings.get.threads)
}
