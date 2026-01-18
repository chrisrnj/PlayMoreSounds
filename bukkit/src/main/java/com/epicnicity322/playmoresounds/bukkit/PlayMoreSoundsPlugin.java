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

package com.epicnicity322.playmoresounds.bukkit;

import com.epicnicity322.epicpluginlib.bukkit.logger.Logger;
import com.epicnicity322.epicpluginlib.core.EpicPluginLib;
import com.epicnicity322.epicpluginlib.core.config.ConfigurationHolder;
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.playmoresounds.bukkit.util.*;
import com.epicnicity322.playmoresounds.core.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.sound.SoundManager;
import com.epicnicity322.playmoresounds.core.util.PlatformUtil;
import com.epicnicity322.playmoresounds.core.util.PlayerUtil;
import com.epicnicity322.playmoresounds.core.util.SoundsToggleState;
import com.epicnicity322.playmoresounds.core.util.TaskFactory;
import org.bstats.bukkit.Metrics;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class PlayMoreSoundsPlugin extends JavaPlugin {
    private static final @NotNull Logger logger = new Logger("&6[&9PlayMoreSounds&6]&e ");
    private final @NotNull PlatformUtil platformUtil = new BukkitPlatformUtil(this);
    private final @NotNull PlayerUtil<Player, Entity> playerUtil = new BukkitPlayerUtil(this);
    private final @NotNull TaskFactory<Entity> taskFactory = EpicPluginLib.Platform.isFolia() ? new FoliaTaskFactory(this) : new BukkitTaskFactory(this);
    private final @NotNull SoundsToggleState<Player> toggleStates = new SoundsToggleState<>(playerUtil);
    private final @NotNull SoundManager<Player, Entity> soundManager = new SoundManager<>(playerUtil, toggleStates, taskFactory);
    private final @NotNull ListenerRegister listenerRegister = new ListenerRegister(platformUtil, soundManager);

    public PlayMoreSoundsPlugin() {
        logger.setLogger(getLogger());
        PlayMoreSounds.setLogger(logger);
    }

    /**
     * Reloads all configurations and listeners of PlayMoreSounds.
     *
     * @return Whether all configurations loaded successfully.
     */
    public boolean reload() {
        Map<ConfigurationHolder, Exception> exceptions = Configurations.manager().loadConfigurations();

        exceptions.forEach((config, exception) -> {
            logger.log("Something went wrong while loading the configuration '" + config.path().getFileName() + "':", ConsoleLogger.Level.ERROR);
            exception.printStackTrace();
            logger.log("Since the configuration could not be loaded, default values will be used.", ConsoleLogger.Level.ERROR);
        });

        listenerRegister.registerAll();

        return exceptions.isEmpty();
    }

    @Override
    public void onEnable() {
        boolean success = reload();
        new Metrics(this, 7985);

        if (success) {
            logger.log("&aPlayMoreSounds was enabled successfully!");
        } else {
            logger.log("&cPlayMoreSounds had some issues while enabling.");
            logger.log("&cPlease go back in the log for more information.");
        }
    }
}
