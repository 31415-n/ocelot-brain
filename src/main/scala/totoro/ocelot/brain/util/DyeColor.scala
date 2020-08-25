package totoro.ocelot.brain.util

object DyeColor {
  val WHITE: DyeColor = DyeColor(0, "dyeWhite", 0xF0F0F0)
  val ORANGE: DyeColor = DyeColor(1, "dyeOrange", 0xEB8844)
  val MAGENTA: DyeColor = DyeColor(2, "dyeMagenta", 0xC354CD)
  val LIGHT_BLUE: DyeColor = DyeColor(3, "dyeLightBlue", 0xAAAAFF)
  val YELLOW: DyeColor = DyeColor(4, "dyeYellow", 0xFFFF66)
  val LIME: DyeColor = DyeColor(5, "dyeLime", 0x66FF66)
  val PINK: DyeColor = DyeColor(6, "dyePink", 0xD88198)
  val GRAY: DyeColor = DyeColor(7, "dyeGray", 0x666666)
  val SILVER: DyeColor = DyeColor(8, "dyeLightGray", 0xABABAB)
  val CYAN: DyeColor = DyeColor(9, "dyeCyan", 0x66FFFF)
  val PURPLE: DyeColor = DyeColor(10, "dyePurple", 0x7B2FBE)
  val BLUE: DyeColor = DyeColor(11, "dyeBlue", 0x6666FF)
  val BROWN: DyeColor = DyeColor(12, "dyeBrown", 0x51301A)
  val GREEN: DyeColor = DyeColor(13, "dyeGreen", 0x339911)
  val RED: DyeColor = DyeColor(14, "dyeRed", 0xB3312C)
  val BLACK: DyeColor = DyeColor(15, "dyeBlack", 0x444444)

  val ALL: List[DyeColor] = List(
    WHITE, ORANGE, MAGENTA, LIGHT_BLUE, YELLOW, LIME, PINK, GRAY, SILVER, CYAN, PURPLE, BLUE, BROWN, GREEN, RED, BLACK)

  def byName(name: String): Option[DyeColor] = ALL.find(color => color.name == name)
  def byCode(code: Int): DyeColor = ALL(code)
}

case class DyeColor(code: Int, name: String, rgb: Int)
