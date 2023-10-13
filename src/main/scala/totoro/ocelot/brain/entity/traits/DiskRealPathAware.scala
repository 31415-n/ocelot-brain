package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.util.Persistable
import totoro.ocelot.brain.workspace.Workspace

import java.nio.file.{Files, InvalidPathException, Path, Paths}

trait DiskRealPathAware extends Environment with Persistable with WorkspaceAware {
  def getDefaultRealPath(fallbackAddress: String): Path = workspace.path.resolve(fallbackAddress)

  private var _customRealPath: Option[Path] = None
  def customRealPath: Option[Path] = _customRealPath
  def customRealPath_=(value: Option[Path]): Unit = {
    _customRealPath = value
  }

  def getRealOrDefaultPath(fallbackAddress: String): Path =
    customRealPath.filter(Files.exists(_)).filter(Files.isDirectory(_)).getOrElse(getDefaultRealPath(fallbackAddress))

  // ----------------------------------------------------------------------- //

  private val RealPathTag = "rp"

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)

    // Custom real path
    if (customRealPath.isDefined)
      nbt.setString(RealPathTag, customRealPath.get.toString)
    else
      nbt.removeTag(RealPathTag)
  }

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)

    // DO. NOT. TOUCH. THIS.
    // Workspace is null during loading by default
    this.workspace = workspace

    customRealPath =
      try {
        Option.when(nbt.hasKey(RealPathTag))(Paths.get(nbt.getString(RealPathTag)))
      }
      catch {
        case _: InvalidPathException => None
      }
  }
}
