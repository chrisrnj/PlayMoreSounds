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
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public final class OnPlayerResourcePackStatus implements Listener
{
    private static final OnPlayerResourcePackStatus instance = new OnPlayerResourcePackStatus();
    private static boolean loaded = false;
    private static @Nullable HashMap<Player, Runnable> waitingUntilResourcePackStatus;

    private OnPlayerResourcePackStatus()
    {
    }

    public static synchronized void load(@NotNull PlayMoreSounds plugin)
    {
        if (VersionUtils.supportsResourcePacks() && Configurations.CONFIG.getConfigurationHolder().getConfiguration().getBoolean("Resource Packs.Request").orElse(false)) {
            if (!loaded) {
                Bukkit.getPluginManager().registerEvents(instance, plugin);
                loaded = true;
            }
        } else {
            if (loaded) {
                HandlerList.unregisterAll(instance);
                loaded = false;
            }
        }
    }

    public static synchronized void waitUntilResourcePackStatus(@NotNull Player player, @NotNull Runnable onAccept)
    {
        if (waitingUntilResourcePackStatus == null) {
            waitingUntilResourcePackStatus = new HashMap<>();
        } else {
            Runnable previousOnAccept = waitingUntilResourcePackStatus.get(player);

            if (previousOnAccept != null) {
                Runnable newOnAccept = onAccept;
                onAccept = () -> {
                    previousOnAccept.run();
                    newOnAccept.run();
                };
            }
        }
        waitingUntilResourcePackStatus.put(player, onAccept);
    }

    private static synchronized Runnable removeWaiting(Player player)
    {
        if (waitingUntilResourcePackStatus == null) return null;

        try {
            return waitingUntilResourcePackStatus.remove(player);
        } finally {
            if (waitingUntilResourcePackStatus.isEmpty()) waitingUntilResourcePackStatus = null;
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerResourcePackStatus(PlayerResourcePackStatusEvent event)
    {
        PlayerResourcePackStatusEvent.Status status = event.getStatus();

        if (status == PlayerResourcePackStatusEvent.Status.ACCEPTED) return;

        Player player = event.getPlayer();
        Runnable runnable = removeWaiting(player);

        if (runnable != null) {
            try {
                runnable.run();
            } catch (Throwable t) {
                PlayMoreSoundsCore.getErrorHandler().report(t, "Player Status Resource Runnable Error");
            }
        }

        Configuration config = Configurations.CONFIG.getConfigurationHolder().getConfiguration();

        if (config.getBoolean("Resource Packs.Force.Enabled").orElse(false) && (status == PlayerResourcePackStatusEvent.Status.DECLINED || (config.getBoolean("Resource Packs.Force.Even If Download Fail").orElse(false) || status != PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD))) {
            player.kickPlayer(PlayMoreSounds.getLanguage().getColored("Resource Packs.Kick Message"));
        }
    }
}
