package totoro.ocelot.brain.entity

import com.google.common.hash.Hashing
import totoro.ocelot.brain.entity.machine.{Arguments, Callback, Context}
import totoro.ocelot.brain.entity.traits.DeviceInfo.{DeviceAttribute, DeviceClass}
import totoro.ocelot.brain.entity.traits.{DeviceInfo, Entity, Environment}
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.network.{Component, Network, Visibility}
import totoro.ocelot.brain.workspace.Workspace
import totoro.ocelot.brain.{Constants, Settings}

class EEPROM extends Entity with Environment with DeviceInfo {
  override val node: Component = Network.newNode(this, Visibility.Neighbors).
    withComponent("eeprom", Visibility.Neighbors).
    create()

  var codeData = Array.empty[Byte]

  var volatileData = Array.empty[Byte]

  var readonly = false

  var label = "EEPROM"

  def checksum: String = Hashing.crc32().hashBytes(codeData).toString

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
  def get(context: Context, args: Arguments): Array[AnyRef] = result(codeData)

  @Callback(doc = """function(data:string) -- Overwrite the currently stored byte array.""")
  def set(context: Context, args: Arguments): Array[AnyRef] = {
    if (readonly) {
      return result((), "storage is readonly")
    }
    val newData = args.optByteArray(0, Array.empty[Byte])
    if (newData.length > Settings.get.eepromSize) throw new IllegalArgumentException("not enough space")
    codeData = newData
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
    if (label.length == 0) label = "EEPROM"
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

  private final val EEPROMTag = "eeprom"
  private final val LabelTag = "label"
  private final val ReadonlyTag = "readonly"
  private final val UserdataTag = "userdata"

  override def load(nbt: NBTTagCompound, workspace: Workspace) {
    super.load(nbt, workspace)
    codeData = nbt.getByteArray(EEPROMTag)
    if (nbt.hasKey(LabelTag)) {
      label = nbt.getString(LabelTag)
    }
    readonly = nbt.getBoolean(ReadonlyTag)
    volatileData = nbt.getByteArray(UserdataTag)
  }

  override def save(nbt: NBTTagCompound) {
    super.save(nbt)
    nbt.setByteArray(EEPROMTag, codeData)
    nbt.setString(LabelTag, label)
    nbt.setBoolean(ReadonlyTag, readonly)
    nbt.setByteArray(UserdataTag, volatileData)
  }
}
