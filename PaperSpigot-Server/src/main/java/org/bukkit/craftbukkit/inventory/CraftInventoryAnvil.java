package org.bukkit.craftbukkit.inventory;

import net.minecraft.server.IInventory;

import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

public class CraftInventoryAnvil extends CraftInventory implements AnvilInventory {
    private final IInventory resultInventory;

    public CraftInventoryAnvil(IInventory inventory, IInventory resultInventory) {
        super(inventory);
        this.resultInventory = resultInventory;
    }

    public IInventory getResultInventory() {
        return resultInventory;
    }

    public IInventory getIngredientsInventory() {
        return inventory;
    }

    @Override
    public ItemStack getItem(int slot) {
    	net.minecraft.server.ItemStack item = (slot < getIngredientsInventory().getSize() ? getIngredientsInventory().getItem(slot) : getResultInventory().getItem(slot - getIngredientsInventory().getSize()));
    	return item == null ? null : CraftItemStack.asCraftMirror(item);
    }

    @Override
    public void setItem(int index, ItemStack item) {
        if (index < getIngredientsInventory().getSize()) {
            getIngredientsInventory().setItem(index, (item == null ? null : CraftItemStack.asNMSCopy(item)));
        } else {
            getResultInventory().setItem((index - getIngredientsInventory().getSize()), (item == null ? null : CraftItemStack.asNMSCopy(item)));
        }
    }

    @Override
    public int getSize() {
        return getResultInventory().getSize() + getIngredientsInventory().getSize();
    }
}
