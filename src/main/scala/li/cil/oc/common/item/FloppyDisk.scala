package li.cil.oc.common.item

import li.cil.oc.Settings

class FloppyDisk(val parent: Delegator) extends traits.Delegate with traits.FileSystemLike {
  // Necessary for anonymous subclasses used for loot disks.
  override def unlocalizedName = "floppydisk"

  val kiloBytes: Int = Settings.get.floppySize
}
