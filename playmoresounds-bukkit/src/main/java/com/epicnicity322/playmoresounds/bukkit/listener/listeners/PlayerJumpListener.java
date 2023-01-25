/*
 * PlayMoreSounds - A minecraft plugin that manages and plays sounds.
 * Copyright (C) 2023 Christiano Rangel
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

package com.epicnicity322.playmoresounds.bukkit.listener.listeners;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSoundsPlugin;
import com.epicnicity322.playmoresounds.bukkit.listener.SinglePMSListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

public final class PlayerJumpListener extends SinglePMSListener {
    public PlayerJumpListener(@NotNull PlayMoreSoundsPlugin plugin) {
        super(plugin, "Player Jump");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJump(PlayerJumpEvent event) {
        if (!event.isCancelled() || !sound.cancellable()) {
            PlayMoreSoundsPlugin.soundManager().play(sound, event.getPlayer());
        }
    }
}
