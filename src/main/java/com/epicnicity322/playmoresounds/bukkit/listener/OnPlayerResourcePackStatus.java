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

import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class OnPlayerResourcePackStatus implements Listener
{
    private static final @NotNull MessageSender lang = PlayMoreSounds.getLanguage();

    @EventHandler
    public void onPlayerResourcePackStatus(PlayerResourcePackStatusEvent event)
    {
        Configuration config = Configurations.CONFIG.getConfigurationHolder().getConfiguration();
        PlayerResourcePackStatusEvent.Status status = event.getStatus();

        if (config.getBoolean("Resource Packs.Request").orElse(false) &&
                config.getBoolean("Resource Packs.Force.Enabled").orElse(false) &&
                status == PlayerResourcePackStatusEvent.Status.DECLINED ||
                status == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
            if (!config.getBoolean("Resource Packs.Force.Even If Download Fail").orElse(false))
                if (status == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD)
                    return;

            new BukkitRunnable()
            {
                public void run()
                {
                    event.getPlayer().kickPlayer(lang.getColored("Resource Packs.Kick Message"));
                }
            }.runTaskLater(PlayMoreSounds.getInstance(), 20);
        }
    }
}
