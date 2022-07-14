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

import com.epicnicity322.epicpluginlib.core.config.ConfigurationHolder;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.gui.InventoryUtils;
import com.epicnicity322.playmoresounds.bukkit.gui.PMSInventory;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.util.PMSHelper;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A inventory GUI that reads the sounds available in a configuration and allows the player to edit them.
 */
@SuppressWarnings("deprecation")
public final class EditorInventory implements PMSInventory {
    private static final @NotNull HashMap<Integer, ArrayList<RichSoundInventory>> pages = new HashMap<>();

    static {
        Runnable pagesUpdater = () -> {
            pages.clear();

            var inventories = new ArrayList<RichSoundInventory>();
            ConfigurationHolder sounds = Configurations.SOUNDS.getConfigurationHolder();

            for (Map.Entry<String, Object> node : sounds.getConfiguration().getNodes().entrySet()) {
                if (!(node.getValue() instanceof ConfigurationSection section)) continue;
                try {
                    inventories.add(new RichSoundInventory(new PlayableRichSound(section), sounds));
                } catch (IllegalStateException ignored) {
                    // Ignoring sounds with invalid namespaced keys.
                }
            }
            pages.putAll(PMSHelper.splitIntoPages(inventories, 45));
        };
        pagesUpdater.run();
        PlayMoreSounds.onEnable(pagesUpdater);
        PlayMoreSounds.onReload(pagesUpdater);
    }

    private final @NotNull Inventory inventory;
    private final @NotNull HashMap<Integer, Consumer<InventoryClickEvent>> buttons = new HashMap<>();

    public EditorInventory() {
        var lang = PlayMoreSounds.getLanguage();
        int soundAmount = pages.get(1).size();
        int size = (pages.size() == 1 ? 0 : 9) + soundAmount;
        if (size > 54) {
            size = 54;
        } else {
            // Making sure size is a multiple of 9
            size = (size % 9 == 0 ? size : size + (9 - (size % 9)));
        }

        this.inventory = Bukkit.createInventory(null, size, lang.getColored("Editor Inventory.Title." + (soundAmount == 0 ? "Empty" : "Default")));
        if (soundAmount != 0) fillRichSounds(1);
    }

    private static ItemStack parseItemStack(String name, String value) {
        return RichSoundInventory.parseItemStack("Editor Inventory", name, value);
    }

    private void fillRichSounds(int page) {
        boolean multiplePages = pages.size() != 1;
        ArrayList<RichSoundInventory> sounds = pages.get(page);

        if (multiplePages) {
            // Removing previous rich sounds from previous page.
            for (int i = 9; i < inventory.getSize(); ++i) {
                inventory.setItem(i, null);
                buttons.remove(i);
            }

            // Adding next page button.
            if (page != pages.size()) {
                int nextPage = page + 1;

                inventory.setItem(8, parseItemStack("Next Page", Integer.toString(nextPage)));
                buttons.put(8, event -> fillRichSounds(nextPage));
            } else {
                buttons.remove(8);
                inventory.setItem(8, null);
            }
            if (page != 1) {
                int previousPage = page - 1;

                inventory.setItem(0, parseItemStack("Previous Page", Integer.toString(previousPage)));
                buttons.put(0, event -> fillRichSounds(previousPage));
            } else {
                buttons.remove(0);
                inventory.setItem(0, null);
            }

            InventoryUtils.fill(Material.BLACK_STAINED_GLASS_PANE, inventory, 0, 8);
        }

        int slot = multiplePages ? 9 : 0;

        for (RichSoundInventory sound : sounds) {
            inventory.setItem(slot, parseItemStack("Rich Sound", sound.getRichSound().getName()));
            buttons.put(slot, event -> sound.openInventory(event.getWhoClicked()));
            ++slot;
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    @Override
    public @NotNull HashMap<Integer, Consumer<InventoryClickEvent>> getButtons() {
        return buttons;
    }

    @Override
    public void openInventory(@NotNull HumanEntity humanEntity) {
        InventoryUtils.openInventory(inventory, buttons, humanEntity);
    }
}
