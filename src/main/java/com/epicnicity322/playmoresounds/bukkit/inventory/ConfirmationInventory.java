/*
 * PlayMoreSounds - A bukkit plugin that manages and plays sounds.
 * Copyright (C) 2022 Christiano Rangel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.epicnicity322.playmoresounds.bukkit.inventory;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.Consumer;

@SuppressWarnings("deprecation")
public final class ConfirmationInventory implements PMSInventory
{
    private final @NotNull Inventory inventory;
    private final @NotNull HashMap<Integer, Consumer<InventoryClickEvent>> buttons = new HashMap<>(2);

    /**
     * Creates a inventory that can be used to confirm an action.
     * Pressing any button on this inventory makes it close, that is before running the {@link Runnable}.
     *
     * @param title   The title, usually what the user is confirming, or null for no title.
     * @param confirm The runnable that will run when the users click confirm button.
     * @param cancel  The runnable that will run when the user click cancel button.
     */
    public ConfirmationInventory(@Nullable String title, @NotNull Runnable confirm, @Nullable Runnable cancel)
    {
        if (title == null) title = "";

        inventory = Bukkit.createInventory(null, 27, title);

        inventory.setItem(12, InventoryUtils.getItemStack("Confirm.Inventory.Items.Confirm"));
        buttons.put(12, event -> {
            InventoryUtils.closeInventory(inventory);
            confirm.run();
        });
        inventory.setItem(14, InventoryUtils.getItemStack("Confirm.Inventory.Items.Cancel"));
        buttons.put(14, event -> {
            InventoryUtils.closeInventory(inventory);
            if (cancel != null) cancel.run();
        });
    }

    /**
     * Opens the confirmation inventory to the player.
     *
     * @param player The player to open the inventory to.
     * @throws IllegalStateException In case PlayMoreSounds is not loaded.
     */
    public void openInventory(@NotNull HumanEntity player)
    {
        InventoryUtils.openInventory(inventory, buttons, player);
    }

    @Override
    public @NotNull Inventory getInventory()
    {
        return inventory;
    }

    @Override
    public @NotNull HashMap<Integer, Consumer<InventoryClickEvent>> getButtons()
    {
        return buttons;
    }
}
