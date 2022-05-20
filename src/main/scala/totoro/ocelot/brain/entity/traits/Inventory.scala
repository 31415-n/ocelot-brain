package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.Ocelot
import totoro.ocelot.brain.nbt.ExtendedNBT._
import totoro.ocelot.brain.nbt.persistence.NBTPersistence
import totoro.ocelot.brain.nbt.{NBT, NBTTagCompound}
import totoro.ocelot.brain.util.Persistable
import totoro.ocelot.brain.workspace.Workspace

import scala.collection.mutable

/**
  * Describes an entity with internal container, which can hold other entities.
  * Such as computer case.
  */
trait Inventory extends WorkspaceAware with Persistable {
  /**
    * Returns the collection of entites indide of the container
    */
  private val slots: mutable.HashMap[Int, Entity] = mutable.HashMap.empty
  private val entitySlotIndices: mutable.HashMap[Entity, Int] = mutable.HashMap.empty

  final val inventory: InventoryProxy = new InventoryProxy

  /**
    * Is called any time new entity is added to the inventory
    */
  def onEntityAdded(entity: Entity): Unit = {
    entity match {
      case e: WorkspaceAware => e.workspace = workspace
      case _ =>
    }
  }

  /**
   * Is called any time new entity is removed from the inventory
   */
  def onEntityRemoved(entity: Entity): Unit = {
    entity match {
      case e: WorkspaceAware => e.workspace = null
      case _ =>
    }
  }

  override def onWorkspaceChange(newWorkspace: Workspace): Unit = {
    super.onWorkspaceChange(newWorkspace)
    slots.foreach {
      case (_, e: WorkspaceAware) => e.workspace = newWorkspace
      case _ =>
    }
  }

  // ----------------------------------------------------------------------- //

  private final val InventoryTag = "inventory"
  private final val SlotIndexTag = "slot"
  private final val SlotContentsTag = "contents"

  override def load(nbt: NBTTagCompound, workspace: Workspace): Unit = {
    super.load(nbt, workspace)
    nbt.getTagList(InventoryTag, NBT.TAG_COMPOUND)
      .iterator[NBTTagCompound]
      .zipWithIndex
      .foreach { case (slotNbt, listIndex) =>
        val (index, contents) = if (!slotNbt.hasKey(SlotIndexTag) || !slotNbt.hasKey(SlotContentsTag)) {
          // Migration code: old saves stored the inventory as a plain array.
          // We use the index in the list as a slot.
          // See https://gitlab.com/cc-ru/ocelot/ocelot-desktop/-/issues/27
          (listIndex, slotNbt)
        } else {
          val index = slotNbt.getInteger(SlotIndexTag)
          val contents = slotNbt.getCompoundTag(SlotContentsTag)

          (index, contents)
        }

        NBTPersistence.load(contents, workspace) match {
          case entity: Entity =>
            inventory(index) = entity

          case _ =>
            Ocelot.log.error("Some problems parsing an entity from NBT tag: " + contents)
        }
      }
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    nbt.setNewTagList(InventoryTag,
      slots.map { case (index, entity) =>
        val slotNbt = new NBTTagCompound()
        slotNbt.setInteger(SlotIndexTag, index)
        slotNbt.setTag(SlotContentsTag, NBTPersistence.save(entity))

        slotNbt
      }
    )
  }

  sealed class Slot private[Inventory] (val index: Int) {
    if (index < 0) {
      throw new IllegalArgumentException("negative slot index: " + index)
    }

    def isEmpty: Boolean = get.isEmpty

    def nonEmpty: Boolean = !isEmpty

    def put(entity: Entity): Option[Entity] = inventory.put(index, entity)

    def remove(): Option[Entity] = inventory.remove(index)

    def get: Option[Entity] = slots.get(index)

    def inventory: InventoryProxy = Inventory.this.inventory
  }

  final class InventoryProxy extends Iterable[Slot] {
    override def iterator: Iterator[Slot] = slots.keysIterator.map(slot)

    def entities: Iterable[Entity] = entitySlotIndices.view.keys

    def apply(index: Int): Slot = slot(index)

    def update(index: Int, entity: Entity): Unit = slot(index).put(entity)

    def slot(index: Int): Slot = new Slot(index)

    def slot(entity: Entity): Option[Slot] = entitySlotIndices.get(entity).map(slot)

    def clear(): Boolean = {
      if (slots.nonEmpty) {
        slots.values.foreach(onEntityRemoved)
        slots.clear()
        entitySlotIndices.clear()

        true
      } else false
    }

    private[Inventory] def remove(index: Int): Option[Entity] = {
      slots.remove(index).tapEach(entity => {
        entitySlotIndices.remove(entity)
        onEntityRemoved(entity)
      }).headOption
    }

    private[Inventory] def remove(entity: Entity): Option[Entity] = {
      entitySlotIndices.remove(entity) match {
        case Some(index) =>
          slots.remove(index)
          onEntityRemoved(entity)

          Some(entity)

        case _ => None
      }
    }

    private[Inventory] def put(index: Int, entity: Entity): Option[Entity] = {
      val previousEntity = remove(index)
      remove(entity)

      slots += index -> entity
      entitySlotIndices += entity -> index
      onEntityAdded(entity)

      previousEntity
    }
  }
}
