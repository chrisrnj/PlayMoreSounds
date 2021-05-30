/*
 * PlayMoreSounds - A bukkit plugin that manages and plays sounds.
 * Copyright (C) 2021 Christiano Rangel
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

import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.epicpluginlib.bukkit.logger.Logger;
import com.epicnicity322.epicpluginlib.core.config.ConfigurationHolder;
import com.epicnicity322.epicpluginlib.core.logger.ErrorHandler;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundManager;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundType;
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.util.PMSHelper;
import com.epicnicity322.yamlhandler.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class ListInventory implements PMSInventory, Listener
{
    private static final @NotNull ConfigurationHolder config = Configurations.CONFIG.getConfigurationHolder();
    private static final @NotNull MessageSender lang = PlayMoreSounds.getLanguage();
    private static final @NotNull Logger logger = PlayMoreSounds.getConsoleLogger();
    private static final @NotNull ErrorHandler errorLogger = PlayMoreSoundsCore.getErrorHandler();
    private static final @NotNull HashMap<Integer, HashMap<Long, ArrayList<String>>> soundPagesCache = new HashMap<>();
    private static final @NotNull Pattern spaceRegex = Pattern.compile(" ");
    private static NamespacedKey button;

    static {
        // Clear cache on disable.
        PlayMoreSounds.addOnDisableRunnable(soundPagesCache::clear);
    }

    private final @NotNull Inventory inventory;
    private final int soundsPerPage;
    private final long page;
    private final @NotNull HashSet<HumanEntity> openInventories = new HashSet<>();

    public ListInventory(long page)
    {
        if (!VersionUtils.hasPersistentData())
            throw new UnsupportedOperationException("This class only works with bukkit 1.14+.");

        if (PlayMoreSounds.getInstance() == null)
            throw new IllegalStateException("PlayMoreSounds is not loaded.");

        if (button == null)
            button = InventoryUtils.getButton();

        PlayMoreSounds.addOnDisableRunnable(() -> {
            openInventories.forEach(HumanEntity::closeInventory);
            openInventories.clear();
        });

        int rowsPerPage = config.getConfiguration().getNumber("Inventories.List.Rows Per Page").orElse(4).intValue();

        if (rowsPerPage > 4)
            rowsPerPage = 4;
        else if (rowsPerPage < 1)
            rowsPerPage = 1;

        soundsPerPage = rowsPerPage * 9;

        HashMap<Long, ArrayList<String>> soundPages;

        if (soundPagesCache.containsKey(soundsPerPage)) {
            soundPages = soundPagesCache.get(soundsPerPage);
        } else {
            soundPages = PMSHelper.splitIntoPages(new TreeSet<>(SoundManager.getSoundList()), soundsPerPage);

            soundPagesCache.put(soundsPerPage, soundPages);
        }

        if (page > soundPages.size())
            page = soundPages.size();
        else if (page < 1)
            page = 1;

        this.page = page;

        int count = 18;

        inventory = Bukkit.createInventory(null, soundsPerPage + count, lang.getColored("List.GUI.Title")
                .replace("<page>", Long.toString(page))
                .replace("<totalpages>", Integer.toString(soundPages.size())));

        if (page > 1) {
            ItemStack previousPageItem = Items.PREVIOUS_PAGE.getItemStack();
            ItemMeta previousPageItemMeta = previousPageItem.getItemMeta();

            previousPageItemMeta.getPersistentDataContainer().set(button, PersistentDataType.STRING,
                    "GOTO " + (page - 1));
            previousPageItem.setItemMeta(previousPageItemMeta);
            inventory.setItem(0, previousPageItem);
        }

        inventory.setItem(4, Items.STOP_SOUND.getItemStack());

        if (page != soundPages.size()) {
            ItemStack nextPageItem = Items.NEXT_PAGE.getItemStack();
            ItemMeta nextPageItemMeta = nextPageItem.getItemMeta();

            nextPageItemMeta.getPersistentDataContainer().set(button, PersistentDataType.STRING,
                    "GOTO " + (page + 1));
            nextPageItem.setItemMeta(nextPageItemMeta);
            inventory.setItem(8, nextPageItem);
        }

        for (String sound : soundPages.get(page)) {
            ItemStack soundItem = Items.SOUND.getItemStack();
            ItemMeta soundItemMeta = soundItem.getItemMeta();

            soundItemMeta.setDisplayName(lang.getColored("List.GUI.Sound.Display Name").replace(
                    "<sound>", sound));
            soundItemMeta.getPersistentDataContainer().set(button, PersistentDataType.STRING, sound);
            soundItem.setItemMeta(soundItemMeta);
            inventory.setItem(count++, soundItem);
        }
    }

    public long getPage()
    {
        return page;
    }

    public int getSoundsPerPage()
    {
        return soundsPerPage;
    }

    @Override
    public @NotNull PMSInventoryItem[] getItems()
    {
        return Items.values();
    }

    @Override
    public @NotNull Inventory getInventory()
    {
        return inventory;
    }

    @Override
    public void openInventory(@NotNull HumanEntity humanEntity)
    {
        humanEntity.openInventory(inventory);
        openInventories.add(humanEntity);
        Bukkit.getPluginManager().registerEvents(this, PlayMoreSounds.getInstance());
    }

    @EventHandler
    public final void onInventoryClick(InventoryClickEvent event)
    {
        ItemStack itemStack = event.getCurrentItem();

        if (itemStack != null) {
            HumanEntity humanEntity = event.getWhoClicked();

            if (openInventories.contains(humanEntity)) {
                event.setCancelled(true);

                Player player = (Player) humanEntity;
                String button = itemStack.getItemMeta().getPersistentDataContainer().get(ListInventory.button,
                        PersistentDataType.STRING);

                if (button.startsWith("GOTO"))
                    Bukkit.getScheduler().runTaskLater(PlayMoreSounds.getInstance(), () -> new ListInventory(Long.parseLong(spaceRegex.split(button)[1])).openInventory(humanEntity), 10);
                else if (button.equals("STOP_SOUND"))
                    SoundManager.stopSounds(player, null, 0);
                else
                    // As the sounds of the list can only be sounds of SoundManager#getSoundList(), sounds are always present.
                    player.playSound(player.getLocation(), SoundType.valueOf(button).getSound().orElse(""), 10, 1);
            }
        }
    }

    @EventHandler
    public final void onInventoryClose(InventoryCloseEvent event)
    {
        openInventories.remove(event.getPlayer());

        if (openInventories.isEmpty())
            HandlerList.unregisterAll(this);
    }

    public enum Items implements PMSInventoryItem
    {
        STOP_SOUND("Stop Sound", itemMeta -> itemMeta.getPersistentDataContainer().set(button,
                PersistentDataType.STRING, "STOP_SOUND")),
        NEXT_PAGE("Next Page"),
        PREVIOUS_PAGE("Previous Page"),
        SOUND("Sound");

        private final @NotNull String name;
        private final @NotNull String configPath;
        private final @NotNull String langPath;
        private final @NotNull String id = name();
        private final @Nullable Consumer<ItemMeta> consumer;
        private int count = 0;

        Items(@NotNull String name)
        {
            this(name, null);
        }


        Items(@NotNull String name, @Nullable Consumer<ItemMeta> consumer)
        {
            this.name = name;
            configPath = "Inventories.List." + name + " Item";
            langPath = "List.GUI." + name;
            this.consumer = consumer;
        }

        @Override
        public @NotNull String getName()
        {
            return name;
        }

        @Override
        public @NotNull String getConfigPath()
        {
            return configPath;
        }

        public @NotNull String getLangPath()
        {
            return langPath;
        }

        @Override
        public @NotNull String getId()
        {
            return id;
        }

        @Override
        public @NotNull ItemStack getItemStack()
        {
            Configuration yamlConfig = config.getConfiguration();
            ItemStack itemStack = new ItemStack(Material.MUSIC_DISC_13);

            try {
                if (name().equals("SOUND")) {
                    ArrayList<String> materials = yamlConfig.getCollection(configPath + ".Material", Object::toString);

                    if (!materials.isEmpty()) {
                        if (count >= materials.size())
                            count = 0;

                        itemStack = new ItemStack(Material.valueOf(materials.get(count)));
                        ++count;
                    }
                } else {
                    itemStack = new ItemStack(Material.valueOf(yamlConfig.getString(configPath + ".Material").orElse("")));
                }
            } catch (IllegalArgumentException ex) {
                logger.log("&cYour config.yml has an invalid Material in the path '" + configPath + "'.");
                errorLogger.report(ex, "Invalid material in config.yml at " + configPath + ":");
            }

            ItemMeta itemMeta = itemStack.getItemMeta();

            itemMeta.setLore(Arrays.asList(lang.getColored(langPath + ".Lore").split("<line>")));
            itemMeta.addItemFlags(ItemFlag.values());

            if (!name().equals("SOUND"))
                itemMeta.setDisplayName(lang.getColored(langPath + ".Display Name"));

            if (yamlConfig.getBoolean(configPath + ".Glowing").orElse(false))
                itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);

            if (consumer != null)
                consumer.accept(itemMeta);

            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }
    }
}
