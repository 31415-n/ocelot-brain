package totoro.ocelot.brain.machine

import java.util

import totoro.ocelot.brain.Ocelot

import scala.collection.convert.WrapAsScala._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.math.ScalaNumber

object Registry {
  val converters: ArrayBuffer[Converter] = mutable.ArrayBuffer.empty[Converter]

  /** Used to keep track of whether we're past the init phase. */
  var locked = false

  /**
    * Registers a new type converter.
    *
    * Type converters are used to automatically convert values returned from
    * callbacks to a "simple" format that can be pushed to any architecture.
    *
    * This must be called in the init phase, ''not'' the pre- or post-init
    * phases.
    *
    * @param converter the converter to register.
    */
  def add(converter: Converter) {
    if (locked) throw new IllegalStateException("Please register all converters in the init phase.")
    if (!converters.contains(converter)) {
      Ocelot.log.debug(s"Registering converter ${converter.getClass.getName}.")
      converters += converter
    }
  }

  def convert(value: Array[AnyRef]): Array[AnyRef] = if (value != null) value.map(arg => convertRecursively(arg, new util.IdentityHashMap())) else null

  def convertRecursively(value: Any, memo: util.IdentityHashMap[AnyRef, AnyRef], force: Boolean = false): AnyRef = {
    val valueRef = value match {
      case number: ScalaNumber => number.underlying
      case reference: AnyRef => reference
      case null => null
      case primitive => primitive.asInstanceOf[AnyRef]
    }
    if (!force && memo.containsKey(valueRef)) {
      memo.get(valueRef)
    }
    else valueRef match {
      case null | Unit | None => null

      case arg: java.lang.Boolean => arg
      case arg: java.lang.Byte => arg
      case arg: java.lang.Character => arg
      case arg: java.lang.Short => arg
      case arg: java.lang.Integer => arg
      case arg: java.lang.Long => arg
      case arg: java.lang.Float => arg
      case arg: java.lang.Double => arg
      case arg: java.lang.Number => Double.box(arg.doubleValue())
      case arg: java.lang.String => arg

      case arg: Array[Boolean] => arg
      case arg: Array[Byte] => arg
      case arg: Array[Character] => arg
      case arg: Array[Short] => arg
      case arg: Array[Integer] => arg
      case arg: Array[Long] => arg
      case arg: Array[Float] => arg
      case arg: Array[Double] => arg
      case arg: Array[String] => arg

      case arg: Value => arg

      case arg: Array[_] => convertList(arg, arg.zipWithIndex.iterator, memo)
      case arg: Product => convertList(arg, arg.productIterator.zipWithIndex, memo)
      case arg: Seq[_] => convertList(arg, arg.zipWithIndex.iterator, memo)

      case arg: Map[_, _] => convertMap(arg, arg, memo)
      case arg: mutable.Map[_, _] => convertMap(arg, arg.toMap, memo)
      case arg: java.util.Map[_, _] => convertMap(arg, arg.toMap, memo)

      case arg: Iterable[_] => convertList(arg, arg.zipWithIndex.toIterator, memo)
      case arg: java.lang.Iterable[_] => convertList(arg, arg.zipWithIndex.iterator, memo)

      case arg =>
        val converted = new util.HashMap[AnyRef, AnyRef]()
        memo += arg -> converted
        converters.foreach(converter => try converter.convert(arg, converted) catch {
          case t: Throwable => Ocelot.log.warn("Type converter threw an exception.", t)
        })
        if (converted.isEmpty) {
          memo += arg -> arg.toString
          arg.toString
        }
        else {
          // This is a little nasty but necessary because we need to keep the
          // 'converted' value up-to-date for any reference created to it in
          // the following convertRecursively call. For example:
          // - Converter C is called for A with map M.
          // - C puts A into M again.
          // - convertRecursively(M) encounters A in the memoization map, uses M.
          //   That M is then 'wrong', as in not fully converted. Hence the clear
          //   plus copy action afterwards.
          memo += converted -> converted // Makes convertMap re-use the map.
          convertRecursively(converted, memo, force = true)
          memo -= converted
          if (converted.size == 1 && converted.containsKey("oc:flatten")) {
            val value = converted.get("oc:flatten")
            memo += arg -> value // Update memoization map.
            value
          }
          else {
            converted
          }
        }
    }
  }

  def convertList(obj: AnyRef, list: Iterator[(Any, Int)], memo: util.IdentityHashMap[AnyRef, AnyRef]): Array[AnyRef] = {
    val converted = mutable.ArrayBuffer.empty[AnyRef]
    memo += obj -> converted
    for ((value, _) <- list) {
      converted += convertRecursively(value, memo)
    }
    converted.toArray
  }

  def convertMap(obj: AnyRef, map: Map[_, _], memo: util.IdentityHashMap[AnyRef, AnyRef]): AnyRef = {
    val converted = memo.getOrElseUpdate(obj, mutable.Map.empty[AnyRef, AnyRef]) match {
      case map: mutable.Map[AnyRef, AnyRef]@unchecked => map
      case map: java.util.Map[AnyRef, AnyRef]@unchecked => mapAsScalaMap(map)
    }
    map.collect {
      case (key: AnyRef, value: AnyRef) => converted += convertRecursively(key, memo) -> convertRecursively(value, memo)
    }
    memo.get(obj)
  }
}
