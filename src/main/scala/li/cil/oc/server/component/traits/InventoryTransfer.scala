package li.cil.oc.server.component.traits

import li.cil.oc.api.machine.{Arguments, Callback, Context}
import li.cil.oc.server.component._

trait InventoryTransfer {
  @Callback(doc = """function(sourceSide:number, sinkSide:number[, count:number[, sourceSlot:number[, sinkSlot:number]]]):boolean -- Transfer some items between two inventories.""")
  def transferItem(context: Context, args: Arguments): Array[AnyRef] = {
    result(Unit, "not implemented yet")
  }

  @Callback(doc = """function(sourceSide:number, sinkSide:number[, count:number]):number -- Transfer some items between two inventories.""")
  def transferFluid(context: Context, args: Arguments): Array[AnyRef] = {
    result(Unit, "not implemented yet")
  }
}
