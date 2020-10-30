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

import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.epicpluginlib.bukkit.logger.Logger;
import com.epicnicity322.epicpluginlib.bukkit.updater.UpdateChecker;
import com.epicnicity322.epicpluginlib.core.config.PluginConfig;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class UpdateManager
{
    private static final @NotNull PluginConfig config = Configurations.CONFIG.getPluginConfig();
    private static final @NotNull Logger logger = PlayMoreSounds.getPMSLogger();
    private static boolean alreadyLoaded = false;
    private static volatile Version latestVersion = null;
    private static volatile boolean updateAvailable = false;

    private UpdateManager()
    {
    }

    public static boolean isUpdateAvailable()
    {
        return updateAvailable;
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
                    if (updateAvailable) {
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

    public static synchronized void check(@NotNull CommandSender sender, boolean log)
    {
        PlayMoreSounds plugin = PlayMoreSounds.getInstance();

        if (plugin == null)
            throw new IllegalStateException("PlayMoreSounds is not loaded.");

        MessageSender lang = PlayMoreSounds.getMessageSender();

        if (updateAvailable) {
            if (log)
                lang.send(sender, lang.get("Update.Available").replace("<version>", latestVersion.getVersion()).replace("<label>", "pms"));
        } else {
            if (log)
                lang.send(sender, lang.get("Update.Check"));

            new Thread(new UpdateChecker(37429, PlayMoreSounds.version)
            {
                @Override
                public void onUpdateCheck(@NotNull CheckResult checkResult, @Nullable Version latestVersion)
                {
                    if (latestVersion != null)
                        UpdateManager.latestVersion = latestVersion;

                    if (checkResult == CheckResult.AVAILABLE) {
                        updateAvailable = true;

                        if (log)
                            lang.send(sender, lang.get("Update.Available").replace("<version>", UpdateManager.latestVersion.getVersion()).replace("<label>", "pms"));

                        Bukkit.getScheduler().runTaskTimer(plugin, () ->
                                logger.log("&2PMS has a new update available. Please download using /pms update download."), 12000, 12000);
                    } else if (log) {
                        switch (checkResult) {
                            case OFFLINE:
                                lang.send(sender, lang.get("Update.Error.Offline"));
                                break;

                            case TIMEOUT:
                                lang.send(sender, lang.get("Update.Error.Timeout"));
                                break;

                            case UNEXPECTED_ERROR:
                                lang.send(sender, lang.get("Update.Error.Default"));
                                break;

                            default:
                                lang.send(sender, lang.get("Update.Not Available"));
                                break;
                        }
                    }
                }
            }, "Update Checker").start();
        }
    }
}
