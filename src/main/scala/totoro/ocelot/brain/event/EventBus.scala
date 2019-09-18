package totoro.ocelot.brain.event

import totoro.ocelot.brain.network.Node

import scala.collection.mutable

/**
  * Main Ocelot event bus for a feedback from the components.
  * Computer beeping, modem LED flashing, screen rendering - all of it goes here,
  * and must be listened to.
  */
object EventBus {
  private val listeners: mutable.HashMap[Class[_ <: Event], mutable.Buffer[Event => Unit]] = mutable.HashMap.empty

  def listenTo(clazz: Class[_ <: Event], listener: Event => Unit): Unit = {
    if (!listeners.contains(clazz)) listeners(clazz) = mutable.Buffer(listener)
    else listeners(clazz) :+= listener
  }

  def send(event: Event): Unit = {
    val clazz = event.getClass
    if (listeners.contains(clazz)) {
      listeners(clazz).foreach { listener => listener(event) }
    }
  }

  private val fileSystemAccessTimeouts = mutable.WeakHashMap.empty[Node, Long]
  def sendDiskActivity(node: Node): Unit = {
    fileSystemAccessTimeouts.get(node) match {
      case Some(timeout) if timeout > System.currentTimeMillis() => // Cooldown.
      case _ =>
        send(FileSystemActivityEvent(node.address))
        fileSystemAccessTimeouts.put(node, System.currentTimeMillis() + 500)
    }
  }
}
