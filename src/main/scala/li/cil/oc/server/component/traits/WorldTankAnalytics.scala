package li.cil.oc.server.component.traits

import li.cil.oc.Settings
import li.cil.oc.api.machine.Arguments
import li.cil.oc.api.machine.Callback
import li.cil.oc.api.machine.Context
import li.cil.oc.server.component.result

trait WorldTankAnalytics {
  @Callback(doc = """function(side:number [, tank:number]):number -- Get the amount of fluid in the tank on the specified side.""")
  def getTankLevel(context: Context, args: Arguments): Array[AnyRef] = {
    result(Unit, "no tank")
  }

  @Callback(doc = """function(side:number [, tank:number]):number -- Get the capacity of the tank on the specified side.""")
  def getTankCapacity(context: Context, args: Arguments): Array[AnyRef] = {
    result(Unit, "no tank")
  }

  @Callback(doc = """function(side:number [, tank:number]):table -- Get a description of the fluid in the the tank on the specified side.""")
  def getFluidInTank(context: Context, args: Arguments): Array[AnyRef] = if (Settings.get.allowItemStackInspection) {
    result(Unit, "no tank")
  }
  else result(Unit, "not enabled in config")
}
