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

package com.epicnicity322.playmoresounds.bukkit.util;

import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.epicpluginlib.bukkit.logger.Logger;
import com.epicnicity322.epicpluginlib.core.config.ConfigurationHolder;
import com.epicnicity322.epicpluginlib.core.tools.Downloader;
import com.epicnicity322.epicpluginlib.core.tools.SpigotUpdateChecker;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsVersion;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

public final class UpdateManager
{
    private static final @NotNull ConfigurationHolder config = Configurations.CONFIG.getConfigurationHolder();
    private static final @NotNull Logger logger = PlayMoreSounds.getConsoleLogger();
    private static final @NotNull AtomicBoolean updateAvailable = new AtomicBoolean(false);
    private static boolean alreadyLoaded = false;
    private static volatile Version latestVersion = null;

    private UpdateManager()
    {
    }

    /**
     * @return If an update is available by last time it was checked.
     */
    public static boolean isUpdateAvailable()
    {
        return updateAvailable.get();
    }

    public static void loadUpdater()
    {
        if (PlayMoreSounds.getInstance() == null)
            throw new IllegalStateException("PlayMoreSounds is not loaded.");

        if (alreadyLoaded)
            return;
        else
            alreadyLoaded = true;

        Configuration yamlConfig = config.getConfiguration();

        if (yamlConfig.getBoolean("Updater.Enabled").orElse(true)) {
            check(Bukkit.getConsoleSender(), true);

            long ticks = yamlConfig.getNumber("Updater.Period").orElse(144000).longValue();

            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if (updateAvailable.get()) {
                        cancel();
                    } else {
                        // Getting YamlConfiguration again because YamlConfiguration field can change in PluginConfig
                        // class when the configuration is reloaded.
                        check(Bukkit.getConsoleSender(), config.getConfiguration().getBoolean("Updater.Log").orElse(true));
                    }
                }
            }.runTaskTimer(PlayMoreSounds.getInstance(), ticks, ticks);
        }
    }

    public static void check(@NotNull CommandSender sender, boolean log)
    {
        MessageSender lang = PlayMoreSounds.getLanguage();

        if (updateAvailable.get() && !sendUnsupportedNotice(sender)) {
            if (log) {
                lang.send(sender, lang.get("Update.Available").replace("<version>", latestVersion.getVersion()).replace("<label>", "pms"));
            }

            return;
        }

        if (log) {
            lang.send(sender, lang.get("Update.Check"));
        }

        SpigotUpdateChecker updateChecker = new SpigotUpdateChecker(37429, PlayMoreSoundsVersion.getVersion());

        updateChecker.check((available, latest) -> {
            if (available) {
                updateAvailable.set(true);
                latestVersion = latest;

                if (!sendUnsupportedNotice(sender)) {
                    if (log) {
                        lang.send(sender, lang.get("Update.Available").replace("<version>", UpdateManager.latestVersion.getVersion()).replace("<label>", "pms"));
                    }

                    if (PlayMoreSounds.getInstance() != null) {
                        Bukkit.getScheduler().runTaskTimer(PlayMoreSounds.getInstance(), () ->
                                logger.log("&2PMS has a new update available. Please download it on spigotmc.org."), 12000, 12000);
                    }
                }
            } else {
                lang.send(sender, lang.get("Update.Not Available"));
            }
        }, (error, exception) -> {
            if (log) {
                switch (error) {
                    case OFFLINE:
                        lang.send(sender, lang.get("Update.Error.Offline"));
                        break;

                    case TIMEOUT:
                        lang.send(sender, lang.get("Update.Error.Timeout"));
                        break;

                    case UNEXPECTED_ERROR:
                        lang.send(sender, lang.get("Update.Error.Default"));
                        break;
                }
            }

            if (error == Downloader.Result.UNEXPECTED_ERROR) {
                PlayMoreSoundsCore.getErrorHandler().report(exception, "Update Check Exception:");
            }
        });
    }

    private static boolean sendUnsupportedNotice(CommandSender sender)
    {
        if (latestVersion.compareTo(new Version("4.1")) >= 0 && PlayMoreSoundsCore.getServerVersion().compareTo(new Version("1.17")) < 0) {
            logger.log(sender, "&4Version " + latestVersion + " is available but your server does not support it. Disable updater on config and restart your server to stop checking for updates.");
            return true;
        }
        return false;
    }
}
