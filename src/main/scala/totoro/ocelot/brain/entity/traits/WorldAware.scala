package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.util.World

trait WorldAware {
  /**
    * The world the entity lives in.
    */
  def world: World = World.Default
}
