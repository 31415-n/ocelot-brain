package totoro.ocelot.brain.util

object Direction extends Enumeration {
  type Direction = Value
  val Down,         /* -Y */
      Up,           /* +Y */
      North,        /* -Z */
      South,        /* +Z */
      West,         /* -X */
      East = Value  /* +X */
}
