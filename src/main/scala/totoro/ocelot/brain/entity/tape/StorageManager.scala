package totoro.ocelot.brain.entity.tape

import totoro.ocelot.brain.Ocelot
import totoro.ocelot.brain.entity.traits.WorkspaceAware
import totoro.ocelot.brain.workspace.Workspace

import java.io.File

class StorageManager(initialWorkspace: Workspace) extends WorkspaceAware {
  workspace = initialWorkspace

  private def saveDir(): File = {
    val saveDir = workspace.path.resolve("computronics").toFile

    if (!saveDir.exists() && !saveDir.mkdir()) {
      Ocelot.log.error(s"Could not create save directory: $saveDir")
    }

    saveDir
  }

  private def filename(storageName: String): String = s"$storageName.dsk"

  def newStorage(size: Int): TapeStorage = {
    var storageName: String = null

    do {
      val nameHex = Array.ofDim[Byte](16)
      workspace.rand.nextBytes(nameHex)
      storageName = nameHex.iterator.map(b => (b & 0xff).toHexString).mkString
    } while (exists(storageName))

    get(storageName, size, 0)
  }

  def exists(name: String): Boolean = new File(saveDir(), filename(name)).exists()

  def get(name: String, size: Int, position: Int): TapeStorage =
    new TapeStorage(name, new File(saveDir(), filename(name)), size, position)
}
