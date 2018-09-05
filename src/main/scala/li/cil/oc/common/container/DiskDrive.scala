package li.cil.oc.common.container

import li.cil.oc.common.Slot
import li.cil.oc.common.tileentity

class DiskDrive(drive: tileentity.DiskDrive) extends Player(playerInventory, drive) {
  addSlotToContainer(80, 35, Slot.Floppy)
}
