package totoro.ocelot.brain.util

/**
  * Helper functions for handling strings with characters outside of the Unicode BMP.
  */
object ExtendedUnicodeHelper {
  def length(s: String): Int = s.codePointCount(0, s.length)

  def reverse(s: String): String = {
    val sb = new StringBuilder

    var i = s.length - 1
    while (i >= 0) {
      val c = s.charAt(i)
      if (Character.isLowSurrogate(c) && i > 0) {
        i -= 1
        val c2 = s.charAt(i)
        if (Character.isHighSurrogate(c2)) {
          sb.append(c2).append(c)
        } else {
          // Invalid surrogate pair?
          sb.append(c).append(c2)
        }
      } else {
        sb.append(c)
      }
      i -= 1
    }

    sb.toString
  }

  def substring(s: String, start: Int, end: Int): String =
    s.substring(s.offsetByCodePoints(0, start), s.offsetByCodePoints(0, end))
}
