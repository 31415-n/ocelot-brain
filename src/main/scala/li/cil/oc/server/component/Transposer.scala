package li.cil.oc.server.component

import java.util

import li.cil.oc.Constants
import li.cil.oc.api.driver.DeviceInfo.DeviceAttribute
import li.cil.oc.api.driver.DeviceInfo.DeviceClass
import li.cil.oc.api
import li.cil.oc.api.driver.DeviceInfo
import li.cil.oc.api.network.{Component, Visibility}
import li.cil.oc.api.prefab.AbstractManagedEnvironment

import scala.collection.convert.WrapAsJava._
import scala.language.existentials

class Transposer extends AbstractManagedEnvironment with traits.WorldInventoryAnalytics
    with traits.WorldTankAnalytics with traits.InventoryTransfer with DeviceInfo {

  override val node: Component = api.Network.newNode(this, Visibility.Network).
    withComponent("transposer").
    create()

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Generic,
    DeviceAttribute.Description -> "Transposer",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "TP4k-iX"
  )

  override def getDeviceInfo: util.Map[String, String] = deviceInfo
}
