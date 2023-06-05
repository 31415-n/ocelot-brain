package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.util.Tier.Tier

/**
 * This is implemented by most things that are tiered in some way.
 *
 * For example, this is implemented by screens, computer cases, robots and
 * drones as well as microcontrollers. If you want you can add tier specific
 * behavior this way.
 */
trait Tiered {
  def tier: Tier
}
