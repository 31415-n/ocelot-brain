package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.traits.{Entity, Hub, RackMountable, StateAware}
import totoro.ocelot.brain.nbt.ExtendedNBT.{extendNBTTagCompound, extendNBTTagList, toNbt}
import totoro.ocelot.brain.nbt.{NBT, NBTTagCompound, NBTTagIntArray}
import totoro.ocelot.brain.network
import totoro.ocelot.brain.network._
import totoro.ocelot.brain.util.Direction
import totoro.ocelot.brain.workspace.Workspace

import scala.collection.mutable

class Rack
  extends Entity
  with traits.Rack
  with traits.ComponentInventory
  with Hub
  with StateAware
{
  def getSizeInventory = 4

  var isRelayEnabled = false
  val lastData = new Array[NBTTagCompound](getSizeInventory)
  val hasChanged: Array[Boolean] = Array.fill(getSizeInventory)(true)

  // Map node connections for each installed mountable. Each mountable may
  // have up to four outgoing connections, with the first one always being
  // the "primary" connection, i.e. being a direct connection allowing
  // component access (i.e. actually connecting to that side of the rack).
  // The other nodes are "secondary" connections and merely transfer network
  // messages.
  // mountable -> connectable -> side
  val nodeMapping: Array[Array[Option[Direction.Value]]] = Array.fill(getSizeInventory)(Array.fill[Option[Direction.Value]](4)(None))
  val snifferNodes: Array[Array[network.Node]] = Array.fill(getSizeInventory)(Array.fill(3)(Network.newNode(this, Visibility.Neighbors).create()))

  def connect(slot: Int, connectableIndex: Int, side: Option[Direction.Value]): Unit = {
    val newSide = side match {
      case Some(direction) if direction != Direction.South => Option(direction)
      case _ => None
    }

    val oldSide = nodeMapping(slot)(connectableIndex + 1)
    if (oldSide == newSide)
      return

    // Cut connection / remove sniffer node.
    val mountable = getMountable(slot)

    if (mountable != null && oldSide.isDefined) {
      if (connectableIndex == -1) {
        val node = mountable.node
        val plug = sidedNode(oldSide.get)

        if (node != null && plug != null) {
          node.disconnect(plug)
        }
      }
      else if (connectableIndex >= 0) {
        snifferNodes(slot)(connectableIndex - 1).remove()
      }
    }

    nodeMapping(slot)(connectableIndex + 1) = newSide

    // Establish connection / add sniffer node.
    if (mountable != null && newSide.isDefined) {
      if (connectableIndex == -1) {
        val node = mountable.node
        val plug = sidedNode(newSide.get)

        if (node != null && plug != null) {
          node.connect(plug)
        }
      }
      else if (connectableIndex >= 0 && connectableIndex < mountable.getConnectableCount) {
        val connectable = mountable.getConnectableAt(connectableIndex)
        if (connectable != null && connectable.node != null) {
          if (connectable.node.network == null) {
            Network.joinNewNetwork(connectable.node)
          }

          connectable.node.connect(snifferNodes(slot)(connectableIndex - 1))
        }
      }
    }
  }

  private def reconnect(plugSide: Direction.Value): Unit = {
    for (slot <- 0 until getSizeInventory) {
      val mapping = nodeMapping(slot)
      mapping(0) match {
        case Some(side) if side == plugSide =>
          val mountable = getMountable(slot)
          val busNode = sidedNode(plugSide)
          if (busNode != null && mountable != null && mountable.node != null && busNode != mountable.node) {
            Network.joinNewNetwork(mountable.node)
            busNode.connect(mountable.node)
          }
        case _ => // Not connected to this side.
      }
      for (connectableIndex <- 0 until 3) {
        mapping(connectableIndex + 1) match {
          case Some(side) if side == plugSide =>
            val mountable = getMountable(slot)
            if (mountable != null && connectableIndex < mountable.getConnectableCount) {
              val connectable = mountable.getConnectableAt(connectableIndex)
              if (connectable != null && connectable.node != null) {
                if (connectable.node.network == null) {
                  Network.joinNewNetwork(connectable.node)
                }
                connectable.node.connect(snifferNodes(slot)(connectableIndex - 1))
              }
            }
          case _ => // Not connected to this side.
        }
      }
    }
  }

  override def onEntityAdded(slot: Slot, entity: Entity): Unit = {
    if (!isLoading) {
      for (connectable <- 0 until 4) {
        nodeMapping(slot.index)(connectable) = None
      }
      lastData(slot.index) = null
      hasChanged(slot.index) = true
    }

    super.onEntityAdded(slot, entity)
  }

  override def onEntityRemoved(slot: Slot, entity: Entity): Unit = {
    if (!isLoading) {
      for (connectable <- 0 until 4) {
        nodeMapping(slot.index)(connectable) = None
      }
      lastData(slot.index) = null
    }

    super.onEntityRemoved(slot, entity)
  }

  override protected def connectItemNode(node: Node): Unit = {
    // By default create a new network for mountables. They have to
    // be wired up manually (mapping is reset in onItemAdded).

    // Implementation note: in ocelot-brain, node.remove() does not set network to null,
    // but creates a new network and assigns the node to it instead.
    // This is not at all how it works in OpenComputers, and it's a bit of a slippery slope in this respect.
    // To safeguard against possible future changes, we're doing an explicit joinNewNetwork,
    // even though currently (as in, at the time of writing this) it does nothing.
    Network.joinNewNetwork(node)
  }

  protected def sendPacketToMountables(sourceSide: Option[Direction.Value], packet: Packet): Unit = {
    // When a message arrives on a bus, also send it to all secondary nodes
    // connected to it. Only deliver it to that very node, if it's not the
    // sender, to avoid loops.
    for (slot <- 0 until getSizeInventory) {
      val mapping = nodeMapping(slot)
      for (connectableIndex <- 0 until 3) {
        mapping(connectableIndex + 1) match {
          case Some(side) if sourceSide.contains(side) =>
            val mountable = getMountable(slot)
            if (mountable != null && connectableIndex < mountable.getConnectableCount) {
              val connectable = mountable.getConnectableAt(connectableIndex)
              if (connectable != null) {
                connectable.receivePacket(packet)
              }
            }
          case _ => // Not connected to a bus.
        }
      }
    }
  }

  // ----------------------------------------------------------------------- //
  // Hub

  override def tryEnqueuePacket(sourceSide: Option[Direction.Value], packet: Packet): Boolean = {
    sendPacketToMountables(sourceSide, packet)
    if (isRelayEnabled)
      super.tryEnqueuePacket(sourceSide, packet)
    else
      true
  }

  override protected def relayPacket(sourceSide: Option[Direction.Value], packet: Packet): Unit = {
    if (isRelayEnabled)
      super.relayPacket(sourceSide, packet)
  }

  override protected def onPlugConnect(plug: Plug, node: network.Node): Unit = {
    super.onPlugConnect(plug, node)

    connectComponents()
    reconnect(plug.side)
  }

//  override val node: Component = Network.newNode(this, Visibility.Network).
//    withComponent("rack").
//    create()

  // ----------------------------------------------------------------------- //
  // Environment

  override def dispose(): Unit = {
    super.dispose()
    disconnectComponents()
  }

  override def onMessage(message: Message): Unit = {
    super.onMessage(message)
    if (message.name == "network.message") message.data match {
      case Array(packet: Packet) => relayIfMessageFromConnectable(message, packet)
      case _ =>
    }
  }

  private def relayIfMessageFromConnectable(message: Message, packet: Packet): Unit = {
    for (slot <- 0 until getSizeInventory) {
      val mountable = getMountable(slot)
      if (mountable != null) {
        val mapping = nodeMapping(slot)
        for (connectableIndex <- 0 until 3) {
          mapping(connectableIndex + 1) match {
            case Some(side) =>
              if (connectableIndex < mountable.getConnectableCount) {
                val connectable = mountable.getConnectableAt(connectableIndex)
                if (connectable != null && connectable.node == message.source) {
                  sidedNode(side).sendToReachable("network.message", packet)
                  relayToConnectablesOnSide(message, packet, side)
                  return
                }
              }
            case _ => // Not connected to a bus.
          }
        }
      }
    }
  }

  private def relayToConnectablesOnSide(message: Message, packet: Packet, sourceSide: Direction.Value): Unit = {
    for (slot <- 0 until getSizeInventory) {
      val mountable = getMountable(slot)
      if (mountable != null) {
        val mapping = nodeMapping(slot)
        for (connectableIndex <- 0 until 3) {
          mapping(connectableIndex + 1) match {
            case Some(side) if side == sourceSide =>
              if (connectableIndex < mountable.getConnectableCount) {
                val connectable = mountable.getConnectableAt(connectableIndex)
                if (connectable != null && connectable.node != message.source) {
                  snifferNodes(slot)(connectableIndex - 1).sendToNeighbors("network.message", packet)
                }
              }
            case _ => // Not connected to a bus.
          }
        }
      }
    }
  }
//
//  // ----------------------------------------------------------------------- //
//  // SidedEnvironment
//
//  override def canConnect(side: Direction.Value): Boolean = side != facing
//
//  override def sidedNode(side: Direction.Value): network.Node = if (side != facing) super.sidedNode(side) else null

  // ----------------------------------------------------------------------- //
  // internal.Rack

  override def indexOfMountable(mountable: RackMountable): Int =
    inventory.iterator.indexWhere(slot => slot.get.contains(mountable))

  override def getMountable(slot: Int): RackMountable = inventory(slot).get match {
    case Some(mountable: RackMountable) => mountable
    case _ => null
  }

  override def getMountableData(slot: Int): NBTTagCompound = lastData(slot)

  override def markChanged(slot: Int): Unit = {
    hasChanged.synchronized(hasChanged(slot) = true)
  }

  // ----------------------------------------------------------------------- //
  // StateAware

  override def getCurrentState: Set[StateAware.State.Value] = {
    val result = new mutable.HashSet[StateAware.State.Value]

    inventory.iterator.collect(slot => {
      slot.get match {
        case Some(mountable: RackMountable) => result.addAll(mountable.getCurrentState)
      }
    })

    result.toSet
  }

  // ----------------------------------------------------------------------- //

  private final val IsRelayEnabledTag = "isRelayEnabled"
  private final val NodeMappingTag = "nodeMapping"
  private final val LastDataTag = "lastData"

  private var isLoading: Boolean = false

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    isLoading = true

    super.load(nbt, workspace)

    isRelayEnabled = nbt.getBoolean(IsRelayEnabledTag)
    nbt.getTagList(NodeMappingTag, NBT.TAG_INT_ARRAY).map((buses: NBTTagIntArray) =>
      buses.getIntArray.map(id => if (id < 0 || id == Direction.South.id) None else Option(Direction(id)))).
      copyToArray(nodeMapping)

    val data = nbt.getTagList(LastDataTag, NBT.TAG_COMPOUND).
      toArray[NBTTagCompound]
    data.copyToArray(lastData)

    connectComponents()

    isLoading = false
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)

    nbt.setBoolean(IsRelayEnabledTag, isRelayEnabled)
    nbt.setNewTagList(NodeMappingTag, nodeMapping.map(buses =>
      toNbt(buses.map(side => side.fold(-1)(_.id)))))

    val data = lastData.map(tag => if (tag == null) new NBTTagCompound() else tag)
    nbt.setNewTagList(LastDataTag, data)
  }

  def isWorking(mountable: RackMountable): Boolean = mountable.getCurrentState.contains(StateAware.State.IsWorking)
}
