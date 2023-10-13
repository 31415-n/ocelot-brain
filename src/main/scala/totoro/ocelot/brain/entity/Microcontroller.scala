package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Constants
import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{Computer, DeviceInfo, Hub, TieredPersistable}
import totoro.ocelot.brain.nbt.ExtendedNBT.{extendNBTTagCompound, extendNBTTagList}
import totoro.ocelot.brain.nbt.{NBT, NBTTagCompound}
import totoro.ocelot.brain.network._
import totoro.ocelot.brain.util.Direction
import totoro.ocelot.brain.util.Direction.Direction
import totoro.ocelot.brain.util.Tier.Tier
import totoro.ocelot.brain.workspace.Workspace

class Microcontroller(override var tier: Tier)
  extends Computer
    with traits.Microcontroller
    with Hub
    with DeviceInfo
    with TieredPersistable
{
  // Forge stuff
  final val getSizeInventory: Int = inventory.iterator.length
  final val facing: Direction.Value = Direction.Front

  // Internal nodes to disable interaction with external components
  // The only exception is network packets handling
  val outputSides: Array[Boolean] = Array.fill(6)(true)

  val snooperNode: Node = Network.newNode(this, Visibility.Network)
    .withComponent("microcontroller")
    .create()

  val componentNodes: Array[Component] = Array.fill(6)(Network.newNode(this, Visibility.Network)
    .withComponent("microcontroller")
    .create())

  // ---------------------------- DeviceInfo ----------------------------

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.System,
    DeviceAttribute.Description -> "Microcontroller",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "Cubicle",
    DeviceAttribute.Capacity -> getSizeInventory.toString
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo

  // ---------------------------- Hub ----------------------------

  // unlike the overridden method, this does NOT assign a random address to the newly created node.
  // (tho I don't see how this would matter, I'd rather stick to what OC does.)
  override protected def createNode(plug: Plug): Node = Network.newNode(plug, Visibility.Network).create()

  override protected def onPlugConnect(plug: Plug, node: Node): Unit = {
    super.onPlugConnect(plug, node)

    if (node == plug.node) {
      Network.joinNewNetwork(machine.node)
      machine.node.connect(snooperNode)
      connectComponents()
    }

    if (plug.isPrimary)
      plug.node.connect(componentNodes(plug.side.id))
    else
      componentNodes(plug.side.id).remove()
  }

  override protected def onPlugDisconnect(plug: Plug, node: Node): Unit = {
    super.onPlugDisconnect(plug, node)

    if (plug.isPrimary && node != plug.node)
      plug.node.connect(componentNodes(plug.side.id))
    else
      componentNodes(plug.side.id).remove()

    if (node == plug.node)
      disconnectComponents()
  }

  override protected def onPlugMessage(plug: Plug, message: Message): Unit = {
    if (message.name == "network.message" && message.source.network != snooperNode.network) {
      snooperNode.sendToReachable(message.name, message.data: _*)
    }
  }

  // ---------------------------- ComponentInventory ----------------------------

  override protected def connectItemNode(node: Node): Unit = {
    if (machine != null && machine.node != null && node != null) {
      Network.joinNewNetwork(machine.node)
      machine.node.connect(node)
    }
  }

  // ---------------------------- Environment ----------------------------

  override def onMessage(message: Message): Unit = {
    if (message.name == "network.message" && message.source.network == snooperNode.network) {
      for (side <- Direction.values if outputSides(side.id) && side != facing) {
        sidedNode(side).sendToReachable(message.name, message.data: _*)
      }
    }
  }

  override def dispose(): Unit = {
    super.dispose()

    disconnectComponents()
  }

  // ---------------------------- Persistable ----------------------------

  private final val OutputsTag = "outputs"
  private final val ComponentNodesTag = "componentNodes"
  private final val SnooperTag = "snooper"

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    nbt.getBooleanArray(OutputsTag)

    nbt
      .getTagList(ComponentNodesTag, NBT.TAG_COMPOUND)
      .toArray[NBTTagCompound]
      .zipWithIndex
      .foreach {
        case (tag, index) => componentNodes(index).load(tag)
      }

    snooperNode.load(nbt.getCompoundTag(SnooperTag))

    super.load(nbt, workspace)

    Network.joinNewNetwork(machine.node)
    machine.node.connect(snooperNode)

    connectComponents()
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)

    nbt.setBooleanArray(OutputsTag, outputSides)

    nbt.setNewTagList(ComponentNodesTag, componentNodes.map {
      case node: Node =>
        val tag = new NBTTagCompound()
        node.save(tag)
        tag
      case _ => new NBTTagCompound()
    })

    nbt.setNewCompoundTag(SnooperTag, snooperNode.save)
  }

  // ---------------------------- Callbacks ----------------------------

  @Callback(doc = """function():boolean -- Starts the microcontroller. Returns true if the state changed.""")
  def start(context: Context, args: Arguments): Array[AnyRef] =
    result(!machine.isPaused && machine.start())

  @Callback(doc = """function():boolean -- Stops the microcontroller. Returns true if the state changed.""")
  def stop(context: Context, args: Arguments): Array[AnyRef] =
    result(machine.stop())

  @Callback(direct = true, doc = """function():boolean -- Returns whether the microcontroller is running.""")
  def isRunning(context: Context, args: Arguments): Array[AnyRef] =
    result(machine.isRunning)

  @Callback(direct = true, doc = """function():string -- Returns the reason the microcontroller crashed, if applicable.""")
  def lastError(context: Context, args: Arguments): Array[AnyRef] =
    result(machine.lastError)

  @Callback(direct = true, doc = """function(side:number):boolean -- Get whether network messages are sent via the specified side.""")
  def isSideOpen(context: Context, args: Arguments): Array[AnyRef] = {
    val side = checkSide(args)
    result(outputSides(side.id))
  }

  @Callback(doc = """function(side:number, open:boolean):boolean -- Set whether network messages are sent via the specified side.""")
  def setSideOpen(context: Context, args: Arguments): Array[AnyRef] = {
    val side = checkSide(args)
    val oldValue = outputSides(side.id)
    outputSides(side.id) = args.checkBoolean(1)
    result(oldValue)
  }

  private def checkSide(args: Arguments): Direction = {
    val side = args.checkInteger(0)

    if (side < 0 || side > 5)
      throw new IllegalArgumentException("invalid side")

    val direction = Direction(side)

    if (direction == facing)
      throw new IllegalArgumentException("invalid side")

    direction
  }
}
