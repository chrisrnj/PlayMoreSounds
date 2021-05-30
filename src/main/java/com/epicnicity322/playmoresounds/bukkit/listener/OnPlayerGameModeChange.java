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
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class OnPlayerGameModeChange extends PMSListener
{
    private final @NotNull HashMap<String, RichSound> specificGameModes = new HashMap<>();
    private final @NotNull PlayMoreSounds plugin;

    public OnPlayerGameModeChange(@NotNull PlayMoreSounds plugin)
    {
        super(plugin);

        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName()
    {
        return "Game Mode Change";
    }

    @Override
    public void load()
    {
        specificGameModes.clear();

        Configuration sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();
        Configuration gameModes = Configurations.GAME_MODES.getConfigurationHolder().getConfiguration();
        ConfigurationSection defaultSection = sounds.getConfigurationSection(getName());

        boolean defaultEnabled = defaultSection != null && defaultSection.getBoolean("Enabled").orElse(false);
        boolean specificGameModeEnabled = false;

        for (Map.Entry<String, Object> gameMode : gameModes.getNodes().entrySet()) {
            if (gameMode.getValue() instanceof ConfigurationSection) {
                ConfigurationSection gameModeSection = (ConfigurationSection) gameMode.getValue();

                if (gameModeSection.getBoolean("Enabled").orElse(false)) {
                    specificGameModes.put(gameMode.getKey().toUpperCase(), new RichSound(gameModeSection));
                    specificGameModeEnabled = true;
                }
            }
        }

        if (defaultEnabled || specificGameModeEnabled) {
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event)
    {
        Player player = event.getPlayer();
        RichSound specificGameModeSound = specificGameModes.get(event.getNewGameMode().name());
        boolean defaultSound = true;

        if (specificGameModeSound != null) {
            if (!event.isCancelled() || !specificGameModeSound.isCancellable()) {
                specificGameModeSound.play(player);

                if (specificGameModeSound.getSection().getBoolean("Prevent Default Sound").orElse(false))
                    defaultSound = false;
            }
        }

        if (defaultSound) {
            RichSound sound = getRichSound();

            if (sound != null)
                if (!event.isCancelled() || !sound.isCancellable())
                    sound.play(player);
        }
    }
}
