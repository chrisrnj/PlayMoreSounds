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
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class OnWeatherChange extends PMSListener {
    private @Nullable PlayableRichSound stopSound;
    private @Nullable PlayableRichSound startSound;

    public OnWeatherChange(@NotNull PlayMoreSounds plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getName() {
        return "Weather Rain|Weather Rain End";
    }

    @Override
    public void load() {
        var sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();
        stopSound = getRichSound(sounds.getConfigurationSection("Weather Rain End"));
        startSound = getRichSound(sounds.getConfigurationSection("Weather Rain"));

        if (stopSound != null || startSound != null) {
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
    public void onWeatherChange(WeatherChangeEvent event) {
        var sound = event.toWeatherState() ? startSound : stopSound;

        if (sound != null && (!event.isCancelled() || !sound.isCancellable()))
            sound.play(event.getWorld().getSpawnLocation());
    }
}
