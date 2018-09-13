package totoro.ocelot.brain.environment.fs

trait Volatile extends VirtualFileSystem {
  override def close() {
    super.close()
    root.children.clear()
  }
}
