package totoro.ocelot.brain.entity.tape

import totoro.ocelot.brain.Ocelot

import java.io.{File, FileInputStream, FileOutputStream, IOException}
import java.util.zip.{GZIPInputStream, GZIPOutputStream}
import scala.util.Using

class TapeStorage(
  val uniqueId: String,
  file: => File,
  private var _size: Int,
  private var _position: Int,
) extends traits.TapeStorage {

  private var data = Array.ofDim[Byte](size)
  private var modified = false

  {
    // so we only evaluate `file` once in this block
    val f = file

    if (!f.exists()) {
      try {
        f.createNewFile()
        writeFile(f)
      } catch {
        case e: Exception => Ocelot.log.error(s"Could not create tape storage $f", e)
      }
    } else {
      try {
        readFile(f)
      } catch {
        case e: Exception => Ocelot.log.error(s"Could not read tape storage $f", e)
      }
    }
  }

  override def name: String = "Tape"

  override def position: Int = _position

  override def size: Int = _size

  override def setPosition(newPosition: Int): Int = {
    _position = newPosition max 0 min (size - 1)

    _position
  }

  private def trySeek(dir: Int): Int = {
    val oldPosition = _position
    val newPosition = (_position + dir) max 0 min (size - 1)

    newPosition - oldPosition
  }

  override def seek(amount: Int): Int = {
    val seek = trySeek(amount)
    _position += seek
    modified = true
    seek
  }

  override def read(simulate: Boolean): Int = {
    if (position >= size) 0
    else if (simulate) data(position) & 0xff
    else {
      modified = true
      val result = data(position)
      _position += 1

      result & 0xff
    }
  }

  def read(v: Array[Byte], offset: Int, simulate: Boolean): Int = {
    val len = v.length.min(size - (position + offset) - 1)
    Array.copy(data, position + offset, v, 0, len)

    if (!simulate) {
      _position += len
      modified = true
    }

    len
  }

  override def read(intoArray: Array[Byte], simulate: Boolean): Int = {
    read(intoArray, 0, simulate)
  }

  override def write(b: Byte): Unit = {
    if (position >= size) {
      return
    }

    modified = true
    data(position) = b
    _position += 1
  }

  override def write(array: Array[Byte]): Int = {
    val len = array.length.min(size - position - 1)

    if (len == 0) {
      return 0
    }

    Array.copy(array, 0, data, position, len)
    _position += len
    modified = true

    len
  }

  @throws[IOException]
  private def readFile(file: File = file): Unit = {
    Using.resource(new FileInputStream(file)) { fileStream =>
      Using.resource(new GZIPInputStream(fileStream)) { stream =>
        val version = stream.read()

        if (version >= 1) {
          val b1 = stream.read() & 0xff
          val b2 = stream.read() & 0xff
          val b3 = stream.read() & 0xff
          val b4 = stream.read() & 0xff
          _position = b1 | b2 << 8 | b3 << 16 | b4 << 24
        }

        data = Array.ofDim(size)

        var position = 0

        while (position < data.length) {
          position += stream.read(data, position, data.length - position)
        }
      }
    }
  }

  @throws[IOException]
  private def writeFile(file: File = file): Unit = {
    Using.resource(new FileOutputStream(file)) { fileStream =>
      Using.resource(new GZIPOutputStream(fileStream)) { stream =>
        stream.write(1)
        stream.write(position & 0xff)
        stream.write(position >>> 8 & 0xff)
        stream.write(position >>> 16 & 0xff)
        stream.write(position >>> 24 & 0xff)
        stream.write(data)
        stream.finish()
        stream.flush()
      }
    }

    modified = true
  }

  @throws[IOException]
  private def writeFileIfModified(): Unit = if (modified) {
    writeFile()
  }

  override def save(): Unit = try {
    writeFileIfModified()
  } catch {
    case e: Exception => Ocelot.log.error(s"Tape ID $uniqueId was NOT saved!", e)
  }

  override def onStorageUnload(): Unit = save()
}
