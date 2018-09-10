package li.cil.oc.common

import java.util.Random
import java.util.concurrent.Callable

import li.cil.oc.{Constants, OpenComputers, Settings, api}
import li.cil.oc.api.fs.FileSystem
import li.cil.oc.common.init.Items
import li.cil.oc.util.Color
import net.minecraft.item.{EnumDyeColor, ItemStack}
import net.minecraft.nbt.{NBT, NBTTagCompound}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object Loot {

  val factories = mutable.Map.empty[String, Callable[FileSystem]]

  val globalDisks: ArrayBuffer[(ItemStack, Int)] = mutable.ArrayBuffer.empty[(ItemStack, Int)]

  val worldDisks: ArrayBuffer[(ItemStack, Int)] = mutable.ArrayBuffer.empty[(ItemStack, Int)]

  def disksForCycling: ArrayBuffer[ItemStack] = if(disksForCyclingClient.nonEmpty) disksForCyclingClient else disksForCyclingServer

  val disksForCyclingServer: ArrayBuffer[ItemStack] = mutable.ArrayBuffer.empty[ItemStack]

  val disksForCyclingClient: ArrayBuffer[ItemStack] = mutable.ArrayBuffer.empty[ItemStack]

  val disksForSampling: ArrayBuffer[ItemStack] = mutable.ArrayBuffer.empty[ItemStack]

  val disksForClient: ArrayBuffer[ItemStack] = mutable.ArrayBuffer.empty[ItemStack]

  def isLootDisk(stack: ItemStack): Boolean = api.Items.get(stack) == api.Items.get(Constants.ItemName.Floppy) &&
    stack.hasTagCompound && stack.getTagCompound.hasKeyOfType(Settings.namespace + "lootFactory", NBT.TAG_STRING)

  def randomDisk(rng: Random): Option[ItemStack] =
    if (disksForSampling.nonEmpty) Some(disksForSampling(rng.nextInt(disksForSampling.length)))
    else None

  def registerLootDisk(name: String, color: EnumDyeColor, factory: Callable[FileSystem], doRecipeCycling: Boolean): ItemStack = {
    OpenComputers.log.debug(s"Registering loot disk '$name'")

    val data = new NBTTagCompound()
    data.setString(Settings.namespace + "fs.label", name)

    val nbt = new NBTTagCompound()
    nbt.setTag(Settings.namespace + "data", data)

    // Store this top level, so it won't get wiped on save.
    nbt.setString(Settings.namespace + "lootFactory", name)
    nbt.setInteger(Settings.namespace + "color", color.code)

    val stack = Items.get(Constants.ItemName.Floppy).createItemStack(1)
    stack.setTagCompound(nbt)

    Loot.factories += name -> factory

    if(doRecipeCycling) {
      Loot.disksForCyclingServer += stack
    }

    stack.copy()
  }

  def init() {
    val list = new java.util.Properties()
    val listStream = getClass.getResourceAsStream("/assets/" + Settings.resourceDomain + "/loot/loot.properties")
    list.load(listStream)
    listStream.close()
    parseLootDisks(list, globalDisks, external = false)
  }

  private def parseLootDisks(list: java.util.Properties, acc: mutable.ArrayBuffer[(ItemStack, Int)], external: Boolean) {
    list.stringPropertyNames().forEach(key => {
      val value = list.getProperty(key)
      try value.split(":") match {
        case Array(name, count, color) =>
          acc += ((createLootDisk(name, key, external, Color.byOreName.get(color)), count.toInt))
        case Array(name, count) =>
          acc += ((createLootDisk(name, key, external), count.toInt))
        case _ =>
          acc += ((createLootDisk(value, key, external), 1))
      }
      catch {
        case t: Throwable => OpenComputers.log.warn("Bad loot descriptor: " + value, t)
      }
    })
  }

  def createLootDisk(name: String, path: String, external: Boolean, color: Option[EnumDyeColor] = None): ItemStack = {
    val callable = if (external) new Callable[FileSystem] {
      override def call(): FileSystem = api.FileSystem.asReadOnly(api.FileSystem.fromSaveDirectory("loot/" + path, 0, false))
    } else new Callable[FileSystem] {
      override def call(): FileSystem = api.FileSystem.fromClass(OpenComputers.getClass, Settings.resourceDomain, "loot/" + path)
    }
    val stack = registerLootDisk(path, color.getOrElse(EnumDyeColor.SILVER), callable, doRecipeCycling = true)
    if (!external) {
      Items.registerStack(stack, path)
    }
    stack
  }
}
