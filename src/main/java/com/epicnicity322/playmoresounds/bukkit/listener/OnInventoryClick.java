/*
 * Copyright (c) 2020 Christiano Rangel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class OnInventoryClick extends PMSListener
{
    private final @NotNull PlayMoreSounds plugin;
    private final @NotNull HashMap<String, RichSound> criteriaSounds = new HashMap<>();

    public OnInventoryClick(@NotNull PlayMoreSounds plugin)
    {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName()
    {
        return "Inventory Click";
    }

    @Override
    public void load()
    {
        for (Map.Entry<String, Object> node : Configurations.ITEMS_CLICKED.getPluginConfig().getConfiguration().getNodes().entrySet()) {
            if (node.getValue() instanceof ConfigurationSection) {
                ConfigurationSection section = (ConfigurationSection) node.getValue();

                if (section.getBoolean("Enabled").orElse(false) && section.contains("Sounds")) {
                    criteriaSounds.put(node.getKey(), new RichSound(section));
                }
            }
        }

        Configuration sounds = Configurations.SOUNDS.getPluginConfig().getConfiguration();
        boolean defaultEnabled = sounds.getBoolean(getName() + ".Enabled").orElse(false);

        if (!criteriaSounds.isEmpty() || defaultEnabled) {
            if (defaultEnabled)
                setRichSound(new RichSound(sounds.getConfigurationSection(getName())));

            if (!isLoaded()) {
                Bukkit.getPluginManager().registerEvents(this, plugin);
                setLoaded(true);
            }
        } else {
            if (isLoaded()) {
                HandlerList.unregisterAll(this);
                setLoaded(false);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event)
    {
        HumanEntity entity = event.getWhoClicked();
        ItemStack item = event.getCurrentItem();

        if (item != null && entity instanceof Player) {
            if (item.getType() == Material.AIR && event.getCursor() != null)
                item = event.getCursor();

            Player player = (Player) entity;
            RichSound sound = getRichSound();
            String material = item.getType().name();

            for (Map.Entry<String, RichSound> criterion : criteriaSounds.entrySet()) {
                if (OnEntityDamageByEntity.matchesCriterion(criterion.getKey(), material)) {
                    RichSound criterionSound = criterion.getValue();

                    if (!event.isCancelled() || !criterionSound.isCancellable()) {
                        criterionSound.play(player);

                        if (criterionSound.getSection().getBoolean("Prevent Other Sounds.Default Sound").orElse(false))
                            sound = null;
                        if (criterionSound.getSection().getBoolean("Prevent Other Sounds.Other Criteria").orElse(false))
                            break;
                    }
                }
            }

            if (sound != null)
                if (!event.isCancelled() || !sound.isCancellable())
                    sound.play(player);
        }
    }
}
