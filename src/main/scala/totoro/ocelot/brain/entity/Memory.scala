package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.Memory.TierTag
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{CallBudget, DeviceInfo, Entity, Environment, Tiered, TieredPersistable}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.{Network, Node, Visibility}
import totoro.ocelot.brain.util.ExtendedTier.ExtendedTier
import totoro.ocelot.brain.util.Tier.Tier
import totoro.ocelot.brain.util.{ExtendedTier, Tier}
import totoro.ocelot.brain.workspace.Workspace
import totoro.ocelot.brain.{Constants, Settings}

class Memory(var memoryTier: ExtendedTier)
  extends Entity
    with Environment
    with DeviceInfo
    with Tiered
    with traits.Memory
    with CallBudget {

  override val node: Node = Network.newNode(this, Visibility.Neighbors).
    create()

  override def tier: Tier = memoryTier.toTier

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Memory,
    DeviceAttribute.Description -> "Memory bank",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> s"MRAM 1x${memoryTier.id}",
    DeviceAttribute.Clock -> (Settings.get.callBudgets(tier.id) * 1000).toInt.toString
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo

  override def amount: Double = Settings.get.ramSizes(memoryTier.id)

  override def callBudget: Double = Settings.get.callBudgets(tier.id max Tier.One.id min Tier.Three.id)

  // Cannot use the implementation from TieredPersistable because we need the extended tier
  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)

    memoryTier = ExtendedTier(nbt.getByte(TierTag))
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)

    nbt.setByte(TierTag, memoryTier.id.toByte)
  }
}

object Memory {
  val TierTag: String = TieredPersistable.TierTag
}
