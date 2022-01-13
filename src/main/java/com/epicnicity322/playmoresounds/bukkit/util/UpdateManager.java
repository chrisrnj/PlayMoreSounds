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
import com.epicnicity322.epicpluginlib.core.config.ConfigurationHolder;
import com.epicnicity322.epicpluginlib.core.tools.Downloader;
import com.epicnicity322.epicpluginlib.core.tools.SpigotUpdateChecker;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.command.subcommand.ConfirmSubCommand;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsVersion;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.YamlConfigurationLoader;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarFile;

public final class UpdateManager
{
    private static final @NotNull ConfigurationHolder config = Configurations.CONFIG.getConfigurationHolder();
    private static final @NotNull MessageSender lang = PlayMoreSounds.getLanguage();
    private static final @NotNull AtomicBoolean updateAvailable = new AtomicBoolean(false);
    private static final @NotNull UUID deleteConfirmationUniqueId = UUID.randomUUID();
    private static @Nullable BukkitTask updateCheckerTask;
    private static @Nullable Version latestVersion;
    private static URL spigetURL;
    private static @Nullable BukkitTask availableNotifierTask;
    private static final @NotNull Runnable updateChecker = () -> {
        if (updateAvailable.get()) {
            if (updateCheckerTask != null) {
                updateCheckerTask.cancel();
            }
        } else {
            check(config.getConfiguration().getBoolean("Updater.Log").orElse(true) ? Bukkit.getConsoleSender() : null);
        }
    };

    static {
        try {
            spigetURL = new URL("https://api.spiget.org/v2/resources/37429/download");
        } catch (MalformedURLException willNeverHappen) {
            willNeverHappen.printStackTrace();
        }
    }

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

    public static synchronized @Nullable Version getLatestVersion()
    {
        return latestVersion;
    }

    private static synchronized void setLatestVersion(@NotNull Version latestVersion)
    {
        UpdateManager.latestVersion = latestVersion;
    }

    public static void loadUpdater(@NotNull PlayMoreSounds instance)
    {
        Configuration yamlConfig = config.getConfiguration();

        if (yamlConfig.getBoolean("Updater.Enabled").orElse(true)) {
            long ticks = yamlConfig.getNumber("Updater.Period").orElse(144000).longValue();

            if (updateCheckerTask != null) {
                updateCheckerTask.cancel();
            }

            updateCheckerTask = Bukkit.getScheduler().runTaskTimerAsynchronously(instance, updateChecker, ticks, ticks);
        } else {
            if (updateCheckerTask != null) {
                updateCheckerTask.cancel();
            }
            if (availableNotifierTask != null) {
                availableNotifierTask.cancel();
            }
        }
    }

    private static synchronized @Nullable BukkitTask getAvailableNotifierTask()
    {
        return availableNotifierTask;
    }

    private static synchronized void setAvailableNotifierTask(@Nullable BukkitTask availableNotifierTask)
    {
        UpdateManager.availableNotifierTask = availableNotifierTask;
    }

    public static void check(@Nullable CommandSender logReceiver)
    {
        boolean log = logReceiver != null;

        if (updateAvailable.get()) {
            if (log) {
                lang.send(logReceiver, lang.get("Update.Available").replace("<version>", getLatestVersion().getVersion()).replace("<label>", "pms"));
            }
            return;
        }

        if (log) {
            lang.send(logReceiver, lang.get("Update.Check"));
        }

        SpigotUpdateChecker updateChecker = new SpigotUpdateChecker(37429, PlayMoreSoundsVersion.getVersion());

        updateChecker.check((available, latest) -> {
            if (available) {
                updateAvailable.set(true);
                setLatestVersion(latest);

                if (log) {
                    if (isUnsupported(latest)) {
                        lang.send(logReceiver, "&4Version " + latest + " is available but your server does not support it. Update to Spigot 1.18 or disable updater on config to stop checking for updates.");
                    } else {
                        lang.send(logReceiver, lang.get("Update.Available").replace("<version>", UpdateManager.latestVersion.getVersion()).replace("<label>", "pms"));
                    }
                }

                if (getAvailableNotifierTask() == null && PlayMoreSounds.getInstance() != null) {
                    setAvailableNotifierTask(Bukkit.getScheduler().runTaskTimerAsynchronously(PlayMoreSounds.getInstance(),
                            () -> {
                                if (isUnsupported(latest)) {
                                    lang.send(logReceiver, "&4Version " + latest + " is available but your server does not support it. Update to Spigot 1.18 or disable updater on config to stop checking for updates.");
                                } else {
                                    PlayMoreSounds.getConsoleLogger().log("&2An update is available for PlayMoreSounds. Download it using &f/pms update download&2.");
                                }
                            }, log ? 36000 : 0, 36000));
                }
            } else {
                if (log) {
                    lang.send(logReceiver, lang.get("Update.Not Available"));
                }
            }
        }, (error, exception) -> {
            if (log) {
                switch (error) {
                    case OFFLINE:
                        lang.send(logReceiver, lang.get("Update.Error.Offline"));
                        break;

                    case TIMEOUT:
                        lang.send(logReceiver, lang.get("Update.Error.Timeout"));
                        break;

                    case UNEXPECTED_ERROR:
                        lang.send(logReceiver, lang.get("Update.Error.Default"));
                        break;
                }
            }

            if (error == Downloader.Result.UNEXPECTED_ERROR) {
                PlayMoreSoundsCore.getErrorHandler().report(exception, "Update Check Exception:");
            }
        });
    }

    /**
     * Downloads the latest version of PlayMoreSounds through api.spiget.org on the main thread.
     *
     * @param sender   The sender to send error messages.
     * @param instance PlayMoreSounds main instance.
     * @return If the download had no issues.
     */
    public static @Nullable String downloadLatest(@NotNull CommandSender sender, @NotNull PlayMoreSounds instance)
    {
        File update = new File(Bukkit.getUpdateFolderFile(), instance.getFile().getName());

        try {
            Bukkit.getUpdateFolderFile().mkdirs();

            if (update.delete()) {
                lang.send(sender, lang.get("Update.Download.Deleted Downloaded"));
            }

            update.createNewFile();
        } catch (Exception e) {
            lang.send(sender, lang.get("Update.Error.Default"));
            PlayMoreSoundsCore.getErrorHandler().report(e, "Update File Creation Unknown Error:");
            return null;
        }

        try (FileOutputStream download = new FileOutputStream(update)) {
            Downloader downloader = new Downloader(spigetURL, download);
            downloader.run();

            if (downloader.getResult() != Downloader.Result.SUCCESS) {
                switch (downloader.getResult()) {
                    case OFFLINE:
                        lang.send(sender, lang.get("Update.Error.Offline"));
                        break;

                    case TIMEOUT:
                        lang.send(sender, lang.get("Update.Error.Timeout"));
                        break;

                    case UNEXPECTED_ERROR:
                        lang.send(sender, lang.get("Update.Error.Default"));
                        PlayMoreSoundsCore.getErrorHandler().report(downloader.getException(), "Unexpected Error Update Download:");
                        break;
                }
                return null;
            }

            JarFile jarFile = new JarFile(update);
            Configuration description = new YamlConfigurationLoader().load(new InputStreamReader(jarFile.getInputStream(jarFile.getJarEntry("plugin.yml"))));
            Version bukkitVersion = VersionUtils.getBukkitVersion();
            String fixedapiversion = description.getString("api-version").orElse(bukkitVersion.getVersion());

            if (fixedapiversion.equals("1.13")) fixedapiversion = bukkitVersion.getVersion();

            if (new Version(fixedapiversion).compareTo(bukkitVersion) > 0) {
                lang.send(sender, lang.get("Update.Download.Error.Not Supported").replace("<apiversion>", fixedapiversion).replace("<current>", bukkitVersion.getVersion()));
                ConfirmSubCommand.addPendingConfirmation(sender, new UniqueRunnable(deleteConfirmationUniqueId)
                {
                    @Override
                    public void run()
                    {
                        lang.send(sender, lang.get("Update.Download.Confirmation.Not Supported.Deleted"));
                        update.delete();
                    }
                }, lang.get("Update.Download.Confirmation.Not Supported.Description"));
                return null;
            }

            String downloadedVersion = description.getString("version").orElse(PlayMoreSoundsVersion.version);

            if (new Version(downloadedVersion).compareTo(PlayMoreSoundsVersion.getVersion()) < 0) {
                lang.send(sender, lang.get("Update.Download.Error.Not Latest").replace("<downloaded>", downloadedVersion).replace("<current>", PlayMoreSoundsVersion.version));
                ConfirmSubCommand.addPendingConfirmation(sender, new UniqueRunnable(deleteConfirmationUniqueId)
                {
                    @Override
                    public void run()
                    {
                        lang.send(sender, lang.get("Update.Download.Confirmation.Not Latest.Deleted"));
                        update.delete();
                    }
                }, lang.get("Update.Download.Confirmation.Not Latest.Description"));
                return null;
            }

            return downloadedVersion;
        } catch (Exception e) {
            lang.send(sender, lang.get("Update.Error.Default"));
            PlayMoreSoundsCore.getErrorHandler().report(e, "Download Update Exception:");
            return null;
        }
    }

    private static boolean isUnsupported(Version version)
    {
        return version.compareTo(new Version("4.2")) >= 0 && PlayMoreSoundsCore.getServerVersion().compareTo(new Version("1.18")) < 0;
    }
}
