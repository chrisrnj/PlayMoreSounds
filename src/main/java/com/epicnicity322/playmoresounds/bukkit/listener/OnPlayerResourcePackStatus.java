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
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.jetbrains.annotations.NotNull;

public final class OnPlayerResourcePackStatus implements Listener
{
    private final @NotNull PlayMoreSounds plugin;

    public OnPlayerResourcePackStatus(@NotNull PlayMoreSounds plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerResourcePackStatus(PlayerResourcePackStatusEvent event)
    {
        Configuration config = Configurations.CONFIG.getConfigurationHolder().getConfiguration();
        PlayerResourcePackStatusEvent.Status status = event.getStatus();

        if (config.getBoolean("Resource Packs.Request").orElse(false) &&
                config.getBoolean("Resource Packs.Force.Enabled").orElse(false) &&
                status == PlayerResourcePackStatusEvent.Status.DECLINED ||
                status == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
            if (!config.getBoolean("Resource Packs.Force.Even If Download Fail").orElse(false) && status == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD)
                return;

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Player player = event.getPlayer();

                if (player.isOnline())
                    player.kickPlayer(PlayMoreSounds.getLanguage().getColored("Resource Packs.Kick Message"));
            }, 20);
        }
    }
}
