package totoro.ocelot.brain.entity

import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{DeviceInfo, Entity, Environment, Tiered}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.{Network, Node, Visibility}
import totoro.ocelot.brain.util.Tier.Tier
import totoro.ocelot.brain.util.{Direction, Tier}
import totoro.ocelot.brain.workspace.Workspace
import totoro.ocelot.brain.{Constants, Settings}

import java.util
import scala.collection.IndexedSeqView
import scala.collection.mutable.ArrayBuffer

object Redstone {
  case class RedstoneInputChanged(side: Direction.Value, oldValue: Int, newValue: Int, color: Int = -1)

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

    override val tier: Tier = Tier.One

    // ----------------------------------------------------------------------- //

    private val _redstoneOutput: Array[Int] = Array.fill(6)(0)
    private val _redstoneInput: Array[Int] = Array.fill(6)(0)

    def redstoneOutput: IndexedSeqView[Int] = _redstoneOutput.view
    def redstoneInput: IndexedSeqView[Int] = _redstoneInput.view

    def setRedstoneOutput(side: Direction.Value, value: Int): Boolean = {
      if (_redstoneOutput(side.id) != value) {
        _redstoneOutput(side.id) = value
        true
      } else false
    }

    def setRedstoneInput(side: Direction.Value, value: Int): Boolean = {
      val oldValue = _redstoneInput(side.id)

      if (oldValue != value) {
        _redstoneInput(side.id) = value
        onRedstoneInputChanged(RedstoneInputChanged(side, oldValue, value))

        true
      } else false
    }

    override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
      super.load(nbt, workspace)

      nbt.getIntArray(Tier1.RedstoneOutputTag).copyToArray(_redstoneOutput)
      nbt.getIntArray(Tier1.RedstoneInputTag).copyToArray(_redstoneInput)
    }

    override def save(nbt: NBTTagCompound): Unit = {
      super.save(nbt)

      nbt.setIntArray(Tier1.RedstoneOutputTag, _redstoneOutput)
      nbt.setIntArray(Tier1.RedstoneInputTag, _redstoneInput)
    }

    @Callback(direct = true, doc = """function([side:number]):number or table -- Get the redstone input (all sides, or optionally on the specified side)""")
    def getInput(context: Context, args: Arguments): Array[AnyRef] = {
      getOptionalSide(args) match {
        case Some(side: Int) => result(_redstoneInput(side))
        case _ => result(valuesToMap(_redstoneInput))
      }
    }

    @Callback(direct = true, doc = """function([side:number]):number or table -- Get the redstone output (all sides, or optionally on the specified side)""")
    def getOutput(context: Context, args: Arguments): Array[AnyRef] = {
      getOptionalSide(args) match {
        case Some(side: Int) => result(_redstoneOutput(side))
        case _ => result(valuesToMap(_redstoneOutput))
      }
    }

    @Callback(doc = """function([side:number, ]value:number or table):number or table -- Set the redstone output (all sides, or optionally on the specified side). Returns previous values""")
    def setOutput(context: Context, args: Arguments): Array[AnyRef] = {
      var ret: AnyRef = null
      getAssignment(args) match {
        case (side: Direction.Value, value: Int) =>
          ret = java.lang.Integer.valueOf(_redstoneOutput(side.id))
          _redstoneOutput(side.id) = value
        case (value: util.Map[_, _], _) =>
          ret = valuesToMap(_redstoneOutput)
          Direction.values.foreach(side => {
            val sideIndex = side.id
            // due to a bug in our jnlua layer, I cannot loop the map
            valueToInt(getObjectFuzzy(value, sideIndex)) match {
              case Some(num: Int) => _redstoneOutput(sideIndex) = num
              case _ =>
            }
          })
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

    protected def onRedstoneInputChanged(args: Redstone.RedstoneInputChanged): Unit = {
      val signalArgs =
        ArrayBuffer[Object]("redstone_changed", Int.box(args.side.id), Int.box(args.oldValue), Int.box(args.newValue))

      if (args.color >= 0) {
        signalArgs += Int.box(args.color)
      }

      node.sendToReachable("computer.signal", signalArgs.toSeq: _*)
    }

    protected def getOptionalSide(args: Arguments): Option[Int] = {
      if (args.count() == 1)
        Option(checkSide(args, 0).id)
      else
        None
    }

    protected def getObjectFuzzy(map: util.Map[_, _], key: Int): Option[AnyRef] = {
      val refMap: util.Map[AnyRef, AnyRef] = map.asInstanceOf[util.Map[AnyRef, AnyRef]]
      if (refMap.containsKey(key))
        Option(refMap.get(key))
      else if (refMap.containsKey(new Integer(key)))
        Option(refMap.get(new Integer(key)))
      else if (refMap.containsKey(new Integer(key) * 1.0))
        Option(refMap.get(new Integer(key) * 1.0))
      else if (refMap.containsKey(key * 1.0))
        Option(refMap.get(key * 1.0))
      else
        None
    }

    protected def valueToInt(value: AnyRef): Option[Int] = {
      value match {
        case Some(num: Number) => Option(num.intValue)
        case _ => None
      }
    }

    protected def getAssignment(args: Arguments): (Any, Any) = {
      args.count() match {
        case 2 => (checkSide(args, 0), args.checkInteger(1))
        case 1 => (args.checkTable(0), null)
        case _ => throw new Exception("invalid number of arguments, expected 1 or 2")
      }
    }

    protected def checkSide(args: Arguments, index: Int): Direction.Value = {
      val side = args.checkInteger(index)
      if (side < 0 || side > 5)
        throw new IllegalArgumentException("invalid side")
      Direction(side)
    }

    private def valuesToMap(ar: Array[Int]): Map[Int, Int] =
      Direction.values.map(_.id).map { case side if side < ar.length => side -> ar(side) }.toMap
  }

  object Tier1 {
    private val RedstoneInputTag = "redstoneInput"
    private val RedstoneOutputTag = "redstoneOutput"
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

    override val tier: Tier = Tier.Two

    // ----------------------------------------------------------------------- //

    private val COLOR_RANGE = 0 until 16

    private val _bundledRedstoneOutput: Array[Array[Int]] = Array.ofDim[Int](6, 16)
    private val _bundledRedstoneInput: Array[Array[Int]] = Array.ofDim[Int](6, 16)

    def bundledRedstoneOutput: IndexedSeqView[IndexedSeqView[Int]] = _bundledRedstoneOutput.map(_.view).view
    def bundledRedstoneInput: IndexedSeqView[IndexedSeqView[Int]] = _bundledRedstoneInput.map(_.view).view

    def setBundledOutput(side: Direction.Value, color: Int, value: Int): Boolean = {
      if (_bundledRedstoneOutput(side.id)(color) != value) {
        _bundledRedstoneOutput(side.id)(color) = value
        true
      } else false
    }

    def setBundledInput(side: Direction.Value, color: Int, value: Int): Boolean = {
      val oldValue = _bundledRedstoneInput(side.id)(color)
      if (oldValue != value) {
        _bundledRedstoneInput(side.id)(color) = value
        onRedstoneInputChanged(RedstoneInputChanged(side, oldValue, value, color))

        true
      } else false
    }

    override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
      super.load(nbt, workspace)

      val nbtOutput = nbt.getIntArray(Tier2.BundledOutputTag)
      val nbtInput = nbt.getIntArray(Tier2.BundledInputTag)

      for ((values, i) <- nbtOutput.iterator.grouped(16).zipWithIndex; (value, j) <- values.zipWithIndex) {
        _bundledRedstoneOutput(i)(j) = value
      }

      for ((values, i) <- nbtInput.iterator.grouped(16).zipWithIndex; (value, j) <- values.zipWithIndex) {
        _bundledRedstoneInput(i)(j) = value
      }
    }

    override def save(nbt: NBTTagCompound): Unit = {
      super.save(nbt)

      nbt.setIntArray(Tier2.BundledOutputTag, _bundledRedstoneOutput.flatten)
      nbt.setIntArray(Tier2.BundledInputTag, _bundledRedstoneInput.flatten)
    }

    private def getBundleKey(args: Arguments): (Option[Int], Option[Int]) = {
      args.count() match {
        case 2 => (Option(checkSide(args, 0).id), Option(checkColor(args, 1)))
        case 1 => (Option(checkSide(args, 0).id), None)
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
        result(_bundledRedstoneInput(side.get)(color.get))
      } else if (side.isDefined) {
        result(colorsToMap(_bundledRedstoneInput(side.get)))
      } else {
        result(sidesToMap(_bundledRedstoneInput))
      }
    }

    @Callback(direct = true, doc = "function([side:number[, color:number]]):number or table -- Fewer params returns set of outputs")
    def getBundledOutput(context: Context, args: Arguments): Array[AnyRef] = {
      val (side, color) = getBundleKey(args)

      if (color.isDefined) {
        result(_bundledRedstoneOutput(side.get)(color.get))
      } else if (side.isDefined) {
        result(colorsToMap(_bundledRedstoneOutput(side.get)))
      } else {
        result(sidesToMap(_bundledRedstoneOutput))
      }
    }

    @Callback(doc = "function([side:number[, color:number,]] value:number or table):number or table --  Fewer params to assign set of outputs. Returns previous values")
    def setBundledOutput(context: Context, args: Arguments): Array[AnyRef] = {
      var ret: Any = null
      getBundleAssignment(args) match {
        case (side: Direction.Value, color: Int, value: Int) =>
          ret = _bundledRedstoneOutput(side.id)(color)
          _bundledRedstoneOutput(side.id)(color) = value
        case (side: Direction.Value, value: Map[Int, Int]@unchecked, _) =>
          ret = _bundledRedstoneOutput(side.id)
          value.foreach(color => _bundledRedstoneOutput(side.id)(color._1) = color._2)
        case (value: Map[Int, Map[Int, Int]]@unchecked, _, _) =>
          ret = _bundledRedstoneOutput
          value.foreach(side => side._2.foreach(color => _bundledRedstoneOutput(side._1)(color._1) = color._2))
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

  object Tier2 {
    private val BundledInputTag = "bundledInput"
    private val BundledOutputTag = "bundledOutput"
  }
}
