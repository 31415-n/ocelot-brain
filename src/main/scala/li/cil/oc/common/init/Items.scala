package li.cil.oc.common.init

import java.util.concurrent.Callable

import li.cil.oc.Constants
import li.cil.oc.OpenComputers
import li.cil.oc.Settings
import li.cil.oc.api.detail.ItemAPI
import li.cil.oc.api.detail.ItemInfo
import li.cil.oc.api.fs.FileSystem
import li.cil.oc.common.Loot
import li.cil.oc.common.Tier
import li.cil.oc.common.item
import net.minecraft.item.{EnumDyeColor, Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object Items extends ItemAPI {
  val descriptors = mutable.Map.empty[String, ItemInfo]

  val names = mutable.Map.empty[Any, String]

  val aliases = Map(
    "datacard" -> Constants.ItemName.DataCardTier1,
    "wlancard" -> Constants.ItemName.WirelessNetworkCardTier2
  )

  override def get(name: String): ItemInfo = descriptors.get(name).orNull

  override def get(stack: ItemStack): ItemInfo = names.get(getItem(stack)) match {
    case Some(name) => get(name)
    case _ => null
  }

  def registerItem(instance: Item, id: String): Item = {
    if (!descriptors.contains(id)) {
      descriptors += id -> new ItemInfo {
        override def name: String = id
        override def item: Item = instance
        override def createItemStack(size: Int): ItemStack =
          new ItemStack(instance, size)
      }
      names += instance -> id
    }
    instance
  }

  def registerStack(stack: ItemStack, id: String): ItemStack = {
    val immutableStack = stack.copy()
    descriptors += id -> new ItemInfo {
      override def name: String = id
      override def createItemStack(size: Int): ItemStack = {
        val copy = immutableStack.copy()
        copy.setCount(size)
        copy
      }
      override def item: Item = immutableStack.getItem
    }
    stack
  }

  private def getItem(stack: ItemStack): Item =
    if (stack.isEmpty) null
    else stack.getItem

  // ----------------------------------------------------------------------- //

  val registeredItems: ArrayBuffer[ItemStack] = mutable.ArrayBuffer.empty[ItemStack]

  override def registerFloppy(name: String, color: EnumDyeColor, factory: Callable[FileSystem], doRecipeCycling: Boolean): ItemStack = {
    val stack = Loot.registerLootDisk(name, color, factory, doRecipeCycling)
    registeredItems += stack
    stack.copy()
  }

  override def registerEEPROM(name: String, code: Array[Byte], data: Array[Byte], readonly: Boolean): ItemStack = {
    val nbt = new NBTTagCompound()
    if (name != null) {
      nbt.setString(Settings.namespace + "label", name.trim.take(24))
    }
    if (code != null) {
      nbt.setByteArray(Settings.namespace + "eeprom", code.take(Settings.get.eepromSize))
    }
    if (data != null) {
      nbt.setByteArray(Settings.namespace + "userdata", data.take(Settings.get.eepromDataSize))
    }
    nbt.setBoolean(Settings.namespace + "readonly", readonly)

    val stackNbt = new NBTTagCompound()
    stackNbt.setTag(Settings.namespace + "data", nbt)

    val stack = get(Constants.ItemName.EEPROM).createItemStack(1)
    stack.setTagCompound(stackNbt)

    registeredItems += stack

    stack.copy()
  }

  // ----------------------------------------------------------------------- //

  def init() {
    initComponents()
    initCards()
    initStorage()

    // Register aliases.
    for ((k, v) <- aliases) {
      descriptors.getOrElseUpdate(k, descriptors(v))
    }
  }

  // General purpose components.
  private def initComponents(): Unit = {
    registerItem(new item.CPU(Tier.One), Constants.ItemName.CPUTier1)
    registerItem(new item.CPU(Tier.Two), Constants.ItemName.CPUTier2)
    registerItem(new item.CPU(Tier.Three), Constants.ItemName.CPUTier3)

    registerItem(new item.Memory(Tier.One), Constants.ItemName.RAMTier1)
    registerItem(new item.Memory(Tier.Two), Constants.ItemName.RAMTier2)
    registerItem(new item.Memory(Tier.Three), Constants.ItemName.RAMTier3)
    registerItem(new item.Memory(Tier.Four), Constants.ItemName.RAMTier4)
    registerItem(new item.Memory(Tier.Five), Constants.ItemName.RAMTier5)
    registerItem(new item.Memory(Tier.Six), Constants.ItemName.RAMTier6)

    registerItem(new item.APU(Tier.One), Constants.ItemName.APUTier1)
    registerItem(new item.APU(Tier.Two), Constants.ItemName.APUTier2)
    registerItem(new item.APU(Tier.Three), Constants.ItemName.APUCreative)
  }

  // Card components.
  private def initCards(): Unit = {
    registerItem(new item.GraphicsCard(Tier.One), Constants.ItemName.GraphicsCardTier1)
    registerItem(new item.GraphicsCard(Tier.Two), Constants.ItemName.GraphicsCardTier2)
    registerItem(new item.GraphicsCard(Tier.Three), Constants.ItemName.GraphicsCardTier3)
    registerItem(new item.RedstoneCard(Tier.One), Constants.ItemName.RedstoneCardTier1)
    registerItem(new item.RedstoneCard(Tier.Two), Constants.ItemName.RedstoneCardTier2)
    registerItem(new item.NetworkCard(), Constants.ItemName.NetworkCard)
    registerItem(new item.WirelessNetworkCard(Tier.Two), Constants.ItemName.WirelessNetworkCardTier2)
    registerItem(new item.InternetCard(), Constants.ItemName.InternetCard)
    registerItem(new item.LinkedCard(), Constants.ItemName.LinkedCard)
    registerItem(new item.DataCard(Tier.One), Constants.ItemName.DataCardTier1)
    registerItem(new item.DataCard(Tier.Two), Constants.ItemName.DataCardTier2)
    registerItem(new item.DataCard(Tier.Three), Constants.ItemName.DataCardTier3)
  }

  // Storage media of all kinds.
  private def initStorage(): Unit = {
    registerItem(new item.EEPROM(), Constants.ItemName.EEPROM)
    registerItem(new item.FloppyDisk(), Constants.ItemName.Floppy)
    registerItem(new item.HardDiskDrive(Tier.One), Constants.ItemName.HDDTier1)
    registerItem(new item.HardDiskDrive(Tier.Two), Constants.ItemName.HDDTier2)
    registerItem(new item.HardDiskDrive(Tier.Three), Constants.ItemName.HDDTier3)

    val luaBios = {
      val code = new Array[Byte](4 * 1024)
      val count = OpenComputers.getClass.getResourceAsStream(Settings.scriptPath + "bios.lua").read(code)
      registerEEPROM("EEPROM (Lua BIOS)", code.take(count), null, readonly = false)
    }
    registerStack(luaBios, Constants.ItemName.LuaBios)
  }
}
