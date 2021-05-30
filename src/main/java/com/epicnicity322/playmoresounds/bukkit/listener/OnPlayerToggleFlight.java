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
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.jetbrains.annotations.NotNull;

public final class OnPlayerToggleFlight extends PMSListener
{
    private final @NotNull PlayMoreSounds plugin;
    private RichSound stopSound;
    private RichSound startSound;

    public OnPlayerToggleFlight(@NotNull PlayMoreSounds plugin)
    {
        super(plugin);

        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName()
    {
        return "Stop Flying|Start Flying";
    }

    @Override
    public void load()
    {
        Configuration sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();
        ConfigurationSection stop = sounds.getConfigurationSection("Stop Flying");
        ConfigurationSection start = sounds.getConfigurationSection("Start Flying");
        boolean stopEnabled = stop != null && stop.getBoolean("Enabled").orElse(false);
        boolean startEnabled = start != null && start.getBoolean("Enabled").orElse(false);

        if (stopEnabled || startEnabled) {
            stopSound = new RichSound(stop);
            startSound = new RichSound(start);

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
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event)
    {
        Player player = event.getPlayer();
        RichSound sound;

        if (player.isFlying())
            sound = stopSound;
        else
            sound = startSound;

        if (!event.isCancelled() || !sound.isCancellable())
            sound.play(player);
    }
}
