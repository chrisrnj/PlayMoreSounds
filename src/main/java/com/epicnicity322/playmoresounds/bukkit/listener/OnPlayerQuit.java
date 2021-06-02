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

import com.epicnicity322.playmoresounds.bukkit.region.RegionManager;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionLeaveEvent;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class OnPlayerQuit implements Listener
{
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        Location location = player.getLocation();

        RegionManager.getRegions().stream().filter(region -> region.isInside(location)).forEach(region -> {
            RegionLeaveEvent regionLeaveEvent = new RegionLeaveEvent(region, player, location, location);
            Bukkit.getPluginManager().callEvent(regionLeaveEvent);
        });

        Configuration sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();
        ConfigurationSection section;

        if (player.isBanned())
            section = sounds.getConfigurationSection("Player Ban");
        else
            section = sounds.getConfigurationSection("Leave Server");

        if (section != null)
            new RichSound(section).play(player);
    }
}
