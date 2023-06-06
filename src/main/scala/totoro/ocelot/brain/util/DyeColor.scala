package totoro.ocelot.brain.util

object DyeColor {
  val White: DyeColor = DyeColor(0, "dyeWhite", 0xF0F0F0)
  val Orange: DyeColor = DyeColor(1, "dyeOrange", 0xEB8844)
  val Magenta: DyeColor = DyeColor(2, "dyeMagenta", 0xC354CD)
  val LightBlue: DyeColor = DyeColor(3, "dyeLightBlue", 0xAAAAFF)
  val Yellow: DyeColor = DyeColor(4, "dyeYellow", 0xFFFF66)
  val Lime: DyeColor = DyeColor(5, "dyeLime", 0x66FF66)
  val Pink: DyeColor = DyeColor(6, "dyePink", 0xD88198)
  val Gray: DyeColor = DyeColor(7, "dyeGray", 0x666666)
  val Silver: DyeColor = DyeColor(8, "dyeLightGray", 0xABABAB)
  val Cyan: DyeColor = DyeColor(9, "dyeCyan", 0x66FFFF)
  val Purple: DyeColor = DyeColor(10, "dyePurple", 0x7B2FBE)
  val Blue: DyeColor = DyeColor(11, "dyeBlue", 0x6666FF)
  val Brown: DyeColor = DyeColor(12, "dyeBrown", 0x51301A)
  val Green: DyeColor = DyeColor(13, "dyeGreen", 0x339911)
  val Red: DyeColor = DyeColor(14, "dyeRed", 0xB3312C)
  val Black: DyeColor = DyeColor(15, "dyeBlack", 0x444444)

  val All: List[DyeColor] = List(
    White, Orange, Magenta, LightBlue, Yellow, Lime, Pink, Gray, Silver, Cyan, Purple, Blue, Brown, Green, Red, Black)

  def byName(name: String): Option[DyeColor] = All.find(color => color.name == name)
  def byCode(code: Int): DyeColor = All(code)
}

case class DyeColor(code: Int, name: String, rgb: Int)
