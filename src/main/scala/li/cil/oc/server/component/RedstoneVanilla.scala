package li.cil.oc.server.component

import java.util

import li.cil.oc.Constants
import li.cil.oc.api.driver.DeviceInfo
import li.cil.oc.api.driver.DeviceInfo.{DeviceAttribute, DeviceClass}
import li.cil.oc.api.machine.{Arguments, Callback, Context}
import li.cil.oc.api.network.{EnvironmentHost, _}
import li.cil.oc.common.tileentity.traits.{RedstoneAware, RedstoneChangedEventArgs}

import scala.collection.convert.WrapAsJava._

trait RedstoneVanilla extends RedstoneSignaller with DeviceInfo {
  def redstone: EnvironmentHost with RedstoneAware

  // ----------------------------------------------------------------------- //

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Communication,
    DeviceAttribute.Description -> "Redstone controller",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "Rs100-V",
    DeviceAttribute.Capacity -> "16",
    DeviceAttribute.Width -> "1"
  )

  override def getDeviceInfo: util.Map[String, String] = deviceInfo

  // ----------------------------------------------------------------------- //

  @Callback(direct = true, doc = """function(side:number):number -- Get the redstone input on the specified side.""")
  def getInput(context: Context, args: Arguments): Array[AnyRef] = {
    result(0)
  }

  @Callback(direct = true, doc = """function(side:number):number -- Get the redstone output on the specified side.""")
  def getOutput(context: Context, args: Arguments): Array[AnyRef] = {
    result(0)
  }

  @Callback(doc = """function(side:number, value:number):number -- Set the redstone output on the specified side.""")
  def setOutput(context: Context, args: Arguments): Array[AnyRef] = {
    result(0)
  }

  @Callback(direct = true, doc = """function(side:number):number -- Get the comparator input on the specified side.""")
  def getComparatorInput(context: Context, args: Arguments): Array[AnyRef] = {
    result(0)
  }

  // ----------------------------------------------------------------------- //

  override def onMessage(message: Message): Unit = {
    super.onMessage(message)
    if (message.name == "redstone.changed") message.data match {
      case Array(args: RedstoneChangedEventArgs) =>
        onRedstoneChanged(args)
      case _ =>
    }
  }
}
