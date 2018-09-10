package totoro.ocelot

import li.cil.oc.common.Tier
import li.cil.oc.common.init.Items
import li.cil.oc.common.tileentity.Case
import li.cil.oc.{Constants, OpenComputers, api}

object Ocelot extends App {
  OpenComputers.init()

  val computer = new Case(Tier.Three)
  api.Network.joinOrCreateNetwork(computer)

  computer.setInventorySlotContents(0, Items.get(Constants.ItemName.CPUTier3).createItemStack(1))
  computer.setInventorySlotContents(1, Items.get(Constants.ItemName.RAMTier6).createItemStack(1))

  computer.machine.start()
  println(computer.machine.lastError())

  computer.machine.stop()
}
