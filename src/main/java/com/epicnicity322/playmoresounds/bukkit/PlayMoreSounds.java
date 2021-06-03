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

package com.epicnicity322.playmoresounds.bukkit;

import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.epicpluginlib.bukkit.logger.Logger;
import com.epicnicity322.epicpluginlib.core.EpicPluginLib;
import com.epicnicity322.epicpluginlib.core.config.ConfigurationHolder;
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.core.logger.ErrorHandler;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.playmoresounds.bukkit.command.CommandLoader;
import com.epicnicity322.playmoresounds.bukkit.listener.*;
import com.epicnicity322.playmoresounds.bukkit.metrics.Metrics;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableSound;
import com.epicnicity322.playmoresounds.bukkit.util.ListenerRegister;
import com.epicnicity322.playmoresounds.bukkit.util.UpdateManager;
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import com.epicnicity322.playmoresounds.core.addons.AddonManager;
import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import com.epicnicity322.playmoresounds.core.addons.StartTime;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.sound.SoundType;
import com.epicnicity322.playmoresounds.core.util.LoadableHashSet;
import com.epicnicity322.playmoresounds.core.util.PMSHelper;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public final class PlayMoreSounds extends JavaPlugin
{
    private static final @NotNull HashSet<Runnable> onDisable = new HashSet<>();
    private static final @NotNull HashSet<Runnable> onEnable = new HashSet<>();
    private static final @NotNull HashSet<Runnable> onInstance = new HashSet<>();
    private static final @NotNull HashSet<Runnable> onReload = new HashSet<>();
    private static final @NotNull Logger logger = new Logger(PMSHelper.isChristmas() ? "&f[&4PlayMoreSounds&f] " : "&6[&9PlayMoreSounds&6] ");
    private static final @NotNull MessageSender language = new MessageSender(
            () -> Configurations.CONFIG.getConfigurationHolder().getConfiguration().getString("Language").orElse("EN_US"),
            logger::getPrefix,
            Configurations.LANGUAGE_EN_US.getConfigurationHolder().getDefaultConfiguration());
    private static @Nullable PlayMoreSounds instance;
    private static boolean enabled = false;
    private static boolean disabled = false;
    private static boolean success = true;

    static {
        language.addLanguage("EN_US", Configurations.LANGUAGE_EN_US.getConfigurationHolder());
        language.addLanguage("ES_LA", Configurations.LANGUAGE_ES_LA.getConfigurationHolder());
        language.addLanguage("PT_BR", Configurations.LANGUAGE_PT_BR.getConfigurationHolder());
        language.addLanguage("ZH_CN", Configurations.LANGUAGE_ZH_CN.getConfigurationHolder());

        // Checking if EpicPluginLib is outdated.
        if (EpicPluginLib.version.compareTo(new Version("2.0")) < 0) {
            success = false;
            logger.log("You are running an old version of EpicPluginLib, make sure you are using 2.0 or similar.", ConsoleLogger.Level.ERROR);
        }
    }

    private final @NotNull LoadableHashSet<String> serverPlugins = new LoadableHashSet<>();
    private final @NotNull AddonManager addonManager = new AddonManager(serverPlugins, logger);
    private final @NotNull ErrorHandler errorHandler = PlayMoreSoundsCore.getErrorHandler();

    public PlayMoreSounds()
    {
        instance = this;

        logger.setLogger(getLogger());
        errorHandler.setLogger(getLogger());

        for (Runnable runnable : onInstance) {
            try {
                runnable.run();
            } catch (Exception e) {
                logger.log("&cAn unknown error occurred on PlayMoreSounds initialization.");
                errorHandler.report(e, "PMS Initialization Error (Unknown):");
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
        onDisable.add(runnable);

        if (disabled) {
            runnable.run();
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
        onEnable.add(runnable);

        if (enabled) {
            runnable.run();
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
        onInstance.add(runnable);

        if (getInstance() != null) {
            runnable.run();
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

    public static @NotNull HashMap<ConfigurationHolder, Exception> reload()
    {
        HashMap<ConfigurationHolder, Exception> exceptions = Configurations.getConfigurationLoader().loadConfigurations();
        ListenerRegister.loadListeners();
        WorldTimeListener.load();
        UpdateManager.check(Bukkit.getConsoleSender(), true);

        for (Runnable runnable : onReload) {
            try {
                runnable.run();
            } catch (Exception e) {
                logger.log("&cAn unknown error occurred on PlayMoreSounds reload.");
                PlayMoreSoundsCore.getErrorHandler().report(e, "PMS Reloading Error (Unknown):");
            }
        }

        return exceptions;
    }

    @Override
    public void onEnable()
    {
        // Checking if PlayMoreSounds was already enabled.
        if (enabled) return;

        try {
            if (!success) return;

            // Loading and registering addons:
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                serverPlugins.add(plugin.getName());
            }

            serverPlugins.setLoaded(true);

            try {
                addonManager.registerAddons();
            } catch (UnsupportedOperationException ignored) {
                // Only thrown if addons were registered before.
            } catch (IOException ex) {
                logger.log("&cFailed to register addons.");
                errorHandler.report(ex, "Addon registration error:");
            }

            addonManager.startAddons(StartTime.BEFORE_CONFIGURATIONS);

            HashMap<ConfigurationHolder, Exception> exceptions = Configurations.getConfigurationLoader().loadConfigurations();

            if (exceptions.isEmpty()) {
                logger.log("&6-> &eConfigurations loaded.");
            } else {
                logger.log("Unable to load configurations.", ConsoleLogger.Level.ERROR);

                exceptions.forEach((config, e) -> errorHandler.report(e, "Configuration: " + config.getPath() + "\nConfig load error:"));

                success = false;
                return;
            }

            addonManager.startAddons(StartTime.BEFORE_LISTENERS);

            // Registering all listeners:
            PluginManager pluginManager = Bukkit.getPluginManager();

            if (VersionUtils.supportsResourcePacks()) {
                pluginManager.registerEvents(new OnPlayerResourcePackStatus(), this);
            }

            // Registering region wand tool listener.
            pluginManager.registerEvents(new OnPlayerInteract(), this);
            // Registering region enter event caller.
            pluginManager.registerEvents(new OnPlayerJoin(this), this);
            // Registering region enter and leave event caller.
            pluginManager.registerEvents(new OnPlayerMove(), this);
            // Registering region leave event caller.
            pluginManager.registerEvents(new OnPlayerQuit(), this);
            // Registering region enter and leave event caller.
            pluginManager.registerEvents(new OnPlayerTeleport(this), this);
            // TimeTrigger checks itself it does need to load or not on load method.
            WorldTimeListener.load();

            logger.log("&6-> &e" + ListenerRegister.loadListeners() + " listeners loaded.");

            addonManager.startAddons(StartTime.BEFORE_COMMANDS);
            CommandLoader.getCommands();
            logger.log("&6-> &eCommands loaded.");

            // Loading Nature Sound Replacer:
            if (VersionUtils.hasSoundEffects()) {
                try {
                    // Checking if ProtocolLib is present
                    Class.forName("com.comphenix.protocol.events.PacketAdapter");
                    new OnNamedSoundEffect(this);
                    logger.log("&eProtocolLib was found and hooked.");
                } catch (ClassNotFoundException ignored) {
                }
            }
        } catch (Exception e) {
            success = false;
            errorHandler.report(e, "PMS Loading Error (Unknown):");
        } finally {
            if (success) {
                logger.log("&6============================================");
                logger.log("&aPlayMoreSounds has been enabled");
                logger.log("&a" + SoundType.getPresentSoundTypes().size() + " sounds available on " + VersionUtils.getBukkitVersion());
                logger.log("&6============================================");

                if (VersionUtils.supportsBStats()) {
                    Metrics metrics = new Metrics(this, 7985);

                    metrics.addCustomChart(new Metrics.AdvancedPie("running_addons", () -> {
                        HashSet<PMSAddon> addons = addonManager.getAddons();
                        HashMap<String, Integer> map = new HashMap<>(addons.size());

                        addons.forEach(addon -> map.put(addon.getDescription().getName(), 1));
                        return map;
                    }));

                    logger.log("&ePlayMoreSounds is using bStats as metrics collector.");
                }

                LocalDateTime now = LocalDateTime.now();
                Random random = new Random();

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

                UpdateManager.loadUpdater();

                addonManager.startAddons(StartTime.END);

                // Bukkit only runs a task once the server has fully loaded.
                Bukkit.getScheduler().runTaskLater(this, () -> addonManager.startAddons(StartTime.SERVER_LOAD_COMPLETE), 1);
            } else {
                logger.log("&6============================================");
                logger.log("&cSomething went wrong while loading PMS");
                logger.log("&cMake sure you read messages before reporting");
                logger.log("&6============================================");
                getLogger().severe("Plugin disabled.");
                Bukkit.getPluginManager().disablePlugin(this);
            }

            for (Runnable runnable : onEnable) {
                try {
                    runnable.run();
                } catch (Exception e) {
                    logger.log("&cAn unknown error occurred on PlayMoreSounds startup.");
                    errorHandler.report(e, "PMS Loading Error (Unknown):");
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

        addonManager.stopAddons();

        for (Runnable runnable : onDisable) {
            try {
                runnable.run();
            } catch (Exception e) {
                logger.log("&cAn unknown error occurred on PlayMoreSounds shutdown.");
                errorHandler.report(e, "PMS Unloading Error (Unknown):");
            }
        }

        disabled = true;
    }
}