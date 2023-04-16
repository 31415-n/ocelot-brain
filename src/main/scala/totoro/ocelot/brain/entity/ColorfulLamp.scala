package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.{Entity, Environment}
import totoro.ocelot.brain.network.{Component, Network, Visibility}

class ColorfulLamp extends Entity with Environment {
  override val node: Component = Network.newNode(this, Visibility.Network).
    withComponent("colorful_lamp", Visibility.Network).
    create()

  var color: Int = 0x6318

  @Callback(doc = "function(color:number):boolean; Sets the lamp color; Set to 0 to turn the off the lamp; Returns true on success")
  def setLampColor(context: Context, args: Arguments): Array[AnyRef] = {
    if (args.checkInteger(0) >= 0 && args.checkInteger(0) <= 0xFFFF) {
      color = args.checkInteger(0) & 0x7FFF
      return result(true)
    }

    result(false, "number must be between 0 and 32767")
  }

  @Callback(doc = "function():number; Returns the current lamp color", direct = true)
  def getLampColor(context: Context, args: Arguments): Array[AnyRef] = {
    result(color)
  }
}
