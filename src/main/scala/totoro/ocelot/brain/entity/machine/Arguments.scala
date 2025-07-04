package totoro.ocelot.brain.entity.machine

import com.google.common.base.Charsets

import java.util
import scala.collection.mutable
import scala.jdk.CollectionConverters._

/**
  * This interface provides access to arguments passed to a [[Callback]].
  *
  * It allows checking for the presence of arguments in a uniform manner, taking
  * care of proper type checking based on what can be passed along by Lua.
  *
  * Note that integer values fetched this way are actually double values that
  * have been truncated. So if a Lua program passes `1.9` and you do a
  * `checkInteger` you'll get a `1`.
  *
  * The indexes passed to the various functions start at zero, i.e. to get the
  * first argument you would use `checkAny(0)`. This is worth mentioning
  * because Lua starts its indexes at one.
  */
class Arguments(val args: Seq[AnyRef]) extends Iterable[AnyRef] {
  def iterator(): Iterator[AnyRef] = args.iterator

  /**
    * The total number of arguments that were passed to the function.
    */
  def count(): Int = args.length

  /**
    * Get whatever is at the specified index.
    * 
    * Throws an error if there are too few arguments.
    * 
    * The returned object will be one of the following, based on the conversion
    * performed internally:
    *
    * - `null` if the Lua value was `nil`.
    * - `java.lang.Boolean` if the Lua value was a boolean.
    * - `java.lang.Double` if the Lua value was a number.
    * - `byte[]` if the Lua value was a string.
    *
    * @param index the index from which to get the argument.
    * @return the raw value at that index.
    * @throws IllegalArgumentException if there is no argument at that index.
    */
  def checkAny(index: Int): AnyRef = {
    checkIndex(index, "value")
    args(index) match {
      case None => null
      case arg => arg
    }
  }

  /**
    * Try to get a boolean value at the specified index.
    * 
    * Throws an error if there are too few arguments.
    *
    * @param index the index from which to get the argument.
    * @return the boolean value at the specified index.
    * @throws IllegalArgumentException if there is no argument at that index,
    *                                  or if the argument is not a boolean.
    */
  def checkBoolean(index: Int): Boolean = {
    checkIndex(index, "boolean")
    args(index) match {
      case value: java.lang.Boolean => value
      case value => throw typeError(index, value, "boolean")
    }
  }

  /**
    * Try to get an integer value at the specified index.
    * 
    * Throws an error if there are too few arguments.
    *
    * @param index the index from which to get the argument.
    * @return the integer value at the specified index.
    * @throws IllegalArgumentException if there is no argument at that index,
    *                                  or if the argument is not a number.
    */
  def checkInteger(index: Int): Int = {
    checkIndex(index, "integer")
    args(index) match {
      // TODO: The below is correct behaviour, but breaks existing OC1 code (f.e. file:read(math.huge))
      /* case value: java.lang.Double =>
        if (!java.lang.Double.isFinite(value) || value < java.lang.Integer.MIN_VALUE || value > java.lang.Integer.MAX_VALUE) {
          throw intError(index, value)
        } else {
          value.intValue
        }
      case value: java.lang.Float =>
        if (!java.lang.Float.isFinite(value) || value < java.lang.Integer.MIN_VALUE || value > java.lang.Integer.MAX_VALUE) {
          throw intError(index, value)
        } else {
          value.intValue
        }
      case value: java.lang.Long =>
        if (value < java.lang.Integer.MIN_VALUE || value > java.lang.Integer.MAX_VALUE) {
          throw intError(index, value)
        } else {
          value.intValue
        } */
      case value: java.lang.Double =>
        if (value.isNaN)
          throw intError(index, value)
        else if (value > java.lang.Integer.MAX_VALUE)
          java.lang.Integer.MAX_VALUE
        else if (value < java.lang.Integer.MIN_VALUE)
          java.lang.Integer.MIN_VALUE
        else
          value.intValue
      case value: java.lang.Float =>
        if (value.isNaN)
          throw intError(index, value)
        else if (value > java.lang.Integer.MAX_VALUE)
          java.lang.Integer.MAX_VALUE
        else if (value < java.lang.Integer.MIN_VALUE)
          java.lang.Integer.MIN_VALUE
        else
          value.intValue
      case value: java.lang.Long =>
        if (value > java.lang.Integer.MAX_VALUE)
          java.lang.Integer.MAX_VALUE
        else if (value < java.lang.Integer.MIN_VALUE)
          java.lang.Integer.MIN_VALUE
        else
          value.intValue
      case value: java.lang.Number => value.intValue
      case value => throw typeError(index, value, "integer")
    }
  }

  /**
    * Try to get a long value at the specified index.
    *
    * Throws an error if there are too few arguments.
    *
    * @param index the index from which to get the argument.
    * @return the long value at the specified index.
    * @throws IllegalArgumentException if there is no argument at that index,
    *                                  or if the argument is not a number.
    * @since OpenComputers 1.8.0
    */
  def checkLong(index: Int): Long = {
    checkIndex(index, "integer")
    args(index) match {
      // TODO: The below is correct behaviour, but breaks existing OC1 code (f.e. file:read(math.huge))
      /* case value: java.lang.Double =>
        if (!java.lang.Double.isFinite(value) || value < java.lang.Long.MIN_VALUE || value > java.lang.Long.MAX_VALUE) {
          throw intError(index, value)
        } else {
          value.longValue
        }
      case value: java.lang.Float =>
        if (!java.lang.Float.isFinite(value) || value < java.lang.Long.MIN_VALUE || value > java.lang.Long.MAX_VALUE) {
          throw intError(index, value)
        } else {
          value.longValue
        } */
      case value: java.lang.Double =>
        if (value.isNaN)
          throw intError(index, value)
        else if (value > java.lang.Long.MAX_VALUE)
          java.lang.Long.MAX_VALUE
        else if (value < java.lang.Long.MIN_VALUE)
          java.lang.Long.MIN_VALUE
        else
          value.longValue
      case value: java.lang.Float =>
        if (value.isNaN)
          throw intError(index, value)
        else if (value > java.lang.Long.MAX_VALUE)
          java.lang.Long.MAX_VALUE
        else if (value < java.lang.Long.MIN_VALUE)
          java.lang.Long.MIN_VALUE
        else
          value.longValue
      case value: java.lang.Number => value.longValue
      case value => throw typeError(index, value, "integer")
    }
  }

  /**
    * Try to get a double value at the specified index.
    *
    * Throws an error if there are too few arguments.
    *
    * @param index the index from which to get the argument.
    * @return the double value at the specified index.
    * @throws IllegalArgumentException if there is no argument at that index,
    *                                  or if the argument is not a number.
    */
  def checkDouble(index: Int): Double = {
    checkIndex(index, "number")
    args(index) match {
      case value: java.lang.Number => value.doubleValue
      case value => throw typeError(index, value, "number")
    }
  }

  /**
    * Try to get a string value at the specified index.
    *
    * Throws an error if there are too few arguments.
    *
    * This will actually check for a byte array and convert it to a string
    * using UTF-8 encoding.
    *
    * @param index the index from which to get the argument.
    * @return the boolean value at the specified index.
    * @throws IllegalArgumentException if there is no argument at that index,
    *                                  or if the argument is not a string.
    */
  def checkString(index: Int): String = {
    checkIndex(index, "string")
    args(index) match {
      case value: java.lang.String => value
      case value: Array[Byte] => new String(value, Charsets.UTF_8)
      case value => throw typeError(index, value, "string")
    }
  }

  /**
    * Try to get a byte array at the specified index.
    *
    * Throws an error if there are too few arguments.
    *
    * @param index the index from which to get the argument.
    * @return the byte array at the specified index.
    * @throws IllegalArgumentException if there is no argument at that index,
    *                                  or if the argument is not a byte array.
    */
  def checkByteArray(index: Int): Array[Byte] = {
    checkIndex(index, "string")
    args(index) match {
      case value: java.lang.String => value.getBytes(Charsets.UTF_8)
      case value: Array[Byte] => value
      case value => throw typeError(index, value, "string")
    }
  }

  /**
    * Try to get a table at the specified index.
    *
    * Throws an error if there are too few arguments.
    *
    * @param index the index from which to get the argument.
    * @return the table at the specified index.
    * @throws IllegalArgumentException if there is no argument at that index,
    *                                  or if the argument is not a table.
    */
  def checkTable(index: Int): util.Map[_, _] = {
    checkIndex(index, "table")
    args(index) match {
      case value: java.util.Map[_, _] => value
      case value: Map[_, _] => value.asJava
      case value: mutable.Map[_, _] => value.asJava
      case value => throw typeError(index, value, "table")
    }
  }

  /**
    * Get whatever is at the specified index.
    *
    * Return the specified default value if there is no such element, behaves
    * like `checkAny(int)` otherwise.
    *
    * The returned object will be one of the following, based on the conversion
    * performed internally:
    *
    * - `null` if the Lua value was `nil`.
    * - `java.lang.Boolean` if the Lua value was a boolean.
    * - `java.lang.Double` if the Lua value was a number.
    * - `byte[]` if the Lua value was a string.
    *
    * @param index the index from which to get the argument.
    * @return the raw value at that index.
    */
  def optAny(index: Int, default: AnyRef): AnyRef = {
    if (!isDefined(index)) default
    else checkAny(index)
  }

  /**
    * Try to get a boolean value at the specified index.
    *
    * Return the specified default value if there is no such element, behaves
    * like `checkBoolean(int)` otherwise.
    *
    * @param index the index from which to get the argument.
    * @return the boolean value at the specified index.
    * @throws IllegalArgumentException if the argument exists and is not a boolean.
    */
  def optBoolean(index: Int, default: Boolean): Boolean = {
    if (!isDefined(index)) default
    else checkBoolean(index)
  }

  /**
    * Try to get an integer value at the specified index.
    *
    * Return the specified default value if there is no such element, behaves
    * like `checkInteger(int)` otherwise.
    *
    * @param index the index from which to get the argument.
    * @return the integer value at the specified index.
    * @throws IllegalArgumentException if the argument exists but is not a number.
    */
  def optInteger(index: Int, default: Int): Int = {
    if (!isDefined(index)) default
    else checkInteger(index)
  }

  /**
    * Try to get a long value at the specified index.
    *
    * Return the specified default value if there is no such element, behaves
    * like `checkLong (int)` otherwise.
    *
    * @param index the index from which to get the argument.
    * @return the long value at the specified index.
    * @throws IllegalArgumentException if the argument exists but is not a number.
    * @since OpenComputers 1.8.0
    */
  def optLong(index: Int, default: Long): Long = {
    if (!isDefined(index)) default
    else checkLong(index)
  }

  /**
    * Try to get a double value at the specified index.
    *
    * Return the specified default value if there is no such element, behaves
    * like `checkDouble(int)` otherwise.
    *
    * @param index the index from which to get the argument.
    * @return the double value at the specified index.
    * @throws IllegalArgumentException if the argument exists and is not a number.
    */
  def optDouble(index: Int, default: Double): Double = {
    if (!isDefined(index)) default
    else checkDouble(index)
  }

  /**
    * Try to get a string value at the specified index.
    *
    * Return the specified default value if there is no such element, behaves
    * like `checkString(int)` otherwise.
    *
    * This will actually check for a byte array and convert it to a string
    * using UTF-8 encoding.
    *
    * @param index the index from which to get the argument.
    * @return the boolean value at the specified index.
    * @throws IllegalArgumentException if the argument exists and is not a string.
    */
  def optString(index: Int, default: String): String = {
    if (!isDefined(index)) default
    else checkString(index)
  }

  /**
    * Try to get a byte array at the specified index.
    *
    * Return the specified default value if there is no such element, behaves
    * like `checkByteArray(int)` otherwise.
    *
    * @param index the index from which to get the argument.
    * @return the byte array at the specified index.
    * @throws IllegalArgumentException if the argument exists and is not a byte array.
    */
  def optByteArray(index: Int, default: Array[Byte]): Array[Byte] = {
    if (!isDefined(index)) default
    else checkByteArray(index)
  }

  /**
    * Try to get a table at the specified index.
    *
    * Return the specified default value if there is no such element, behaves
    * like `checkTable(int)` otherwise.
    *
    * @param index the index from which to get the argument.
    * @return the table at the specified index.
    * @throws IllegalArgumentException if the argument exists and is not a table.
    */
  def optTable(index: Int, default: util.Map[_, _]): util.Map[_, _] = {
    if (!isDefined(index)) default
    else checkTable(index)
  }

  /**
    * Tests whether the argument at the specified index is a boolean value.
    *
    * This will return false if there is ''no'' argument at the specified
    * index, i.e. if there are too few arguments.
    *
    * @param index the index to check.
    * @return true if the argument is a boolean; false otherwise.
    */
  def isBoolean(index: Int): Boolean =
    index >= 0 && index < count && (args(index) match {
      case _: java.lang.Boolean => true
      case _ => false
    })

  /**
    * Tests whether the argument at the specified index is an integer value.
    *
    * This will return false if there is ''no'' argument at the specified
    * index, i.e. if there are too few arguments.
    *
    * @param index the index to check.
    * @return true if the argument is an integer; false otherwise.
    */
  def isInteger(index: Int): Boolean =
    index >= 0 && index < count && (args(index) match {
      // TODO: The below is correct behaviour, but may break existing OC1 code
      /* case value: java.lang.Double =>
        java.lang.Double.isFinite(value) && value >= java.lang.Integer.MIN_VALUE && value <= java.lang.Integer.MAX_VALUE
      case value: java.lang.Float =>
        java.lang.Float.isFinite(value) && value >= java.lang.Integer.MIN_VALUE && value <= java.lang.Integer.MAX_VALUE
      case value: java.lang.Long =>
        value >= java.lang.Integer.MIN_VALUE && value <= java.lang.Integer.MAX_VALUE */
      case value: java.lang.Double => !value.isNaN
      case value: java.lang.Float => !value.isNaN
      case value: java.lang.Number => true
      case _ => false
    })

  /**
    * Tests whether the argument at the specified index is a long value.
    *
    * This will return false if there is ''no'' argument at the specified
    * index, i.e. if there are too few arguments.
    *
    * @param index the index to check.
    * @return true if the argument is a long; false otherwise.
    */
  def isLong (index: Int): Boolean =
    index >= 0 && index < count && (args(index) match {
      // TODO: The below is correct behaviour, but may break existing OC1 code
      /* case value: java.lang.Double =>
        java.lang.Double.isFinite(value) && value >= java.lang.Long.MIN_VALUE && value <= java.lang.Long.MAX_VALUE
      case value: java.lang.Float =>
        java.lang.Float.isFinite(value) && value >= java.lang.Long.MIN_VALUE && value <= java.lang.Long.MAX_VALUE */
      case value: java.lang.Double => !value.isNaN
      case value: java.lang.Float => !value.isNaN
      case value: java.lang.Number => true
      case _ => false
    })

  /**
    * Tests whether the argument at the specified index is a double value.
    *
    * This will return false if there is ''no'' argument at the specified
    * index, i.e. if there are too few arguments.
    *
    * @param index the index to check.
    * @return true if the argument is a double; false otherwise.
    */
  def isDouble(index: Int): Boolean =
    index >= 0 && index < count && (args(index) match {
      case value: java.lang.Number => true
      case _ => false
    })

  /**
    * Tests whether the argument at the specified index is a string value.
    *
    * This will return false if there is ''no'' argument at the specified
    * index, i.e. if there are too few arguments.
    *
    * @param index the index to check.
    * @return true if the argument is a string; false otherwise.
    */
  def isString(index: Int): Boolean =
    index >= 0 && index < count && (args(index) match {
      case _: java.lang.String => true
      case _: Array[Byte] => true
      case _ => false
    })

  /**
    * Tests whether the argument at the specified index is a byte array.
    *
    * This will return false if there is ''no'' argument at the specified
    * index, i.e. if there are too few arguments.
    *
    * @param index the index to check.
    * @return true if the argument is a byte array; false otherwise.
    */
  def isByteArray(index: Int): Boolean =
    index >= 0 && index < count && (args(index) match {
      case _: java.lang.String => true
      case _: Array[Byte] => true
      case _ => false
    })

  /**
    * Tests whether the argument at the specified index is a table.
    *
    * This will return false if there is ''no'' argument at the specified
    * index, i.e. if there are too few arguments.
    *
    * @param index the index to check.
    * @return true if the argument is a table; false otherwise.
    */
  def isTable(index: Int): Boolean =
    index >= 0 && index < count && (args(index) match {
      case _: java.util.Map[_, _] => true
      case _: Map[_, _] => true
      case _: mutable.Map[_, _] => true
      case _ => false
    })

  /**
    * Tests whether the argument at the specified index is an item stack.
    *
    * This will return false if there is ''no'' argument at the specified
    * index, i.e. if there are too few arguments.
    *
    * @param index the index to check.
    * @return true if the argument is an item stack; false otherwise.
    */
  def isItemStack(index: Int): Boolean =
    isTable(index) && {
      val map = checkTable(index)
      map.get("name") match {
        case _: String => true
        case _: Array[Byte] => true
        case _ => false
      }
    }

  /**
    * Converts the argument list to a standard Java array, converting byte
    * arrays to strings automatically, since this is usually what others
    * want - if you need the actual raw byte arrays, don't use this method!
    *
    * @return an array containing all arguments.
    */
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

  private def intError(index: Int, have: AnyRef) =
    new IllegalArgumentException(
      s"bad argument #${index + 1} (${typeName(have)} has no integer representation)")

  private def typeName(value: AnyRef): String = value match {
    case null | None => "nil"
    case _: java.lang.Boolean => "boolean"
    case _: java.lang.Byte => "integer"
    case _: java.lang.Short => "integer"
    case _: java.lang.Integer => "integer"
    case _: java.lang.Long => "integer"
    case _: java.lang.Number => "number"
    case _: java.lang.String => "string"
    case _: Array[Byte] => "string"
    case _: java.util.Map[_, _] => "table"
    case _: Map[_, _] => "table"
    case _: mutable.Map[_, _] => "table"
    case _ => value.getClass.getSimpleName
  }
}
