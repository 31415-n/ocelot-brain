package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.machine.Architecture
import totoro.ocelot.brain.entity.traits.{Entity, Environment, Processor, TieredPersistable}
import totoro.ocelot.brain.network.{Network, Node, Visibility}
import totoro.ocelot.brain.util.Tier.Tier

import java.util.UUID

class ComponentBus(var tier: Tier) extends Entity with Environment with TieredPersistable with Processor {
  // FIXME: RETARD NODE ALERT, IT CAN'T HAVE ADDRESS, BUT NEED TO BE AVAILABLE AS SLOT ITEM, FIX THIS LATER
  override val node: Node =
    Network
      .newNode(this, Visibility.None, UUID.randomUUID().toString)
      .create()

  override def supportedComponents: Int = Settings.get.cpuComponentSupport(tier.id)
  override def architecture: Class[_ <: Architecture] = null
}
