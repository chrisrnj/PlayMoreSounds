package com.epicnicity322.playmoresounds.bukkit.inventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public interface PMSInventory
{
    @NotNull PMSInventoryItem[] getItems();

    @NotNull Inventory getInventory();

    void openInventory(@NotNull HumanEntity humanEntity);
}
