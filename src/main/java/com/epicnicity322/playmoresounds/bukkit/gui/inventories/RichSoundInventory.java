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
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableSound;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.sound.SoundCategory;
import com.epicnicity322.playmoresounds.core.util.PMSHelper;
import com.epicnicity322.yamlhandler.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@SuppressWarnings("deprecation")
public class RichSoundInventory implements PMSInventory {
    private static final @NotNull ArrayList<Material> soundMaterials = new ArrayList<>();
    private static final @NotNull AtomicInteger soundMaterialIndex = new AtomicInteger(0);

    static {
        Runnable materialsUpdater = () -> {
            soundMaterials.clear();

            ArrayList<String> materialNames = Configurations.CONFIG.getConfigurationHolder().getConfiguration().getCollection("Rich Sound Inventory.Items.Sound.Materials", Object::toString);
            materialNames.forEach(materialName -> {
                Material material = Material.matchMaterial(materialName);
                if (material != null && !material.isAir()) {
                    soundMaterials.add(material);
                }
            });
            if (soundMaterials.isEmpty()) soundMaterials.add(Material.STONE);
        };
        materialsUpdater.run();
        PlayMoreSounds.onEnable(materialsUpdater);
        PlayMoreSounds.onReload(materialsUpdater);
    }

    protected final @NotNull ConfigurationHolder save;
    final @NotNull ArrayList<SoundInventory> childSounds = new ArrayList<>();
    private final @NotNull HashMap<Integer, Consumer<InventoryClickEvent>> buttons = new HashMap<>();
    private final @NotNull PlayableRichSound richSound;
    // Should configurations be reloaded on save?
    private final boolean reload;
    private Inventory inventory;

    public RichSoundInventory(@NotNull PlayableRichSound richSound, @NotNull ConfigurationHolder save) {
        this.richSound = richSound;
        this.save = save;
        this.reload = Configurations.getConfigurationLoader().getConfigurations().contains(save);

        // Creating the child inventories.
        for (PlayableSound sound : richSound.getChildSounds()) {
            childSounds.add(new SoundInventory(sound, this));
        }

        // Setting up the inventory and childSoundPages
        updateInventory();

        // Setting up buttons.
        buttons.put(0, event -> {
            richSound.setEnabled(!richSound.isEnabled());
            updateButtonsItems();
        });
        buttons.put(8, event -> {
            richSound.setCancellable(!richSound.isCancellable());
            updateButtonsItems();
        });
        buttons.put(13, event -> {
            // Creating a new sound with default values and random id.
            var newSound = new PlayableSound(null, "BLOCK_NOTE_BLOCK_PLING", SoundCategory.MASTER, 10, 1, 0, null);

            if (richSound.addChildSound(newSound)) {
                var newSoundInventory = new SoundInventory(newSound, this);
                childSounds.add(newSoundInventory);
                // Opening the new sound inventory.
                newSoundInventory.openInventory(event.getWhoClicked());
                // Updating, new sound might add a new row to the inventory.
                updateInventory();
                // Going to last page.
                fillChildSounds(Integer.MAX_VALUE);
            }
        });
    }

    private static Material nextSoundMaterial() {
        int next = soundMaterialIndex.get();
        if (next + 1 >= soundMaterials.size()) {
            soundMaterialIndex.set(0);
        } else {
            soundMaterialIndex.set(next + 1);
        }
        return soundMaterials.get(next);
    }

    static ItemStack parseItemStack(@NotNull String inventoryName, @NotNull String itemName, @Nullable String value) {
        if (value == null) value = "null";

        ItemStack itemStack = InventoryUtils.getItemStack(inventoryName + ".Items." + itemName);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(meta.getDisplayName().replace("<value>", value));

        List<String> previousLore = meta.getLore();

        if (previousLore != null) {
            var lore = new ArrayList<String>(previousLore.size());
            for (String string : previousLore) lore.add(string.replace("<value>", value));
            meta.setLore(lore);
        }

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private static ItemStack parseItemStack(String name, String value) {
        return parseItemStack("Rich Sound Inventory", name, value);
    }

    void updateInventory() {
        // Checking if a previous inventory was set and closing to all viewers.
        ArrayList<HumanEntity> viewers = null;

        if (inventory != null) {
            buttons.remove(inventory.getSize() - 1);
            viewers = new ArrayList<>(inventory.getViewers());
            for (HumanEntity viewer : viewers) {
                viewer.closeInventory();
            }
        }

        int inventorySize = richSound.getChildSounds().size() + 19;
        if (inventorySize > 54) {
            inventorySize = 54;
        } else {
            // Making sure size is a multiple of 9
            inventorySize = (inventorySize % 9 == 0 ? inventorySize : inventorySize + (9 - (inventorySize % 9)));
        }

        this.inventory = Bukkit.createInventory(null, inventorySize, PlayMoreSounds.getLanguage().getColored("Rich Sound Inventory.Title").replace("<richsound>", richSound.getName()));
        updateButtonsItems();
        fillChildSounds(1);
        InventoryUtils.fill(Material.BLACK_STAINED_GLASS_PANE, inventory, 9, 17);

        // Setting save button.
        buttons.put(inventorySize - 1, event -> {
            try {
                save();
            } catch (Exception e) {
                PlayMoreSounds.getLanguage().send(event.getWhoClicked(), PlayMoreSounds.getLanguage().get("Rich Sound Inventory.Items.Save.Error").replace("<richsound>", richSound.getName()).replace("<config>", save.getPath().getFileName().toString()));
                PlayMoreSoundsCore.getErrorHandler().report(e, "RichSound: " + richSound + "\nSaving through GUI exception:");
            }
            event.getWhoClicked().closeInventory();
        });

        // Reopening the inventory to the viewers after updating.
        if (viewers != null) {
            for (HumanEntity viewer : viewers) {
                openInventory(viewer);
            }
        }
    }

    protected void save() throws IOException {
        Configuration config = save.getConfiguration();
        Path path = save.getPath();

        richSound.set(config);
        Files.deleteIfExists(path);
        config.save(path);

        if (reload) {
            // TODO: Reload only the configuration that was changed.
            PlayMoreSounds.reload();
        }
    }

    protected void updateButtonsItems() {
        inventory.setItem(0, InventoryUtils.getItemStack("Rich Sound Inventory.Items.Status." + (richSound.isEnabled() ? "Enabled" : "Disabled")));

        String configName = save.getPath().getFileName().toString();
        // Replacing variables of info item.
        ItemStack infoItem = InventoryUtils.getItemStack("Rich Sound Inventory.Items.Info");
        ItemMeta meta = infoItem.getItemMeta();
        var previousLore = meta.getLore();
        if (previousLore != null) {
            var newLore = new ArrayList<String>(previousLore.size());

            for (String line : previousLore) {
                newLore.add(line
                        .replace("<name>", richSound.getName())
                        .replace("<child-amount>", Integer.toString(richSound.getChildSounds().size()))
                        .replace("<config>", configName));
            }

            meta.setLore(newLore);
            infoItem.setItemMeta(meta);
        }

        inventory.setItem(4, infoItem);
        inventory.setItem(8, parseItemStack("Cancellable", Boolean.toString(richSound.isCancellable())));
        inventory.setItem(inventory.getSize() - 1, parseItemStack("Save", configName));
    }

    private void fillChildSounds(int page) {
        HashMap<Integer, ArrayList<SoundInventory>> childInventoriesPages = PMSHelper.splitIntoPages(childSounds, 35);
        int lastPageNumber = childInventoriesPages.size();

        if (page > lastPageNumber) {
            page = lastPageNumber;
        } else if (page < 1) {
            page = 1;
        }

        // Removing previous child sounds from current open inventory.
        for (int i = 18; i < inventory.getSize() - 1; ++i) {
            inventory.setItem(i, null);
            buttons.remove(i);
        }

        // Adding next page button.
        if (page != lastPageNumber) {
            int nextPage = page + 1;

            inventory.setItem(16, parseItemStack("Next Page", Integer.toString(nextPage)));
            buttons.put(16, event -> fillChildSounds(nextPage));
        } else {
            // Removing previous 'next page' button in case there was one.
            buttons.remove(16);
            InventoryUtils.forceFill(Material.BLACK_STAINED_GLASS_PANE, inventory, 16, 16);
        }
        // Adding previous page button.
        if (page != 1) {
            int previousPage = page - 1;

            inventory.setItem(10, parseItemStack("Previous Page", Integer.toString(previousPage)));
            buttons.put(10, event -> fillChildSounds(previousPage));
        } else {
            // Removing previous 'previous page' button in case there was one.
            buttons.remove(10);
            InventoryUtils.forceFill(Material.BLACK_STAINED_GLASS_PANE, inventory, 10, 10);
        }

        // Adding 'Add Sound' button.
        inventory.setItem(13, parseItemStack("Add Sound", Integer.toString(page)));
        // Unlike change page buttons, add sound button does not need to be added to #buttons map again, because it does
        //the same thing everytime: add a new sound. So it's set on constructor.

        // Filling with the child sound on this page.
        ArrayList<SoundInventory> childInventoriesPage = childInventoriesPages.get(page);
        int slot = 18;
        boolean glowing = Configurations.CONFIG.getConfigurationHolder().getConfiguration().getBoolean("Rich Sound Inventory.Items.Sound.Glowing").orElse(false);

        for (var childInventory : childInventoriesPage) {
            PlayableSound sound = childInventory.getSound();
            // Creating sound item and replacing the lore and name variables.
            ItemStack soundItem = new ItemStack(nextSoundMaterial());
            ItemMeta meta = soundItem.getItemMeta();

            meta.setDisplayName(PlayMoreSounds.getLanguage().getColored("Rich Sound Inventory.Items.Sound.Display Name").replace("<id>", sound.getId()));

            String[] configLore = PlayMoreSounds.getLanguage().getColored("Rich Sound Inventory.Items.Sound.Lore").split("<line>");
            var lore = new ArrayList<String>(configLore.length);

            for (String line : configLore) {
                lore.add(line
                        .replace("<sound>", sound.getSound())
                        .replace("<volume>", Float.toString(sound.getVolume()))
                        .replace("<pitch>", Float.toString(sound.getPitch())));
            }

            meta.setLore(lore);

            if (glowing) meta.addEnchant(Enchantment.DURABILITY, 1, true);

            meta.addItemFlags(ItemFlag.values());
            soundItem.setItemMeta(meta);

            inventory.setItem(slot, soundItem);
            buttons.put(slot, event -> childInventory.openInventory(event.getWhoClicked()));
            ++slot;
        }
    }

    public @NotNull PlayableRichSound getRichSound() {
        return richSound;
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
