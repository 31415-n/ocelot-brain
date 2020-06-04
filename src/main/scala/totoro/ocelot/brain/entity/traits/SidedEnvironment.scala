package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.network.Node
import totoro.ocelot.brain.util.Direction

/**
  * This trait allows an environment to
  * specify different node access for its different sides.
  *
  * This trait is intended to be used on entities that are environments.
  */
trait SidedEnvironment {
  /**
    * The node this environment uses for the specified side.
    *
    * This is the side aware version of the normal [[Environment.node]]
    * method.
    *
    * @param side the side to get the node for.
    * @return the node for the specified side.
    * @see [[Environment.node]]
    */
  def sidedNode(side: Direction.Value): Node

  /**
    * Whether the environment provides a node to connect to on the specified
    * side.
    *
    * For each side the environment returns `false` here, it should
    * return `null` from [[sidedNode]], and for each side it
    * returns `true` for it should return a node.
    *
    * @param side the side to check for.
    * @return whether the environment provides a node for the specified side.
    */
  def canConnect(side: Direction.Value): Boolean

  // convenience connection methods
  def connect(sourceSide: Direction.Value, target: SidedEnvironment, targetSide: Direction.Value): Unit =
    if (canConnect(sourceSide) && target.canConnect(targetSide)) sidedNode(sourceSide).connect(target.sidedNode(targetSide))

  def connect(sourceSide: Direction.Value, target: Environment): Unit =
    if (canConnect(sourceSide)) sidedNode(sourceSide).connect(target.node)

  def connect(sourceSide: Direction.Value, target: Node): Unit =
    if (canConnect(sourceSide)) sidedNode(sourceSide).connect(target)

  def disconnect(sourceSide: Direction.Value, target: SidedEnvironment, targetSide: Direction.Value): Unit =
    if (canConnect(sourceSide) && target.canConnect(targetSide)) sidedNode(sourceSide).disconnect(target.sidedNode(targetSide))

  def disconnect(sourceSide: Direction.Value, target: Environment): Unit =
    if (canConnect(sourceSide)) sidedNode(sourceSide).disconnect(target.node)

  def disconnect(sourceSide: Direction.Value, target: Node): Unit =
    if (canConnect(sourceSide)) sidedNode(sourceSide).disconnect(target)
}
