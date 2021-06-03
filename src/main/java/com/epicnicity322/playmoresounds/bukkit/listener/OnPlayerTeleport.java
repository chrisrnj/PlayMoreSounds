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
import com.epicnicity322.yamlhandler.ConfigurationSection;
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
    private static @Nullable RichSound teleport;

    static {
        Runnable soundUpdater = () -> {
            ConfigurationSection teleportSection = Configurations.SOUNDS.getConfigurationHolder().getConfiguration().getConfigurationSection("Teleport");

            if (teleportSection != null) {
                teleport = new RichSound(teleportSection);

                if (!teleport.isEnabled())
                    teleport = null;
            }
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

        if (event.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND && teleport != null && (!event.isCancelled() || !teleport.isCancellable())) {
            scheduler.runTask(main, () -> teleport.play(player));
        }
    }
}
