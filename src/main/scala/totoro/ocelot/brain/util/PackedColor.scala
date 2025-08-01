package totoro.ocelot.brain.util

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.nbt.NBTTagCompound
import totoro.ocelot.brain.workspace.Workspace

object PackedColor {

  object Depth {
    def bits(depth: ColorDepth.Value): Int = depth match {
      case ColorDepth.OneBit => 1
      case ColorDepth.FourBit => 4
      case ColorDepth.EightBit => 8
    }

    def format(depth: ColorDepth.Value): ColorFormat = depth match {
      case ColorDepth.OneBit => SingleBitFormat
      case ColorDepth.FourBit => new MutablePaletteFormat
      case ColorDepth.EightBit => new HybridFormat
    }
  }

  private val rShift32 = 16
  private val gShift32 = 8
  private val bShift32 = 0

  private def extract(value: Int) = {
    val r = (value >>> rShift32) & 0xFF
    val g = (value >>> gShift32) & 0xFF
    val b = (value >>> bShift32) & 0xFF
    (r, g, b)
  }

  trait ColorFormat extends Persistable {
    def depth: ColorDepth.Value

    def inflate(value: Int): Int

    def validate(value: Color): Unit = {
      if (value.isPalette) {
        throw new IllegalArgumentException("color palette not supported")
      }
    }

    def deflate(value: Color): Byte

    def isFromPalette(value: Int): Boolean = false
  }

  class SingleBitFormat(val color: Int) extends ColorFormat {
    override def depth: ColorDepth.Value = ColorDepth.OneBit

    override def inflate(value: Int): Int = if (value == 0) 0x000000 else color

    override def deflate(value: Color): Byte = {
      (if (value.value == 0) 0 else 1).toByte
    }
  }

  object SingleBitFormat extends SingleBitFormat(Settings.get.monochromeColor)

  abstract class PaletteFormat extends ColorFormat {
    override def inflate(value: Int): Int = palette(math.max(0, math.min(palette.length - 1, value)))

    override def validate(value: Color): Unit = {
      if (value.isPalette && (value.value < 0 || value.value >= palette.length)) {
        throw new IllegalArgumentException("invalid palette index")
      }
    }

    override def deflate(value: Color): Byte =
      if (value.isPalette) (math.max(0, value.value) % palette.length).toByte
      else palette.map(delta(value.value, _)).zipWithIndex.minBy(_._1)._2.toByte

    override def isFromPalette(value: Int) = true

    protected def palette: Array[Int]

    protected def delta(colorA: Int, colorB: Int): Double = {
      val (rA, gA, bA) = extract(colorA)
      val (rB, gB, bB) = extract(colorB)
      val dr = rA - rB
      val dg = gA - gB
      val db = bA - bB
      0.2126 * dr * dr + 0.7152 * dg * dg + 0.0722 * db * db
    }
  }

  class MutablePaletteFormat extends PaletteFormat {
    override def depth: ColorDepth.Value = ColorDepth.FourBit

    def apply(index: Int): Int = palette(index)

    def update(index: Int, value: Int): Unit = palette(index) = value

    protected val palette: Array[Int] = Array(
      0xFFFFFF, 0xFFCC33, 0xCC66CC, 0x6699FF,
      0xFFFF33, 0x33CC33, 0xFF6699, 0x333333,
      0xCCCCCC, 0x336699, 0x9933CC, 0x333399,
      0x663300, 0x336600, 0xFF3333, 0x000000)

    override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
      super.load(nbt, workspace)
      val loaded = nbt.getIntArray("palette")
      Array.copy(loaded, 0, palette, 0, math.min(loaded.length, palette.length))
    }

    override def save(nbt: NBTTagCompound): Unit = {
      super.save(nbt)
      nbt.setIntArray("palette", palette)
    }
  }

  class HybridFormat extends MutablePaletteFormat {
    private val reds = 6
    private val greens = 8
    private val blues = 5

    private val staticPalette = new Array[Int](240)

    {
      for (index <- staticPalette.indices) {
        val idxB = index % blues
        val idxG = (index / blues) % greens
        val idxR = (index / blues / greens) % reds
        val r = (idxR * 0xFF / (reds - 1.0) + 0.5).toInt
        val g = (idxG * 0xFF / (greens - 1.0) + 0.5).toInt
        val b = (idxB * 0xFF / (blues - 1.0) + 0.5).toInt
        staticPalette(index) = (r << rShift32) | (g << gShift32) | (b << bShift32)
      }
    }

    // Initialize palette to grayscale, excluding black and white, because
    // those are already contained in the normal color cube.
    for (i <- palette.indices) {
      val shade = 0xFF * (i + 1) / (palette.length + 1)
      this (i) = (shade << rShift32) | (shade << gShift32) | (shade << bShift32)
    }

    override def depth: ColorDepth.Value = ColorDepth.EightBit

    override def inflate(value: Int): Int =
      if (isFromPalette(value)) super.inflate(value)
      else staticPalette((value - palette.length) % 240)

    override def deflate(value: Color): Byte = {
      val paletteIndex = super.deflate(value)
      if (value.isPalette) paletteIndex
      else {
        val (r, g, b) = extract(value.value)
        val idxR = (r * (reds - 1.0) / 0xFF + 0.5).toInt
        val idxG = (g * (greens - 1.0) / 0xFF + 0.5).toInt
        val idxB = (b * (blues - 1.0) / 0xFF + 0.5).toInt
        val deflated = (palette.length + idxR * greens * blues + idxG * blues + idxB).toByte
        if (delta(inflate(deflated & 0xFF), value.value) < delta(inflate(paletteIndex & 0xFF), value.value)) {
          deflated
        }
        else {
          paletteIndex
        }
      }
    }

    override def isFromPalette(value: Int): Boolean = value >= 0 && value < palette.length
  }

  case class Color(value: Int, isPalette: Boolean = false)

  // Colors are packed: 0xFFBB (F = foreground, B = background)
  val ForegroundShift = 8
  val BackgroundMask = 0x000000FF

  def pack(foreground: Color, background: Color, format: ColorFormat): Short = {
    (((format.deflate(foreground) & 0xFF) << ForegroundShift) | (format.deflate(background) & 0xFF)).toShort
  }

  def extractForeground(color: Short): Int = (color & 0xFFFF) >>> ForegroundShift

  def extractBackground(color: Short): Int = color & BackgroundMask

  def unpackForeground(color: Short, format: ColorFormat): Int =
    format.inflate(extractForeground(color))

  def unpackBackground(color: Short, format: ColorFormat): Int =
    format.inflate(extractBackground(color))
}
