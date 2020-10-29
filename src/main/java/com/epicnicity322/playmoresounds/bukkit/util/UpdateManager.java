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

package com.epicnicity322.playmoresounds.bukkit.util;

import com.epicnicity322.epicpluginlib.bukkit.logger.Logger;
import com.epicnicity322.epicpluginlib.bukkit.updater.Updater;
import com.epicnicity322.epicpluginlib.core.config.PluginConfig;
import com.epicnicity322.epicpluginlib.core.tools.Downloader;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;

public final class UpdateManager
{
    private static final @NotNull PluginConfig config = Configurations.CONFIG.getPluginConfig();
    private static final @NotNull Logger logger = PlayMoreSounds.getPMSLogger();
    private static boolean updateAvailable = false;
    private static boolean alreadyLoaded = false;
    private static boolean updateDownloaded = false;
    private static Updater updater;

    static {
        PlayMoreSounds.addOnInstanceRunnable(() -> updater = new Updater(PlayMoreSounds.getInstance().getJar().toFile(), PlayMoreSounds.getVersion(), 37429));
    }

    private UpdateManager()
    {
    }

    public static boolean isUpdateAvailable()
    {
        return updateAvailable;
    }

    public static boolean isUpdateDownloaded()
    {
        PlayMoreSounds instance = PlayMoreSounds.getInstance();

        return updateDownloaded || instance != null &&
                Files.exists(Bukkit.getUpdateFolderFile().toPath().resolve(instance.getJar().getFileName().toString()));
    }

    public static Updater getUpdater()
    {
        return updater;
    }

    public static void loadUpdater()
    {
        if (updater == null)
            throw new IllegalStateException("PlayMoreSounds is not loaded.");

        if (alreadyLoaded)
            return;
        else
            alreadyLoaded = true;

        Configuration yamlConfig = config.getConfiguration();

        if (yamlConfig.getBoolean("Updater.Enabled").orElse(true)) {
            check(true);

            long ticks = yamlConfig.getNumber("Updater.Period").orElse(144000).longValue();

            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if (updateAvailable) {
                        cancel();
                    } else {
                        // Getting YamlConfiguration again because YamlConfiguration field can change in PluginConfig
                        // class when the configuration is reloaded.
                        check(config.getConfiguration().getBoolean("Updater.Log").orElse(true));
                    }
                }
            }.runTaskTimer(PlayMoreSounds.getInstance(), ticks, ticks);
        }
    }

    public static @NotNull Updater.CheckResult check(boolean log)
    {
        PlayMoreSounds plugin = PlayMoreSounds.getInstance();

        if (updater == null)
            throw new IllegalStateException("PlayMoreSounds is not loaded.");

        if (updateAvailable) {
            if (log)
                logger.log("&2Update Available! To download use: &n/pms update download");

            return Updater.CheckResult.AVAILABLE;
        } else {
            if (log)
                logger.log("&6Checking for updates...");

            Updater.CheckResult result = getUpdater().check();

            switch (result) {
                case AVAILABLE:
                    updateAvailable = true;

                    if (log)
                        logger.log("&2UPDATE FOUND! Type &n/pms update download&r&2 to update.");

                    Bukkit.getScheduler().runTaskTimer(plugin, () ->
                                    logger.log("&2PMS has a new update available. Please download using /pms update download."),
                            12000, 12000);

                    break;

                case OFFLINE:
                    if (log)
                        logger.log("&cFailed: &eThe network is off or spigot is down.");

                    break;

                case TIMEOUT:
                    if (log)
                        logger.log("&cFailed: &eTook too long to connect to api.spiget.org.");

                    break;

                case UNEXPECTED_ERROR:
                    if (log)
                        logger.log("&cSomething went wrong while checking for updates.");

                    break;

                default:
                    if (log)
                        logger.log("&6No updates found.");

                    break;
            }

            return result;
        }
    }

    public static @NotNull Downloader.Result download(boolean log)
    {
        if (updater == null)
            throw new IllegalStateException("PlayMoreSounds is not loaded.");

        if (updateDownloaded) {
            if (log)
                logger.log("&2Update Downloaded! Restart your server to start using it.");
            return Downloader.Result.SUCCESS;
        } else {
            if (log)
                logger.log("&6Downloading update...");

            Downloader.Result result = getUpdater().download();

            switch (result) {
                case SUCCESS:
                    updateDownloaded = true;

                    if (log)
                        logger.log("&2Update was downloaded successfully and it will be installed on restart.");

                    Bukkit.getScheduler().runTaskTimer(PlayMoreSounds.getInstance(), () ->
                                    logger.log("&2PMS has a new update downloaded. Please restart your server."),
                            144000, 144000);

                    break;
                case OFFLINE:
                    if (log)
                        logger.log("&cFailed: &eThe network is off or spigot is down.");

                    break;

                case TIMEOUT:
                    if (log)
                        logger.log("&cFailed: &eTook too long to connect to api.spiget.org.");

                    break;

                case UNEXPECTED_ERROR:
                    if (log)
                        logger.log("&cSomething went wrong while downloading update.");

                    break;
            }

            return result;
        }
    }
}
