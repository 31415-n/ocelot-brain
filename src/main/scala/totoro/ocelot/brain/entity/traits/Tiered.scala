package totoro.ocelot.brain.entity.traits

/**
 * This is implemented by most things that are tiered in some way.
 *
 * For example, this is implemented by screens, computer cases, robots and
 * drones as well as microcontrollers. If you want you can add tier specific
 * behavior this way.
 */
trait Tiered {
  /**
   * The zero-based tier of this... thing.
   *
   * For example, a tier one screen will return 0 here, a tier three screen
   * will return 2.
   */
  var tier: Int
}
