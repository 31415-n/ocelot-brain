package li.cil.oc.common.item

import li.cil.oc.Settings

class HardDiskDrive(val tier: Int) extends traits.FileSystemLike {
  val kiloBytes: Int = Settings.get.hddSizes(tier)
  val platterCount: Int = Settings.get.hddPlatterCounts(tier)
}
