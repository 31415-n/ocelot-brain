package totoro.ocelot.brain.entity.tape

import totoro.ocelot.brain.entity.tape.Dfpwm.{LPF_STRENGTH, RESP_PREC}

import java.nio.ByteBuffer

class Dfpwm {
  private var response = 0
  private var level = 0
  private var lastBit = false

  private var lastLevel = 0
  private var lpfLevel = 0

  private def update(curBit: Boolean): Unit = {
    val target = if (curBit) 127 else -128
    var nextLevel = level + ((response * (target - level) + (1 << RESP_PREC - 1)) >> RESP_PREC)

    if (nextLevel == level && level != target) {
      nextLevel += (if (curBit) 1 else -1)
    }

    val responseTarget =
      if (curBit == lastBit) (1 << RESP_PREC) - 1
      else 0

    var nextResponse = response

    if (response != responseTarget) {
      nextResponse += (if (curBit == lastBit) 1 else -1)
    }

    if (RESP_PREC > 8) {
      nextResponse = nextResponse max 2 << RESP_PREC - 8
    }

    response = nextResponse
    lastBit = curBit
    level = nextLevel
  }

  def decompress(dst: ByteBuffer, src: ByteBuffer, len: Int): Unit = {
    for (_ <- 0 until len) {
      var byte = src.get()

      for (_ <- 0 until 8) {
        val curBit = (byte & 1) != 0
        val lastBit = this.lastBit
        update(curBit)
        byte = (byte >> 1).toByte

        // noise shaping (averages out bit changes)
        val bitLevel =
          if (curBit == lastBit) level
          else lastLevel + level + 1 >> 1
        lastLevel = level

        // low-pass filtering
        lpfLevel += LPF_STRENGTH * (bitLevel - lpfLevel) + 0x80 >> 8

        dst.put(lpfLevel.toByte)
      }
    }
  }

  def decompress(dst: ByteBuffer, src: ByteBuffer): Unit = decompress(dst, src, src.remaining())
}

object Dfpwm {
  private val RESP_PREC = 10
  private val LPF_STRENGTH = 140
}
