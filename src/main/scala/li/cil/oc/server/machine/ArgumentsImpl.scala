package li.cil.oc.server.machine

import java.util

import com.google.common.base.Charsets
import li.cil.oc.api.detail.ItemInfo
import li.cil.oc.api.machine.Arguments
import li.cil.oc.common.init.Items
import li.cil.oc.util.ItemUtils
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

import scala.collection.convert.WrapAsJava._
import scala.collection.mutable

class ArgumentsImpl(val args: Seq[AnyRef]) extends Arguments {
  def iterator(): util.Iterator[AnyRef] = args.iterator

  def count(): Int = args.length

  def checkAny(index: Int): AnyRef = {
    checkIndex(index, "value")
    args(index) match {
      case Unit | None => null
      case arg => arg
    }
  }

  def optAny(index: Int, default: AnyRef): AnyRef = {
    if (!isDefined(index)) default
    else checkAny(index)
  }

  def checkBoolean(index: Int): Boolean = {
    checkIndex(index, "boolean")
    args(index) match {
      case value: java.lang.Boolean => value
      case value => throw typeError(index, value, "boolean")
    }
  }

  def optBoolean(index: Int, default: Boolean): Boolean = {
    if (!isDefined(index)) default
    else checkBoolean(index)
  }

  def checkDouble(index: Int): Double = {
    checkIndex(index, "number")
    args(index) match {
      case value: java.lang.Number => value.doubleValue
      case value => throw typeError(index, value, "number")
    }
  }

  def optDouble(index: Int, default: Double): Double = {
    if (!isDefined(index)) default
    else checkDouble(index)
  }

  def checkInteger(index: Int): Int = {
    checkIndex(index, "number")
    args(index) match {
      case value: java.lang.Number => value.intValue
      case value => throw typeError(index, value, "number")
    }
  }

  def optInteger(index: Int, default: Int): Int = {
    if (!isDefined(index)) default
    else checkInteger(index)
  }

  def checkString(index: Int): String = {
    checkIndex(index, "string")
    args(index) match {
      case value: java.lang.String => value
      case value: Array[Byte] => new String(value, Charsets.UTF_8)
      case value => throw typeError(index, value, "string")
    }
  }

  def optString(index: Int, default: String): String = {
    if (!isDefined(index)) default
    else checkString(index)
  }

  def checkByteArray(index: Int): Array[Byte] = {
    checkIndex(index, "string")
    args(index) match {
      case value: java.lang.String => value.getBytes(Charsets.UTF_8)
      case value: Array[Byte] => value
      case value => throw typeError(index, value, "string")
    }
  }

  def optByteArray(index: Int, default: Array[Byte]): Array[Byte] = {
    if (!isDefined(index)) default
    else checkByteArray(index)
  }

  def checkTable(index: Int): util.Map[_, _] = {
    checkIndex(index, "table")
    args(index) match {
      case value: java.util.Map[_, _] => value
      case value: Map[_, _] => value
      case value: mutable.Map[_, _] => value
      case value => throw typeError(index, value, "table")
    }
  }

  def optTable(index: Int, default: util.Map[_, _]): util.Map[_, _] = {
    if (!isDefined(index)) default
    else checkTable(index)
  }

  def checkItemStack(index: Int): ItemStack = {
    val map = checkTable(index)
    map.get("name") match {
      case name: String =>
        val damage = map.get("damage") match {
          case number: java.lang.Number => number.intValue
          case _ => 0
        }
        val tag = map.get("tag") match {
          case ba: Array[Byte] => toNbtTagCompound(ba)
          case s: String => toNbtTagCompound(s.getBytes(Charsets.UTF_8))
          case _ => None
        }
        makeStack(name, damage, tag)
      case _ => throw new IllegalArgumentException("invalid item stack")
    }
  }

  def optItemStack(index: Int, default: ItemStack): ItemStack = {
    if (!isDefined(index)) default
    else checkItemStack(index)
  }

  def isBoolean(index: Int): Boolean =
    index >= 0 && index < count && (args(index) match {
      case _: java.lang.Boolean => true
      case _ => false
    })

  def isDouble(index: Int): Boolean =
    index >= 0 && index < count && (args(index) match {
      case _: java.lang.Float => true
      case _: java.lang.Double => true
      case _ => false
    })

  def isInteger(index: Int): Boolean =
    index >= 0 && index < count && (args(index) match {
      case _: java.lang.Byte => true
      case _: java.lang.Short => true
      case _: java.lang.Integer => true
      case _: java.lang.Long => true
      case _: java.lang.Double => true
      case _ => false
    })

  def isString(index: Int): Boolean =
    index >= 0 && index < count && (args(index) match {
      case _: java.lang.String => true
      case _: Array[Byte] => true
      case _ => false
    })

  def isByteArray(index: Int): Boolean =
    index >= 0 && index < count && (args(index) match {
      case _: java.lang.String => true
      case _: Array[Byte] => true
      case _ => false
    })

  def isTable(index: Int): Boolean =
    index >= 0 && index < count && (args(index) match {
      case _: java.util.Map[_, _] => true
      case _: Map[_, _] => true
      case _: mutable.Map[_, _] => true
      case _ => false
    })

  def isItemStack(index: Int): Boolean =
    isTable(index) && {
      val map = checkTable(index)
      map.get("name") match {
        case _: String => true
        case _: Array[Byte] => true
        case _ => false
      }
    }

  def toArray: Array[AnyRef] = args.map {
    case value: Array[Byte] => new String(value, Charsets.UTF_8)
    case value => value
  }.toArray

  private def isDefined(index: Int) = index >= 0 && index < args.length && args(index) != null

  private def checkIndex(index: Int, name: String): Unit =
    if (index < 0) throw new IndexOutOfBoundsException()
    else if (args.length <= index) throw new IllegalArgumentException(
      s"bad arguments #${index + 1} ($name expected, got no value)")

  private def typeError(index: Int, have: AnyRef, want: String) =
    new IllegalArgumentException(
      s"bad argument #${index + 1} ($want expected, got ${typeName(have)})")

  private def typeName(value: AnyRef): String = value match {
    case null | Unit | None => "nil"
    case _: java.lang.Boolean => "boolean"
    case _: java.lang.Number => "double"
    case _: java.lang.String => "string"
    case _: Array[Byte] => "string"
    case _: java.util.Map[_, _] => "table"
    case _: Map[_, _] => "table"
    case _: mutable.Map[_, _] => "table"
    case _ => value.getClass.getSimpleName
  }

  private def makeStack(name: String, damage: Int, tag: Option[NBTTagCompound]) = {
    Items.get(name) match {
      case null => throw new IllegalArgumentException("invalid item stack")
      case itemInfo: ItemInfo =>
        val stack = new ItemStack(itemInfo.item(), 1, damage)
        tag.foreach(stack.setTagCompound)
        stack
    }
  }

  private def toNbtTagCompound(data: Array[Byte]) = Option(ItemUtils.loadTag(data))
}
