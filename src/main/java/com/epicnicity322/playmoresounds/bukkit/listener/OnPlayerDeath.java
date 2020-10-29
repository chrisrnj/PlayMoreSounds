/*
 * Copyright (c) 2020 Christiano Rangel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
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

public final class OnPlayerDeath extends PMSListener
{
    private static @Nullable Object namespacedKey;

    static {
        if (VersionUtils.hasPersistentData()) {
            PlayMoreSounds.addOnInstanceRunnable(() ->
                    namespacedKey = new NamespacedKey(PlayMoreSounds.getInstance(), "lastDmg")
            );
        }
    }

    private final @NotNull HashMap<String, RichSound> specificDeaths = new HashMap<>();
    private final @NotNull PlayMoreSounds plugin;

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

        Configuration sounds = Configurations.SOUNDS.getPluginConfig().getConfiguration();
        Configuration deathTypes = Configurations.DEATH_TYPES.getPluginConfig().getConfiguration();
        ConfigurationSection defaultSection = sounds.getConfigurationSection(getName());

        boolean defaultEnabled = defaultSection != null && defaultSection.getBoolean("Enabled").orElse(false);
        boolean specificDeathEnabled = false;

        if (VersionUtils.hasPersistentData()) {
            for (Map.Entry<String, Object> deathType : deathTypes.getNodes().entrySet()) {
                if (deathType.getValue() instanceof ConfigurationSection) {
                    ConfigurationSection deathTypeSection = (ConfigurationSection) deathType.getValue();

                    if (deathTypeSection.getBoolean("Enabled").orElse(false)) {
                        specificDeaths.put(deathType.getKey().toUpperCase(), new RichSound(deathTypeSection));
                        specificDeathEnabled = true;
                    }
                }
            }
        }

        if (defaultEnabled || specificDeathEnabled) {
            if (defaultEnabled)
                setRichSound(new RichSound(defaultSection));

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
        Player player = event.getEntity();
        boolean defaultSound = true;

        if (VersionUtils.hasPersistentData()) {
            String cause = player.getPersistentDataContainer().get((NamespacedKey) namespacedKey, PersistentDataType.STRING);

            if (cause != null) {
                RichSound specificDeathSound = specificDeaths.get(cause);

                if (specificDeathSound != null) {
                    specificDeathSound.play(player);

                    if (specificDeathSound.getSection().getBoolean("Stop Other Sounds.Default Sound").orElse(false))
                        defaultSound = false;
                }
            }
        }

        if (defaultSound) {
            RichSound sound = getRichSound();

            if (sound != null)
                sound.play(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();

        if (entity instanceof Player)
            if (!event.isCancelled() && VersionUtils.hasPersistentData())
                entity.getPersistentDataContainer().set((NamespacedKey) namespacedKey, PersistentDataType.STRING,
                        event.getCause().toString());
    }
}
