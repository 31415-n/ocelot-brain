package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.result
import totoro.ocelot.brain.network.{Component, Network, Visibility}

trait GenericCamera extends Environment {
  override val node: Component = Network.newNode(this, Visibility.Network).
    withComponent("camera", Visibility.Network).
    create()

  @Callback(direct = true, doc = "function([x:number, y:number]):number; " +
    "Returns the distance to the block the ray is shot at with the specified x-y offset, " +
    "or if the block directly in front")
  def distance(context: Context, args: Arguments): Array[AnyRef] = {
    result(-1)
  }
}