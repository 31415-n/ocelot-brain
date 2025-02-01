package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.entity.machine.Architecture
import totoro.ocelot.brain.entity.traits.{Entity, Processor, Tiered}
import totoro.ocelot.brain.util.Tier.Tier

class ComponentBus(var tier: Tier) extends Entity with Tiered with Processor {
  override def supportedComponents: Int = Settings.get.cpuComponentSupport(tier.id)
  override def architecture: Class[_ <: Architecture] = null
}
