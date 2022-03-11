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

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableSound;
import com.epicnicity322.playmoresounds.core.sound.SoundCategory;
import com.epicnicity322.playmoresounds.core.sound.SoundType;
import com.epicnicity322.playmoresounds.core.util.PMSHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

@SuppressWarnings("deprecation")
public final class SoundInventory implements PMSInventory
{
    private final @NotNull Inventory inventory;
    private final @NotNull HashMap<Integer, Consumer<InventoryClickEvent>> buttons = new HashMap<>(11);
    private final @NotNull PlayableSound sound;
    private final @Nullable String parentName;

    public SoundInventory(@NotNull PlayableSound sound)
    {
        this(sound, null);
    }

    public SoundInventory(@NotNull PlayableSound sound, @Nullable RichSoundInventory parent)
    {
        String title = PlayMoreSounds.getLanguage().getColored(parent == null ? "Sound Inventory.Title.Default" : "Sound Inventory.Title.Parent");

        if (parent != null) {
            title = title.replace("<richsound>", parent.getRichSound().getName());
            this.parentName = parent.getRichSound().getName();
        } else {
            this.parentName = null;
        }

        this.inventory = Bukkit.createInventory(null, 45, sound.getSection() == null ? title.replace("<id>", "1") : title.replace("<id>", sound.getSection().getName()));
        this.sound = sound;
        updateButtonItems();

        InventoryUtils.fill(Material.BLACK_STAINED_GLASS_PANE, inventory, 0, 8);
        InventoryUtils.fill(Material.GLASS_PANE, inventory, 9, 35);
        InventoryUtils.fill(Material.BLACK_STAINED_GLASS_PANE, inventory, 36, 44);

        buttons.put(10, event -> openInput(event, input -> {
            String soundType;

            if (SoundType.getPresentSoundNames().contains(input)) {
                soundType = SoundType.valueOf(input).getSound().orElse("block.note_block.pling");
            } else if (PMSHelper.isNamespacedKey(input)) {
                soundType = input;
            } else {
                return false;
            }

            sound.setSound(soundType);
            return true;
        }, "Sound Inventory.Items.Sound.Input.Title", "Sound Inventory.Items.Sound.Input.Invalid"));
        buttons.put(12, event -> openInput(event, input -> {
            try {
                sound.setVolume(Float.parseFloat(input));
                return true;
            } catch (NumberFormatException ignored) {
                return false;
            }
        }, "Sound Inventory.Items.Volume.Input.Title", "Sound Inventory.Items.Volume.Input.Invalid"));
        buttons.put(14, event -> openInput(event, input -> {
            try {
                sound.setPitch(Float.parseFloat(input));
                return true;
            } catch (NumberFormatException ignored) {
                return false;
            }
        }, "Sound Inventory.Items.Pitch.Input.Title", "Sound Inventory.Items.Pitch.Input.Invalid"));
        buttons.put(16, event -> openInput(event, input -> {
            try {
                sound.setCategory(SoundCategory.valueOf(input.toUpperCase(Locale.ROOT)));
                return true;
            } catch (IllegalArgumentException ignored) {
                return false;
            }
        }, "Sound Inventory.Items.Category.Input.Title", "Sound Inventory.Items.Category.Input.Invalid"));
        buttons.put(27, event -> openInput(event, input -> {
            try {
                sound.setDelay(Long.parseLong(input));
                return true;
            } catch (NumberFormatException ignored) {
                return false;
            }
        }, "Sound Inventory.Items.Delay.Input.Title", "Sound Inventory.Items.Delay.Input.Invalid"));

        var options = sound.getOptions();

        buttons.put(29, event -> openInput(event, input -> {
            try {
                options.setRadius(Double.parseDouble(input));
                return true;
            } catch (NumberFormatException ignored) {
                return false;
            }
        }, "Sound Inventory.Items.Radius.Input.Title", "Sound Inventory.Items.Radius.Input.Invalid"));
        buttons.put(31, event -> {
            options.setIgnoresDisabled(!options.ignoresDisabled());
            inventory.setItem(31, parseItemStack(options.ignoresDisabled() ? "Ignores Toggle.Enabled" : "Ignores Toggle.Disabled", Boolean.toString(options.ignoresDisabled())));
        });
        buttons.put(33, event -> openInput(event, input -> {
            if (input.isEmpty() || input.equalsIgnoreCase("null")) {
                options.setPermissionRequired(null);
            } else {
                options.setPermissionRequired(input);
            }
            return true;
        }, "Sound Inventory.Items.Permission Required.Input.Title", null));
        buttons.put(35, event -> openInput(event, input -> {
            if (input.isEmpty() || input.equalsIgnoreCase("null")) {
                options.setPermissionToListen(null);
            } else {
                options.setPermissionToListen(input);
            }
            return true;
        }, "Sound Inventory.Items.Permission To Listen.Input.Title", null));

        buttons.put(37, event -> sound.play((Player) event.getWhoClicked()));
        if (parent != null) {
            buttons.put(43, event -> parent.openInventory(event.getWhoClicked()));
        } else {
            buttons.put(43, event -> event.getWhoClicked().closeInventory());
        }
    }

    private void openInput(InventoryClickEvent event, Validator validator, String title, String error)
    {
        HumanEntity player = event.getWhoClicked();
        var lang = PlayMoreSounds.getLanguage();

        new InputGetterInventory((Player) player, lang.getColored(title), input -> {
            if (validator.validate(input)) {
                updateButtonItems();
                openInventory(player);
            } else {
                PlayMoreSounds.getLanguage().send(player, lang.get(error));
                Bukkit.getScheduler().runTaskLater(PlayMoreSounds.getInstance(), () -> openInventory(player), 40);
            }
        }).openInventory();
    }

    private void updateButtonItems()
    {
        inventory.setItem(10, parseItemStack("Sound", sound.getSound()));
        inventory.setItem(12, parseItemStack("Volume", Float.toString(sound.getVolume())));
        inventory.setItem(14, parseItemStack("Pitch", Float.toString(sound.getPitch())));
        inventory.setItem(16, parseItemStack("Category", sound.getCategory().name()));
        inventory.setItem(27, parseItemStack("Delay", Long.toString(sound.getDelay())));

        var options = sound.getOptions();

        inventory.setItem(29, parseItemStack("Radius", Double.toString(options.getRadius())));
        inventory.setItem(31, parseItemStack(options.ignoresDisabled() ? "Ignores Toggle.Enabled" : "Ignores Toggle.Disabled", Boolean.toString(options.ignoresDisabled())));
        inventory.setItem(33, parseItemStack("Permission Required", options.getPermissionRequired()));
        inventory.setItem(35, parseItemStack("Permission To Listen", options.getPermissionToListen()));

        inventory.setItem(37, parseItemStack("Play", sound.getSound()));
        if (parentName != null) {
            var item = InventoryUtils.getItemStack("Sound Inventory.Items.Done.Parent");
            var meta = item.getItemMeta();

            meta.setDisplayName(meta.getDisplayName().replace("<parent>", parentName));
            item.setItemMeta(meta);
            inventory.setItem(43, item);
        } else {
            inventory.setItem(43, InventoryUtils.getItemStack("Sound Inventory.Items.Done.Default"));
        }
    }

    private ItemStack parseItemStack(String name, String value)
    {
        if (value == null) value = "null";
        ItemStack itemStack = InventoryUtils.getItemStack("Sound Inventory.Items." + name);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(meta.getDisplayName().replace("<value>", value));

        List<String> previousLore = meta.getLore();
        var lore = new ArrayList<String>(previousLore.size());

        for (String string : previousLore) lore.add(string.replace("<value>", value));

        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public @NotNull PlayableSound getSound()
    {
        return sound;
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

    @Override
    public void openInventory(@NotNull HumanEntity humanEntity)
    {
        InventoryUtils.openInventory(inventory, buttons, humanEntity);
    }

    private interface Validator
    {
        boolean validate(String input);
    }
}
