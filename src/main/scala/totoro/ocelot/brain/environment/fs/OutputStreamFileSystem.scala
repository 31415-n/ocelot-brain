package totoro.ocelot.brain.environment.fs

import java.io.{FileNotFoundException, IOException}

import totoro.ocelot.brain.environment.fs
import totoro.ocelot.brain.nbt.{NBT, NBTTagCompound, NBTTagList}

import scala.collection.mutable

trait OutputStreamFileSystem extends InputStreamFileSystem {
  private val handles = mutable.Map.empty[Int, OutputHandle]

  // ----------------------------------------------------------------------- //

  override def isReadOnly = false

  // ----------------------------------------------------------------------- //

  override def open(path: String, mode: Mode.Value): Int = this.synchronized(mode match {
    case Mode.Read => super.open(path, mode)
    case _ =>
      FileSystemAPI.validatePath(path)
      if (!isDirectory(path)) {
        val handle = Iterator.continually((Math.random() * Int.MaxValue).toInt + 1).filterNot(handles.contains).next()
        openOutputHandle(handle, path, mode) match {
          case Some(fileHandle) =>
            handles += handle -> fileHandle
            handle
          case _ => throw new FileNotFoundException(path)
        }
      } else throw new FileNotFoundException(path)
  })

  override def getHandle(handle: Int): fs.Handle = this.synchronized(Option(super.getHandle(handle)).orElse(handles.get(handle)).orNull)

  override def close(): Unit = this.synchronized {
    super.close()
    for (handle <- handles.values)
      handle.close()
    handles.clear()
  }

  // ----------------------------------------------------------------------- //

  private final val OutputTag = "output"
  private final val HandleTag = "handle"
  private final val PathTag = "path"

  override def load(nbt: NBTTagCompound) {
    super.load(nbt)

    val handlesNbt = nbt.getTagList(OutputTag, NBT.TAG_COMPOUND)
    (0 until handlesNbt.tagCount).map(handlesNbt.getCompoundTagAt).foreach(handleNbt => {
      val handle = handleNbt.getInteger(HandleTag)
      val path = handleNbt.getString(PathTag)
      openOutputHandle(handle, path, Mode.Append) match {
        case Some(fileHandle) => handles += handle -> fileHandle
        case _ => // The source file seems to have changed since last time.
      }
    })
  }

  override def save(nbt: NBTTagCompound): Unit = this.synchronized {
    super.save(nbt)

    val handlesNbt = new NBTTagList()
    for (file <- handles.values) {
      assert(!file.isClosed)
      val handleNbt = new NBTTagCompound()
      handleNbt.setInteger(HandleTag, file.handle)
      handleNbt.setString(PathTag, file.path)
      handlesNbt.appendTag(handleNbt)
    }
    nbt.setTag(OutputTag, handlesNbt)
  }

  // ----------------------------------------------------------------------- //

  protected def openOutputHandle(id: Int, path: String, mode: Mode.Value): Option[OutputHandle]

  // ----------------------------------------------------------------------- //

  protected abstract class OutputHandle(val owner: OutputStreamFileSystem, val handle: Int, val path: String) extends fs.Handle {
    protected var _isClosed = false

    def isClosed: Boolean = _isClosed

    override def close(): Unit = if (!isClosed) {
      _isClosed = true
      owner.handles -= handle
    }

    override def read(into: Array[Byte]): Int = throw new IOException("bad file descriptor")

    override def seek(to: Long): Long = throw new IOException("bad file descriptor")
  }
}
