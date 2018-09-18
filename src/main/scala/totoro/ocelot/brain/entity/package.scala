package totoro.ocelot.brain

import totoro.ocelot.brain.util.ResultWrapper

import scala.language.implicitConversions

package object entity {
  implicit def result(args: Any*): Array[AnyRef] = ResultWrapper.result(args: _*)
}
