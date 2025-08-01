package totoro.ocelot.brain.entity.machine

import li.cil.repack.com.naef.jnlua.{LuaState, LuaType}
import totoro.ocelot.brain.{Ocelot, Settings}

import java.util
import scala.collection.{immutable, mutable}
import scala.jdk.CollectionConverters._
import scala.language.implicitConversions
import scala.math.ScalaNumber
import scala.runtime.BoxedUnit

object ExtendedLuaState {

  implicit def extendLuaState(state: LuaState): ExtendedLuaState = new ExtendedLuaState(state)

  class ExtendedLuaState(val lua: LuaState) {
    def pushScalaFunction(f: LuaState => Int): Unit = lua.pushJavaFunction((state: LuaState) => f(state))

    def pushValue(value: Any, memo: util.IdentityHashMap[Any, Int] = new util.IdentityHashMap()): Unit = {
      val recursive = memo.size > 0
      val oldTop = lua.getTop
      if (memo.containsKey(value)) {
        lua.pushValue(memo.get(value))
      }
      else {
        (value match {
          case number: ScalaNumber => number.underlying
          case reference: AnyRef => reference
          case null => null
          case primitive => primitive.asInstanceOf[AnyRef]
        }) match {
          case null | () | _: BoxedUnit => lua.pushNil()
          case value: java.lang.Boolean => lua.pushBoolean(value.booleanValue)
          case value: java.lang.Byte => lua.pushInteger(value.byteValue)
          case value: java.lang.Character => lua.pushString(String.valueOf(value))
          case value: java.lang.Short => lua.pushInteger(value.shortValue)
          case value: java.lang.Integer => lua.pushInteger(value.intValue)
          case value: java.lang.Long => lua.pushInteger(value.longValue)
          case value: java.lang.Float => lua.pushNumber(value.floatValue)
          case value: java.lang.Double => lua.pushNumber(value.doubleValue)
          case value: java.lang.String => lua.pushString(value)
          case value: Array[Byte] => lua.pushByteArray(value)
          case value: Array[_] => pushList(value, value.zipWithIndex.iterator, memo)
          case value: Value if Settings.get.allowUserdata => lua.pushJavaObjectRaw(value)
          case value: Product => pushList(value, value.productIterator.zipWithIndex, memo)
          case value: Seq[_] => pushList(value, value.zipWithIndex.iterator, memo)
          case value: java.util.Map[_, _] => pushTable(value, value.asScala.toMap, memo)
          case value: Map[_, _] => pushTable(value, value, memo)
          case value: mutable.Map[_, _] => pushTable(value, value.toMap, memo)
          case _ =>
            Ocelot.log.warn("Tried to push an unsupported value of type to Lua: " + value.getClass.getName + ".")
            lua.pushNil()
        }
        // Remove values kept on the stack for memoization if this is the
        // original call (not a recursive one, where we might need the memo
        // info even after returning).
        if (!recursive) {
          lua.setTop(oldTop + 1)
        }
      }
    }

    def pushList(obj: Any, list: Iterator[(Any, Int)], memo: util.IdentityHashMap[Any, Int]): Unit = {
      lua.newTable()
      val tableIndex = lua.getTop
      memo.put(obj, tableIndex)
      var count = 0
      list.foreach {
        case (value, index) =>
          pushValue(value, memo)
          lua.rawSet(tableIndex, index + 1)
          count = count + 1
      }
      // Bring table back to top (in case memo values were pushed).
      lua.pushValue(tableIndex)
    }

    def pushTable(obj: AnyRef, map: Map[_, _], memo: util.IdentityHashMap[Any, Int]): Unit = {
      lua.newTable(0, map.size)
      val tableIndex = lua.getTop
      memo.put(obj, tableIndex)
      for ((key: AnyRef, value: AnyRef) <- map) {
        if (key != null && !key.isInstanceOf[BoxedUnit]) {
          pushValue(key, memo)
          val keyIndex = lua.getTop
          pushValue(value, memo)
          // Bring key to front, in case of memo from value push.
          // Cannot actually move because that might shift memo info.
          lua.pushValue(keyIndex)
          lua.insert(-2)
          lua.setTable(tableIndex)
        }
      }
      // Bring table back to top (in case memo values were pushed).
      lua.pushValue(tableIndex)
    }

    def toSimpleJavaObject(index: Int): AnyRef = lua.`type`(index) match {
      case LuaType.BOOLEAN => Boolean.box(lua.toBoolean(index))
      case LuaType.NUMBER => if (lua.isInteger(index)) Long.box(lua.toInteger(index)) else Double.box(lua.toNumber(index))
      case LuaType.STRING => lua.toByteArray(index)
      case LuaType.TABLE => lua.toJavaObject(index, classOf[java.util.Map[_, _]])
      case LuaType.USERDATA => lua.toJavaObjectRaw(index)
      case _ => null
    }

    def toSimpleJavaObjects(start: Int): immutable.IndexedSeq[AnyRef] =
      for (index <- start to lua.getTop) yield toSimpleJavaObject(index)
  }
}
