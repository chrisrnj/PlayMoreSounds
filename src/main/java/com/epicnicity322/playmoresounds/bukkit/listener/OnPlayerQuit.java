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
import com.epicnicity322.playmoresounds.bukkit.region.RegionManager;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionLeaveEvent;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;

public final class OnPlayerQuit implements Listener
{
    private static @Nullable PlayableRichSound playerBan;
    private static @Nullable PlayableRichSound leaveServer;

    static {
        Runnable soundUpdater = () -> {
            var sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();
            playerBan = PMSListener.getRichSound(sounds.getConfigurationSection("Player Ban"));
            leaveServer = PMSListener.getRichSound(sounds.getConfigurationSection("Leave Server"));
        };

        PlayMoreSounds.onInstance(soundUpdater);
        PlayMoreSounds.onEnable(soundUpdater); // Make sure to load once all configurations have been reloaded.
        PlayMoreSounds.onReload(soundUpdater);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        var player = event.getPlayer();
        var location = player.getLocation();

        RegionManager.getRegions().stream().filter(region -> region.isInside(location)).forEach(region -> {
            var regionLeaveEvent = new RegionLeaveEvent(region, player, location, location);
            Bukkit.getPluginManager().callEvent(regionLeaveEvent);
        });

        if (player.isBanned()) {
            if (playerBan != null) playerBan.play(player);
        } else if (leaveServer != null) leaveServer.play(player);
    }
}
