package totoro.ocelot.brain.event

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.event.FileSystemActivityType.ActivityType
import totoro.ocelot.brain.network.Node

import scala.collection.immutable.ArraySeq
import scala.collection.mutable

/** The main Ocelot event bus.
  * Dispatches events like computer beeping, modem LED flashing, screen rendering, etc. to subscribed listeners.
  *
  * The event bus is thread-safe (or, at least, that is the intention — if it isn't, it's a bug).
  */
object EventBus {
  // JLS 17.7 guarantees atomicity of writes to fields containing reference types.
  // @volatile ensures that writes are broadcast to other threads when they read the variable.
  @volatile
  private var listeners = ArraySeq.empty[Subscription]

  /** Creates a subscription for an event.
    *
    * The same listener may be registered multiple times.
    * Each registration will create an independent subscription.
    *
    * @return a handle to manage the subscription
    *
    * @note The subscription takes effect on the next event dispatch.
    *       If this method is called while a dispatch is in progress, the listener will not receive an event.
    */
  def subscribe(listener: PartialFunction[Event, Unit]): Subscription = {
    val subscription = Subscription(listener)
    listeners :+= subscription

    subscription
  }

  /** Dispatches an event to listeners subscribed to its runtime class.
    *
    * This method is thread-safe, so it's fine to call it from a non-main thread.
    *
    * This method is also reentrant: you can send an event while handling another event.
    */
  def send(event: Event): Unit = {
    listeners.foreach(_.handleIfDefined(event))
  }

  // Avoid spamming the network with disk activity notices.
  private val fileSystemAccessTimeouts = mutable.WeakHashMap.empty[Node, Long]

  def sendDiskActivity(node: Node, activityType: ActivityType): Unit = {
    val diskActivitySoundDelay = Settings.get.diskActivitySoundDelay

    if (diskActivitySoundDelay >= 0) {
      fileSystemAccessTimeouts.get(node) match {
        case Some(timeout) if timeout > System.currentTimeMillis() => // Cooldown.
        case _ =>
          send(FileSystemActivityEvent(node.address, activityType))
          fileSystemAccessTimeouts.put(node, System.currentTimeMillis() + diskActivitySoundDelay)
      }
    }
  }

  def sendNetworkActivity(node: Node): Unit = {
    send(NetworkActivityEvent(node.address))
  }

  final case class Subscription private[EventBus] (private val listener: PartialFunction[Event, Unit]) {
    private[EventBus] def handleIfDefined(event: Event): Unit = listener.applyOrElse(event, (_: Event) => ())

    /** Cancels the subscription.
      *
      * @note The cancellation takes effect on the next event dispatch.
      *       If this method is called while a dispatch is in progress, the listener may still receive an event.
      */
    def cancel(): Unit = {
      listeners = listeners.filterNot(_ eq this)
    }
  }
}
