package li.cil.oc.util

import li.cil.oc.api.machine.Arguments
import net.minecraft.util.EnumFacing

import scala.language.implicitConversions

object ExtendedArguments {

  implicit def extendedArguments(args: Arguments): ExtendedArguments = new ExtendedArguments(args)

  class ExtendedArguments(val args: Arguments) {

    def checkSideAny(index: Int): EnumFacing = checkSide(index, EnumFacing.values: _*)

    private def checkSide(index: Int, allowed: EnumFacing*) = {
      val side = args.checkInteger(index)
      if (side < 0 || side > 5) {
        throw new IllegalArgumentException("invalid side")
      }
      val direction = EnumFacing.getFront(side)
      if (allowed.isEmpty || (allowed contains direction)) direction
      else throw new IllegalArgumentException("unsupported side")
    }
  }
}
