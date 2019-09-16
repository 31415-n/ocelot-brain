package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Constants
import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{ComponentInventory, DeviceInfo, Entity, Environment}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.{Component, Network, Node, Visibility}

class FloppyDiskDrive extends Entity with Environment with ComponentInventory with DeviceInfo {
  val node: Component = Network.newNode(this, Visibility.Network).
    withComponent("disk_drive").
    create()

  def filesystemNode: Option[Node] = inventory.head match {
    case environment: Environment => Option(environment.node)
    case _ => None
  }

  // ----------------------------------------------------------------------- //

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Disk,
    DeviceAttribute.Description -> "Floppy disk drive",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "Exponat #266"
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo

  // ----------------------------------------------------------------------- //

  @Callback(doc = """function():boolean -- Checks whether some medium is currently in the drive.""")
  def isEmpty(context: Context, args: Arguments): Array[AnyRef] = {
    result(filesystemNode.isEmpty)
  }

  @Callback(doc = """function([velocity:number]):boolean -- Eject the currently present medium from the drive.""")
  def eject(context: Context, args: Arguments): Array[AnyRef] = {
    result(clear())
  }

  @Callback(doc = """function(): string -- Return the internal floppy disk address""")
  def media(context: Context, args: Arguments): Array[AnyRef] = {
    if (filesystemNode.isEmpty)
      result((), "drive is empty")
    else
      result(filesystemNode.head.address)
  }

  // ----------------------------------------------------------------------- //

  override def onEntityAdded(entity: Entity) {
    super.onEntityAdded(entity)
    entity match {
      case environment: Environment => environment.node match {
        case component: Component => component.setVisibility(Visibility.Network)
      }
      case _ =>
    }
  }

  // ----------------------------------------------------------------------- //

  private final val DiskTag = "disk"

  override def load(nbt: NBTTagCompound) {
    super.load(nbt)
    if (nbt.hasKey(DiskTag)) {
      // TODO: read the floppy from NBT here
    }
  }

  override def save(nbt: NBTTagCompound) {
    super.save(nbt)
    if (inventory.nonEmpty) {
      // TODO: save the floppy to NBT here
    }
  }
}
