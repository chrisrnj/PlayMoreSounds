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
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class OnPlayerTeleport implements Listener
{
    private static @Nullable PlayableRichSound teleport;
    private static @Nullable PlayableRichSound worldChange;

    static {
        Runnable soundUpdater = () -> {
            Configuration sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();

            if (sounds.getBoolean("Teleport.Enabled").orElse(false))
                teleport = new PlayableRichSound(sounds.getConfigurationSection("Teleport"));
            else
                teleport = null;

            if (sounds.getBoolean("World Change.Enabled").orElse(false))
                worldChange = new PlayableRichSound(sounds.getConfigurationSection("World Change"));
            else
                worldChange = null;
        };

        PlayMoreSounds.onInstance(soundUpdater);
        PlayMoreSounds.onReload(soundUpdater);
    }

    private final @NotNull PlayMoreSounds main;
    private final @NotNull BukkitScheduler scheduler;

    public OnPlayerTeleport(@NotNull PlayMoreSounds main)
    {
        this.main = main;
        scheduler = Bukkit.getScheduler();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event)
    {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        if (!event.isCancelled())
            OnPlayerMove.callRegionEnterLeaveEvents(event, player, from, to);

        OnPlayerMove.checkBiomeEnterLeaveSounds(event, player, from, to);

        if (event.getCause() != PlayerTeleportEvent.TeleportCause.COMMAND) return;

        boolean playTeleport = teleport != null && (!event.isCancelled() || !teleport.isCancellable());
        boolean playWorldChange = worldChange != null && !from.getWorld().equals(to.getWorld()) && (!event.isCancelled() || !worldChange.isCancellable());

        if (playTeleport || playWorldChange)
            scheduler.runTask(main, () -> {
                if (playWorldChange) {
                    worldChange.play(player);

                    if (worldChange.getSection().getBoolean("Prevent Teleport Sound").orElse(false)) return;
                }

                if (playTeleport)
                    teleport.play(player);
            });
    }
}
