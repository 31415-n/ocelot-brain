package totoro.ocelot.brain.entity.fs

trait Volatile extends VirtualFileSystem {
  override def close(): Unit = {
    super.close()
    root.children.clear()
  }
}
