package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Constants
import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{DeviceInfo, Entity, Environment}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.{Component, Network, Visibility}
import totoro.ocelot.brain.workspace.Workspace

class ColorfulLamp extends Entity with Environment with DeviceInfo {
  override val node: Component = Network.newNode(this, Visibility.Network).
    withComponent("colorful_lamp", Visibility.Network).
    create()

  var color: Int = 0x6318
  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Communication,
    DeviceAttribute.Description -> "RGB Lamp 1x1x1",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "xLED-228"
  )

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

  override def getDeviceInfo: Map[String, String] = deviceInfo

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)
    if (nbt.hasKey("color"))
      color = nbt.getInteger("color")
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    nbt.setInteger("color", color)
  }
}
