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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

public final class OnAsyncPlayerChat extends PMSListener
{
    private final @NotNull PlayMoreSounds plugin;
    private final @NotNull HashMap<String, HashSet<RichSound>> filtersAndCriteria = new HashMap<>();

    public OnAsyncPlayerChat(@NotNull PlayMoreSounds plugin)
    {
        super(plugin);
        this.plugin = plugin;
    }

    protected static boolean matchesFilter(String filter, String criteria, String message)
    {
        switch (filter) {
            case "Starts With":
                return message.startsWith(criteria);
            case "Ends With":
                return message.endsWith(criteria);
            case "Contains SubString":
                return message.toLowerCase().contains(criteria.toLowerCase());
            case "Contains":
                return message.toLowerCase().matches(".*\\b" + Pattern.quote(criteria.toLowerCase()) + "\\b.*");
            case "Equals Ignore Case":
                return message.equalsIgnoreCase(criteria);
            case "Equals Exactly":
                return message.equals(criteria);
            default:
                return false;
        }
    }

    @Override
    public @NotNull String getName()
    {
        return "Player Chat";
    }

    @Override
    public void load()
    {
        filtersAndCriteria.clear();

        Configuration sounds = Configurations.SOUNDS.getPluginConfig().getConfiguration();
        Configuration chatTriggers = Configurations.CHAT_SOUNDS.getPluginConfig().getConfiguration();
        ConfigurationSection defaultSection = sounds.getConfigurationSection(getName());

        boolean defaultEnabled = defaultSection != null && defaultSection.getBoolean("Enabled").orElse(false);
        boolean triggerEnabled = false;

        for (Map.Entry<String, Object> filter : chatTriggers.getNodes().entrySet()) {
            if (filter.getValue() instanceof ConfigurationSection) {
                ConfigurationSection filterSection = (ConfigurationSection) filter.getValue();
                HashSet<RichSound> criteria = new HashSet<>();

                for (Map.Entry<String, Object> criterion : filterSection.getNodes().entrySet()) {
                    if (criterion.getValue() instanceof ConfigurationSection) {
                        ConfigurationSection criterionSection = (ConfigurationSection) criterion.getValue();

                        if (criterionSection.getBoolean("Enabled").orElse(false)) {
                            criteria.add(new RichSound(criterionSection));
                            triggerEnabled = true;
                        }
                    }
                }

                filtersAndCriteria.put(filter.getKey(), criteria);
            }
        }

        if (defaultEnabled || triggerEnabled) {
            if (defaultEnabled)
                setRichSound(new RichSound(defaultSection));

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
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event)
    {
        String message = event.getMessage();
        Player player = event.getPlayer();
        boolean defaultSound = true;

        filterLoop:
        for (Map.Entry<String, HashSet<RichSound>> filter : filtersAndCriteria.entrySet()) {
            for (RichSound criteria : filter.getValue()) {
                ConfigurationSection criteriaSection = criteria.getSection();

                if (!event.isCancelled() || !criteria.isCancellable()) {
                    if (matchesFilter(filter.getKey(), criteriaSection.getName(), message)) {
                        Bukkit.getScheduler().runTask(plugin, () -> criteria.play(player));

                        if (criteriaSection.getBoolean("Prevent Other Sounds.Default Sound").orElse(false))
                            defaultSound = false;

                        if (criteriaSection.getBoolean("Prevent Other Sounds.Other Filters").orElse(false))
                            break filterLoop;
                    }
                }
            }
        }

        if (defaultSound) {
            RichSound sound = getRichSound();

            if (sound != null)
                if (!event.isCancelled() || !sound.isCancellable())
                    Bukkit.getScheduler().runTask(plugin, () -> sound.play(player));
        }
    }
}
