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

package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public final class OnPlayerCommandPreprocess extends PMSListener
{
    private final @NotNull HashMap<String, HashSet<PlayableRichSound>> filtersAndCriteria = new HashMap<>();
    private final @NotNull PlayMoreSounds plugin;

    public OnPlayerCommandPreprocess(@NotNull PlayMoreSounds plugin)
    {
        super(plugin);

        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName()
    {
        return "Send Command";
    }

    @Override
    public void load()
    {
        filtersAndCriteria.clear();

        Configuration sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();
        Configuration commandTriggers = Configurations.COMMANDS.getConfigurationHolder().getConfiguration();
        ConfigurationSection defaultSection = sounds.getConfigurationSection(getName());

        boolean defaultEnabled = defaultSection != null && defaultSection.getBoolean("Enabled").orElse(false);
        boolean triggerEnabled = false;

        for (Map.Entry<String, Object> filter : commandTriggers.getNodes().entrySet()) {
            if (filter.getValue() instanceof ConfigurationSection) {
                ConfigurationSection filterSection = (ConfigurationSection) filter.getValue();
                HashSet<PlayableRichSound> criteria = new HashSet<>();

                for (Map.Entry<String, Object> criterion : filterSection.getNodes().entrySet()) {
                    if (criterion.getValue() instanceof ConfigurationSection) {
                        ConfigurationSection criterionSection = (ConfigurationSection) criterion.getValue();

                        if (criterionSection.getBoolean("Enabled").orElse(false)) {
                            criteria.add(new PlayableRichSound(criterionSection));
                            triggerEnabled = true;
                        }
                    }
                }

                filtersAndCriteria.put(filter.getKey(), criteria);
            }
        }

        if (defaultEnabled || triggerEnabled) {
            if (defaultEnabled)
                setRichSound(new PlayableRichSound(defaultSection));

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
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        String message = event.getMessage();
        Player player = event.getPlayer();
        boolean defaultSound = true;

        filterLoop:
        for (Map.Entry<String, HashSet<PlayableRichSound>> filter : filtersAndCriteria.entrySet()) {
            for (PlayableRichSound criteria : filter.getValue()) {
                ConfigurationSection criteriaSection = criteria.getSection();

                if (!event.isCancelled() || !criteria.isCancellable()) {
                    if (OnAsyncPlayerChat.matchesFilter(filter.getKey(), criteriaSection.getName(), message)) {
                        criteria.play(player);

                        if (criteriaSection.getBoolean("Prevent Other Sounds.Default Sound").orElse(false))
                            defaultSound = false;

                        if (criteriaSection.getBoolean("Prevent Other Sounds.Other Filters").orElse(false))
                            break filterLoop;
                    }
                }
            }
        }

        if (defaultSound) {
            PlayableRichSound sound = getRichSound();

            if (sound != null)
                if (!event.isCancelled() || !sound.isCancellable())
                    sound.play(player);
        }
    }
}
