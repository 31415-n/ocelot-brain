package totoro.ocelot.brain.network

/**
  * This type is used to deliver messages sent in a component network.
  *
  * We use an extra class to deliver messages to nodes to make the cancel logic
  * more clear (returning a boolean can get annoying very fast).
  *
  * @param source the node that sent the message.
  * @param name the name of this message
  * @param data the values passed along in the message.
  */
class Message(val source: Node, val name: String, val data: Array[AnyRef]) {
  var isCanceled = false

  /**
    * Stop further propagation of a broadcast message.
    *
    * This can be used to stop further distributing messages when either
    * serving a message to a specific address and there are multiple nodes
    * with that address, or when serving a broadcast message.
    */
  def cancel(): Unit = isCanceled = true
}
