package totoro.ocelot.brain.entity

import com.google.common.hash.Hashing
import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{DeviceInfo, Entity, Environment}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.{Component, Network, Visibility}
import totoro.ocelot.brain.workspace.Workspace
import totoro.ocelot.brain.{Constants, Settings}

import java.nio.file.{Files, Path, Paths}
import scala.reflect.ClassTag.Nothing

class EEPROM extends Entity with Environment with DeviceInfo {
  override val node: Component = Network.newNode(this, Visibility.Neighbors).
    withComponent("eeprom", Visibility.Neighbors).
    create()


  var volatileData = Array.empty[Byte]

  var readonly = false

  var label = "EEPROM"

  // ----------------------------------------------------------------------- //

  var _codeBytes: Option[Array[Byte]] = None

  def codeBytes: Option[Array[Byte]] = _codeBytes

  def codeBytes_=(value: Option[Array[Byte]]): Unit = {
    _codeBytes = value
    _codePath = None
  }

  // ----------------------------------------------------------------------- //

  var _codePath: Option[Path] = None

  def codePath: Option[Path] = _codePath

  def codePath_=(value: Option[Path]): Unit = {
    _codePath = value
    _codeBytes = None
  }

  // ----------------------------------------------------------------------- //

  private def getBytes: Array[Byte] = {
    if (codeBytes.isDefined) {
      codeBytes.get
    }
    else if (codePath.isDefined && !Files.isDirectory(codePath.get)) {
      Files.readAllBytes(codePath.get)
    }
    else {
      Array.empty[Byte]
    }
  }

  def checksum: String = Hashing.crc32().hashBytes(getBytes).toString

  // ----------------------------------------------------------------------- //

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Memory,
    DeviceAttribute.Description -> "EEPROM",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.DefaultVendor,
    DeviceAttribute.Product -> "TurboFlash",
    DeviceAttribute.Capacity -> Settings.get.eepromSize.toString,
    DeviceAttribute.Size -> Settings.get.eepromSize.toString
  )

  override def getDeviceInfo: Map[String, String] = deviceInfo

  // ----------------------------------------------------------------------- //

  @Callback(direct = true, doc = """function():string -- Get the currently stored byte array.""")
  def get(context: Context, args: Arguments): Array[AnyRef] = {
    result(getBytes)
  }

  @Callback(doc = """function(data:string) -- Overwrite the currently stored byte array.""")
  def set(context: Context, args: Arguments): Array[AnyRef] = {
    if (readonly)
      return result((), "storage is readonly")

    val newData = args.optByteArray(0, Array.empty[Byte])

    if (newData.length > Settings.get.eepromSize)
      throw new IllegalArgumentException("not enough space")

    if (codeBytes.isDefined) {
      _codeBytes = Some(newData)
    }
    else if (codePath.isDefined && !Files.isDirectory(codePath.get)) {
      Files.write(codePath.get, newData)
    }

    context.pause(2) // deliberately slow to discourage use as normal storage medium
    null
  }

  @Callback(direct = true, doc = """function():string -- Get the label of the EEPROM.""")
  def getLabel(context: Context, args: Arguments): Array[AnyRef] = result(label)

  @Callback(doc = """function(data:string):string -- Set the label of the EEPROM.""")
  def setLabel(context: Context, args: Arguments): Array[AnyRef] = {
    if (readonly) {
      return result((), "storage is readonly")
    }
    label = args.optString(0, "EEPROM").trim.take(24)
    if (label.isEmpty) label = "EEPROM"
    result(label)
  }

  @Callback(direct = true, doc = """function():number -- Get the storage capacity of this EEPROM.""")
  def getSize(context: Context, args: Arguments): Array[AnyRef] = result(Settings.get.eepromSize)

  @Callback(direct = true, doc = """function():string -- Get the checksum of the data on this EEPROM.""")
  def getChecksum(context: Context, args: Arguments): Array[AnyRef] = result(checksum)

  @Callback(direct = true, doc = """function(checksum:string):boolean -- Make this EEPROM readonly if it isn't already. This process cannot be reversed!""")
  def makeReadonly(context: Context, args: Arguments): Array[AnyRef] = {
    if (args.checkString(0) == checksum) {
      readonly = true
      result(true)
    }
    else result((), "incorrect checksum")
  }

  @Callback(direct = true, doc = """function():number -- Get the storage capacity of this EEPROM.""")
  def getDataSize(context: Context, args: Arguments): Array[AnyRef] = result(Settings.get.eepromDataSize)

  @Callback(direct = true, doc = """function():string -- Get the currently stored byte array.""")
  def getData(context: Context, args: Arguments): Array[AnyRef] = result(volatileData)

  @Callback(doc = """function(data:string) -- Overwrite the currently stored byte array.""")
  def setData(context: Context, args: Arguments): Array[AnyRef] = {
    val newData = args.optByteArray(0, Array.empty[Byte])
    if (newData.length > Settings.get.eepromDataSize) throw new IllegalArgumentException("not enough space")
    volatileData = newData
    context.pause(1) // deliberately slow to discourage use as normal storage medium
    null
  }

  // ----------------------------------------------------------------------- //

  private final val CodeBytesTag = "eeprom"
  private final val CodePathTag = "eepromPath"
  private final val LabelTag = "label"
  private final val ReadonlyTag = "readonly"
  private final val UserdataTag = "userdata"

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)

    _codeBytes = if (nbt.hasKey(CodeBytesTag)) Some(nbt.getByteArray(CodeBytesTag)) else None
    _codePath = if (nbt.hasKey(CodePathTag)) Some(Paths.get(nbt.getString(CodePathTag))) else None

    if (nbt.hasKey(LabelTag))
      label = nbt.getString(LabelTag)

    readonly = nbt.getBoolean(ReadonlyTag)
    volatileData = nbt.getByteArray(UserdataTag)
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)

    if (codeBytes.isDefined) {
      nbt.setByteArray(CodeBytesTag, codeBytes.get)
    }
    else {
      nbt.removeTag(CodeBytesTag)
    }

    if (codePath.isDefined) {
      nbt.setString(CodePathTag, codePath.get.toString)
    }
    else {
      nbt.removeTag(CodePathTag)
    }

    nbt.setString(LabelTag, label)
    nbt.setBoolean(ReadonlyTag, readonly)
    nbt.setByteArray(UserdataTag, volatileData)
  }
}
