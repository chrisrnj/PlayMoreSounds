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

package com.epicnicity322.playmoresounds.bukkit;

import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.epicpluginlib.bukkit.logger.Logger;
import com.epicnicity322.epicpluginlib.core.EpicPluginLib;
import com.epicnicity322.epicpluginlib.core.config.ConfigurationHolder;
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.epicpluginlib.core.util.PathUtils;
import com.epicnicity322.playmoresounds.bukkit.command.CommandLoader;
import com.epicnicity322.playmoresounds.bukkit.command.subcommands.AddonsSubCommand;
import com.epicnicity322.playmoresounds.bukkit.gui.inventories.ListInventory;
import com.epicnicity322.playmoresounds.bukkit.listeners.*;
import com.epicnicity322.playmoresounds.bukkit.metrics.Metrics;
import com.epicnicity322.playmoresounds.bukkit.region.RegionManager;
import com.epicnicity322.playmoresounds.bukkit.region.SoundRegion;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableSound;
import com.epicnicity322.playmoresounds.bukkit.util.ListenerRegister;
import com.epicnicity322.playmoresounds.bukkit.util.UpdateManager;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import com.epicnicity322.playmoresounds.core.addons.AddonManager;
import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import com.epicnicity322.playmoresounds.core.addons.StartTime;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.sound.SoundType;
import com.epicnicity322.playmoresounds.core.util.LoadableHashSet;
import com.epicnicity322.playmoresounds.core.util.PMSHelper;
import com.epicnicity322.yamlhandler.YamlConfigurationLoader;
import org.bukkit.Bukkit;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public final class PlayMoreSounds extends JavaPlugin
{
    private static final @NotNull Logger logger = new Logger(PMSHelper.isChristmas() ? "&f[&4PlayMoreSounds&f] " : "&6[&9PlayMoreSounds&6] ");
    private static final @NotNull MessageSender language;
    private static final @NotNull LoadableHashSet<String> serverPlugins = new LoadableHashSet<>();
    private static final @NotNull AddonManager addonManager = new AddonManager(serverPlugins, logger);
    private static @Nullable HashSet<Runnable> onDisable;
    private static @Nullable HashSet<Runnable> onEnable;
    private static @Nullable HashSet<Runnable> onInstance;
    private static @Nullable HashSet<Runnable> onReload;
    private static @Nullable PlayMoreSounds instance;
    private static boolean enabled = false;
    private static boolean disabled = false;

    static {
        // Checking if EpicPluginLib is outdated.
        if (EpicPluginLib.version.compareTo(new Version("2.2.1")) < 0) {
            throw new UnknownDependencyException("You are running an old version of EpicPluginLib, make sure you are using 2.2.1 or similar.");
        }

        PlayMoreSoundsCore.getErrorHandler().setLogger(logger);

        language = new MessageSender(
                () -> Configurations.CONFIG.getConfigurationHolder().getConfiguration().getString("Language").orElse("EN_US"),
                logger::getPrefix,
                Configurations.LANGUAGE_EN_US.getConfigurationHolder().getDefaultConfiguration());
        language.addLanguage("EN_US", Configurations.LANGUAGE_EN_US.getConfigurationHolder());
        language.addLanguage("ES_LA", Configurations.LANGUAGE_ES_LA.getConfigurationHolder());
        language.addLanguage("PT_BR", Configurations.LANGUAGE_PT_BR.getConfigurationHolder());
        language.addLanguage("ZH_CN", Configurations.LANGUAGE_ZH_CN.getConfigurationHolder());
    }

    public PlayMoreSounds()
    {
        instance = this;

        logger.setLogger(getLogger());

        if (onInstance == null) return;
        for (Runnable runnable : onInstance) {
            try {
                runnable.run();
            } catch (Throwable e) {
                logger.log("&cAn unknown error occurred on PlayMoreSounds initialization.");
                PlayMoreSoundsCore.getErrorHandler().report(e, "PMS Initialization Error (Unknown):");
            }
        }
    }

    /**
     * Adds a runnable to run when the plugin is disabled. If the plugin was already disabled, then the runnable is run
     * immediately.
     * If a exception is caught, PlayMoreSounds automatically handles it and logs into the data folder.
     *
     * @param runnable Runnable to run on disable.
     */
    public static void onDisable(@NotNull Runnable runnable)
    {
        if (disabled) {
            try {
                runnable.run();
            } catch (Throwable t) {
                PlayMoreSoundsCore.getErrorHandler().report(t, "Plugin already disabled so instantly execute #onDisable:");
            }
        } else {
            if (onDisable == null) onDisable = new HashSet<>();
            onDisable.add(runnable);
        }
    }

    /**
     * Adds a runnable to run when the plugin is enabled. If the plugin was already enabled, then the runnable is run
     * immediately.
     * If a exception is caught, PlayMoreSounds automatically handles it and logs into the data folder.
     *
     * @param runnable Runnable to run on enable.
     */
    public static void onEnable(@NotNull Runnable runnable)
    {
        if (enabled) {
            try {
                runnable.run();
            } catch (Throwable t) {
                PlayMoreSoundsCore.getErrorHandler().report(t, "Plugin already enabled so instantly execute #onEnable:");
            }
        } else {
            if (onEnable == null) onEnable = new HashSet<>();
            onEnable.add(runnable);
        }
    }

    /**
     * Adds a runnable to run when the plugin is loaded. If the plugin was already loaded, then the runnable is run
     * immediately.
     * If a exception is caught, PlayMoreSounds automatically handles it and logs into the data folder.
     *
     * @param runnable Runnable to run on load.
     */
    public static void onInstance(@NotNull Runnable runnable)
    {
        if (getInstance() != null) {
            try {
                runnable.run();
            } catch (Throwable t) {
                PlayMoreSoundsCore.getErrorHandler().report(t, "Plugin already instantiated so instantly execute #onInstance:");
            }
        } else {
            if (onInstance == null) onInstance = new HashSet<>();
            onInstance.add(runnable);
        }
    }

    /**
     * Adds a runnable to run when the configurations are reloaded.
     * If a exception is caught, PlayMoreSounds automatically handles it and logs into the data folder.
     *
     * @param runnable Runnable to run on configurations reload.
     */
    public static void onReload(@NotNull Runnable runnable)
    {
        if (onReload == null) onReload = new HashSet<>();
        onReload.add(runnable);
    }

    /**
     * The instance of PlayMoreSounds' main class, although this is not the best approach, I decided to leave it like
     * that for easy use of the API, like playing sounds on {@link PlayableSound}
     *
     * @return The instance of PlayMoreSounds JavaPlugin class, or null if the plugin wasn't loaded yet.
     */
    public static @Nullable PlayMoreSounds getInstance()
    {
        return instance;
    }

    /**
     * @return The logger with PlayMoreSounds' prefix.
     */
    public static @NotNull Logger getConsoleLogger()
    {
        return logger;
    }

    /**
     * @return PlayMoreSounds' {@link MessageSender} containing every message from language files.
     */
    public static @NotNull MessageSender getLanguage()
    {
        return language;
    }

    /**
     * Gets the addon manager for this platform.
     *
     * @return The addon manager.
     */
    public static @NotNull AddonManager getAddonManager()
    {
        return addonManager;
    }

    public static @NotNull HashMap<ConfigurationHolder, Exception> reload()
    {
        if (instance == null) throw new IllegalStateException("PlayMoreSounds is not loaded.");

        HashMap<ConfigurationHolder, Exception> exceptions = Configurations.getConfigurationLoader().loadConfigurations();
        RegionManager.reload();
        ListenerRegister.loadListeners();
        WorldTimeListener.load();
        UpdateManager.loadUpdater(instance);
        ListInventory.refreshListInventories();
        OnPlayerResourcePackStatus.load(instance);

        if (onReload == null) return exceptions;

        synchronized (PlayMoreSounds.class) {
            for (Runnable runnable : onReload) {
                try {
                    runnable.run();
                } catch (Throwable e) {
                    logger.log("&cAn unknown error occurred on PlayMoreSounds reload.");
                    PlayMoreSoundsCore.getErrorHandler().report(e, "PMS Reloading Error (Unknown):");
                }
            }
        }

        return exceptions;
    }

    @Override
    public void onEnable()
    {
        // Checking if PlayMoreSounds was already enabled.
        if (enabled) return;

        var errorHandler = PlayMoreSoundsCore.getErrorHandler();
        boolean success = false;

        try {
            // Loading and registering addons:
            for (var plugin : Bukkit.getPluginManager().getPlugins()) {
                serverPlugins.add(plugin.getName());
            }

            serverPlugins.setLoaded(true);

            try {
                addonManager.registerAddons();
            } catch (UnsupportedOperationException ignored) {
                // Only thrown if addons were registered before, which is irrelevant.
            } catch (IOException ex) {
                logger.log("&cFailed to register addons.");
                errorHandler.report(ex, "Addon registration error:");
            }

            addonManager.startAddons(StartTime.BEFORE_CONFIGURATIONS);

            HashMap<ConfigurationHolder, Exception> exceptions = Configurations.getConfigurationLoader().loadConfigurations();

            if (exceptions.isEmpty()) {
                logger.log("&6-> &eConfigurations loaded.");
            } else {
                logger.log("Unable to load some configurations.", ConsoleLogger.Level.ERROR);
                exceptions.forEach((config, e) -> errorHandler.report(e, "Configuration: " + config.getPath() + "\nConfig load error:"));
                success = false;
                return;
            }

            RegionManager.reload();

            addonManager.startAddons(StartTime.BEFORE_LISTENERS);

            // Registering all listeners:
            var pm = Bukkit.getPluginManager();

            OnPlayerResourcePackStatus.load(this);
            // Registering region wand tool listener.
            pm.registerEvents(new OnPlayerInteract(), this);
            // Registering region enter event caller.
            pm.registerEvents(new OnPlayerJoin(), this);
            // Registering region enter and leave event caller.
            pm.registerEvents(new OnPlayerMove(), this);
            // Registering region leave event caller.
            pm.registerEvents(new OnPlayerQuit(), this);
            // Registering region enter and leave event caller.
            pm.registerEvents(new OnPlayerTeleport(this), this);
            // TimeTrigger checks itself it does need to load or not on load method.
            WorldTimeListener.load();

            logger.log("&6-> &e" + ListenerRegister.loadListeners() + " listeners loaded.");

            addonManager.startAddons(StartTime.BEFORE_COMMANDS);
            CommandLoader.getCommands();
            logger.log("&6-> &eCommands loaded.");
            success = true;
        } catch (Exception e) {
            errorHandler.report(e, "PMS Loading Error (Unknown):");
        } finally {
            if (success) {
                var serverVersion = EpicPluginLib.Platform.getVersion();

                logger.log("&6============================================");
                logger.log("&aPlayMoreSounds has been enabled");
                logger.log("&a" + SoundType.getPresentSoundTypes().size() + " sounds available on " + serverVersion);
                logger.log("&6============================================");

                if (serverVersion.compareTo(new Version("1.17")) < 0) {
                    logger.log("PlayMoreSounds detected you are on version " + serverVersion + ". This version is not supported so errors will no longer be logged.", ConsoleLogger.Level.WARN);
                } else if (serverVersion.compareTo(new Version("1.19")) >= 0) {
                    logger.log("PlayMoreSounds detected you are on version " + serverVersion + ". This version was not tested and might throw errors.", ConsoleLogger.Level.WARN);
                }

                boolean bStats = false;

                try {
                    bStats = new YamlConfigurationLoader().load(Paths.get("plugins/bStats/config.yml")).getBoolean("enabled").orElse(false);
                } catch (Exception ignored) {
                }

                if (bStats) {
                    var metrics = new Metrics(this, 7985);

                    metrics.addCustomChart(new Metrics.AdvancedPie("running_addons", () -> {
                        HashSet<PMSAddon> addons = addonManager.getAddons();
                        HashMap<String, Integer> map = new HashMap<>(addons.size());

                        addons.forEach(addon -> map.put(addon.getDescription().getName(), 1));
                        return map;
                    }));
                    metrics.addCustomChart(new Metrics.SimplePie("checking_for_updates", () -> Boolean.toString(Configurations.CONFIG.getConfigurationHolder().getConfiguration().getBoolean("Updater.Enabled").orElse(false))));

                    logger.log("&ePlayMoreSounds is using bStats as metrics collector.");
                }

                var now = LocalDateTime.now();
                var random = new Random();

                if (now.getMonth() == Month.OCTOBER && now.getDayOfMonth() == 31) {
                    if (random.nextBoolean())
                        logger.log("&6H&ea&6p&ep&6y&e H&6a&el&6l&eo&6w&ee&6e&en&6!");
                    else
                        logger.log("&6T&er&6i&ec&6k&e o&6r&e T&6r&ee&6a&et&6?");
                }

                if (PMSHelper.isChristmas()) {
                    if (random.nextBoolean())
                        logger.log("&cMerry Christmas!");
                    else
                        logger.log("&cHappy Christmas!");
                }

                if (Configurations.CONFIG.getConfigurationHolder().getConfiguration().getBoolean("Updater.Enabled").orElse(true)) {
                    UpdateManager.check(Bukkit.getConsoleSender());
                    UpdateManager.loadUpdater(this);
                }

                addonManager.startAddons(StartTime.END);

                // Bukkit only runs a task once the server has fully loaded.
                Bukkit.getScheduler().runTask(this, () -> addonManager.startAddons(StartTime.SERVER_LOAD_COMPLETE));
            } else {
                logger.log("&6============================================", ConsoleLogger.Level.ERROR);
                logger.log("&cSomething went wrong while loading PMS", ConsoleLogger.Level.ERROR);
                logger.log("&cMake sure you read errors before reporting", ConsoleLogger.Level.ERROR);
                logger.log("&6============================================", ConsoleLogger.Level.ERROR);
                logger.log("Plugin disabled.", ConsoleLogger.Level.ERROR);
                Bukkit.getPluginManager().disablePlugin(this);
            }

            if (onEnable != null) {
                for (Runnable runnable : onEnable) {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        logger.log("&cAn unknown error occurred on PlayMoreSounds startup.");
                        errorHandler.report(e, "Success: " + success + "\nPMS Loading Error (Unknown):");
                    }
                }
            }

            enabled = true;
        }
    }

    @Override
    public void onDisable()
    {
        // Checking if PlayMoreSounds was already disabled.
        if (disabled) return;

        if (!RegionManager.getRegions().isEmpty()) {
            logger.log("&eSaving regions...");
            int count = 0;

            for (SoundRegion region : RegionManager.getRegions()) {
                try {
                    RegionManager.save(region);
                    count++;
                } catch (Exception e) {
                    logger.log("Unable to save " + region.getName() + " region.", ConsoleLogger.Level.WARN);
                }
            }

            if (count != 0) {
                logger.log("&e" + count + " regions were saved.");
            }
        }

        addonManager.stopAddons();

        if (onDisable != null) {
            for (Runnable runnable : onDisable) {
                try {
                    runnable.run();
                } catch (Throwable e) {
                    logger.log("&cAn unknown error occurred on PlayMoreSounds shutdown.");
                    PlayMoreSoundsCore.getErrorHandler().report(e, "PMS Unloading Error (Unknown):");
                }
            }
        }

        try {
            PathUtils.deleteAll(PlayMoreSoundsCore.getFolder().resolve("Temp"));
        } catch (IOException e) {
            PlayMoreSoundsCore.getErrorHandler().report(e, "Temp Folder Delete Exception:");
        }

        if (!AddonsSubCommand.ADDONS_TO_UNINSTALL.isEmpty()) {
            logger.log("Uninstalling requested addons...");

            for (PMSAddon addon : AddonsSubCommand.ADDONS_TO_UNINSTALL) {
                try {
                    Files.delete(addon.getJar());
                    logger.log("&a" + addon.getDescription().getName() + " addon was uninstalled.");
                } catch (Exception e) {
                    logger.log("&cUnable to uninstall " + addon.getDescription().getName() + " addon.");
                    PlayMoreSoundsCore.getErrorHandler().report(e, "Addon Uninstall Exception");
                }
            }

            AddonsSubCommand.ADDONS_TO_UNINSTALL.clear();
        }

        disabled = true;
    }

    @Override
    public @NotNull File getFile()
    {
        return super.getFile();
    }
}