package li.cil.oc.server.component

import java.util

import li.cil.oc.api.driver.DeviceInfo
import li.cil.oc.api.driver.DeviceInfo.{DeviceAttribute, DeviceClass}
import li.cil.oc.api.machine.{Arguments, Callback, Context}
import li.cil.oc.api.network.{Component, EnvironmentHost, Visibility}
import li.cil.oc.api.prefab.AbstractManagedEnvironment
import li.cil.oc.{Constants, Settings, api}

import scala.collection.convert.WrapAsJava._
import scala.language.existentials

class Geolyzer(val host: EnvironmentHost) extends AbstractManagedEnvironment with traits.WorldControl with DeviceInfo {
  override val node: Component = api.Network.newNode(this, Visibility.Network).
    withComponent("geolyzer").
    create()

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Generic,
    DeviceAttribute.Description -> "Geolyzer",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "Terrain Analyzer MkII",
    DeviceAttribute.Capacity -> Settings.get.geolyzerRange.toString
  )

  override def getDeviceInfo: util.Map[String, String] = deviceInfo

  // ----------------------------------------------------------------------- //

  private def canSeeSky: Boolean = true

  @Callback(doc = """function():boolean -- Returns whether there is a clear line of sight to the sky directly above.""")
  def canSeeSky(computer: Context, args: Arguments): Array[AnyRef] = {
    result(canSeeSky)
  }

  @Callback(doc = """function():boolean -- Return whether the sun is currently visible directly above.""")
  def isSunVisible(computer: Context, args: Arguments): Array[AnyRef] = {
    result(true)
  }

  @Callback(doc = """function(x:number, z:number[, y:number, w:number, d:number, h:number][, ignoreReplaceable:boolean|options:table]):table -- Analyzes the density of the column at the specified relative coordinates.""")
  def scan(computer: Context, args: Arguments): Array[AnyRef] = {
    val (minX, minY, minZ, maxX, maxY, maxZ, _) = getScanArgs(args)
    val volume = (maxX - minX + 1) * (maxZ - minZ + 1) * (maxY - minY + 1)
    if (volume > 64) throw new IllegalArgumentException("volume too large (maximum is 64)")
    else result(Array.fill[Float](64)(0))
  }

  private def getScanArgs(args: Arguments) = {
    val minX = args.checkInteger(0)
    val minZ = args.checkInteger(1)
    if (args.isInteger(2) && args.isInteger(3) && args.isInteger(4) && args.isInteger(5)) {
      val minY = args.checkInteger(2)
      val w = args.checkInteger(3)
      val d = args.checkInteger(4)
      val h = args.checkInteger(5)
      val maxX = minX + w - 1
      val maxY = minY + h - 1
      val maxZ = minZ + d - 1

      (math.min(minX, maxX), math.min(minY, maxY), math.min(minZ, maxZ),
        math.max(minX, maxX), math.max(minY, maxY), math.max(minZ, maxZ),
        6)
    }
    else {
      (minX, -32, minZ, minX, 31, minZ, 2)
    }
  }

  @Callback(doc = """function(side:number[,options:table]):table -- Get some information on a directly adjacent block.""")
  def analyze(computer: Context, args: Arguments): Array[AnyRef] = if (Settings.get.allowItemStackInspection)
    result(new util.HashMap[String, Object]())
  else result(Unit, "not enabled in config")

  @Callback(doc = """function(side:number, dbAddress:string, dbSlot:number):boolean -- Store an item stack representation of the block on the specified side in a database component.""")
  def store(computer: Context, args: Arguments): Array[AnyRef] = {
    result(Unit, "block has no registered item representation")
  }
}
