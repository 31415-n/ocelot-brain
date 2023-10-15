package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.fs.{FileSystem, FileSystemAPI, FileSystemTrait, ReadWriteLabel}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.Node
import totoro.ocelot.brain.workspace.Workspace

import java.nio.file.Path
import java.util.UUID

/**
  * Basic trait for all managed-disk-like entities.
  */
trait DiskManaged extends Disk with DiskRealPathAware with WorkspaceAware {
  protected var envLock = false // refactor later

  protected var address: Option[String] = None

  private var _fileSystem: FileSystem = _
  def fileSystem: FileSystem = {
    if (_fileSystem == null)
      generateAndSetEnvironment()

    _fileSystem
  }

  override def node: Node = {
    if (envLock) {
      if (_fileSystem != null) _fileSystem.node
      else null
    }
    else {
      if (fileSystem != null)fileSystem.node
      else null
    }
  }

  protected def generateEnvironment(): FileSystem = {
    address = address.orElse(Some(UUID.randomUUID().toString))

    // Restoring default path if component was just inserted to slot (without NBT data loading)
    // or if user has changed/deleted/renamed previously set path
    val realPath = getRealPath(address.get)

    var fileSystemTrait: FileSystemTrait = FileSystemAPI.fromDirectory(
      realPath.toFile,
      capacity max 0,
      Settings.get.bufferChanges,
    )

    if (isLocked)
      fileSystemTrait = FileSystemAPI.asReadOnly(fileSystemTrait)

    FileSystemAPI.asManagedEnvironment(address.get, fileSystemTrait, new ReadWriteLabel(), speed, activityType.orNull)
  }

  private def generateAndSetEnvironment(): Unit = _fileSystem = generateEnvironment()

  // -------------------------------- DiskRealPathAware --------------------------------

  override def customRealPath_=(value: Option[Path]): Unit = {
    super.customRealPath_=(value)

    generateAndSetEnvironment()
  }

  // -------------------------------- Disk --------------------------------

  def saveToNbtAndLoad(): Unit = {
    if (_fileSystem == null)
      return

    // save changes
    val nbt = new NBTTagCompound()
    fileSystem.save(nbt)
    // regenerate filesystem instance
    generateAndSetEnvironment()
    // restore parameters
    _fileSystem.load(nbt, workspace)
  }

  override def onLockChange(oldLockInfo: String): Unit = {
    super.onLockChange(oldLockInfo)

    // do no touch the file system without need
    if (isLocked(oldLockInfo) != isLocked)
      saveToNbtAndLoad()
  }

  // -------------------------------- Persistable --------------------------------

  private val FileSystemTag = "fs"

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)

    if (_fileSystem != null) {
      val fsNbt = new NBTTagCompound
      _fileSystem.save(fsNbt)
      nbt.setTag(FileSystemTag, fsNbt)
    }
  }

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    envLock = true

    super.load(nbt, workspace)

    if (nbt.hasKey(FileSystemTag)) {
      // Obtaining address first
      val nodeNbt = nbt.getCompoundTag(Environment.NodeTag)
      address = Option(nodeNbt.getString(Node.AddressTag))

      // Then we can safely generate environment & fill it with loaded data
      val fsNbt = nbt.getCompoundTag(FileSystemTag)
      fileSystem.load(fsNbt, workspace)
    }
    else {
      // Without NBT we just generating new environment with new address
      generateAndSetEnvironment()
    }

    envLock = false
  }
}
