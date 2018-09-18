package totoro.ocelot.brain.entity.fs

import java.io
import java.nio.channels

trait FileInputStreamFileSystem extends InputStreamFileSystem {
  protected def root: io.File

  // ----------------------------------------------------------------------- //

  override def spaceTotal: Long = spaceUsed

  override def spaceUsed: Long = spaceUsed_

  private lazy val spaceUsed_ = {
    def recurse(path: io.File): Long = {
      if (path.isDirectory)
        path.listFiles.foldLeft(0L)((acc, f) => acc + recurse(f))
      else
        path.length
    }

    recurse(root)
  }

  // ----------------------------------------------------------------------- //

  override def exists(path: String): Boolean = new io.File(root, FileSystemAPI.validatePath(path)).exists()

  override def size(path: String): Long = new io.File(root, FileSystemAPI.validatePath(path)) match {
    case file if file.isFile => file.length()
    case _ => 0L
  }

  override def isDirectory(path: String): Boolean = new io.File(root, FileSystemAPI.validatePath(path)).isDirectory

  override def lastModified(path: String): Long = new io.File(root, FileSystemAPI.validatePath(path)).lastModified

  override def list(path: String): Array[String] = new io.File(root, FileSystemAPI.validatePath(path)) match {
    case file if file.exists() && file.isFile => Array(file.getName)
    case directory if directory.exists() && directory.isDirectory && directory.list() != null =>
      directory.listFiles().map(file => if (file.isDirectory) file.getName + "/" else file.getName)
    case _ => throw new io.FileNotFoundException("no such file or directory: " + path)
  }

  // ----------------------------------------------------------------------- //

  override protected def openInputChannel(path: String) = Some(new FileChannel(new io.File(root, path)))

  protected class FileChannel(file: io.File) extends InputChannel {
    val channel: channels.FileChannel = new io.RandomAccessFile(file, "r").getChannel

    override def position(newPosition: Long): Long = {
      channel.position(newPosition)
      channel.position
    }

    override def position: Long = channel.position

    override def close(): Unit = channel.close()

    override def isOpen: Boolean = channel.isOpen

    override def read(dst: Array[Byte]): Int = channel.read(java.nio.ByteBuffer.wrap(dst))
  }

}
