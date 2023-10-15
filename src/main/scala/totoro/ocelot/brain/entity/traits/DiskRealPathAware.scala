package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.util.Persistable
import totoro.ocelot.brain.workspace.Workspace

import java.nio.file.{Files, InvalidPathException, Path, Paths}

trait DiskRealPathAware extends Environment with Persistable with WorkspaceAware {
  /**
   * Gets the default workspace path the disk should be bound, which is analogue of .minecraft/saves/save_name/opencomputers/disk_address
   *
   * @param address The address of filesystem
   * @return Default workspace path the disk should be bound
   */
  def getDefaultRealPath(address: String): Path =
    workspace.path.resolve(address)

  /**
   * Custom user-defined path to any "real" directory on "real" computer.
   * It substitutes default workspace path and allows to work with files from
   * "real" filesystem, like it's native one
   *
   * Getter/setter logic is required for overriding purposes in DiskManaged/RAID
   */
  private var _customRealPath: Option[Path] = None

  def customRealPath: Option[Path] =
    _customRealPath

  def customRealPath_=(value: Option[Path]): Unit =
    _customRealPath = value

  /**
   * Gets the actual path the disk should be bound, whether it is "custom" or "workspaced"
   *
   * @param fallbackAddress The address of filesystem if "customRealPath" wasn't set
   * @return The actual path the disk should be bound
   */
  def getRealPath(fallbackAddress: String): Path =
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

    // Don't trigger setter in case if it was overriden
    _customRealPath =
      try {
        Option.when(nbt.hasKey(RealPathTag))(Paths.get(nbt.getString(RealPathTag)))
      }
      catch {
        case _: InvalidPathException => None
      }
  }
}
