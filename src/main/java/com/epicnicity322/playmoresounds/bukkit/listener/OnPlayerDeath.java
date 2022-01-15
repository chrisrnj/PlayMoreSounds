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
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
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
    private static @Nullable Object namespacedKey;

    static {
        if (VersionUtils.hasPersistentData()) {
            PlayMoreSounds.onInstance(() -> namespacedKey = new NamespacedKey(PlayMoreSounds.getInstance(), "last_damage"));
        }
    }

    private final @NotNull HashMap<String, PlayableRichSound> specificDeaths = new HashMap<>();
    private final @NotNull PlayMoreSounds plugin;
    private @Nullable PlayableRichSound playerKilled;
    private @Nullable PlayableRichSound playerKill;

    public OnPlayerDeath(@NotNull PlayMoreSounds plugin)
    {
        super(plugin);

        this.plugin = plugin;
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
        ConfigurationSection defaultSection = sounds.getConfigurationSection(getName());

        boolean defaultEnabled = defaultSection != null && defaultSection.getBoolean("Enabled").orElse(false);
        boolean specificDeathEnabled = false;

        if (VersionUtils.hasPersistentData()) {
            for (Map.Entry<String, Object> deathType : deathTypes.getNodes().entrySet()) {
                if (deathType.getValue() instanceof ConfigurationSection) {
                    ConfigurationSection deathTypeSection = (ConfigurationSection) deathType.getValue();

                    if (deathTypeSection.getBoolean("Enabled").orElse(false)) {
                        specificDeaths.put(deathType.getKey().toUpperCase(), new PlayableRichSound(deathTypeSection));
                        specificDeathEnabled = true;
                    }
                }
            }
        }

        if (defaultEnabled || specificDeathEnabled) {
            if (defaultEnabled)
                setRichSound(new PlayableRichSound(defaultSection));

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

        if (!VersionUtils.hasPersistentData()) EntityDamageEvent.getHandlerList().unregister(this);

        if (sounds.getBoolean("Player Kill.Enabled").orElse(false)) {
            playerKill = new PlayableRichSound(sounds.getConfigurationSection("Player Kill"));
        }
        if (sounds.getBoolean("Player Killed.Enabled").orElse(false)) {
            playerKilled = new PlayableRichSound(sounds.getConfigurationSection("Player Killed"));
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        Player player = event.getEntity();
        boolean defaultSound = true;

        if (VersionUtils.hasPersistentData()) {
            String killerUUID = player.getPersistentDataContainer().get(new NamespacedKey(plugin, "killer_uuid"), PersistentDataType.STRING);

            if (killerUUID != null) {
                if (playerKill != null) {
                    Player killer = Bukkit.getPlayer(UUID.fromString(killerUUID));

                    playerKill.play(killer);
                }
                if (playerKilled != null) {
                    playerKilled.play(player);

                    if (playerKilled.getSection().getBoolean("Prevent Death Sounds").orElse(false)) {
                        return;
                    }
                }
            }

            String cause = player.getPersistentDataContainer().get((NamespacedKey) namespacedKey, PersistentDataType.STRING);

            if (cause != null) {
                PlayableRichSound specificDeathSound = specificDeaths.get(cause);

                if (specificDeathSound != null) {
                    specificDeathSound.play(player);

                    if (specificDeathSound.getSection().getBoolean("Prevent Default Sound").orElse(false))
                        defaultSound = false;
                }
            }
        }

        if (defaultSound) {
            PlayableRichSound sound = getRichSound();

            if (sound != null)
                sound.play(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();

        if (VersionUtils.hasPersistentData() && !event.isCancelled() && entity instanceof Player) {
            if (((Player) entity).getHealth() - event.getFinalDamage() <= 0) {
                entity.getPersistentDataContainer().set((NamespacedKey) namespacedKey, PersistentDataType.STRING, event.getCause().toString());
            }
        }
    }
}
