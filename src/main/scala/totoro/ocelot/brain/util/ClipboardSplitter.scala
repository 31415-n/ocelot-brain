package totoro.ocelot.brain.util

/**
  * Splits a string the way OC does when sending clipboard packets from the client.
  */
class ClipboardSplitter {
  var clipboardCooldown = 0L

  /**
    * Splits the string into chunks, where each would be sent as a separate packet.
    *
    * @return an iterator (possibly empty) over individual chunks, or [[None]] if limits are exceeded.
    */
  def split(value: String): Option[Iterator[String]] = {
    if (value.nonEmpty) {
      if (value.length > 64 * 1024 || System.currentTimeMillis() < clipboardCooldown) {
        None
      } else {
        clipboardCooldown = System.currentTimeMillis() + value.length / 10

        Some(value.grouped(16 * 1024))
      }
    } else {
      Some(Iterator.empty)
    }
  }
}
