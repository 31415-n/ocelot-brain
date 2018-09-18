package totoro.ocelot.brain.entity.fs

trait Volatile extends VirtualFileSystem {
  override def close() {
    super.close()
    root.children.clear()
  }
}
