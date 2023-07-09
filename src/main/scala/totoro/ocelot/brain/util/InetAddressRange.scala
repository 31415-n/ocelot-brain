package totoro.ocelot.brain.util

import com.google.common.net.InetAddresses
import java.net.InetAddress

// Originally by SquidDev
class InetAddressRange(min: Array[Byte], max: Array[Byte]) {
  def matches(address: InetAddress): Boolean = {
    val entry = address.getAddress
    if (entry.length != min.length) return false

    for (i <- 0 until entry.length) {
      val value = 0xff & entry(i)
      if (value < (0xff & min(i)) || value > (0xff & max(i))) return false
    }

    true
  }
}

object InetAddressRange {
  def parse(addressStr: String, prefixSizeStr: String): InetAddressRange = {
    val prefixSize =
      try prefixSizeStr.toInt
      catch {
        case _: NumberFormatException =>
          throw new IllegalArgumentException(
            s"Malformed address range entry '$addressStr/$prefixSizeStr': Cannot extract size of CIDR mask from '$prefixSizeStr'."
          )
      }

    val address =
      try InetAddresses.forString(addressStr)
      catch {
        case _: IllegalArgumentException =>
          throw new IllegalArgumentException(
            s"Malformed address range entry '$addressStr/$prefixSizeStr': Cannot extract IP address from '$addressStr'."
          )
      }

    // Mask the bytes of the IP address.
    val minBytes = address.getAddress
    val maxBytes = address.getAddress
    var size = prefixSize
    for (i <- 0 until minBytes.length) {
      if (size <= 0) {
        minBytes(i) = 0.toByte
        maxBytes(i) = 0xff.toByte
      } else if (size < 8) {
        minBytes(i) = (minBytes(i) & 0xff << (8 - size)).toByte
        maxBytes(i) = (maxBytes(i) | ~(0xff << (8 - size))).toByte
      }
      size -= 8
    }

    new InetAddressRange(minBytes, maxBytes)
  }
}
