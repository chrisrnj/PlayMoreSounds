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

package com.epicnicity322.playmoresounds.bukkit;

import com.epicnicity322.epicpluginlib.bukkit.logger.Logger;
import com.epicnicity322.epicpluginlib.core.config.ConfigurationHolder;
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.playmoresounds.bukkit.listener.ListenerRegister;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundManager;
import com.epicnicity322.playmoresounds.core.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public final class PlayMoreSoundsPlugin extends JavaPlugin {
    private static final @NotNull Logger logger = new Logger("&6[&9PlayMoreSounds&6]&e ");
    private static PlayMoreSoundsPlugin instance;
    private final @NotNull SoundManager soundManager = new SoundManager(this);
    private final @NotNull ListenerRegister listenerRegister = new ListenerRegister(this);

    public PlayMoreSoundsPlugin() {
        instance = this;
        logger.setLogger(getLogger());
        PlayMoreSounds.setLogger(logger);
    }

    @NotNull
    public static SoundManager soundManager() {
        return instance.soundManager;
    }

    @NotNull
    public static ListenerRegister listenerRegister() {
        return instance.listenerRegister;
    }

    /**
     * Reloads all configurations and listeners of PlayMoreSounds.
     *
     * @return Whether all configurations loaded successfully.
     */
    public boolean reload() {
        HashMap<ConfigurationHolder, Exception> exceptions = Configurations.loader().loadConfigurations();

        exceptions.forEach((config, exception) -> {
            logger.log("Something went wrong while loading the configuration '" + config.getPath().getFileName() + "':", ConsoleLogger.Level.ERROR);
            exception.printStackTrace();
            logger.log("Since the configuration could not be loaded, default values will be used.", ConsoleLogger.Level.ERROR);
        });

        listenerRegister.registerAll();

        return exceptions.isEmpty();
    }

    @Override
    public void onEnable() {
        boolean success = reload();

        if (success) {
            logger.log("&aPlayMoreSounds was enabled successfully!");
        } else {
            logger.log("&cPlayeMoreSounds had some issues while enabling.");
            logger.log("&cPlease go back in the log for more information.");
        }
    }
}
