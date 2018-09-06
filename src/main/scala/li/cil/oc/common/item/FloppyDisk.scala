package li.cil.oc.common.item

import li.cil.oc.Settings

class FloppyDisk() extends traits.FileSystemLike {
  override def kiloBytes: Int = Settings.get.floppySize
}
