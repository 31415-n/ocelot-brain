package totoro.ocelot.brain.environment.fs

import java.io
import java.io.ByteArrayInputStream
import java.util.concurrent.Callable
import java.util.zip.{ZipEntry, ZipFile}

import com.google.common.cache.CacheBuilder
import totoro.ocelot.brain.Ocelot
import totoro.ocelot.brain.environment.fs.ZipFileInputStreamFileSystem.{ArchiveDirectory, ArchiveFile}

import scala.collection.mutable
import scala.language.postfixOps

class ZipFileInputStreamFileSystem(private val archive: ArchiveDirectory) extends InputStreamFileSystem {

  def spaceTotal: Long = spaceUsed

  def spaceUsed: Long = spaceUsed_

  private lazy val spaceUsed_ = ZipFileInputStreamFileSystem.synchronized {
    def recurse(d: ArchiveDirectory): Long = d.children.foldLeft(0L)((acc, c) => acc + (c match {
      case directory: ArchiveDirectory => recurse(directory)
      case file: ArchiveFile => file.size
    }))

    recurse(archive)
  }

  // ----------------------------------------------------------------------- //

  override def exists(path: String): Boolean = ZipFileInputStreamFileSystem.synchronized {
    entry(path).isDefined
  }

  override def size(path: String): Long = ZipFileInputStreamFileSystem.synchronized {
    entry(path) match {
      case Some(file) if !file.isDirectory => file.size
      case _ => 0L
    }
  }

  override def isDirectory(path: String): Boolean = ZipFileInputStreamFileSystem.synchronized {
    entry(path).exists(_.isDirectory)
  }

  def lastModified(path: String): Long = ZipFileInputStreamFileSystem.synchronized {
    entry(path) match {
      case Some(file) => file.lastModified
      case _ => 0L
    }
  }

  override def list(path: String): Array[String] = ZipFileInputStreamFileSystem.synchronized {
    entry(path) match {
      case Some(entry) if entry.isDirectory => entry.list()
      case _ => null
    }
  }

  // ----------------------------------------------------------------------- //

  override protected def openInputChannel(path: String): Option[InputStreamChannel] = ZipFileInputStreamFileSystem.synchronized {
    entry(path).map(entry => new InputStreamChannel(entry.openStream()))
  }

  // ----------------------------------------------------------------------- //

  private def entry(path: String) = {
    val cleanPath = "/" + path.replace("\\", "/").replace("//", "/").stripPrefix("/").stripSuffix("/")
    if (cleanPath == "/") Some(archive)
    else archive.find(cleanPath.split("/"))
  }
}

object ZipFileInputStreamFileSystem {
  private val cache = com.google.common.cache.CacheBuilder.newBuilder().
    weakValues().
    asInstanceOf[CacheBuilder[String, ArchiveDirectory]].
    build[String, ArchiveDirectory]()

  def fromFile(file: io.File, innerPath: String): ZipFileInputStreamFileSystem = ZipFileInputStreamFileSystem.synchronized {
    try {
      Option(cache.get(file.getPath + ":" + innerPath, new Callable[ArchiveDirectory] {
        def call: ArchiveDirectory = {
          val zip = new ZipFile(file.getPath)
          try {
            val cleanedPath = innerPath.stripPrefix("/").stripSuffix("/") + "/"
            val rootEntry = zip.getEntry(cleanedPath)
            if (rootEntry == null || !rootEntry.isDirectory) {
              throw new IllegalArgumentException(s"Root path $innerPath doesn't exist or is not a directory in ZIP file ${file.getName}.")
            }
            val directories = mutable.Set.empty[ArchiveDirectory]
            val files = mutable.Set.empty[ArchiveFile]
            val iterator = zip.entries()
            while (iterator.hasMoreElements) {
              val entry = iterator.nextElement()
              if (entry.getName.startsWith(cleanedPath)) {
                if (entry.isDirectory) directories += new ArchiveDirectory(entry, cleanedPath)
                else files += new ArchiveFile(zip, entry, cleanedPath)
              }
            }
            var root: ArchiveDirectory = null
            for (entry <- directories ++ files) {
              if (entry.path.length > 0) {
                val parent = entry.path.substring(0, math.max(entry.path.lastIndexOf('/'), 0))
                directories.find(d => d.path == parent) match {
                  case Some(directory) => directory.children += entry
                  case _ =>
                }
              }
              else {
                assert(entry.isInstanceOf[ArchiveDirectory])
                root = entry.asInstanceOf[ArchiveDirectory]
              }
            }
            root
          }
          finally {
            zip.close()
          }
        }
      })) match {
        case Some(archive) => new ZipFileInputStreamFileSystem(archive)
        case _ => null
      }
    }
    catch {
      case e: Throwable =>
        Ocelot.log.warn("Failed creating ZIP file system.", e)
        null
    }
  }

  abstract class Archive(entry: ZipEntry, root: String) {
    val path: String = entry.getName.stripPrefix(root).stripSuffix("/")

    val name: String = path.substring(path.lastIndexOf('/') + 1)

    val lastModified: Long = entry.getTime

    val isDirectory: Boolean = entry.isDirectory

    def size: Int

    def list(): Array[String]

    def openStream(): io.InputStream

    def find(path: Iterable[String]): Option[Archive]
  }

  private class ArchiveFile(zip: ZipFile, entry: ZipEntry, root: String) extends Archive(entry, root) {
    val data: Array[Byte] = {
      val in = zip.getInputStream(entry)
      Iterator.continually(in.read).takeWhile(-1 !=).map(_.toByte).toArray
    }

    val size: Int = data.length

    def list(): Null = null

    def openStream() = new ByteArrayInputStream(data)

    def find(path: Iterable[String]): Option[ArchiveFile] =
      if (path.size == 1 && path.head == name) Some(this)
      else None
  }

  private class ArchiveDirectory(entry: ZipEntry, root: String) extends Archive(entry, root) {
    val children = mutable.Set.empty[Archive]

    val size = 0

    def list(): Array[String] = children.map(c => c.name + (if (c.isDirectory) "/" else "")).toArray

    def openStream(): Null = null

    def find(path: Iterable[String]): Option[Archive] =
      if (path.head == name) {
        if (path.size == 1) Some(this)
        else {
          val subPath = path.drop(1)
          children.map(_.find(subPath)).collectFirst {
            case Some(a) => a
          }
        }
      }
      else None
  }

}
