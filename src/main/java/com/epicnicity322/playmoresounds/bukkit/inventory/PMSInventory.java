package com.epicnicity322.playmoresounds.bukkit.inventory;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface PMSInventory
{
    PMSInventoryItem[] getItems();

    ItemStack getItemStack(PMSInventoryItem item);

    Inventory getInventory();
}
