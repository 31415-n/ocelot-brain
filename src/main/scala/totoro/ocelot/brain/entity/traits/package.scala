package totoro.ocelot.brain.entity

import totoro.ocelot.brain.util.ResultWrapper

import scala.language.implicitConversions

package object traits {
  implicit def result(args: Any*): Array[AnyRef] = ResultWrapper.result(args: _*)
}
