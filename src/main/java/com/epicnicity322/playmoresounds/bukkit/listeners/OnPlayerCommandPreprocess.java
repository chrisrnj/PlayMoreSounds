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

package com.epicnicity322.playmoresounds.bukkit.listeners;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
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

    public OnPlayerCommandPreprocess(@NotNull PlayMoreSounds plugin)
    {
        super(plugin);
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

        var sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();
        var commandTriggers = Configurations.COMMANDS.getConfigurationHolder().getConfiguration();

        for (Map.Entry<String, Object> filter : commandTriggers.getNodes().entrySet()) {
            if (filter.getValue() instanceof ConfigurationSection filterSection) {
                HashSet<PlayableRichSound> criteria = new HashSet<>();

                for (Map.Entry<String, Object> criterion : filterSection.getNodes().entrySet()) {
                    if (criterion.getValue() instanceof ConfigurationSection criterionSection) {

                        if (criterionSection.getBoolean("Enabled").orElse(false)) {
                            criteria.add(getRichSound(criterionSection));
                        }
                    }
                }

                if (!criteria.isEmpty()) filtersAndCriteria.put(filter.getKey(), criteria);
            }
        }

        setRichSound(getRichSound(sounds.getConfigurationSection(getName())));

        if (getRichSound() != null || !filtersAndCriteria.isEmpty()) {
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
        var message = event.getMessage();
        var player = event.getPlayer();
        boolean defaultSound = getRichSound() != null;

        filterLoop:
        for (Map.Entry<String, HashSet<PlayableRichSound>> filter : filtersAndCriteria.entrySet()) {
            for (var criteria : filter.getValue()) {
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

        if (defaultSound && (!event.isCancelled() || !getRichSound().isCancellable()))
            getRichSound().play(player);
    }
}
