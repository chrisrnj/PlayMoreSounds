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
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.jetbrains.annotations.NotNull;

public final class OnPlayerToggleFlight extends PMSListener
{
    private PlayableRichSound stopSound;
    private PlayableRichSound startSound;

    public OnPlayerToggleFlight(@NotNull PlayMoreSounds plugin)
    {
        super(plugin);
    }

    @Override
    public @NotNull String getName()
    {
        return "Stop Flying|Start Flying";
    }

    @Override
    public void load()
    {
        var sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();
        boolean stopEnabled = sounds.getBoolean("Stop Flying.Enabled").orElse(false);
        boolean startEnabled = sounds.getBoolean("Start Flying.Enabled").orElse(false);

        if (stopEnabled || startEnabled) {
            if (stopEnabled) {
                stopSound = new PlayableRichSound(sounds.getConfigurationSection("Stop Flying"));
            } else {
                stopSound = null;
            }
            if (startEnabled) {
                startSound = new PlayableRichSound(sounds.getConfigurationSection("Start Flying"));
            } else {
                startSound = null;
            }

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
        var player = event.getPlayer();
        PlayableRichSound sound;

        if (player.isFlying())
            sound = stopSound;
        else
            sound = startSound;

        if (!event.isCancelled() || !sound.isCancellable())
            sound.play(player);
    }
}
