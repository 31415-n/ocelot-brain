package totoro.ocelot

import li.cil.oc.{Constants, OpenComputers, api}
import li.cil.oc.common.init.Items
import li.cil.oc.common.{Slot, Tier}
import li.cil.oc.common.tileentity.{Cable, Case}

object Ocelot extends App {
  OpenComputers.preInit()
  OpenComputers.init()
  OpenComputers.postInit()

  api.Network.joinOrCreateNetwork(new Cable())

  val computer = new Case(Tier.Three)
  computer.setInventorySlotContents(Slot.CPU, Items.get(Constants.ItemName.CPUTier3))

  api.Network.joinOrCreateNetwork(computer)
}
