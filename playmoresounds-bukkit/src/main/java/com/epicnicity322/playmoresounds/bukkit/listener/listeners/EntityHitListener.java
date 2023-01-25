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

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSoundsPlugin;
import com.epicnicity322.playmoresounds.bukkit.listener.MultiplePMSListener;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

public final class EntityHitListener extends MultiplePMSListener {
    public EntityHitListener(@NotNull PlayMoreSoundsPlugin plugin) {
        super(plugin, new String[]{"Entity Hit", "Player Kill", "Player Killed"}, Configurations.SOUNDS);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) return;
        Entity damager = event.getDamager();

        if (victim.getHealth() - event.getFinalDamage() <= 0.0 && victim instanceof Player victimPlayer
                && damager instanceof Player damagerPlayer) {
            if (sounds[1] != null && (!event.isCancelled() || !sounds[1].cancellable())) {
                PlayMoreSoundsPlugin.soundManager().play(sounds[1], damagerPlayer);
            }
            if (sounds[2] != null && (!event.isCancelled() || !sounds[2].cancellable())) {
                PlayMoreSoundsPlugin.soundManager().play(sounds[2], victimPlayer);
            }
        }
        if (sounds[0] != null && (!event.isCancelled() || !sounds[0].cancellable())) {
            PlayMoreSoundsPlugin.soundManager().play(sounds[0], damager.getLocation());
        }
    }
}
