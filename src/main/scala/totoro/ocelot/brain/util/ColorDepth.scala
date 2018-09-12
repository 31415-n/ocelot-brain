package totoro.ocelot.brain.util

/**
  * Used when setting a buffer's maximum color depth.
  */
object ColorDepth extends Enumeration {
  type ColorDepth = Value
  val

  /**
    * Monochrome color, black and white.
    */
  OneBit,

  /**
    * 16 color palette, defaults to Minecraft colors.
    */
  FourBit,

  /**
    * 240 colors, 16 color palette, defaults to grayscale.
    */
  EightBit = Value
}
