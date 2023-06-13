package totoro.ocelot.brain.event

import totoro.ocelot.brain.entity.traits.{Entity, Inventory}

case class InventoryEntityAddedEvent(override val slot: Inventory#Slot, entity: Entity) extends InventoryEvent
