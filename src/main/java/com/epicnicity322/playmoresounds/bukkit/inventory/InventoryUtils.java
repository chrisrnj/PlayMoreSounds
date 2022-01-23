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

import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.epicpluginlib.core.util.ObjectUtils;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Consumer;

@SuppressWarnings("deprecation")
public final class InventoryUtils
{
    private static final @NotNull Material glassPanel;
    private static final @NotNull HashMap<HumanEntity, HashMap<Integer, Consumer<InventoryClickEvent>>> openInventories = new HashMap<>();
    private static final @NotNull HashMap<HumanEntity, Consumer<InventoryCloseEvent>> onClose = new HashMap<>();
    private static final @NotNull Listener inventoryListener = new Listener()
    {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onInventoryClick(InventoryClickEvent event)
        {
            if (event.getClickedInventory() == null) return;

            HumanEntity player = event.getWhoClicked();
            HashMap<Integer, Consumer<InventoryClickEvent>> buttons = openInventories.get(player);

            if (buttons == null) return;

            event.setCancelled(true);

            Consumer<InventoryClickEvent> button = buttons.get(event.getRawSlot());

            if (button != null) try {
                Bukkit.getScheduler().runTask(PlayMoreSounds.getInstance(), () -> button.accept(event));
            } catch (Throwable t) {
                PlayMoreSoundsCore.getErrorHandler().report(t, "Button Click Error:");
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event)
        {
            HumanEntity player = event.getPlayer();

            if (openInventories.remove(player) != null) {
                if (openInventories.isEmpty()) HandlerList.unregisterAll(this);

                Consumer<InventoryCloseEvent> runnable = onClose.remove(player);

                if (runnable != null) try {
                    Bukkit.getScheduler().runTask(PlayMoreSounds.getInstance(), () -> runnable.accept(event));
                } catch (Throwable t) {
                    PlayMoreSoundsCore.getErrorHandler().report(t, "On Close Error:");
                }
            }
        }
    };

    static {
        if (PlayMoreSoundsCore.getServerVersion().compareTo(new Version("1.13")) < 0) {
            glassPanel = Material.valueOf("THIN_GLASS");
        } else {
            glassPanel = Material.GLASS_PANE;
        }

        PlayMoreSounds.onDisable(() -> {
            openInventories.keySet().forEach(HumanEntity::closeInventory);
            openInventories.clear();
        });
    }

    private InventoryUtils()
    {
    }

    /**
     * Fills a inventory with glass panes, ignoring items that are not air.
     *
     * @param inventory  The inventory to fill.
     * @param from_index The slot to start the filling.
     * @param to_index   The slot to stop the filling.
     */
    public static void fillWithGlass(@NotNull Inventory inventory, int from_index, int to_index)
    {
        if (from_index < 0 || to_index < 0 || from_index > 53 || to_index > 53) return;

        for (int slot = from_index; slot <= to_index; ++slot) {
            if (inventory.getItem(slot) != null) continue;

            ItemStack glassPane = new ItemStack(glassPanel);
            ItemMeta meta = glassPane.getItemMeta();

            meta.setDisplayName(" ");
            if (VersionUtils.hasItemFlags()) meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            glassPane.setItemMeta(meta);
            inventory.setItem(slot, glassPane);
        }
    }

    public static @NotNull ItemStack getItemStack(@NotNull String configPrefix, @NotNull String name)
    {
        Configuration config = Configurations.CONFIG.getConfigurationHolder().getConfiguration();
        ItemStack itemStack = new ItemStack(ObjectUtils.getOrDefault(Material.matchMaterial(config.getString(configPrefix + "." + name + ".Material").orElse("STONE")), Material.STONE));
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setDisplayName(PlayMoreSounds.getLanguage().getColored(configPrefix + "." + name + ".Display Name"));
        itemMeta.setLore(Arrays.asList(PlayMoreSounds.getLanguage().getColored(configPrefix + "." + name + ".Lore").split("<line>")));

        if (config.getBoolean(configPrefix + "." + name + " Item.Glowing").orElse(false))
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
        if (VersionUtils.hasItemFlags())
            itemMeta.addItemFlags(ItemFlag.values());

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    /**
     * Opens an inventory that can't have its items moved/stolen.
     * This inventory is closed when PlayMoreSounds is disabled.
     *
     * @param inventory The inventory to open.
     * @param player    The player to open the inventory to.
     * @throws IllegalStateException If PlayMoreSounds is not loaded.
     * @see #openInventory(Inventory inventory, HashMap buttons, HumanEntity player, Consumer onClose)
     */
    public static void openInventory(@NotNull Inventory inventory, @NotNull HumanEntity player)
    {
        openInventory(inventory, null, player, null);
    }

    /**
     * Opens an inventory that you can map each slot number to a {@link Consumer} for {@link InventoryClickEvent}, so
     * when the player clicks specified slot, the consumer is accepted.
     * The items in this inventory can not be moved/stolen.
     * This inventory is closed when PlayMoreSounds is disabled.
     *
     * @param inventory The inventory to open.
     * @param buttons   The map with the number of the slot that when clicked will run the {@link Runnable}.
     * @param player    The player to open the inventory to.
     * @throws IllegalStateException If PlayMoreSounds is not loaded.
     * @see #openInventory(Inventory inventory, HashMap buttons, HumanEntity player, Consumer onClose)
     */
    public static void openInventory(@NotNull Inventory inventory, @NotNull HashMap<Integer, Consumer<InventoryClickEvent>> buttons, @NotNull HumanEntity player)
    {
        openInventory(inventory, buttons, player, null);
    }

    /**
     * Opens an inventory that you can map each slot number to a {@link Consumer} for {@link InventoryClickEvent}, so
     * when the player clicks the specified slot, the consumer is accepted. Also a {@link Consumer} for
     * {@link InventoryCloseEvent} is accepted when the inventory closes.
     * The items in this inventory can not be moved/stolen.
     * This inventory is closed when PlayMoreSounds is disabled.
     *
     * @param inventory The inventory to open.
     * @param buttons   The map with the number of the slot that when clicked will run the {@link Runnable}.
     * @param player    The player to open the inventory to.
     * @param onClose   The runnable to run when the inventory is closed.
     * @throws IllegalStateException If PlayMoreSounds is not loaded.
     */
    public static void openInventory(@NotNull Inventory inventory, @Nullable HashMap<Integer, Consumer<InventoryClickEvent>> buttons, @NotNull HumanEntity player, @Nullable Consumer<InventoryCloseEvent> onClose)
    {
        if (PlayMoreSounds.getInstance() == null) throw new IllegalStateException("PlayMoreSounds is not loaded.");

        player.openInventory(inventory);

        if (openInventories.isEmpty())
            Bukkit.getPluginManager().registerEvents(inventoryListener, PlayMoreSounds.getInstance());

        if (buttons == null) buttons = new HashMap<>(0);

        openInventories.put(player, buttons);
        InventoryUtils.onClose.put(player, onClose);
    }
}
