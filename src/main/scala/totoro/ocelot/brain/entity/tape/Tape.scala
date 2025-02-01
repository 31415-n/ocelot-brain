package totoro.ocelot.brain.entity.tape

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.tape.Tape.{KindTag, LabelTag, StorageTag}
import totoro.ocelot.brain.entity.traits.WorkspaceAware
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.workspace.Workspace

import scala.annotation.unused

class Tape(var kind: Tape.Kind, private var storageName: Option[String] = None)
  extends traits.Tape
    with WorkspaceAware {

  def this() = this(Tape.Kind.Iron, None)

  var label = ""

  def size: Int = Tape.size(kind)

  def lengthMinutes: Float = Tape.lengthMinutes(kind)

  def storageId: Option[String] = Option.when(workspace != null)(storage.uniqueId)

  // NOTE: be careful: this method will blow up if `workspace` is unset
  override def storage: traits.TapeStorage = {
    val existingStorage = storageName
      .filter(workspace.tapeStorage.exists)
      .map(workspace.tapeStorage.get(_, size, 0))

    existingStorage match {
      case Some(storage) => storage

      case None =>
        val storage = workspace.tapeStorage.newStorage(size)
        storageName = Some(storage.uniqueId)

        storage
    }
  }

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)

    label = nbt.getString(LabelTag)
    kind = Tape.Kind(nbt.getInteger(KindTag))
    storageName = Option.when(nbt.hasKey(StorageTag))(nbt.getString(StorageTag))
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)

    nbt.setString(LabelTag, label)
    nbt.setInteger(KindTag, kind.id)

    for (storageName <- storageName) {
      nbt.setString(StorageTag, storageName)
    }
  }
}

object Tape {
  private val LabelTag = "label"
  private val KindTag = "size"
  private val StorageTag = "storage"

  type Kind = Kind.Value

  object Kind extends Enumeration {
    @unused("actually used but indirectly")
    val Iron, Gold, Golder, Diamond, NetherStar, Copper, Steel, Greg, NetherStarrer, Ig = Value
  }

  def size(kind: Kind): Int = Settings.get.tapeSizes(kind.id)

  def lengthMinutes(kind: Kind): Float = size(kind) / Settings.TapeMinuteSize
}
