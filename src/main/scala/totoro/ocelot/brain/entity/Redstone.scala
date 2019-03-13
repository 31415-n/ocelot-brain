package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{DeviceInfo, Tiered}
import totoro.ocelot.brain.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.network.{Network, Node, Visibility}
import totoro.ocelot.brain.util.Tier
import totoro.ocelot.brain.{Constants, Settings}

object Redstone {
  class Tier1 extends Environment with DeviceInfo with Tiered {
    override val node: Node = Network.newNode(this, Visibility.Neighbors).
      withComponent("redstone", Visibility.Neighbors).
      create()

    private final lazy val deviceInfo = Map(
      DeviceAttribute.Class -> DeviceClass.Communication,
      DeviceAttribute.Description -> "Redstone controller",
      DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
      DeviceAttribute.Product -> "Rx900-M",
      DeviceAttribute.Capacity -> "16",
      DeviceAttribute.Width -> "1"
    )

    override def getDeviceInfo: Map[String, String] = deviceInfo

    override var tier: Int = Tier.One

    // ----------------------------------------------------------------------- //

    val redstoneOutput = new Array[Int](6)
    val redstoneInput = new Array[Int](6)

    @Callback(direct = true, doc = """function(side:number):number -- Get the redstone input on the specified side.""")
    def getInput(context: Context, args: Arguments): Array[AnyRef] = {
      val side = checkSide(args, 0)
      result(redstoneInput(side))
    }

    @Callback(direct = true, doc = """function(side:number):number -- Get the redstone output on the specified side.""")
    def getOutput(context: Context, args: Arguments): Array[AnyRef] = {
      val side = checkSide(args, 0)
      result(redstoneOutput(side))
    }

    @Callback(doc = """function(side:number, value:number):number -- Set the redstone output on the specified side.""")
    def setOutput(context: Context, args: Arguments): Array[AnyRef] = {
      val side = checkSide(args, 0)
      val value = args.checkInteger(1)
      if (Settings.get.redstoneDelay > 0)
        context.pause(Settings.get.redstoneDelay)
      redstoneOutput(side) = value
      result(redstoneOutput(side))
    }

    @Callback(direct = true, doc = """function(side:number):number -- Get the comparator input on the specified side.""")
    def getComparatorInput(context: Context, args: Arguments): Array[AnyRef] = {
      checkSide(args, 0)
      result(0)
    }

    protected def checkSide(args: Arguments, index: Int): Int = {
      val side = args.checkInteger(index)
      if (side < 0 || side > 5)
        throw new IllegalArgumentException("invalid side")
      side
    }
  }

  class Tier2 extends Tier1 {
    override val node: Node = Network.newNode(this, Visibility.Neighbors).
      withComponent("redstone", Visibility.Neighbors).
      create()

    private final lazy val deviceInfo = Map(
      DeviceAttribute.Class -> DeviceClass.Communication,
      DeviceAttribute.Description -> "Advanced redstone controller",
      DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
      DeviceAttribute.Product -> "Rx900-MX",
      DeviceAttribute.Capacity -> "65536",
      DeviceAttribute.Width -> "16"
    )

    override def getDeviceInfo: Map[String, String] = deviceInfo

    tier = Tier.Two

    // ----------------------------------------------------------------------- //

    val bundledRedstoneOutput: Array[Array[Int]] = Array.ofDim[Int](6, 16)
    val bundledRedstoneInput: Array[Array[Int]] = Array.ofDim[Int](6, 16)

    @Callback(direct = true, doc = "function(side:number[, color:number]):number or table " +
      "-- Get the bundled redstone input on the specified side and with the specified color.")
    def getBundledInput(context: Context, args: Arguments): Array[AnyRef] = {
      val side = checkSide(args, 0)
      if (args.optAny(1, null) == null)
        result(bundledRedstoneInput(side).zipWithIndex.map(_.swap).toMap)
      else
        result(bundledRedstoneInput(side)(checkColor(args, 1)))
    }

    @Callback(direct = true, doc = "function(side:number[, color:number]):number or table " +
      "-- Get the bundled redstone output on the specified side and with the specified color.")
    def getBundledOutput(context: Context, args: Arguments): Array[AnyRef] = {
      val side = checkSide(args, 0)
      if (args.optAny(1, null) == null)
        result(bundledRedstoneOutput(side).zipWithIndex.map(_.swap).toMap)
      else
        result(bundledRedstoneOutput(side)(checkColor(args, 1)))
    }

    @Callback(doc = "function(side:number, color:number, value:number):number " +
      "-- Set the bundled redstone output on the specified side and with the specified color.")
    def setBundledOutput(context: Context, args: Arguments): Array[AnyRef] = {
      val side = checkSide(args, 0)
      if (args.isTable(1)) {
        val table = args.checkTable(1)
        (0 to 15).map(color => (color, table.get(color))).foreach {
          case (color, number: Number) => bundledRedstoneOutput(side)(color) = number.intValue()
          case _ =>
        }
        if (Settings.get.redstoneDelay > 0)
          context.pause(Settings.get.redstoneDelay)
        result(true)
      }
      else {
        val color = checkColor(args, 1)
        val value = args.checkInteger(2)
        bundledRedstoneOutput(side)(color) = value
        if (Settings.get.redstoneDelay > 0)
          context.pause(Settings.get.redstoneDelay)
        result(bundledRedstoneOutput(side)(color))
      }
    }

    private def checkColor(args: Arguments, index: Int): Int = {
      val color = args.checkInteger(index)
      if (color < 0 || color > 15)
        throw new IllegalArgumentException("invalid color")
      color
    }
  }
}
