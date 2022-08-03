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
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class OnPlayerDeath extends PMSListener {
    private final @NotNull NamespacedKey lastDamageKey;
    private final @NotNull NamespacedKey killerUUIDKey;
    private @Nullable HashMap<String, PlayableRichSound> specificDeaths;
    private @Nullable PlayableRichSound playerKilled;
    private @Nullable PlayableRichSound playerKill;

    public OnPlayerDeath(@NotNull PlayMoreSounds plugin) {
        super(plugin);
        lastDamageKey = new NamespacedKey(plugin, "last_damage");
        killerUUIDKey = new NamespacedKey(plugin, "killer_uuid");
    }

    @Override
    public @NotNull String getName() {
        return "Player Death";
    }

    @Override
    public void load() {
        var sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();
        var deathTypes = Configurations.DEATH_TYPES.getConfigurationHolder().getConfiguration();

        // Adding specific death sounds to map.
        specificDeaths = null;
        for (Map.Entry<String, Object> deathType : deathTypes.getNodes().entrySet()) {
            if (deathType.getValue() instanceof ConfigurationSection deathTypeSection) {
                if (deathTypeSection.getBoolean("Enabled").orElse(false)) {
                    if (specificDeaths == null) specificDeaths = new HashMap<>();
                    specificDeaths.put(deathType.getKey().toUpperCase(), getRichSound(deathTypeSection));
                }
            }
        }

        setRichSound(getRichSound(sounds.getConfigurationSection(getName())));
        playerKill = getRichSound(sounds.getConfigurationSection("Player Kill"));
        playerKilled = getRichSound(sounds.getConfigurationSection("Player Killed"));

        if (getRichSound() != null || specificDeaths != null || playerKill != null || playerKilled != null) {
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

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        var player = event.getEntity();
        var playerData = player.getPersistentDataContainer();

        // Checking if player was killed by another player.
        if (playerKill != null || playerKilled != null) {
            String killerUUID = playerData.get(killerUUIDKey, PersistentDataType.STRING);

            if (killerUUID != null) {
                playerData.remove(killerUUIDKey);
                if (playerKill != null) {
                    var killer = Bukkit.getPlayer(UUID.fromString(killerUUID));

                    if (killer != null) playerKill.play(killer);
                }
                if (playerKilled != null) {
                    playerKilled.play(player);

                    // If the death sounds, specific or default should be prevented from being played.
                    if (playerKilled.getSection().getBoolean("Prevent Death Sounds").orElse(false)) return;
                }
            }
        }

        if (specificDeaths != null) {
            // Checking cause of death to play specific death sounds.
            String lastDamage = playerData.get(lastDamageKey, PersistentDataType.STRING);

            if (lastDamage != null) {
                playerData.remove(lastDamageKey);
                var specificDeathSound = specificDeaths.get(lastDamage);

                if (specificDeathSound != null) {
                    specificDeathSound.play(player);

                    // If the default death sound should be prevented from being played.
                    if (specificDeathSound.getSection().getBoolean("Prevent Default Sound").orElse(false)) return;
                }
            }
        }

        // Playing default death sound.
        if (getRichSound() != null) {
            getRichSound().play(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (specificDeaths == null) return;
        var entity = event.getEntity();

        // The players last damage cause is set here, so it can be get in PlayerDeathEvent and be used to play specific death sounds.
        // Then, after the sound is played, the last damage key is removed from player data.
        if (!event.isCancelled() && entity instanceof Player player && player.getHealth() - event.getFinalDamage() <= 0) {
            entity.getPersistentDataContainer().set(lastDamageKey, PersistentDataType.STRING, event.getCause().name());
        }
    }
}
