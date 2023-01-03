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

package com.epicnicity322.playmoresounds.core;

import com.epicnicity322.epicpluginlib.core.EpicPluginLib;
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public final class PlayMoreSounds {
    @NotNull
    public static final Path DATA_FOLDER;
    @NotNull
    public static final String VERSION_STRING = PlayMoreSoundsVersion.version;
    @NotNull
    public static final Version VERSION = new Version(VERSION_STRING);
    @NotNull
    private static ConsoleLogger<?> logger = ConsoleLogger.simpleLogger("[PlayMoreSounds] ");

    static {
        if (EpicPluginLib.Platform.getPlatform() == EpicPluginLib.Platform.BUKKIT) {
            DATA_FOLDER = Path.of("plugins", "PlayMoreSounds");
        } else {
            DATA_FOLDER = Path.of("config", "PlayMoreSounds");
        }
    }

    public static @NotNull ConsoleLogger<?> logger() {
        return logger;
    }

    public static void setLogger(@NotNull ConsoleLogger<?> logger) {
        PlayMoreSounds.logger = logger;
    }
}
