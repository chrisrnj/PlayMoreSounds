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
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
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

public final class OnPlayerDeath extends PMSListener
{
    private final @NotNull HashMap<String, PlayableRichSound> specificDeaths = new HashMap<>();
    private final @NotNull PlayMoreSounds plugin;
    private final @NotNull NamespacedKey lastDamageKey;
    private final @NotNull NamespacedKey killerUUIDKey;
    private @Nullable PlayableRichSound playerKilled;
    private @Nullable PlayableRichSound playerKill;

    public OnPlayerDeath(@NotNull PlayMoreSounds plugin)
    {
        super(plugin);

        this.plugin = plugin;
        lastDamageKey = new NamespacedKey(PlayMoreSounds.getInstance(), "last_damage");
        killerUUIDKey = new NamespacedKey(plugin, "killer_uuid");
    }

    @Override
    public @NotNull String getName()
    {
        return "Player Death";
    }

    @Override
    public void load()
    {
        specificDeaths.clear();

        Configuration sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();
        Configuration deathTypes = Configurations.DEATH_TYPES.getConfigurationHolder().getConfiguration();

        for (Map.Entry<String, Object> deathType : deathTypes.getNodes().entrySet()) {
            if (deathType.getValue() instanceof ConfigurationSection deathTypeSection) {
                if (deathTypeSection.getBoolean("Enabled").orElse(false)) {
                    specificDeaths.put(deathType.getKey().toUpperCase(), new PlayableRichSound(deathTypeSection));
                }
            }
        }

        boolean defaultEnabled = sounds.getBoolean(getName() + ".Enabled").orElse(false);
        boolean playerKillEnabled = sounds.getBoolean("Player Kill.Enabled").orElse(false);
        boolean playerKilledEnabled = sounds.getBoolean("Player Kill.Enabled").orElse(false);

        if (defaultEnabled) {
            setRichSound(new PlayableRichSound(sounds.getConfigurationSection(getName())));
        } else {
            setRichSound(null);
        }
        if (playerKillEnabled) {
            playerKill = new PlayableRichSound(sounds.getConfigurationSection("Player Kill"));
        } else {
            playerKill = null;
        }
        if (playerKilledEnabled) {
            playerKilled = new PlayableRichSound(sounds.getConfigurationSection("Player Killed"));
        } else {
            playerKilled = null;
        }

        if (defaultEnabled || !specificDeaths.isEmpty() || playerKillEnabled || playerKilledEnabled) {
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
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        var player = event.getEntity();
        var playerData = player.getPersistentDataContainer();
        boolean defaultSound = getRichSound() != null;

        String killerUUID = playerData.get(killerUUIDKey, PersistentDataType.STRING);

        if (killerUUID != null) {
            if (playerKill != null) {
                var killer = Bukkit.getPlayer(UUID.fromString(killerUUID));

                if (killer != null) playerKill.play(killer);
            }
            if (playerKilled != null) {
                playerKilled.play(player);

                if (playerKilled.getSection().getBoolean("Prevent Death Sounds").orElse(false)) return;
            }
        }

        String lastDamage = playerData.get(this.lastDamageKey, PersistentDataType.STRING);

        if (lastDamage != null) {
            var specificDeathSound = specificDeaths.get(lastDamage);

            if (specificDeathSound != null) {
                specificDeathSound.play(player);

                if (specificDeathSound.getSection().getBoolean("Prevent Default Sound").orElse(false))
                    defaultSound = false;
            }
        }

        if (defaultSound) {
            getRichSound().play(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event)
    {
        if (specificDeaths.isEmpty()) return;
        var entity = event.getEntity();

        // The players last damage cause is set here, so it can be get in PlayerDeathEvent and be used to play specific death sounds.
        // Then, after the player respawns, the last damage key is removed from player data.
        if (!event.isCancelled() && entity instanceof Player player && player.getHealth() - event.getFinalDamage() <= 0) {
            entity.getPersistentDataContainer().set(lastDamageKey, PersistentDataType.STRING, event.getCause().name());
        }
    }
}
