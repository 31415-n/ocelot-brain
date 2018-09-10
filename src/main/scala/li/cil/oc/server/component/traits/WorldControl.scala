package li.cil.oc.server.component.traits

import li.cil.oc.api.machine.{Arguments, Callback, Context}
import li.cil.oc.util.ResultWrapper.result

trait WorldControl {
  @Callback(doc = "function(side:number):boolean, string -- Checks the contents of the block on the specified sides and returns the findings.")
  def detect(context: Context, args: Arguments): Array[AnyRef] = {
    result(false, "no inventory")
  }
}
