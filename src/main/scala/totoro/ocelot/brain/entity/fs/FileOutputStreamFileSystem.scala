package totoro.ocelot.brain.entity.fs

import totoro.ocelot.brain.nbt.NBTTagCompound

import java.io
import java.io.RandomAccessFile
import java.nio.file.{Files, StandardCopyOption}

trait FileOutputStreamFileSystem extends FileInputStreamFileSystem with OutputStreamFileSystem {
  override def spaceTotal: Long = -1

  override def spaceUsed: Long = -1

  // ----------------------------------------------------------------------- //

  override def delete(path: String): Boolean = {
    val file = new io.File(root, FileSystemAPI.validatePath(path))
    file == root || file.delete()
  }

  override def makeDirectory(path: String): Boolean = new io.File(root, FileSystemAPI.validatePath(path)).mkdir()

  override def rename(from: String, to: String): Boolean = {
    try {
      Files.move(
        new io.File(root, FileSystemAPI.validatePath(from)).toPath,
        new io.File(root, FileSystemAPI.validatePath(to)).toPath,
        StandardCopyOption.REPLACE_EXISTING
      )
      true
    } catch {
      case e: Exception => false
    }
  }

  override def setLastModified(path: String, time: Long): Boolean = new io.File(root, FileSystemAPI.validatePath(path)).setLastModified(time)

  // ----------------------------------------------------------------------- //

  override protected def openOutputHandle(id: Int, path: String, mode: Mode.Value): Option[OutputHandle] =
    Some(new FileHandle(new RandomAccessFile(new io.File(root, path), mode match {
      case Mode.Append | Mode.Write => "rw"
      case _ => throw new IllegalArgumentException()
    }), this, id, path, mode))

  // ----------------------------------------------------------------------- //

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    root.mkdirs()
    root.setLastModified(System.currentTimeMillis())
  }

  // ----------------------------------------------------------------------- //

  protected class FileHandle(val file: RandomAccessFile, owner: OutputStreamFileSystem,
                             handle: Int, path: String, mode: Mode.Value) extends OutputHandle(owner, handle, path) {
    if (mode == Mode.Write) {
      file.setLength(0)
    }

    override def position: Long = file.getFilePointer

    override def length: Long = file.length()

    override def close(): Unit = {
      super.close()
      file.close()
    }

    override def seek(to: Long): Long = {
      file.seek(to)
      to
    }

    override def write(value: Array[Byte]): Unit = file.write(value)
  }

}
