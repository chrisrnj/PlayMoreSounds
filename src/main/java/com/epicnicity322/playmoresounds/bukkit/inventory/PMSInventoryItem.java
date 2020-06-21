package com.epicnicity322.playmoresounds.bukkit.inventory;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface PMSInventoryItem
{
    @NotNull String getName();

    @NotNull String getConfigPath();

    @NotNull String getId();

    @NotNull ItemStack getItemStack();
}
