package totoro.ocelot.brain

import scala.language.implicitConversions

package object environment {
  implicit def result(args: Any*): Array[AnyRef] = li.cil.oc.util.ResultWrapper.result(args: _*)
}
