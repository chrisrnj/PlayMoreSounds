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

package com.epicnicity322.playmoresounds.core;

import com.epicnicity322.epicpluginlib.core.logger.ErrorHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public final class PlayMoreSoundsCore
{
    private static final @NotNull Path folder;
    private static final @NotNull ErrorHandler errorHandler;
    private static @NotNull Platform platform;

    static {
        try {
            Class.forName("org.bukkit.Bukkit");

            platform = Platform.BUKKIT;
        } catch (ClassNotFoundException e) {
            platform = Platform.SPONGE;
        }

        if (platform == Platform.BUKKIT) {
            folder = Paths.get("plugins").resolve("PlayMoreSounds");
        } else {
            folder = Paths.get("config").resolve("playmoresounds");
        }

        if (Files.notExists(folder)) {
            try {
                Files.createDirectories(folder);
            } catch (IOException e) {
                System.out.println("Something went wrong while creating PlayMoreSounds data folder, the plugin must be disabled immediately.");
                e.printStackTrace();
            }
        }

        errorHandler = new ErrorHandler(folder, "PlayMoreSounds", PlayMoreSoundsVersion.version,
                Collections.singleton("Epicnicity322"), "https://github.com/Epicnicity322/PlayMoreSounds/");
    }

    /**
     * @return The platform PlayMoreSounds is running on.
     */
    public static @NotNull Platform getPlatform()
    {
        return platform;
    }

    /**
     * @return The folder where all PlayMoreSounds' configurations are stored.
     */
    public static @NotNull Path getFolder()
    {
        return folder;
    }

    /**
     * @return PlayMoreSounds' error handling class.
     */
    public static @NotNull ErrorHandler getErrorHandler()
    {
        return errorHandler;
    }

    public enum Platform
    {
        BUKKIT,
        SPONGE
    }
}
