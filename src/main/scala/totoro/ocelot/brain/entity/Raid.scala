package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.fs.{FileSystem, FileSystemAPI, Label}
import totoro.ocelot.brain.entity.traits.{DiskActivityAware, DiskManaged, DiskRealPathAware, Entity, Environment, Inventory}
import totoro.ocelot.brain.event.FileSystemActivityType
import totoro.ocelot.brain.nbt.ExtendedNBT.extendNBTTagCompound
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.{Network, Node, Visibility}
import totoro.ocelot.brain.workspace.Workspace

import java.nio.file.Path
import java.util.UUID

class Raid
  extends Entity
    with Environment
    with Inventory
    with DiskRealPathAware
    with DiskActivityAware
{
  // Forge stuff
  final val getSizeInventory = 3

  override val node: Node = Network.newNode(this, Visibility.None).create()

  var filesystem: Option[FileSystem] = None
  val label = new RaidLabel()

  private def tryCreateRaid(raidAddress: String): Unit = {
    if (inventory.iterator.count(!_.isEmpty) == getSizeInventory && filesystem.fold(true)(fs => fs.node == null || fs.node.address != raidAddress)) {
      filesystem.foreach(fs => {
        if (fs.node != null)
          fs.node.remove()
      })

      val realPath = getRealOrDefaultPath(raidAddress)

      val fs = FileSystemAPI.asManagedEnvironment(
        FileSystemAPI.fromDirectory(
          realPath.toFile,
          wipeDisksAndComputeSpace,
          Settings.get.bufferChanges
        ),
        label,
        6,
        FileSystemActivityType.HDD
      )

      val nbtToSetAddress = new NBTTagCompound()
      nbtToSetAddress.setString("address", raidAddress)
      fs.node.load(nbtToSetAddress)
      fs.node.setVisibility(Visibility.Network)
      // Ensure we're in a network before connecting the raid fs.
      Network.joinNewNetwork(node)
      node.connect(fs.node)

      filesystem = Option(fs)
    }
  }

  private def tryCreateRaid(): Unit = tryCreateRaid(UUID.randomUUID().toString)

  private def wipeDisksAndComputeSpace: Long = inventory.iterator.foldLeft(0L)((acc, slot) => {
    acc + (slot.get match {
      case Some(disk: DiskManaged) =>
        val fs = disk.fileSystem.fileSystem

        // IDK if NBT saving is required here or not
        // fs.load(nbt)
        fs.close()
        fs.list("/").foreach(fs.delete)
        // fs.save(nbt)

        fs.spaceTotal
      case _ => 0L
    })
  })

  // -------------------------------- DiskRealPathAware --------------------------------

  override def customRealPath_=(value: Option[Path]): Unit = {
    super.customRealPath_=(value)

    if (isLoading)
      return

    tryCreateRaid()
  }

  // -------------------------------- Inventory --------------------------------

  override def onEntityAdded(slot: Slot, entity: Entity): Unit = {
    super.onEntityAdded(slot, entity)

    if (isLoading)
      return

    this.synchronized {
      tryCreateRaid()
    }
  }

  override def onEntityRemoved(slot: Slot, entity: Entity): Unit = {
    super.onEntityRemoved(slot, entity)

    this.synchronized {
      filesystem.foreach(fs => {
        fs.fileSystem.close()
        fs.fileSystem.list("/").foreach(fs.fileSystem.delete)
        fs.save(new NBTTagCompound()) // Flush buffered fs.
        fs.node.remove()

        filesystem = None
      })
    }
  }

  // -------------------------------- Persistable --------------------------------

  private var isLoading: Boolean = false

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    isLoading = true

    super.load(nbt, workspace)

    if (nbt.hasKey(Settings.namespace + "fs")) {
      val tag = nbt.getCompoundTag(Settings.namespace + "fs")
      val address = tag.getCompoundTag("node").getString("address")
      tryCreateRaid(address)
      filesystem.foreach(fs => fs.load(tag, workspace))
    }

    // Label
    label.load(nbt, workspace)
    label.setLabel(nbt.getString("label"))

    isLoading = false
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)

    filesystem.foreach(fs => nbt.setNewCompoundTag(Settings.namespace + "fs", fs.save))

    // Label
    label.save(nbt)

    if (label.getLabel != null)
      nbt.setString("label", label.getLabel)
  }

  // -------------------------------- Label --------------------------------

  class RaidLabel extends Label {
    var label = "raid"

    override def getLabel: String = label

    override def setLabel(value: String): Unit = label = Option(value).map(_.take(16)).orNull

    override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
      if (nbt.hasKey(Settings.namespace + "label")) {
        label = nbt.getString(Settings.namespace + "label")
      }
    }

    override def save(nbt: NBTTagCompound): Unit = {
      nbt.setString(Settings.namespace + "label", label)
    }
  }
}