package totoro.ocelot.brain.entity.fs

import totoro.ocelot.brain.nbt.{NBT, NBTTagCompound, NBTTagList}
import totoro.ocelot.brain.workspace.Workspace

import java.io
import java.io.{FileNotFoundException, InputStream}
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait VirtualFileSystem extends OutputStreamFileSystem {
  protected val root = new VirtualDirectory

  // ----------------------------------------------------------------------- //

  override def exists(path: String): Boolean =
    root.get(segments(path)).isDefined

  override def isDirectory(path: String): Boolean =
    root.get(segments(path)) match {
      case Some(obj) => obj.isDirectory
      case _ => false
    }

  override def size(path: String): Long =
    root.get(segments(path)) match {
      case Some(obj) => obj.size
      case _ => 0L
    }

  override def lastModified(path: String): Long =
    root.get(segments(path)) match {
      case Some(obj) => obj.lastModified
      case _ => 0L
    }

  override def list(path: String): Array[String] =
    root.get(segments(path)) match {
      case Some(obj: VirtualDirectory) => obj.list()
      case _ => null
    }

  // ----------------------------------------------------------------------- //

  override def delete(path: String): Boolean = {
    val parts = segments(path)
    if (parts.isEmpty) true
    else {
      root.get(parts.dropRight(1)) match {
        case Some(parent: VirtualDirectory) => parent.delete(parts.last)
        case _ => false
      }
    }
  }

  override def makeDirectory(path: String): Boolean = {
    val parts = segments(path)
    if (parts.isEmpty) false
    else {
      root.get(parts.dropRight(1)) match {
        case Some(parent: VirtualDirectory) => parent.makeDirectory(parts.last)
        case _ => false
      }
    }
  }

  override def rename(from: String, to: String): Boolean =
    if (from == "" || !exists(from)) throw new FileNotFoundException(from)
    else {
      val segmentsTo = segments(to)
      root.get(segmentsTo.dropRight(1)) match {
        case Some(toParent: VirtualDirectory) =>
          val toName = segmentsTo.last
          val segmentsFrom = segments(from)
          val fromParent = root.get(segmentsFrom.dropRight(1)).get.asInstanceOf[VirtualDirectory]
          val fromName = segmentsFrom.last
          val obj = fromParent.children(fromName)

          if (toParent.get(List(toName)).isDefined) {
            toParent.delete(toName)
          }

          fromParent.children -= fromName
          fromParent.lastModified = System.currentTimeMillis()

          toParent.children += toName -> obj
          toParent.lastModified = System.currentTimeMillis()

          obj.lastModified = System.currentTimeMillis()
          true
        case _ => false
      }
    }

  override def setLastModified(path: String, time: Long): Boolean =
    root.get(segments(path)) match {
      case Some(obj) if time >= 0 =>
        obj.lastModified = time
        true
      case _ => false
    }

  // ----------------------------------------------------------------------- //

  protected def openInputChannel(path: String): Option[InputStreamChannel] =
    root.get(segments(path)) match {
      case Some(obj: VirtualFile) => obj.openInputStream().map(new InputStreamChannel(_))
      case _ => None
    }

  protected def openOutputHandle(id: Int, path: String, mode: Mode.Value): Option[OutputHandle] = {
    val parts = segments(path)
    if (parts.isEmpty) None
    else {
      root.get(parts.dropRight(1)) match {
        case Some(directory: VirtualDirectory) => directory.touch(parts.last) match {
          case Some(file: VirtualFile) => file.openOutputHandle(this, id, path, mode)
          case _ => None
        }
        case _ => None
      }
    }
  }

  // ----------------------------------------------------------------------- //

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    if (!this.isInstanceOf[Buffered]) root.load(nbt)
    super.load(nbt, workspace) // Last to ensure streams can be re-opened.
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt) // First to allow flushing.
    if (!this.isInstanceOf[Buffered]) root.save(nbt)
  }

  // ----------------------------------------------------------------------- //

  protected def segments(path: String): Array[String] = FileSystemAPI.validatePath(path).split("/").filter(_ != "")

  // ----------------------------------------------------------------------- //

  protected trait VirtualObject {
    def isDirectory: Boolean

    def size: Long

    var lastModified: Long = System.currentTimeMillis()

    def load(nbt: NBTTagCompound): Unit = {
      if (nbt.hasKey("lastModified"))
        lastModified = nbt.getLong("lastModified")
    }

    def save(nbt: NBTTagCompound): Unit = {
      nbt.setLong("lastModified", lastModified)
    }

    def get(path: Iterable[String]): Option[VirtualObject] =
      if (path.isEmpty) Some(this) else None

    def canDelete: Boolean
  }

  // ----------------------------------------------------------------------- //

  protected class VirtualFile extends VirtualObject {
    val data: ArrayBuffer[Byte] = mutable.ArrayBuffer.empty[Byte]

    var handle: Option[VirtualOutputHandle] = None

    override def isDirectory = false

    override def size: Long = data.length

    def openInputStream(): Option[InputStream] = Some(new VirtualFileInputStream(this))

    def openOutputHandle(owner: OutputStreamFileSystem, id: Int, path: String, mode: Mode.Value): Option[OutputHandle] =
      if (handle.isDefined) None
      else {
        if (mode == Mode.Write) {
          data.clear()
          lastModified = System.currentTimeMillis()
        }
        handle = Some(new VirtualOutputHandle(this, owner, id, path))
        handle
      }

    override def load(nbt: NBTTagCompound): Unit = {
      super.load(nbt)
      data.clear()
      data ++= nbt.getByteArray("data")
    }

    override def save(nbt: NBTTagCompound): Unit = {
      super.save(nbt)
      nbt.setByteArray("data", data.toArray)
    }

    override def canDelete: Boolean = handle.isEmpty
  }

  // ----------------------------------------------------------------------- //

  protected class VirtualDirectory extends VirtualObject {
    val children = mutable.Map.empty[String, VirtualObject]

    override def isDirectory = true

    override def size = 0

    def list(): Array[String] = children.map {
      case (childName, child) => if (child.isDirectory) childName + "/" else childName
    }.toArray

    def makeDirectory(name: String): Boolean =
      if (children.contains(name)) false
      else {
        children += name -> new VirtualDirectory
        lastModified = System.currentTimeMillis()
        true
      }

    def delete(name: String): Boolean = {
      children.get(name) match {
        case Some(child) if child.canDelete =>
          children -= name
          lastModified = System.currentTimeMillis()
          true
        case _ => false
      }
    }

    def touch(name: String): Option[VirtualFile] =
      children.get(name) match {
        case Some(obj: VirtualFile) => Some(obj)
        case None =>
          val child = new VirtualFile
          children += name -> child
          lastModified = System.currentTimeMillis()
          Some(child)
        case _ => None // Directory.
      }

    private final val ChildrenTag = "children"
    private final val IsDirectoryTag = "isDirectory"
    private final val NameTag = "name"

    override def load(nbt: NBTTagCompound): Unit = {
      super.load(nbt)
      val childrenNbt = nbt.getTagList(ChildrenTag, NBT.TAG_COMPOUND)
      (0 until childrenNbt.tagCount).map(childrenNbt.getCompoundTagAt).foreach(childNbt => {
        val child =
          if (childNbt.getBoolean(IsDirectoryTag)) new VirtualDirectory
          else new VirtualFile
        child.load(childNbt)
        children += childNbt.getString(NameTag) -> child
      })
    }

    override def save(nbt: NBTTagCompound): Unit = {
      super.save(nbt)
      val childrenNbt = new NBTTagList()
      for ((childName, child) <- children) {
        val childNbt = new NBTTagCompound()
        childNbt.setBoolean(IsDirectoryTag, child.isDirectory)
        childNbt.setString(NameTag, childName)
        child.save(childNbt)
        childrenNbt.appendTag(childNbt)
      }
      nbt.setTag(ChildrenTag, childrenNbt)
    }

    override def get(path: Iterable[String]): Option[VirtualObject] =
      super.get(path) orElse {
        children.get(path.head) match {
          case Some(child) => child.get(path.drop(1))
          case _ => None
        }
      }

    override def canDelete: Boolean = children.isEmpty
  }

  // ----------------------------------------------------------------------- //

  protected class VirtualFileInputStream(val file: VirtualFile) extends io.InputStream {
    private var isClosed = false

    private var position = 0

    override def available(): Int =
      if (isClosed) 0
      else math.max(file.data.length - position, 0)

    override def close(): Unit = isClosed = true

    override def read(): Int =
      if (!isClosed) {
        if (available == 0) -1
        else {
          position += 1
          file.data(position - 1)
        }
      }
      else throw new io.IOException("file is closed")

    override def read(b: Array[Byte], off: Int, len: Int): Int =
      if (!isClosed) {
        val count = available()
        if (count == 0) -1
        else {
          val n = math.min(len, count)
          file.data.view.slice(position, file.data.length).copyToArray(b, off, n)
          position += n
          n
        }
      }
      else throw new io.IOException("file is closed")

    override def reset(): Unit =
      if (!isClosed) {
        position = 0
      }
      else throw new io.IOException("file is closed")

    override def skip(n: Long): Long =
      if (!isClosed) {
        position = math.min((position + n).toInt, Int.MaxValue)
        position
      }
      else throw new io.IOException("file is closed")
  }

  // ----------------------------------------------------------------------- //

  protected class VirtualOutputHandle(val file: VirtualFile, owner: OutputStreamFileSystem, handle: Int, path: String)
    extends OutputHandle(owner, handle, path) {

    override def length: Long = file.size

    var position: Long = file.data.length

    override def close(): Unit = if (!isClosed) {
      super.close()
      assert(file.handle.get == this)
      file.handle = None
    }

    override def seek(to: Long): Long = {
      if (to < 0) throw new io.IOException("invalid offset")
      position = to
      position
    }

    override def write(b: Array[Byte]): Unit =
      if (!isClosed) {
        val pos = position.toInt
        file.data.insertAll(file.data.length, Seq.fill[Byte]((pos + b.length) - file.data.length)(0))
        for (i <- b.indices) {
          file.data(pos + i) = b(i)
        }
        position += b.length
        file.lastModified = System.currentTimeMillis()
      }
      else throw new io.IOException("file is closed")
  }
}
