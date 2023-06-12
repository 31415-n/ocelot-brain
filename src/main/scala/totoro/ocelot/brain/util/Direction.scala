package totoro.ocelot.brain.util

object Direction extends Enumeration {
  type Direction = Value
  val Down,         /* -Y */
      Up,           /* +Y */
      North,        /* -Z */
      South,        /* +Z */
      West,         /* -X */
      East = Value  /* +X */

  val Bottom: Value = Down
  val Top: Value = Up
  val Back: Value = North
  val Front: Value = South
  val Right: Value = West
  val Left: Value = East
}
