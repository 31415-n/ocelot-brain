package totoro.ocelot.brain.util

object DyeColor {
  val WHITE: DyeColor = DyeColor(0, "dyeWhite")
  val ORANGE: DyeColor = DyeColor(1, "dyeOrange")
  val MAGENTA: DyeColor = DyeColor(2, "dyeMagenta")
  val LIGHT_BLUE: DyeColor = DyeColor(3, "dyeLightBlue")
  val YELLOW: DyeColor = DyeColor(4, "dyeYellow")
  val LIME: DyeColor = DyeColor(5, "dyeLime")
  val PINK: DyeColor = DyeColor(6, "dyePink")
  val GRAY: DyeColor = DyeColor(7, "dyeGray")
  val SILVER: DyeColor = DyeColor(8, "dyeLightGray")
  val CYAN: DyeColor = DyeColor(9, "dyeCyan")
  val PURPLE: DyeColor = DyeColor(10, "dyePurple")
  val BLUE: DyeColor = DyeColor(11, "dyeBlue")
  val BROWN: DyeColor = DyeColor(12, "dyeBrown")
  val GREEN: DyeColor = DyeColor(13, "dyeGreen")
  val RED: DyeColor = DyeColor(14, "dyeRed")
  val BLACK: DyeColor = DyeColor(15, "dyeBlack")

  val ALL: List[DyeColor] = List(
    WHITE, ORANGE, MAGENTA, LIGHT_BLUE, YELLOW, LIME, PINK, GRAY, SILVER, CYAN, PURPLE, BLUE, BROWN, GREEN, RED, BLACK)

  def byName(name: String): Option[DyeColor] = ALL.find(color => color.name == name)
  def byCode(code: Int): DyeColor = ALL(code)
}

case class DyeColor(code: Int, name: String)
