package totoro.ocelot.brain.entity.fs

import totoro.ocelot.brain.entity.fs
import totoro.ocelot.brain.nbt.{NBT, NBTTagCompound, NBTTagList}
import totoro.ocelot.brain.workspace.Workspace

import java.io.{FileNotFoundException, IOException}
import java.nio.ByteBuffer
import java.nio.channels.ReadableByteChannel
import scala.collection.mutable

trait InputStreamFileSystem extends FileSystemTrait {
  private val handles = mutable.Map.empty[Int, Handle]

  // ----------------------------------------------------------------------- //

  override def isReadOnly = true

  override def delete(path: String) = false

  override def makeDirectory(path: String) = false

  override def rename(from: String, to: String) = false

  override def setLastModified(path: String, time: Long) = false

  // ----------------------------------------------------------------------- //

  override def open(path: String, mode: Mode.Value): Int = {
    FileSystemAPI.validatePath(path)
    this.synchronized(if (mode == Mode.Read && exists(path) && !isDirectory(path)) {
      val handle = Iterator.continually((Math.random() * Int.MaxValue).toInt + 1).filterNot(handles.contains).next()
      openInputChannel(path) match {
        case Some(channel) =>
          handles += handle -> new Handle(this, handle, path, channel)
          handle
        case _ => throw new FileNotFoundException(path)
      }
    } else throw new FileNotFoundException(path))
  }

  override def getHandle(handle: Int): fs.Handle = this.synchronized(handles.get(handle).orNull)

  override def close(): Unit = this.synchronized {
    for (handle <- handles.values)
      handle.close()
    handles.clear()
  }

  // ----------------------------------------------------------------------- //

  private final val InputTag = "input"
  private final val HandleTag = "handle"
  private final val PathTag = "path"
  private final val PositionTag = "position"

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)
    val handlesNbt = nbt.getTagList(InputTag, NBT.TAG_COMPOUND)
    (0 until handlesNbt.tagCount).map(handlesNbt.getCompoundTagAt).foreach(handleNbt => {
      val handle = handleNbt.getInteger(HandleTag)
      val path = handleNbt.getString(PathTag)
      val position = handleNbt.getLong(PositionTag)
      openInputChannel(path) match {
        case Some(channel) =>
          val fileHandle = new Handle(this, handle, path, channel)
          channel.position(position)
          handles += handle -> fileHandle
        case _ => // The source file seems to have disappeared since last time.
      }
    })
  }

  override def save(nbt: NBTTagCompound): Unit = this.synchronized {
    super.save(nbt)
    val handlesNbt = new NBTTagList()
    for (file <- handles.values) {
      assert(file.channel.isOpen)
      val handleNbt = new NBTTagCompound()
      handleNbt.setInteger(HandleTag, file.handle)
      handleNbt.setString(PathTag, file.path)
      handleNbt.setLong(PositionTag, file.position)
      handlesNbt.appendTag(handleNbt)
    }
    nbt.setTag(InputTag, handlesNbt)
  }

  // ----------------------------------------------------------------------- //

  protected def openInputChannel(path: String): Option[InputChannel]

  protected trait InputChannel extends ReadableByteChannel {
    def isOpen: Boolean

    def close(): Unit

    def position: Long

    def position(newPosition: Long): Long

    def read(dst: Array[Byte]): Int

    override def read(dst: ByteBuffer): Int = {
      if (dst.hasArray) {
        read(dst.array())
      }
      else {
        val count = math.max(0, dst.limit() - dst.position())
        val buffer = new Array[Byte](count)
        val n = read(buffer)
        if (n > 0) dst.put(buffer, 0, n)
        n
      }
    }
  }

  protected class InputStreamChannel(val inputStream: java.io.InputStream) extends InputChannel {
    var isOpen = true

    private var position_ = 0L

    override def close(): Unit = if (isOpen) {
      isOpen = false
      inputStream.close()
    }

    override def position: Long = position_

    override def position(newPosition: Long): Long = {
      inputStream.reset()
      position_ = inputStream.skip(newPosition)
      position_
    }

    override def read(dst: Array[Byte]): Int = {
      val read = inputStream.read(dst)
      position_ += read
      read
    }
  }

  // ----------------------------------------------------------------------- //

  private class Handle(val owner: InputStreamFileSystem, val handle: Int, val path: String, val channel: InputChannel) extends fs.Handle {
    override def position: Long = channel.position

    override def length: Long = owner.size(path)

    override def close(): Unit = if (channel.isOpen) {
      owner.handles -= handle
      channel.close()
    }

    override def read(into: Array[Byte]): Int = channel.read(into)

    override def seek(to: Long): Long = channel.position(to)

    override def write(value: Array[Byte]): Unit = throw new IOException("bad file descriptor")
  }
}
