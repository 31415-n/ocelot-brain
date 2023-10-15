package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Ocelot
import totoro.ocelot.brain.entity.TapeDrive.{PacketSizeTag, SoundVolumeTag, StateTag}
import totoro.ocelot.brain.entity.TapeDriveState.State
import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.tape.traits.Tape
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{DeviceInfo, Entity, Environment, Inventory}
import totoro.ocelot.brain.event.{EventBus, TapeEjectEvent, TapeInsertEvent}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.{Network, Node, Visibility}
import totoro.ocelot.brain.workspace.Workspace

class TapeDrive extends Entity with Environment with DeviceInfo with Inventory {
  override val node: Node = Network
    .newNode(this, Visibility.Network)
    .withComponent("tape_drive", Visibility.Network)
    .create()

  // a bunch of getters
  def tape: Option[Tape] = inventory(0).get.map(_.asInstanceOf[Tape])

  private val _state = new TapeDriveState()

  def state: TapeDriveState = _state

  def storageName: String = tape.map(_.label).getOrElse("")

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Tape,
    DeviceAttribute.Description -> "Tape drive",
    DeviceAttribute.Vendor -> "Yanaki Sound Systems",
    DeviceAttribute.Product -> "DFPWM 1",
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo

  // properties

  private def enumState: TapeDriveState.State = state.state

  def setSpeed(speed: Float): Unit = {
    state.setSpeed(speed)
  }

  def setVolume(vol: Float): Unit = {
    state.setVolume(vol)
  }

  def isEnd: Boolean =
    state.storage.exists(storage => storage.position + state.packetSize > storage.size)

  def isReady: Boolean = state.storage.nonEmpty

  def size: Int = state.storage.map(_.size).getOrElse(0)

  def position: Int = state.storage.map(_.position).getOrElse(0)

  // i/o

  def seek(bytes: Int): Int = state.storage.map(_.seek(bytes)).getOrElse(0)

  def read(): Int = read(false)

  def read(simulate: Boolean): Int =
    state.storage.map(_.read(simulate) & 0xff).getOrElse(0)

  def read(amount: Int): Array[Byte] = state.storage match {
    case Some(storage) =>
      val data = Array.ofDim[Byte](amount)
      storage.read(data, simulate = false)
      data

    case None => Array()
  }

  def write(b: Byte): Unit = state.storage.foreach(_.write(b))

  def write(bytes: Array[Byte]): Int = state.storage.map(_.write(bytes)).getOrElse(0)

  // lifecycle

  override def needUpdate: Boolean = true

  override def update(): Unit = {
    super.update()
    state.update()
  }

  override def dispose(): Unit = {
    unloadStorage()
    super.dispose()
  }

  // storage loading/unloading

  private def loadStorage(): Unit = {
    if (state.storage.isDefined) {
      unloadStorage()
    }

    state.storage = tape.map(_.storage)
  }

  private def saveStorage(): Unit = state.storage.foreach(_.save())

  private def unloadStorage(): Unit = if (tape.isDefined) {
    state.switchState(State.Stopped)

    try {
      state.storage.get.onStorageUnload()
    } catch {
      case e: Exception => Ocelot.log.error("Storage.onStorageUnload failed", e)
    }

    state.storage = None
  }

  // inventory management

  override def onEntityAdded(slot: Slot, entity: Entity): Unit = {
    super.onEntityAdded(slot, entity)

    loadStorage()

    if (tape.isDefined) {
      EventBus.send(TapeInsertEvent(node.address))
    }
  }

  override def onEntityRemoved(slot: Slot, entity: Entity): Unit = {
    super.onEntityRemoved(slot, entity)

    if (tape.isDefined) {
      EventBus.send(TapeEjectEvent(node.address))
    }

    unloadStorage()
  }

  // persistence

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)

    if (nbt.hasKey(StateTag)) {
      state.state = State(nbt.getByte(StateTag))
    }

    if (nbt.hasKey(PacketSizeTag)) {
      state.packetSize = nbt.getShort(PacketSizeTag)
    }

    state.volume =
      if (nbt.hasKey(SoundVolumeTag)) nbt.getByte(SoundVolumeTag)
      else 127

    loadStorage()
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)

    nbt.setByte(StateTag, state.state.id.toByte)
    nbt.setShort(PacketSizeTag, state.packetSize.toShort)

    if (state.volume != 127) {
      nbt.setByte(SoundVolumeTag, state.volume)
    }

    saveStorage()
  }

  // callbacks

  @Callback(
    doc = "function():boolean; Returns true if the tape drive is empty or the inserted tape has reached its end",
    direct = true,
  )
  def isEnd(context: Context, args: Arguments): Array[AnyRef] = {
    result(isEnd)
  }

  @Callback(doc = "function():boolean; Returns true if there is a tape inserted", direct = true)
  def isReady(context: Context, args: Arguments): Array[AnyRef] = {
    result(isReady)
  }

  @Callback(doc = "function():number; Returns the size of the tape, in bytes", direct = true)
  def getSize(context: Context, args: Arguments): Array[AnyRef] = {
    result(size)
  }

  @Callback(doc = "function():number; Returns the position of the tape, in bytes", direct = true)
  def getPosition(context: Context, args: Arguments): Array[AnyRef] = {
    result(position)
  }

  @Callback(
    doc = "function(label:string):string; Sets the label of the tape. " +
      "Returns the new label, or nil if there is no tape inserted"
  )
  def setLabel(context: Context, args: Arguments): Array[AnyRef] = {
    for (tape <- tape) {
      tape.label = args.checkString(0)
    }

    result(tape.map(_.label).orNull)
  }

  @Callback(doc = "function():string; Returns the current label of the tape, or nil if there is no tape inserted")
  def getLabel(context: Context, args: Arguments): Array[AnyRef] =
    result(tape.map(_.label).orNull)

  @Callback(doc = "function(length:number):number; Seeks the specified amount of bytes on the tape. " +
    "Negative values for rewinding. Returns the amount of bytes sought, or nil if there is no tape inserted")
  def seek(context: Context, args: Arguments): Array[AnyRef] = {
    if (state.storage.isDefined) {
      result(seek(args.checkInteger(0)))
    } else {
      result()
    }
  }

  @Callback(doc = "function([length:number]):string; " +
    "Reads and returns the specified amount of bytes or a single byte from the tape. " +
    "Returns nil if there is no tape inserted")
  def read(context: Context, args: Arguments): Array[AnyRef] = {
    if (state.storage.isDefined) {
      if (args.count() >= 1 && args.isInteger(0) && args.checkInteger(0) >= 0) {
        result(read(args.checkInteger(0)))
      } else {
        result(read())
      }
    } else {
      result()
    }
  }

  @Callback(doc = "function(data:number or string); Writes the specified data to the tape if there is one inserted")
  def write(context: Context, args: Arguments): Array[AnyRef] = {
    if (state.storage.isDefined && args.count() >= 1) {
      if (args.isInteger(0)) {
        write(args.checkInteger(0).toByte)
      } else if (args.isByteArray(0)) {
        write(args.checkByteArray(0))
      } else {
        // sic.
        throw new IllegalArgumentException("bad arguments #1 (number of string expected)")
      }
    }

    result()
  }

  @Callback(doc = "function():boolean; Make the Tape Drive start playing the tape. Returns true on success")
  def play(context: Context, args: Arguments): Array[AnyRef] = {
    state.switchState(State.Playing)

    result(state.storage.isDefined && enumState == State.Playing)
  }

  @Callback(doc = "function():boolean; Make the Tape Drive stop playing the tape. Returns true on success")
  def stop(context: Context, args: Arguments): Array[AnyRef] = {
    state.switchState(State.Stopped)

    result(state.storage.isDefined && enumState == State.Stopped)
  }

  @Callback(doc = "function(speed:number):boolean; Sets the speed of the tape drive. Needs to be beween 0.25 and 2. " +
    "Returns true on success")
  def setSpeed(context: Context, args: Arguments): Array[AnyRef] = {
    result(state.setSpeed(args.checkDouble(0).toFloat))
  }

  @Callback(doc = "function(speed:number); Sets the volume of the tape drive. Needs to be beween 0 and 1")
  def setVolume(context: Context, args: Arguments): Array[AnyRef] = {
    state.setVolume(args.checkDouble(0).toFloat)
    result()
  }

  @Callback(doc = "function():string; Returns the current state of the tape drive", direct = true)
  def getState(context: Context, args: Arguments): Array[AnyRef] = {
    result(state.state.name)
  }
}

object TapeDrive {
  private val StateTag = "state"
  private val PacketSizeTag = "sp"
  private val SoundVolumeTag = "vo"
}
