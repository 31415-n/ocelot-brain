package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{DeviceInfo, Entity, Environment, Tiered}
import totoro.ocelot.brain.network.{Network, Node, Visibility}
import totoro.ocelot.brain.util.{Direction, Tier}
import totoro.ocelot.brain.{Constants, Settings}

object Redstone {
  class Tier1 extends Entity with Environment with DeviceInfo with Tiered {
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

    @Callback(direct = true, doc = """function([side:number]):number or table -- Get the redstone input (all sides, or optionally on the specified side)""")
    def getInput(context: Context, args: Arguments): Array[AnyRef] = {
      getOptionalSide(args) match {
        case Some(side: Int) => result(redstoneInput(side))
        case _ => result(valuesToMap(redstoneInput))
      }
    }

    @Callback(direct = true, doc = """function([side:number]):number or table -- Get the redstone output (all sides, or optionally on the specified side)""")
    def getOutput(context: Context, args: Arguments): Array[AnyRef] = {
      getOptionalSide(args) match {
        case Some(side: Int) => result(redstoneOutput(side))
        case _ => result(valuesToMap(redstoneOutput))
      }
    }

    @Callback(doc = """function([side:number, ]value:number or table):number or table -- Set the redstone output (all sides, or optionally on the specified side). Returns previous values""")
    def setOutput(context: Context, args: Arguments): Array[AnyRef] = {
      var ret: AnyRef = null
      getAssignment(args) match {
        case (side: Direction.Value, value: Int) =>
          ret = java.lang.Integer.valueOf(redstoneOutput(side.id))
          redstoneOutput(side.id) = value
        case (value: Map[Int, Int]@unchecked, _) =>
          ret = valuesToMap(redstoneOutput)
          value.foreach(item => redstoneOutput(item._1) = item._2)
      }
      if (Settings.get.redstoneDelay > 0)
        context.pause(Settings.get.redstoneDelay)
      result(ret)
    }

    @Callback(direct = true, doc = """function(side:number):number -- Get the comparator input on the specified side.""")
    def getComparatorInput(context: Context, args: Arguments): Array[AnyRef] = {
      checkSide(args, 0)
      result(0)
    }

    protected def getOptionalSide(args: Arguments): Option[Int] = {
      if (args.count == 1)
        Option(checkSide(args, 0))
      else
        None
    }

    protected def getAssignment(args: Arguments): (Any, Any) = {
      args.count() match {
        case 2 => (checkSide(args, 0), args.checkInteger(1))
        case 1 => (args.checkTable(0), null)
        case _ => throw new Exception("invalid number of arguments, expected 1 or 2")
      }
    }

    protected def checkSide(args: Arguments, index: Int): Int = {
      val side = args.checkInteger(index)
      if (side < 0 || side > 5)
        throw new IllegalArgumentException("invalid side")
      side
    }

    private def valuesToMap(ar: Array[Int]): Map[Int, Int] =
      Direction.values.map(_.id).map { case side if side < ar.length => side -> ar(side) }.toMap
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

    private val COLOR_RANGE = 0 until 16

    val bundledRedstoneOutput: Array[Array[Int]] = Array.ofDim[Int](6, 16)
    val bundledRedstoneInput: Array[Array[Int]] = Array.ofDim[Int](6, 16)

    private def getBundleKey(args: Arguments): (Option[Int], Option[Int]) = {
      args.count() match {
        case 2 => (Option(checkSide(args, 0)), Option(checkColor(args, 1)))
        case 1 => (Option(checkSide(args, 0)), None)
        case 0 => (None, None)
        case _ => throw new Exception("too many arguments, expected 0, 1, or 2")
      }
    }

    private def tableToColorValues(table: Map[Int, Int]): Array[Int] = {
      COLOR_RANGE.collect {
        case color: Int if table.contains(color) => table(color)
      }.toArray
    }

    private def colorsToMap(ar: Array[Int]): Map[Int, Int] = {
      COLOR_RANGE.map{
        case color if color < ar.length => color -> ar(color)
      }.toMap
    }

    private def sidesToMap(ar: Array[Array[Int]]): Map[Int, Map[Int, Int]] = {
      Direction.values.unsorted.map {
        case side if side.id < ar.length && ar(side.id).length > 0 => side.id -> colorsToMap(ar(side.id))
      }.toMap
    }

    private def getBundleAssignment(args: Arguments): (Any, Any, Any) = {
      args.count() match {
        case 3 => (checkSide(args, 0), checkColor(args, 1), args.checkInteger(2))
        case 2 => (checkSide(args, 0), args.checkTable(1), null)
        case 1 => (args.checkTable(0), null, null)
        case _ => throw new Exception("invalid number of arguments, expected 1, 2, or 3")
      }
    }

    @Callback(direct = true, doc = "function([side:number[, color:number]]):number or table -- Fewer params returns set of inputs")
    def getBundledInput(context: Context, args: Arguments): Array[AnyRef] = {
      val (side, color) = getBundleKey(args)

      if (color.isDefined) {
        result(bundledRedstoneInput(side.get)(color.get))
      } else if (side.isDefined) {
        result(colorsToMap(bundledRedstoneInput(side.get)))
      } else {
        result(sidesToMap(bundledRedstoneInput))
      }
    }

    @Callback(direct = true, doc = "function([side:number[, color:number]]):number or table -- Fewer params returns set of outputs")
    def getBundledOutput(context: Context, args: Arguments): Array[AnyRef] = {
      val (side, color) = getBundleKey(args)

      if (color.isDefined) {
        result(bundledRedstoneOutput(side.get)(color.get))
      } else if (side.isDefined) {
        result(colorsToMap(bundledRedstoneOutput(side.get)))
      } else {
        result(sidesToMap(bundledRedstoneOutput))
      }
    }

    @Callback(doc = "function([side:number[, color:number,]] value:number or table):number or table --  Fewer params to assign set of outputs. Returns previous values")
    def setBundledOutput(context: Context, args: Arguments): Array[AnyRef] = {
      var ret: Any = null
      getBundleAssignment(args) match {
        case (side: Int, color: Int, value: Int) =>
          ret = bundledRedstoneOutput(side)(color)
          bundledRedstoneOutput(side)(color) = value
        case (side: Int, value: Map[Int, Int]@unchecked, _) =>
          ret = bundledRedstoneOutput(side)
          value.foreach(color => bundledRedstoneOutput(side)(color._1) = color._2)
        case (value: Map[Int, Map[Int, Int]]@unchecked, _, _) =>
          ret = bundledRedstoneOutput
          value.foreach(side => side._2.foreach(color => bundledRedstoneOutput(side._1)(color._1) = color._2))
      }
      if (Settings.get.redstoneDelay > 0)
        context.pause(Settings.get.redstoneDelay)
      result(ret)
    }

    private def checkColor(args: Arguments, index: Int): Int = {
      val color = args.checkInteger(index)
      if (!COLOR_RANGE.contains(color))
        throw new IllegalArgumentException("invalid color")
      color
    }
  }
}
