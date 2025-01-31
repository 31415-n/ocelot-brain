package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.util.Tier.Tier

/**
  * Implemented by entities that have a meaningful tier.
  *
  * @see TieredPersistable
  */
trait Tiered {
  def tier: Tier
}
