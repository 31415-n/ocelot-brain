package totoro.ocelot.brain.event

import totoro.ocelot.brain.entity.traits.Inventory

trait InventoryEvent extends Event {
  def slot: Inventory#Slot
}
