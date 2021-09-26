package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.util.{ColorDepth, GenericTextBuffer, PackedColor}

trait TextBufferProxy {
  def data: GenericTextBuffer

  /**
    * Get the current horizontal resolution.
    *
    * @see `setResolution(int, int)`
    */
  def getWidth: Int = data.width

  /**
    * Get the current vertical resolution.
    *
    * @see `setResolution(int, int)`
    */
  def getHeight: Int = data.height

  /**
    * Sets the maximum color depth supported by this buffer.
    *
    * Note that this is the ''maximum'' supported depth, lower depths
    * will be supported, too. So when setting this to four bit, one bit will
    * be supported, too. When setting this to eight bit, four and one bit
    * will be supported, also.
    *
    * @param depth the maximum color depth of the buffer.
    */
  def setMaximumColorDepth(depth: ColorDepth.Value): Unit

  /**
    * Get the maximum color depth supported.
    */
  def getMaximumColorDepth: ColorDepth.Value

  /**
    * Set the active color depth for this buffer.
    *
    * @param depth the new color depth.
    * @return `true` if the color depth changed.
    */
  def setColorDepth(depth: ColorDepth.Value): Boolean = {
    if (depth.id > getMaximumColorDepth.id)
      throw new IllegalArgumentException("unsupported depth")
    data.format = PackedColor.Depth.format(depth)
  }

  /**
    * Get the active color depth of this buffer.
    */
  def getColorDepth: ColorDepth.Value = data.format.depth

  def onBufferPaletteColorChange(index: Int, color: Int): Unit = {}

  /**
    * Set the color of the active color palette at the specified index.
    *
    * This will error if the current depth does not have a palette (one bit).
    *
    * @param index the index at which to set the color.
    * @param color the color to set for the specified index.
    */
  def setPaletteColor(index: Int, color: Int): Unit = data.format match {
    case palette: PackedColor.MutablePaletteFormat =>
      palette(index) = color
      onBufferPaletteColorChange(index, color)
    case _ => throw new Exception("palette not available")
  }

  /**
    * Get the color in the active color palette at the specified index.
    *
    * This will error if the current depth does not have a palette (one bit).
    *
    * @param index the index at which to get the color.
    * @return the color in the active palette at the specified index.
    */
  def getPaletteColor(index: Int): Int = data.format match {
    case palette: PackedColor.MutablePaletteFormat => palette(index)
    case _ => throw new Exception("palette not available")
  }

  def onBufferForegroundColorChange(color: PackedColor.Color): Unit = {}

  /**
    * Set the active foreground color, not using a palette.
    *
    * @param color the new foreground color.
    * @see `setForegroundColor(int, boolean)`
    */
  def setForegroundColor(color: Int): Unit = setForegroundColor(color, isFromPalette = false)

  /**
    * Set the active foreground color.
    *
    * If the value is not from the palette, the actually stored value may
    * differ from the specified one, as it is converted to the buffer's
    * current color depth.
    *
    * For palette-only color formats (four bit) the best fit from the palette
    * is chosen, if the value is not from the palette.
    *
    * @param color         the color or palette index.
    * @param isFromPalette `true` if `color` specifies a palette index.
    */
  def setForegroundColor(color: Int, isFromPalette: Boolean): Unit = {
    val value = PackedColor.Color(color, isFromPalette)
    if (data.foreground != value) {
      data.foreground = value
      onBufferForegroundColorChange(value)
    }
  }

  /**
    * The active foreground color.
    */
  def getForegroundColor: Int = data.foreground.value

  /**
    * `true` if the foreground color is from the color palette, meaning
    * the value returned from `getForegroundColor()` is the color
    * palette index.
    */
  def isForegroundFromPalette: Boolean = data.foreground.isPalette

  def onBufferBackgroundColorChange(color: PackedColor.Color): Unit = {}

  /**
    * Set the active background color, not using a palette.
    *
    * @param color the new background color.
    * @see `setBackgroundColor(int, boolean)`
    */
  def setBackgroundColor(color: Int): Unit = setBackgroundColor(color, isFromPalette = false)

  /**
    * Set the active background color.
    *
    * If the value is not from the palette, the actually stored value may
    * differ from the specified one, as it is converted to the buffer's
    * current color depth.
    *
    * For palette-only color formats (four bit) the best fit from the palette
    * is chosen, if the value is not from the palette.
    *
    * @param color         the color or palette index.
    * @param isFromPalette `true` if `color` specifies a palette index.
    */
  def setBackgroundColor(color: Int, isFromPalette: Boolean): Unit = {
    val value = PackedColor.Color(color, isFromPalette)
    if (data.background != value) {
      data.background = value
      onBufferBackgroundColorChange(value)
    }
  }

  /**
    * The active background color.
    */
  def getBackgroundColor: Int = data.background.value

  /**
    * `true` if the background color is from the color palette, meaning
    * the value returned from `getBackgroundColor()` is the color
    * palette index.
    */
  def isBackgroundFromPalette: Boolean = data.background.isPalette

  def onBufferCopy(col: Int, row: Int, w: Int, h: Int, tx: Int, ty: Int): Unit = {}

  /**
    * Copy a portion of the text buffer.
    *
    * This will copy the area's text and colors.
    *
    * @param column                the starting horizontal index of the area to copy.
    * @param row                   the starting vertical index of the area to copy.
    * @param width                 the width of the area to copy.
    * @param height                the height of the area to copy.
    * @param horizontalTranslation the horizontal offset, relative to the starting column to copy the are to.
    * @param verticalTranslation   the vertical offset, relative to the starting row to copy the are to.
    */
  def copy(column: Int, row: Int, width: Int, height: Int, horizontalTranslation: Int, verticalTranslation: Int): Unit =
    if (data.copy(column, row, width, height, horizontalTranslation, verticalTranslation))
      onBufferCopy(column, row, width, height, horizontalTranslation, verticalTranslation)

  def onBufferFill(col: Int, row: Int, w: Int, h: Int, c: Char): Unit = {}

  /**
    * Fill a portion of the text buffer.
    *
    * This will set the area's colors to the currently active ones.
    *
    * @param column the starting horizontal index of the area to fill.
    * @param row    the starting vertical index of the area to fill.
    * @param width  the width of the area to fill.
    * @param height the height of the area to fill.
    * @param value  the character to fill the area with.
    */
  def fill(column: Int, row: Int, width: Int, height: Int, value: Char): Unit =
    if (data.fill(column, row, width, height, value))
      onBufferFill(column, row, width, height, value)

  def onBufferSet(col: Int, row: Int, s: String, vertical: Boolean): Unit = {}

  /**
    * Write a string into the text buffer.
    *
    * This will apply the currently active colors to the changed area.
    *
    * @param column   the starting horizontal index to write at.
    * @param row      the starting vertical index to write at.
    * @param value    the string to write.
    * @param vertical `true` if the string should be written vertically instead of horizontally.
    */
  def set(column: Int, row: Int, value: String, vertical: Boolean): Unit =
    if (column < data.width && (column >= 0 || -column < value.length)) {
      // Make sure the string isn't longer than it needs to be, in particular to
      // avoid sending too much data to our clients.
      val (x, y, truncated) =
      if (vertical) {
        if (row < 0) (column, 0, value.substring(-row))
        else (column, row, value.substring(0, math.min(value.length, data.height - row)))
      }
      else {
        if (column < 0) (0, row, value.substring(-column))
        else (column, row, value.substring(0, math.min(value.length, data.width - column)))
      }
      if (data.set(x, y, truncated, vertical))
        onBufferSet(x, row, truncated, vertical)
    }

  /**
    * Get the character in the text buffer at the specified location.
    *
    * @param column the horizontal index.
    * @param row    the vertical index.
    * @return the character at that index.
    */
  def get(column: Int, row: Int): Char = data.get(column, row)

  /**
    * Get the foreground color of the text buffer at the specified location.
    *
    * '''Important''': this may be a palette index.
    *
    * @param column the horizontal index.
    * @param row    the vertical index.
    * @return the foreground color at that index.
    */
  def getForegroundColor(column: Int, row: Int): Int =
    if (isForegroundFromPalette(column, row)) {
      PackedColor.extractForeground(color(column, row))
    }
    else {
      PackedColor.unpackForeground(color(column, row), data.format)
    }

  /**
    * Whether the foreground color of the text buffer at the specified
    * location if from the color palette.
    *
    * @param column the horizontal index.
    * @param row    the vertical index.
    * @return whether the foreground at that index is from the palette.
    */
  def isForegroundFromPalette(column: Int, row: Int): Boolean =
    data.format.isFromPalette(PackedColor.extractForeground(color(column, row)))

  /**
    * Get the background color of the text buffer at the specified location.
    *
    * '''Important''': this may be a palette index.
    *
    * @param column the horizontal index.
    * @param row    the vertical index.
    * @return the background color at that index.
    */
  def getBackgroundColor(column: Int, row: Int): Int =
    if (isBackgroundFromPalette(column, row)) {
      PackedColor.extractBackground(color(column, row))
    }
    else {
      PackedColor.unpackBackground(color(column, row), data.format)
    }

  /**
    * Whether the background color of the text buffer at the specified
    * location if from the color palette.
    *
    * @param column the horizontal index.
    * @param row    the vertical index.
    * @return whether the background at that index is from the palette.
    */
  def isBackgroundFromPalette(column: Int, row: Int): Boolean =
    data.format.isFromPalette(PackedColor.extractBackground(color(column, row)))

  /**
    * Overwrites a portion of the text in raw mode.
    *
    * This will copy the given char array into the buffer, starting at the
    * specified column and row. The array is expected to be indexed row-
    * first, i.e. the first dimension is the vertical axis, the second
    * the horizontal.
    *
    * '''Important''': this performs no checks as to whether something
    * actually changed. It will always send the changed patch to clients.
    * It will also not crop the specified array to the actually used range.
    * In other words, this is not intended to be exposed as-is to user code,
    * it should always be called with validated, and, as necessary, cropped
    * values.
    *
    * @param column the horizontal index.
    * @param row    the vertical index.
    * @param text   the text to write.
    */
  def rawSetText(column: Int, row: Int, text: Array[Array[Char]]): Unit = {
    for (y <- row until ((row + text.length) min data.height)) {
      val line = text(y - row)
      Array.copy(line, 0, data.buffer(y), column, line.length min data.width)
    }
  }

  /**
    * Overwrites a portion of the foreground color information in raw mode.
    *
    * This will convert the specified RGB data (in `0xRRGGBB` format)
    * to the internal, packed representation and copy it into the buffer,
    * starting at the specified column and row. The array is expected to be
    * indexed row-first, i.e. the first dimension is the vertical axis, the
    * second the horizontal.
    *
    * '''Important''': this performs no checks as to whether something
    * actually changed. It will always send the changed patch to clients.
    * It will also not crop the specified array to the actually used range.
    * In other words, this is not intended to be exposed as-is to user code,
    * it should always be called with validated, and, as necessary, cropped
    * values.
    *
    * @param column the horizontal index.
    * @param row    the vertical index.
    * @param color  the foreground color data to write.
    */
  def rawSetForeground(column: Int, row: Int, color: Array[Array[Int]]): Unit = {
    for (y <- row until ((row + color.length) min data.height)) {
      val line = color(y - row)
      for (x <- column until ((column + line.length) min data.width)) {
        val packedBackground = data.format.deflate(PackedColor.Color(line(x - column))) & 0x00FF
        val packedForeground = data.color(row)(column) & 0xFF00
        data.color(row)(column) = (packedForeground | packedBackground).toShort
      }
    }
  }

  /**
    * Overwrites a portion of the background color information in raw mode.
    *
    * This will convert the specified RGB data (in `0xRRGGBB` format)
    * to the internal, packed representation and copy it into the buffer,
    * starting at the specified column and row. The array is expected to be
    * indexed row-first, i.e. the first dimension is the vertical axis, the
    * second the horizontal.
    *
    * '''Important''': this performs no checks as to whether something
    * actually changed. It will always send the changed patch to clients.
    * It will also not crop the specified array to the actually used range.
    * In other words, this is not intended to be exposed as-is to user code,
    * it should always be called with validated, and, as necessary, cropped
    * values.
    *
    * @param column the horizontal index.
    * @param row    the vertical index.
    * @param color  the background color data to write.
    */
  def rawSetBackground(column: Int, row: Int, color: Array[Array[Int]]): Unit = {
    for (y <- row until ((row + color.length) min data.height)) {
      val line = color(y - row)
      for (x <- column until ((column + line.length) min data.width)) {
        val packedBackground = data.color(row)(column) & 0x00FF
        val packedForeground = (data.format.deflate(PackedColor.Color(line(x - column))) << PackedColor.ForegroundShift) & 0xFF00
        data.color(row)(column) = (packedForeground | packedBackground).toShort
      }
    }
  }

  private def color(column: Int, row: Int): Short = {
    if (column < 0 || column >= getWidth || row < 0 || row >= getHeight)
      throw new IndexOutOfBoundsException()
    else data.color(row)(column)
  }
}
