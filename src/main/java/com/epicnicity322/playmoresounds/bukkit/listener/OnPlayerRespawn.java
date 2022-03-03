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
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.util.PMSHelper;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;

public final class OnPlayerRespawn extends PMSListener
{
    private final @NotNull NamespacedKey lastDamageKey;
    private final @NotNull NamespacedKey killerUUIDKey;

    public OnPlayerRespawn(@NotNull PlayMoreSounds plugin)
    {
        super(plugin);
        lastDamageKey = new NamespacedKey(plugin, "last_damage");
        killerUUIDKey = new NamespacedKey(plugin, "killer_uuid");
    }

    @Override
    public void load()
    {
        var sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();

        boolean playerKillKilledEnabled = sounds.getBoolean("Player Kill.Enabled").orElse(false) || sounds.getBoolean("Player Killer.Enabled").orElse(false);
        boolean deathTypeEnabled = PMSHelper.anySoundEnabled(Configurations.DEATH_TYPES.getConfigurationHolder().getConfiguration(), null);

        setRichSound(getRichSound(sounds.getConfigurationSection(getName())));

        if (getRichSound() != null || playerKillKilledEnabled || deathTypeEnabled) {
            if (!isLoaded()) {
                Bukkit.getPluginManager().registerEvents(this, plugin);
                setLoaded(true);
            }
        } else {
            if (isLoaded()) {
                HandlerList.unregisterAll(this);
                setLoaded(false);
            }
        }
    }

    @Override
    public @NotNull String getName()
    {
        return "Respawn";
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event)
    {
        var player = event.getPlayer();

        player.getPersistentDataContainer().remove(lastDamageKey);
        player.getPersistentDataContainer().remove(killerUUIDKey);

        if (getRichSound() != null)
            getRichSound().play(player);
    }
}
