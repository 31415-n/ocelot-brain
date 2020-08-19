package totoro.ocelot.brain.entity.fs

import java.io.FileNotFoundException

import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.workspace.Workspace

private class ReadOnlyWrapper(val fileSystem: FileSystemTrait) extends FileSystemTrait {
  override def isReadOnly = true

  override def spaceTotal: Long = fileSystem.spaceUsed

  override def spaceUsed: Long = fileSystem.spaceUsed

  override def exists(path: String): Boolean = fileSystem.exists(path)

  override def size(path: String): Long = fileSystem.size(path)

  override def isDirectory(path: String): Boolean = fileSystem.isDirectory(path)

  override def lastModified(path: String): Long = fileSystem.lastModified(path)

  override def list(path: String): Array[String] = fileSystem.list(path)

  override def delete(path: String) = false

  override def makeDirectory(path: String) = false

  override def rename(from: String, to: String) = false

  override def setLastModified(path: String, time: Long) = false

  override def open(path: String, mode: Mode.Value): Int = mode match {
    case Mode.Read => fileSystem.open(path, mode)
    case Mode.Write => throw new FileNotFoundException("read-only filesystem; cannot open for writing: " + path)
    case Mode.Append => throw new FileNotFoundException("read-only filesystem; cannot open for appending: " + path)
  }

  override def getHandle(handle: Int): Handle = fileSystem.getHandle(handle)

  override def close(): Unit = fileSystem.close()

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = fileSystem.load(nbt, workspace)

  override def save(nbt: NBTTagCompound): Unit = fileSystem.save(nbt)
}
