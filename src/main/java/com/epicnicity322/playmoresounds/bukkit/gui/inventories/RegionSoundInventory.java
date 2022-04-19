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

package com.epicnicity322.playmoresounds.bukkit.gui.inventories;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.gui.InventoryUtils;
import com.epicnicity322.playmoresounds.bukkit.region.SoundRegion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings("deprecation")
public class RegionSoundInventory
{
    private final @NotNull UUID editor;
    private final @NotNull Inventory inventory;
    private final @NotNull HashMap<Integer, Consumer<InventoryClickEvent>> buttons = new HashMap<>(3);

    public RegionSoundInventory(@NotNull SoundRegion region, @NotNull Player editor)
    {
        this.editor = editor.getUniqueId();
        var lang = PlayMoreSounds.getLanguage();

        inventory = Bukkit.createInventory(null, 9, lang.getColored("Region.Set.Sound"));
    }

    public void openInventory()
    {
        Player editor = Bukkit.getPlayer(this.editor);

        if (editor == null) return;
        InventoryUtils.openInventory(inventory, buttons, editor);
    }
}
