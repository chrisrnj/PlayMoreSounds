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

import com.epicnicity322.epicpluginlib.core.util.ObjectUtils;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundManager;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.sound.SoundType;
import com.epicnicity322.playmoresounds.core.util.PMSHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings("deprecation")
public final class ListInventory implements PMSInventory
{
    private static final @NotNull ArrayList<ListInventory> listInventories = new ArrayList<>();
    private final @NotNull Inventory inventory;
    private final int page;
    private final @NotNull HashMap<Integer, Consumer<InventoryClickEvent>> buttons = new HashMap<>();

    private ListInventory(int page)
    {
        if (PlayMoreSounds.getInstance() == null) throw new IllegalStateException("PlayMoreSounds is not loaded.");

        var config = Configurations.CONFIG.getConfigurationHolder().getConfiguration();
        var lang = PlayMoreSounds.getLanguage();
        int rowsPerPage = config.getNumber("List.Inventory.Rows Per Page").orElse(4).intValue();

        if (rowsPerPage > 4) {
            rowsPerPage = 4;
        } else if (rowsPerPage < 1) {
            rowsPerPage = 1;
        }

        HashMap<Integer, ArrayList<SoundType>> soundPages = PMSHelper.splitIntoPages(SoundType.getPresentSoundTypes(), rowsPerPage * 9);

        if (page > soundPages.size())
            page = soundPages.size();
        else if (page < 1)
            page = 1;

        ArrayList<SoundType> soundList = soundPages.get(page);
        this.page = page;
        int count = 18;
        int size = count + (soundList.size() % 9 == 0 ? soundList.size() : soundList.size() + (9 - (soundList.size() % 9)));

        inventory = Bukkit.createInventory(null, size, lang.getColored("List.Inventory.Title")
                .replace("<page>", Integer.toString(page))
                .replace("<totalpages>", Integer.toString(soundPages.size())));

        if (page > 1) {
            inventory.setItem(0, InventoryUtils.getItemStack("List.Inventory.Items.Previous Page"));
            buttons.put(0, event -> getListInventory(this.page - 1).openInventory(event.getWhoClicked()));
        }

        inventory.setItem(4, InventoryUtils.getItemStack("List.Inventory.Items.Stop Sound"));
        buttons.put(4, event -> SoundManager.stopSounds((Player) event.getWhoClicked(), null, 0));

        if (page != soundPages.size()) {
            inventory.setItem(8, InventoryUtils.getItemStack("List.Inventory.Items.Next Page"));
            buttons.put(8, event -> getListInventory(this.page + 1).openInventory(event.getWhoClicked()));
        }

        InventoryUtils.fillWithGlass(inventory, 9, count);

        ArrayList<String> soundMaterials = config.getCollection("List.Inventory.Items.Sound.Material", Object::toString);
        if (soundMaterials.size() == 0)
            soundMaterials = Configurations.CONFIG.getConfigurationHolder().getDefaultConfiguration().getCollection("List.Inventory.Sound Item.Material", Object::toString);
        Iterator<String> soundMaterialsIterator = getIterator(soundMaterials, page);

        for (SoundType sound : soundList) {
            if (!soundMaterialsIterator.hasNext()) soundMaterialsIterator = soundMaterials.iterator();

            var soundItem = new ItemStack(ObjectUtils.getOrDefault(Material.matchMaterial(soundMaterialsIterator.next()), Material.STONE));
            var soundItemMeta = soundItem.getItemMeta();

            soundItemMeta.setDisplayName(lang.getColored("List.Inventory.Items.Sound.Display Name").replace("<sound>", sound.name()));
            soundItemMeta.setLore(Arrays.asList(lang.getColored("List.Inventory.Items.Sound.Lore").split("<line>")));

            if (config.getBoolean("List.Inventory.Items.Sound.Glowing").orElse(false))
                soundItemMeta.addEnchant(Enchantment.DURABILITY, 1, true);

            soundItemMeta.addItemFlags(ItemFlag.values());

            soundItem.setItemMeta(soundItemMeta);
            inventory.setItem(count, soundItem);
            buttons.put(count, event -> {
                Player player = (Player) event.getWhoClicked();
                player.playSound(player.getLocation(), sound.getSound().orElse("block.note_block.pling"), 10, 1);
            });
            count++;
        }
    }

    public static void refreshListInventories()
    {
        if (PlayMoreSounds.getInstance() == null) throw new IllegalStateException("PlayMoreSounds is not loaded.");
        synchronized (listInventories) {
            listInventories.clear();
        }

        int rowsPerPage = Configurations.CONFIG.getConfigurationHolder().getConfiguration().getNumber("List.Inventory.Rows Per Page").orElse(4).intValue();

        if (rowsPerPage > 4) {
            rowsPerPage = 4;
        } else if (rowsPerPage < 1) {
            rowsPerPage = 1;
        }

        TreeMap<Integer, ArrayList<SoundType>> soundPages = new TreeMap<>(PMSHelper.splitIntoPages(SoundType.getPresentSoundTypes(), rowsPerPage * 9));
        synchronized (listInventories) {
            for (var page : soundPages.keySet()) {
                listInventories.add(new ListInventory(page));
            }
        }
    }

    public static ListInventory getListInventory(int page)
    {
        synchronized (listInventories) {
            if (listInventories.isEmpty()) refreshListInventories();
            if (page > listInventories.size()) page = listInventories.size();
            if (page < 1) page = 1;
            return listInventories.get(page - 1);
        }
    }

    private static Iterator<String> getIterator(ArrayList<String> list, int page)
    {
        while (page > list.size()) {
            page = page - list.size();
        }

        return list.listIterator(page);
    }

    public int getPage()
    {
        return page;
    }

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
