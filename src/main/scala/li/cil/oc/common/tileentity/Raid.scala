package li.cil.oc.common.tileentity

import java.util.UUID

import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.api.Driver
import li.cil.oc.api.fs.Label
import li.cil.oc.api.network.{Node, Visibility}
import li.cil.oc.common.Slot
import li.cil.oc.common.item.data.NodeData
import li.cil.oc.server.component.FileSystem
import li.cil.oc.util.ExtendedNBT._
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

class Raid extends traits.Environment with traits.Inventory {
  val node: Node = api.Network.newNode(this, Visibility.None).create()

  var filesystem: Option[FileSystem] = None

  val label = new RaidLabel()

  // ----------------------------------------------------------------------- //

  override def getSizeInventory = 3

  override def getInventoryStackLimit = 1

  override def isItemValidForSlot(slot: Int, stack: ItemStack): Boolean = Option(Driver.driverFor(stack, getClass)) match {
    case Some(driver) => driver.slot(stack) == Slot.HDD
    case _ => false
  }

  override protected def onItemAdded(slot: Int, stack: ItemStack) {
    super.onItemAdded(slot, stack)
    this.synchronized {
      tryCreateRaid(UUID.randomUUID().toString)
    }
  }

  override protected def onItemRemoved(slot: Int, stack: ItemStack) {
    super.onItemRemoved(slot, stack)
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

  def tryCreateRaid(id: String) {
    if (items.count(!_.isEmpty) == items.length && filesystem.fold(true)(fs => fs.node == null || fs.node.address != id)) {
      filesystem.foreach(fs => if (fs.node != null) fs.node.remove())
      val fs = api.FileSystem.asManagedEnvironment(
        api.FileSystem.fromSaveDirectory(id, wipeDisksAndComputeSpace, Settings.get.bufferChanges),
        label, this, Settings.resourceDomain + ":hdd_access", 6).
        asInstanceOf[FileSystem]
      val nbtToSetAddress = new NBTTagCompound()
      nbtToSetAddress.setString(NodeData.AddressTag, id)
      fs.node.load(nbtToSetAddress)
      fs.node.setVisibility(Visibility.Network)
      // Ensure we're in a network before connecting the raid fs.
      api.Network.joinNewNetwork(node)
      node.connect(fs.node)
      filesystem = Option(fs)
    }
  }

  private def wipeDisksAndComputeSpace = items.foldLeft(0L) {
    case (acc, hdd) if !hdd.isEmpty => acc + (Option(api.Driver.driverFor(hdd)) match {
      case Some(driver) => driver.createEnvironment(hdd, this) match {
        case fs: FileSystem =>
          val nbt = driver.dataTag(hdd)
          fs.load(nbt)
          fs.fileSystem.close()
          fs.fileSystem.list("/").foreach(fs.fileSystem.delete)
          fs.save(nbt)
          fs.fileSystem.spaceTotal.toInt
        case _ => 0L // Ignore.
      }
      case _ => 0L
    })
    case (acc, stack) if stack.isEmpty => acc
  }

  // ----------------------------------------------------------------------- //

  private final val FileSystemTag = Settings.namespace + "fs"

  override def readFromNBT(nbt: NBTTagCompound) {
    super.readFromNBT(nbt)
    if (nbt.hasKey(FileSystemTag)) {
      val tag = nbt.getCompoundTag(FileSystemTag)
      tryCreateRaid(tag.getCompoundTag(NodeData.NodeTag).getString(NodeData.AddressTag))
      filesystem.foreach(fs => fs.load(tag))
    }
    label.load(nbt)
  }

  override def writeToNBT(nbt: NBTTagCompound) {
    super.writeToNBT(nbt)
    filesystem.foreach(fs => nbt.setNewCompoundTag(FileSystemTag, fs.save))
    label.save(nbt)
  }

  // ----------------------------------------------------------------------- //

  class RaidLabel extends Label {
    var label = "raid"

    override def getLabel: String = label

    override def setLabel(value: String): Unit = label = Option(value).map(_.take(16)).orNull

    override def load(nbt: NBTTagCompound) {
      if (nbt.hasKey(Settings.namespace + "label")) {
        label = nbt.getString(Settings.namespace + "label")
      }
    }

    override def save(nbt: NBTTagCompound) {
      nbt.setString(Settings.namespace + "label", label)
    }
  }
}
