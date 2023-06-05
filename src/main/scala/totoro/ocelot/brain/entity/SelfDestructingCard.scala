package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{DeviceInfo, Entity, Environment, Tiered}
import totoro.ocelot.brain.event.{EventBus, SelfDestructingCardBoomEvent}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.{Component, Network, Visibility}
import totoro.ocelot.brain.util.Tier
import totoro.ocelot.brain.util.Tier.Tier
import totoro.ocelot.brain.workspace.Workspace

class SelfDestructingCard extends Entity with Environment with DeviceInfo with Tiered {
  override val node: Component =
    Network
    .newNode(this, Visibility.Network)
    .withComponent("self_destruct", Visibility.Neighbors)
    .create()

  override def tier: Tier = Tier.One

  // --------------------------- Time countdown ---------------------------

  private var time: Int = -1

  @Callback(doc = "function([time:number]):number; Starts the countdown; Will be ticking down until the time is reached. 5 seconds by default. Returns the time set")
  def start(context: Context, args: Arguments): Array[AnyRef] = {
    if (time >= 0)
      return result(-1, "fuse has already been set")

    val fuse = args.optDouble(0, 5)

    if (fuse > 100000)
      throw new IllegalArgumentException("time may not be greater than 100000")

    time = Math.floor(fuse * 20).round.toInt

    result(fuse)
  }

  @Callback(doc = "function():number; Returns the time in seconds left", direct = true)
  def time(context: Context, args: Arguments): Array[AnyRef] = {
    if (time < 0)
      return result(-1, "fuse has not been set")

    result(time.toDouble / 20d)
  }

  override def update(): Unit = {
    super.update()

    if (time < 0)
      return

    // No boom :(
    if (time > 0) {
      time -= 1
    }
    // Boom!
    else {
      EventBus.send(SelfDestructingCardBoomEvent(node.address))
    }
  }

  // --------------------------- NBT ---------------------------

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)

    if (nbt.getBoolean("ticking"))
      time = nbt.getInteger("time")
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)

    if (time >= 0) {
      nbt.setBoolean("ticking", true)
      nbt.setInteger("time", time)
    }
    else {
      nbt.setBoolean("ticking", false)
    }
  }

  // --------------------------- Miscellaneous ---------------------------

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Generic,
    DeviceAttribute.Description -> "Machine destruction service",
    DeviceAttribute.Vendor -> "Hugging Creeper Industries",
    DeviceAttribute.Product -> "SD-Struct 1"
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo
}
