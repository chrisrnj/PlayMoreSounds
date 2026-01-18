/*
 * PlayMoreSounds - A Minecraft plugin that manages and plays sounds.
 * Copyright (C) 2023-2026 Christiano Rangel
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

import com.epicnicity322.playmoresounds.core.listener.SinglePMSListener;
import com.epicnicity322.playmoresounds.core.sound.SoundManager;
import com.epicnicity322.playmoresounds.core.util.PlatformUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.jetbrains.annotations.NotNull;

public final class PlayerSwingListener extends SinglePMSListener implements Listener {
    private final @NotNull SoundManager<Player, Entity> soundManager;

    public PlayerSwingListener(@NotNull PlatformUtil platformUtil, @NotNull SoundManager<Player, Entity> soundManager) {
        super(platformUtil, "Player Swing");
        this.soundManager = soundManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerSwing(PlayerAnimationEvent event) {
        if (!event.isCancelled() || !sound.cancellable()) {
            soundManager.play(sound, event.getPlayer());
        }
    }
}
