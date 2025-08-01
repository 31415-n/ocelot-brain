package totoro.ocelot.brain.util

import totoro.ocelot.brain.Settings
import totoro.ocelot.brain.nbt._
import totoro.ocelot.brain.workspace.Workspace

import java.lang

/**
  * This stores chars in a 2D-Array and provides some manipulation functions.
  *
  * The main purpose of this is to allow moving most implementation detail to
  * the Lua side while keeping bandwidth costs low and still allowing for
  * relatively fast updates, given a smart algorithm (using copy()/fill()
  * instead of set()ing everything).
  */
class GenericTextBuffer(var width: Int, var height: Int, initialFormat: PackedColor.ColorFormat) {

  def this(size: (Int, Int), format: PackedColor.ColorFormat) = this(size._1, size._2, format)

  private var _format = initialFormat

  private var _foreground = PackedColor.Color(0xFFFFFF)

  private var _background = PackedColor.Color(0x000000)

  private var packed = PackedColor.pack(_foreground, _background, _format)

  def foreground: PackedColor.Color = _foreground

  def foreground_=(value: PackedColor.Color): GenericTextBuffer = {
    format.validate(value)
    _foreground = value
    packed = PackedColor.pack(_foreground, _background, _format)
    this
  }

  def background: PackedColor.Color = _background

  def background_=(value: PackedColor.Color): GenericTextBuffer = {
    format.validate(value)
    _background = value
    packed = PackedColor.pack(_foreground, _background, _format)
    this
  }

  def format: PackedColor.ColorFormat = _format

  def format_=(value: PackedColor.ColorFormat): Boolean = {
    if (format.depth != value.depth) {
      for (row <- 0 until height) {
        val rowColor = color(row)
        for (col <- 0 until width) {
          val packed = rowColor(col)
          val fg = PackedColor.Color(PackedColor.unpackForeground(packed, _format))
          val bg = PackedColor.Color(PackedColor.unpackBackground(packed, _format))
          rowColor(col) = PackedColor.pack(fg, bg, value)
        }
      }
      _format = value
      packed = PackedColor.pack(_foreground, _background, _format)
      true
    }
    else false
  }

  var color: Array[Array[Short]] = Array.fill(height, width)(packed)

  var buffer: Array[Array[Int]] = Array.fill(height, width)(0x20)

  /** The current buffer size in columns by rows. */
  def size: (Int, Int) = (width, height)

  /**
    * Set the new buffer size, returns true if the size changed.
    *
    * This will perform a proper resize as required, keeping as much of the
    * buffer valid as possible if the size decreases, i.e. only data outside the
    * new buffer size will be truncated, all data still inside will be copied.
    */
  def size_=(value: (Int, Int)): Boolean = {
    val (iw, ih) = value
    val (w, h) = (math.max(iw, 1), math.max(ih, 1))
    if (width != w || height != h) {
      val newBuffer = Array.fill(h, w)(0x20)
      val newColor = Array.fill(h, w)(packed)
      (0 until math.min(h, height)).foreach(y => {
        Array.copy(buffer(y), 0, newBuffer(y), 0, math.min(w, width))
        Array.copy(color(y), 0, newColor(y), 0, math.min(w, width))
      })
      buffer = newBuffer
      color = newColor
      width = w
      height = h
      true
    }
    else false
  }

  /** Get the char at the specified index. */
  def get(col: Int, row: Int): Int = {
    if (col < 0 || col >= width || row < 0 || row >= height)
      throw new IndexOutOfBoundsException()
    else buffer(row)(col)
  }

  /** String based fill starting at a specified location. */
  def set(col: Int, row: Int, s: String, vertical: Boolean): Boolean = {
    val sLength = ExtendedUnicodeHelper.length(s)
    if (vertical) {
      if (col < 0 || col >= width) false
      else {
        var changed = false
        var cx = 0
        for (y <- row until math.min(row + sLength, height)) if (y >= 0) {
          val line = buffer(y)
          val lineColor = color(y)
          val c = s.codePointAt(cx)
          changed = changed || (line(col) != c) || (lineColor(col) != packed)
          setChar(line, lineColor, col, c)
          cx = s.offsetByCodePoints(cx, 1)
        }
        changed
      }
    }
    else {
      if (row < 0 || row >= height) false
      else {
        var changed = false
        val line = buffer(row)
        val lineColor = color(row)
        var bx = math.max(col, 0)
        var cx = 0
        for (x <- bx until math.min(col + sLength, width) if bx < line.length) {
          val c = s.codePointAt(cx)
          changed = changed || (line(bx) != c) || (lineColor(bx) != packed)
          setChar(line, lineColor, bx, c)
          bx += math.max(1, FontUtils.wcwidth(c))
          cx = s.offsetByCodePoints(cx, 1)
        }
        changed
      }
    }
  }

  /** Fills an area of the buffer with the specified character. */
  def fill(col: Int, row: Int, w: Int, h: Int, c: Int): Boolean = {
    // Anything to do at all?
    if (w <= 0 || h <= 0) return false
    if (col + w < 0 || row + h < 0 || col >= width || row >= height) return false
    var changed = false
    for (y <- math.max(row, 0) until math.min(row + h, height)) {
      val line = buffer(y)
      val lineColor = color(y)
      var bx = math.max(col, 0)
      for (_ <- bx until math.min(col + w, width) if bx < line.length) {
        changed = changed || (line(bx) != c) || (lineColor(bx) != packed)
        setChar(line, lineColor, bx, c)
        bx += math.max(1, FontUtils.wcwidth(c))
      }
    }
    changed
  }

  /** Copies a portion of the buffer. */
  def copy(col: Int, row: Int, w: Int, h: Int, tx: Int, ty: Int): Boolean = {
    // Anything to do at all?
    if (w <= 0 || h <= 0) return false
    if (tx == 0 && ty == 0) return false
    // Loop over the target rectangle, starting from the directions away from
    // the source rectangle and copy the data. This way we ensure we don't
    // overwrite anything we still need to copy.
    val (dx0, dx1) = (math.max(0, math.min(width - 1, col + tx + w - 1)), math.max(0, math.min(width, col + tx))) match {
      case dx if tx > 0 => dx
      case dx => dx.swap
    }
    val left_edge = math.min(dx0, dx1) - 1
    if (left_edge >= width - 1) return false // no work
    val (dy0, dy1) = (math.max(0, math.min(height - 1, row + ty + h - 1)), math.max(0, math.min(height, row + ty))) match {
      case dy if ty > 0 => dy
      case dy => dy.swap
    }
    val (sx, sy) = (if (tx > 0) -1 else 1, if (ty > 0) -1 else 1)
    // Copy values to destination rectangle if there source is valid.
    var changed = false
    for (ny <- dy0 to dy1 by sy) {
      val nl = buffer(ny)
      val nc = color(ny)
      ny - ty match {
        case oy if oy >= 0 && oy < height =>
          val ol = buffer(oy)
          val oc = color(oy)
          for (nx <- dx0 to dx1 by sx) nx - tx match {
            case ox if ox >= 0 && ox < width =>
              changed = changed || (nl(nx) != ol(ox)) || (nc(nx) != oc(ox))
              nl(nx) = ol(ox)
              nc(nx) = oc(ox)
              for (offset <- 1 until FontUtils.wcwidth(nl(nx))) {
                nl(nx + offset) = ' '
                nc(nx + offset) = oc(nx)
              }
            case _ => /* Got no source column. */
          }
          // any wide chars along the left edge of the target rectangle need to be cleared
          // don't change their colors
          if (left_edge >= 0 && FontUtils.wcwidth(nl(left_edge)) > 1) {
            nl(left_edge) = ' '
            changed = true
          }
        case _ => /* Got no source row. */
      }
    }
    changed
  }

  // copy a portion of another buffer into this buffer
  def rawcopy(col: Int, row: Int, w: Int, h: Int, src: GenericTextBuffer, fromCol: Int, fromRow: Int): Boolean = {
    var changed: Boolean = false
    val col_index = col - 1
    val row_index = row - 1
    for (yOffset <- 0 until h) {
      val dstCharLine = buffer(row_index + yOffset)
      val dstColorLine = color(row_index + yOffset)
      for (xOffset <- 0 until w) {
        val srcChar = src.buffer(fromRow + yOffset - 1)(fromCol + xOffset - 1)
        var srcColor = src.color(fromRow + yOffset - 1)(fromCol + xOffset - 1)

        if (this.format.depth != src.format.depth) {
          val fg = PackedColor.Color(PackedColor.unpackForeground(srcColor, src.format))
          val bg = PackedColor.Color(PackedColor.unpackBackground(srcColor, src.format))
          srcColor = PackedColor.pack(fg, bg, format)
        }

        if (srcChar != dstCharLine(col_index + xOffset) || srcColor != dstColorLine(col_index + xOffset)) {
          changed = true
          dstCharLine(col_index + xOffset) = srcChar
          dstColorLine(col_index + xOffset) = srcColor
        }
      }
    }

    changed
  }

  private def setChar(line: Array[Int], lineColor: Array[Short], x: Int, c: Int): Unit = {
    if (FontUtils.wcwidth(c) > 1 && x >= line.length - 1) {
      // Don't allow setting wide chars in right-most col.
      return
    }
    line(x) = c
    lineColor(x) = packed
    for (x1 <- x + 1 until x + FontUtils.wcwidth(c)) {
      line(x1) = ' '
      lineColor(x1) = packed
    }
    if (x > 0 && FontUtils.wcwidth(line(x - 1)) > 1) {
      // remove previous wide char (but don't change its color)
      line(x - 1) = ' '
    }
  }

  def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    val maxResolution = math.max(Settings.screenResolutionsByTier.last._1, Settings.screenResolutionsByTier.last._2)
    val w = nbt.getInteger("width") min maxResolution max 1
    val h = nbt.getInteger("height") min maxResolution max 1
    size = (w, h)

    val b = nbt.getTagList("buffer", NBT.TAG_STRING)
    for (i <- 0 until math.min(h, b.tagCount)) {
      val value = b.getStringTagAt(i)
      val valueIt = value.codePoints.iterator()
      var j = 0
      while (j < buffer(i).length && valueIt.hasNext) {
        buffer(i)(j) = valueIt.nextInt()
        j += 1
      }
    }

    val depth = ColorDepth(nbt.getInteger("depth") min ColorDepth.maxId max 0)
    _format = PackedColor.Depth.format(depth)
    _format.load(nbt, workspace)
    foreground = PackedColor.Color(nbt.getInteger("foreground"), nbt.getBoolean("foregroundIsPalette"))
    background = PackedColor.Color(nbt.getInteger("background"), nbt.getBoolean("backgroundIsPalette"))

    if (!NbtDataStream.getShortArray(nbt, "colors", color, w, h)) {
      NbtDataStream.getIntArrayLegacy(nbt, "color", color, w, h)
    }
  }

  def save(nbt: NBTTagCompound): Unit = {
    nbt.setInteger("width", width)
    nbt.setInteger("height", height)

    val b = new NBTTagList()
    for (i <- 0 until height) {
      b.appendTag(new NBTTagString(lineToString(i)))
    }
    nbt.setTag("buffer", b)

    nbt.setInteger("depth", _format.depth.id)
    _format.save(nbt)
    nbt.setInteger("foreground", _foreground.value)
    nbt.setBoolean("foregroundIsPalette", _foreground.isPalette)
    nbt.setInteger("background", _background.value)
    nbt.setBoolean("backgroundIsPalette", _background.isPalette)

    NbtDataStream.setShortArray(nbt, "colors", color.flatten)
  }

  def lineToString(y: Int): String = {
    val b = new lang.StringBuilder()
    if (buffer.length > 0) {
      for (x <- 0 until width) {
        b.appendCodePoint(buffer(y)(x))
      }
    }
    b.toString
  }

  override def toString: String = {
    val b = new lang.StringBuilder()
    if (buffer.length > 0) {
      for (x <- 0 until width) {
        b.appendCodePoint(buffer(0)(x))
      }
      for (y <- 1 until height) {
        b.append('\n')
        for (x <- 0 until width) {
          b.appendCodePoint(buffer(y)(x))
        }
      }
    }
    b.toString
  }
}
