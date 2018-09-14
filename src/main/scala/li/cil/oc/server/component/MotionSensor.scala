package li.cil.oc.server.component

import java.util

import li.cil.oc.api.driver.DeviceInfo
import li.cil.oc.api.driver.DeviceInfo.{DeviceAttribute, DeviceClass}
import li.cil.oc.api.machine.{Arguments, Callback, Context}
import li.cil.oc.api.network.{Component, EnvironmentHost, Visibility}
import li.cil.oc.api.prefab
import li.cil.oc.{Constants, Settings, api}
import net.minecraft.nbt.NBTTagCompound

import scala.collection.convert.WrapAsJava._

class MotionSensor(val host: EnvironmentHost) extends prefab.AbstractManagedEnvironment with DeviceInfo {
  override val node: Component = api.Network.newNode(this, Visibility.Network).
    withComponent("motion_sensor").
    create()

  private val radius = 8

  private var sensitivity = 0.4

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Generic,
    DeviceAttribute.Description -> "Motion sensor",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "Blinker M1K0",
    DeviceAttribute.Capacity -> radius.toString
  )

  override def getDeviceInfo: util.Map[String, String] = deviceInfo

  // ----------------------------------------------------------------------- //

  private def isServer: Boolean = true

  override def canUpdate: Boolean = isServer

  // ----------------------------------------------------------------------- //

  @Callback(direct = true, doc = """function():number -- Gets the current sensor sensitivity.""")
  def getSensitivity(computer: Context, args: Arguments): Array[AnyRef] = result(sensitivity)

  @Callback(direct = true, doc = """function(value:number):number -- Sets the sensor's sensitivity. Returns the old value.""")
  def setSensitivity(computer: Context, args: Arguments): Array[AnyRef] = {
    val oldValue = sensitivity
    sensitivity = math.max(0.2, args.checkDouble(0))
    result(oldValue)
  }

  // ---------------------------------------------------------------------- //

  private final val SensitivityTag = Settings.namespace + "sensitivity"

  override def load(nbt: NBTTagCompound) {
    super.load(nbt)
    sensitivity = nbt.getDouble(SensitivityTag)
  }

  override def save(nbt: NBTTagCompound) {
    super.save(nbt)
    nbt.setDouble(SensitivityTag, sensitivity)
  }

  // ----------------------------------------------------------------------- //
}
