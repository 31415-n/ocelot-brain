package totoro.ocelot.brain.event

import totoro.ocelot.brain.event.FileSystemActivityType.ActivityType
import totoro.ocelot.brain.network.Node

import scala.collection.mutable

/**
  * Main Ocelot event bus for a feedback from the components.
  * Computer beeping, modem LED flashing, screen rendering - all of it goes here,
  * and must be listened to.
  */
object EventBus {
  private val listeners = new mutable.ArrayBuffer[PartialFunction[Event, Unit]]
  private val canceledSet = mutable.HashSet.empty[Subscription]
  private var dispatchInProgress: Boolean = false

  /**
    * Creates a subscription for an event.
    *
    * If multiple instances of the same listener are registered, the callback will be invoked multiple times.
    *
    * @return a handle to manage the subscription
    */
  def subscribe(listener: PartialFunction[Event, Unit]): Subscription = {
    listeners += listener
    Subscription(listener)
  }

  /**
    * Dispatches an event to listeners subscribed to its runtime class.
    */
  def send(event: Event): Unit = {
    dispatchInProgress = true
    listeners
      .iterator
      .filter(listener => listener.isDefinedAt(event) && !canceledSet.contains(Subscription(listener)))
      .foreach(listener => listener(event))
    dispatchInProgress = false

    canceledSet.foreach(_.remove())
    canceledSet.clear()
  }

  private val fileSystemAccessTimeouts = mutable.WeakHashMap.empty[Node, Long]

  def sendDiskActivity(node: Node, activityType: ActivityType): Unit = {
    fileSystemAccessTimeouts.get(node) match {
      case Some(timeout) if timeout > System.currentTimeMillis() => // Cooldown.
      case _ =>
        send(FileSystemActivityEvent(node.address, activityType))
        fileSystemAccessTimeouts.put(node, System.currentTimeMillis() + 500)
    }
  }

  final case class Subscription private[EventBus](private val listener: PartialFunction[Event, Unit]) {
    // does not immediately remove the subscription to avoid messing up iterators during dispatch
    def cancel(): Unit =
      if (dispatchInProgress) canceledSet += this
      else remove()

    private[EventBus] def remove(): Unit = {
      listeners -= listener
    }
  }
}