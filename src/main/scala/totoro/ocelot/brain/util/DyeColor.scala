package totoro.ocelot.brain.util

object DyeColor {
  val WHITE = DyeColor(0)
  val ORANGE = DyeColor(1)
  val MAGENTA = DyeColor(2)
  val LIGHT_BLUE = DyeColor(3)
  val YELLOW = DyeColor(4)
  val LIME = DyeColor(5)
  val PINK = DyeColor(6)
  val GRAY = DyeColor(7)
  val SILVER = DyeColor(8)
  val CYAN = DyeColor(9)
  val PURPLE = DyeColor(10)
  val BLUE = DyeColor(11)
  val BROWN = DyeColor(12)
  val GREEN = DyeColor(13)
  val RED = DyeColor(14)
  val BLACK = DyeColor(15)

  val byName = Map(
    "dyeBlack" -> DyeColor.BLACK,
    "dyeRed" -> DyeColor.RED,
    "dyeGreen" -> DyeColor.GREEN,
    "dyeBrown" -> DyeColor.BROWN,
    "dyeBlue" -> DyeColor.BLUE,
    "dyePurple" -> DyeColor.PURPLE,
    "dyeCyan" -> DyeColor.CYAN,
    "dyeLightGray" -> DyeColor.SILVER,
    "dyeGray" -> DyeColor.GRAY,
    "dyePink" -> DyeColor.PINK,
    "dyeLime" -> DyeColor.LIME,
    "dyeYellow" -> DyeColor.YELLOW,
    "dyeLightBlue" -> DyeColor.LIGHT_BLUE,
    "dyeMagenta" -> DyeColor.MAGENTA,
    "dyeOrange" -> DyeColor.ORANGE,
    "dyeWhite" -> DyeColor.WHITE)
}

case class DyeColor(code: Int)
