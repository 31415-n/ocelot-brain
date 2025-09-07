package totoro.ocelot.brain.util

import totoro.ocelot
import totoro.ocelot.brain
import totoro.ocelot.brain.util

object Direction extends Enumeration {
  type Direction = DirectionVal

  val Down: util.Direction.Direction = DirectionVal("down", "bottom") // -Y
  val Up: brain.util.Direction.Direction = DirectionVal("up", "top") // +Y
  val North: ocelot.brain.util.Direction.Direction = DirectionVal("north", "back") // -Z
  val South: DirectionVal = DirectionVal("south", "front") // +Z
  val West: DirectionVal = DirectionVal("west", "right") // -X
  val East: DirectionVal = DirectionVal("east", "left") // +X

  val Bottom: Direction = Down
  val Top: Direction = Up
  val Back: Direction = North
  val Front: Direction = South
  val Right: Direction = West
  val Left: Direction = East

  protected case class DirectionVal(cardinal: String, side: String) extends super.Val

  import scala.language.implicitConversions
  implicit def valueToDirectionVal(x: Value): DirectionVal = x.asInstanceOf[DirectionVal]
}