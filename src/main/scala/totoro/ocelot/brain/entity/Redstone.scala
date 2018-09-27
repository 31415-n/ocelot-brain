package totoro.ocelot.brain.entity

import totoro.ocelot.brain.Constants
import totoro.ocelot.brain.entity.traits.{DeviceInfo, Tiered}
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.network.{Network, Node, Visibility}
import totoro.ocelot.brain.util.Tier

object Redstone {
  class Tier1 extends Environment with DeviceInfo with Tiered {
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

    var tier = Tier.One

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

  class Tier2 extends Tier1 with DeviceInfo with Tiered {
    override val node: Node = Network.newNode(this, Visibility.Neighbors).
      withComponent("redstone", Visibility.Neighbors).
      create()

    private final lazy val deviceInfo = Map(
      DeviceAttribute.Class -> DeviceClass.Communication,
      DeviceAttribute.Description -> "Combined redstone controller",
      DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
      DeviceAttribute.Product -> "Rx900-MX",
      DeviceAttribute.Capacity -> "65536",
      DeviceAttribute.Width -> "16"
    )

    override def getDeviceInfo: Map[String, String] = deviceInfo

    tier = Tier.Two

    // ----------------------------------------------------------------------- //

    val bundledRestoneOutput = Array.ofDim[Int](6, 15)
    val bundledRestoneInput = Array.ofDim[Int](6, 15)

    @Callback
    def getBundledInput(context: Context, args: Arguments): Array[AnyRef] = this.synchronized {
      val side = args.checkInteger(0)
      val color = args.checkInteger(1)
      result(bundledRestoneInput(side)(color))
    }

    @Callback
    def getBundledOutput(context: Context, args: Arguments): Array[AnyRef] = this.synchronized {
      val side = args.checkInteger(0)
      val color = args.checkInteger(1)
      result(bundledRestoneOutput(side)(color))
    }

    @Callback
    def setBundledInput(context: Context, args: Arguments): Array[AnyRef] = this.synchronized {
      val side = args.checkInteger(0)
      val color = args.checkInteger(1)
      val power = args.checkInteger(2)
      bundledRestoneOutput(side)(color) = power
      result()
    }

  }
}
