package totoro.ocelot.brain.entity.machine

import java.util

/**
  * A converter is a callback that can be used to transparently convert Java
  * types to something that can be pushed to a machine's architecture.
  *
  * Note that converters operating on the same object type may override each
  * other when using the same keys in the resulting `Map`. The order in
  * which converters are called depends on the order they were registered in.
  */
trait Converter {
  /**
    * Converts a type to a Map.
    *
    * The keys and values in the resulting map will be converted in turn.
    * If after those conversions the map still contains unsupported values,
    * they will not be retained.
    *
    * The conversion result should be placed into the the passed map, i.e. the
    * map will represent the original object. For example, if the value had a
    * field `name`, add a key `name` to the map with the value
    * of that field.
    *
    * @param value  the value to convert.
    * @param output the map conversion results are accumulated into.
    */
  def convert(value: Any, output: util.Map[AnyRef, AnyRef]): Unit
}
