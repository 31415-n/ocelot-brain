package net.minecraft.inventory;

import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public interface IInventory {
    /**
     * Returns the number of slots in the inventory.
     */
    int getSizeInventory();

    /**
     * Returns the stack in the given slot.
     */
    @Nullable
    ItemStack getStackInSlot(int index);

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    @Nullable
    ItemStack decrStackSize(int index, int count);

    /**
     * Removes a stack from the given slot and returns it.
     */
    @Nullable
    ItemStack removeStackFromSlot(int index);

    /**
     * Sets the given item stack to the specified slot in the inventory.
     */
    void setInventorySlotContents(int index, ItemStack stack);

    /**
     * Returns the maximum stack size for a inventory slot.
     */
    int getInventoryStackLimit();

    /**
     * Returns true if it is allowed to insert the given stack (ignoring stack size) into the given slot.
     */
    boolean isItemValidForSlot(int index, ItemStack stack);

    /**
     * Removes all items from the inventory.
     */
    void clear();

    /**
     * Returns true if the nventory is empty.
     */
    boolean isEmpty();
}
