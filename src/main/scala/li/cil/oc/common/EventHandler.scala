package li.cil.oc.common

import li.cil.oc._
import li.cil.oc.api.Network
import li.cil.oc.api.network.Environment
import li.cil.oc.server.component.Keyboard
import li.cil.oc.server.machine.Machine

import scala.collection.convert.WrapAsScala._
import scala.collection.mutable

object EventHandler {
  private var serverTicks = 0L
  private val pendingServerTimed = mutable.PriorityQueue.empty[(Long, () => Unit)](Ordering.by(x => -x._1))

  private val pendingServer = mutable.Buffer.empty[() => Unit]

  private val pendingClient = mutable.Buffer.empty[() => Unit]

  private val keyboards = java.util.Collections.newSetFromMap[Keyboard](new java.util.WeakHashMap[Keyboard, java.lang.Boolean])

  private val machines = mutable.Set.empty[Machine]

  def addKeyboard(keyboard: Keyboard): Unit = keyboards += keyboard

  def scheduleClose(machine: Machine): Unit = machines += machine

  def unscheduleClose(machine: Machine): Unit = machines -= machine

  def scheduleServer(environment: Environment) {
    pendingServer.synchronized {
      pendingServer += (() => Network.joinOrCreateNetwork(environment))
    }
  }

  def scheduleServer(f: () => Unit) {
    pendingServer.synchronized {
      pendingServer += f
    }
  }

  def scheduleServer(f: () => Unit, delay: Int): Unit = {
    pendingServerTimed.synchronized {
      pendingServerTimed += (serverTicks + (delay max 0)) -> f
    }
  }

  def scheduleClient(f: () => Unit) {
    pendingClient.synchronized {
      pendingClient += f
    }
  }

  def onServerTick(): Unit = {
    pendingServer.synchronized {
      val adds = pendingServer.toArray
      pendingServer.clear()
      adds
    } foreach (callback => {
      try callback() catch {
        case t: Throwable => OpenComputers.log.warn("Error in scheduled tick action.", t)
      }
    })

    serverTicks += 1
    while (pendingServerTimed.nonEmpty && pendingServerTimed.head._1 < serverTicks) {
      val (_, callback) = pendingServerTimed.dequeue()
      try callback() catch {
        case t: Throwable => OpenComputers.log.warn("Error in scheduled tick action.", t)
      }
    }

    pendingClient.synchronized {
      val adds = pendingClient.toArray
      pendingClient.clear()
      adds
    } foreach (callback => {
      try callback() catch {
        case t: Throwable => OpenComputers.log.warn("Error in scheduled tick action.", t)
      }
    })

    // Clean up machines *after* a tick, to allow stuff to be saved, first.
    val closed = mutable.ArrayBuffer.empty[Machine]
    machines.foreach(machine => if (machine.tryClose()) {
      closed += machine
      if (machine.host.world == null) {
        if (machine.node != null) machine.node.remove()
      }
    })
    machines --= closed
  }

  def onWorldUnload(): Unit = this.synchronized {
    // TODO: maybe unload here some entities
    // possibly need a way to register them in the first place
  }
}
