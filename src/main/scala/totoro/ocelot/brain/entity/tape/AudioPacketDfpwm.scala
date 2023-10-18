package totoro.ocelot.brain.entity.tape

import java.nio.ByteBuffer

case class AudioPacketDfpwm(volume: Byte, frequency: Int, data: Array[Byte]) {
  def decode(codec: Dfpwm): ByteBuffer = {
    val audio = ByteBuffer.allocate(data.length * 8)
    codec.decompress(audio, ByteBuffer.wrap(data))

    // supposedly converts signed data to unsigned.
    // but to me it looks like it just flips the sign bit. weird. whatever.
    audio.array().mapInPlace(b => ((b & 0xff) ^ 0x80).toByte)
    audio.flip()

    audio
  }
}
