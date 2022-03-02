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
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class OnPlayerGameModeChange extends PMSListener
{
    private final @NotNull HashMap<String, PlayableRichSound> specificGameModes = new HashMap<>();

    public OnPlayerGameModeChange(@NotNull PlayMoreSounds plugin)
    {
        super(plugin);
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

        var sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();
        var gameModes = Configurations.GAME_MODES.getConfigurationHolder().getConfiguration();

        for (Map.Entry<String, Object> gameMode : gameModes.getNodes().entrySet()) {
            if (gameMode.getValue() instanceof ConfigurationSection gameModeSection) {
                if (gameModeSection.getBoolean("Enabled").orElse(false)) {
                    specificGameModes.put(gameMode.getKey().toUpperCase(), new PlayableRichSound(gameModeSection));
                }
            }
        }

        boolean defaultEnabled = sounds.getBoolean(getName() + ".Enabled").orElse(false);

        if (defaultEnabled) {
            setRichSound(new PlayableRichSound(sounds.getConfigurationSection(getName())));
        } else {
            setRichSound(null);
        }

        if (defaultEnabled || !specificGameModes.isEmpty()) {
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
        var player = event.getPlayer();
        PlayableRichSound specificGameModeSound = specificGameModes.get(event.getNewGameMode().name());
        boolean defaultSound = getRichSound() != null;

        if (specificGameModeSound != null) {
            if (!event.isCancelled() || !specificGameModeSound.isCancellable()) {
                specificGameModeSound.play(player);

                if (specificGameModeSound.getSection().getBoolean("Prevent Default Sound").orElse(false))
                    defaultSound = false;
            }
        }

        if (defaultSound && (!event.isCancelled() || !getRichSound().isCancellable()))
            getRichSound().play(player);
    }
}
