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

package com.epicnicity322.playmoresounds.core;

import com.epicnicity322.epicpluginlib.core.logger.ErrorHandler;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.epicpluginlib.core.util.PathUtils;
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import com.epicnicity322.playmoresounds.core.sound.SoundType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public final class PlayMoreSoundsCore
{
    private static final @NotNull Path folder;
    private static final @NotNull ErrorHandler errorHandler;
    private static final @NotNull Version serverVersion;
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
            serverVersion = VersionUtils.getBukkitVersion();
        } else {
            folder = Paths.get("config").resolve("playmoresounds");
            serverVersion = com.epicnicity322.playmoresounds.sponge.util.VersionUtils.getSpongeVersion();
        }

        if (Files.notExists(folder)) {
            try {
                Files.createDirectories(folder);
            } catch (IOException e) {
                System.out.println("Something went wrong while creating PlayMoreSounds data folder, the plugin must be disabled immediately.");
                e.printStackTrace();
            }
        }

        // Removing error reports if the server was reloaded
        if (Objects.equals(System.getProperty("PlayMoreSounds Enabled"), "true")) {
            System.out.println("PlayMoreSounds NOTICE: A reload was detected and since PlayMoreSounds does not support reloads, all PlayMoreSounds errors thrown from now on will not be logged.");
            errorHandler = new UselessErrorHandler(folder, "PlayMoreSounds", PlayMoreSoundsVersion.version,
                    Collections.singleton("Epicnicity322"), "https://github.com/Epicnicity322/PlayMoreSounds/");
        } else {
            errorHandler = new ErrorHandler(folder, "PlayMoreSounds", PlayMoreSoundsVersion.version,
                    Collections.singleton("Epicnicity322"), "https://github.com/Epicnicity322/PlayMoreSounds/");
        }

        System.setProperty("PlayMoreSounds Enabled", "true");

        // Creating available sounds file
        Path availableSounds = folder.resolve("available sounds.txt");
        StringBuilder data = new StringBuilder();

        data.append("A list of sounds available in this minecraft version.\n")
                .append("This file is not a configuration and any information stored here is not used anywhere in the plugin.\n")
                .append("This file is restored everytime the server starts.\n")
                .append("\n")
                .append("List of sounds available in your minecraft version:\n");

        for (String sound : SoundType.getPresentSoundNames())
            data.append("\n- ").append(sound);

        try {
            Files.deleteIfExists(availableSounds);
            PathUtils.write(data.toString(), availableSounds);
        } catch (IOException ex) {
            errorHandler.report(ex, "Fail to create available sounds file:");
        }
    }

    /**
     * @return The platform PlayMoreSounds is running on.
     */
    public static @NotNull Platform getPlatform()
    {
        return platform;
    }

    /**
     * @return The minecraft version the server is currently running on.
     */
    public static @NotNull Version getServerVersion()
    {
        return serverVersion;
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

    private static final class UselessErrorHandler extends ErrorHandler
    {
        public UselessErrorHandler(@NotNull Path errorFolder, @NotNull String pluginName, @NotNull String pluginVersion, @NotNull Collection<String> authors, @Nullable String website)
        {
            super(errorFolder, pluginName, pluginVersion, authors, website);
        }

        @Override
        public void report(@NotNull Throwable throwable, @NotNull String title)
        {
            // Do nothing.
        }
    }
}
