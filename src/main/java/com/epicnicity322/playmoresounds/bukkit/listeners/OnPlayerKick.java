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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerKickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class OnPlayerKick extends PMSListener {
    private @Nullable PlayableRichSound playerBan;

    public OnPlayerKick(@NotNull PlayMoreSounds plugin) {
        super(plugin);
    }

    @Override
    public void load() {
        var sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();

        synchronized (this) {
            setRichSound(getRichSound(sounds.getConfigurationSection("Player Kicked")));
            playerBan = getRichSound(sounds.getConfigurationSection("Player Ban"));

            if (getRichSound() == null || playerBan == null) {
                if (isLoaded()) {
                    HandlerList.unregisterAll(this);
                    setLoaded(false);
                }
            } else {
                if (!isLoaded()) {
                    Bukkit.getPluginManager().registerEvents(this, plugin);
                    setLoaded(true);
                }
            }
        }
    }

    @Override
    public @NotNull String getName() {
        return "Player Kicked|Player Ban";
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        PlayableRichSound playerKicked = getRichSound();

        if (player.isBanned()) {
            if (playerBan != null && (!event.isCancelled() || !playerBan.isCancellable())) {
                playerBan.play(player);
            }
        } else if (playerKicked != null && (!event.isCancelled() || !playerKicked.isCancellable())) {
            playerKicked.play(event.getPlayer());
        }
    }
}
