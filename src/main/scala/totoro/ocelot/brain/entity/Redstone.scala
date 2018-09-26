package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Constants
import totoro.ocelot.brain.entity.traits.DeviceInfo
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.network.{Network, Node, Visibility}

class Redstone extends Environment with DeviceInfo {
  override val node: Node = Network.newNode(this, Visibility.Neighbors).
    withComponent("redstone", Visibility.Neighbors).
    create()

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Communication,
    DeviceAttribute.Description -> "Combined redstone controller",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "Rx900-M",
    DeviceAttribute.Capacity -> "65536",
    DeviceAttribute.Width -> "16"
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo

  // ----------------------------------------------------------------------- //

  val redstoneOutput = new Array[Int](6)
  val redstoneInput = new Array[Int](6)

  @Callback(doc = """function(side:number, value:number):number -- Set the redstone output on the specified side.""")
  def setOutput(context: Context, args: Arguments): Array[AnyRef] = this.synchronized {
    val power = args.checkInteger(1)
    val side = args.checkInteger(0)
    redstoneOutput(side) = power
    result()
  }

  @Callback(doc = """function(side:number):number -- Get the redstone output on the specified side.""")
  def getOutput(context: Context, args: Arguments): Array[AnyRef] = this.synchronized {
    val side = args.checkInteger(0)
    result(redstoneOutput(side))
  }

  @Callback(doc = """function(side:number):number -- Get the redstone input on the specified side.""")
  def getInput(context: Context, args: Arguments): Array[AnyRef] = this.synchronized {
    val side = args.checkInteger(0)
    result(redstoneInput(side))
  }
}
