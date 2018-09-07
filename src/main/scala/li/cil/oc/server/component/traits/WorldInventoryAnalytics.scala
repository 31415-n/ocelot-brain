package li.cil.oc.server.component.traits

import li.cil.oc.Settings
import li.cil.oc.api.machine.Arguments
import li.cil.oc.api.machine.Callback
import li.cil.oc.api.machine.Context
import li.cil.oc.server.component.result

trait WorldInventoryAnalytics extends NetworkAware {
  @Callback(doc = """function(side:number):number -- Get the number of slots in the inventory on the specified side of the device.""")
  def getInventorySize(context: Context, args: Arguments): Array[AnyRef] = {
    result(Unit, "no inventory")
  }

  @Callback(doc = """function(side:number, slot:number):number -- Get number of items in the specified slot of the inventory on the specified side of the device.""")
  def getSlotStackSize(context: Context, args: Arguments): Array[AnyRef] = {
    result(Unit, "no inventory")
  }

  @Callback(doc = """function(side:number, slot:number):number -- Get the maximum number of items in the specified slot of the inventory on the specified side of the device.""")
  def getSlotMaxStackSize(context: Context, args: Arguments): Array[AnyRef] = {
    result(Unit, "no inventory")
  }

  @Callback(doc = """function(side:number, slotA:number, slotB:number[, checkNBT:boolean=false]):boolean -- Get whether the items in the two specified slots of the inventory on the specified side of the device are of the same type.""")
  def compareStacks(context: Context, args: Arguments): Array[AnyRef] = {
    result(Unit, "no inventory")
  }

  @Callback(doc = """function(side:number, slot:number, dbAddress:string, dbSlot:number[, checkNBT:boolean=false]):boolean -- Compare an item in the specified slot in the inventory on the specified side with one in the database with the specified address.""")
  def compareStackToDatabase(context: Context, args: Arguments): Array[AnyRef] = {
    result(Unit, "no inventory")
  }

  @Callback(doc = """function(side:number, slotA:number, slotB:number):boolean -- Get whether the items in the two specified slots of the inventory on the specified side of the device are equivalent (have shared OreDictionary IDs).""")
  def areStacksEquivalent(context: Context, args: Arguments): Array[AnyRef] = {
    result(Unit, "no inventory")
  }

  @Callback(doc = """function(side:number, slot:number):table -- Get a description of the stack in the inventory on the specified side of the device.""")
  def getStackInSlot(context: Context, args: Arguments): Array[AnyRef] = if (Settings.get.allowItemStackInspection) {
    result(Unit, "no inventory")
  }
  else result(Unit, "not enabled in config")

  @Callback(doc = """function(side:number):userdata -- Get a description of all stacks in the inventory on the specified side of the device.""")
  def getAllStacks(context: Context, args: Arguments): Array[AnyRef] = if (Settings.get.allowItemStackInspection) {
    result(Unit, "no inventory")
  }
  else result(Unit, "not enabled in config")

  @Callback(doc = """function(side:number):string -- Get the the name of the inventory on the specified side of the device.""")
  def getInventoryName(context: Context, args: Arguments): Array[AnyRef] = if (Settings.get.allowItemStackInspection) {
    result(Unit, "no inventory")
  }
  else result(Unit, "not enabled in config")

  @Callback(doc = """function(side:number, slot:number, dbAddress:string, dbSlot:number):boolean -- Store an item stack description in the specified slot of the database with the specified address.""")
  def store(context: Context, args: Arguments): Array[AnyRef] = {
    result(Unit, "no inventory")
  }
}
