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

package com.epicnicity322.playmoresounds.sponge;

import com.epicnicity322.epicpluginlib.core.EpicPluginLib;
import com.epicnicity322.epicpluginlib.core.config.ConfigurationHolder;
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.core.logger.ErrorHandler;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.epicpluginlib.sponge.lang.MessageSender;
import com.epicnicity322.epicpluginlib.sponge.logger.Logger;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import com.epicnicity322.playmoresounds.core.addons.AddonManager;
import com.epicnicity322.playmoresounds.core.addons.StartTime;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.sound.SoundType;
import com.epicnicity322.playmoresounds.core.util.LoadableHashSet;
import com.epicnicity322.playmoresounds.core.util.PMSHelper;
import com.epicnicity322.playmoresounds.sponge.metrics.Metrics;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Server;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.metric.MetricsConfigManager;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

@Plugin("playmoresounds")
public final class PlayMoreSounds
{
    private static final @NotNull HashSet<Runnable> onDisableRunnables = new HashSet<>();
    private static final @NotNull HashSet<Runnable> onEnableRunnables = new HashSet<>();
    private static final @NotNull HashSet<Runnable> onReloadRunnables = new HashSet<>();
    private static final @NotNull LoadableHashSet<String> serverPlugins = new LoadableHashSet<>();
    private static final @NotNull MessageSender language = new MessageSender(
            () -> Configurations.CONFIG.getConfigurationHolder().getConfiguration().getString("Language").orElse("EN_US"),
            () -> "&6[&9PlayMoreSounds&6]&e ",
            Configurations.LANGUAGE_EN_US.getConfigurationHolder().getDefaultConfiguration());
    private static boolean success = true;
    private static boolean enabled = false;
    private static boolean disabled = false;

    static {
        // Checking if EpicPluginLib is outdated.
        if (EpicPluginLib.version.compareTo(new Version("2.2")) < 0) {
            throw new IllegalStateException("You are running an old version of EpicPluginLib, make sure you are using 2.2 or similar.");
        }

        language.addLanguage("EN_US", Configurations.LANGUAGE_EN_US.getConfigurationHolder());
        language.addLanguage("ES_LA", Configurations.LANGUAGE_ES_LA.getConfigurationHolder());
        language.addLanguage("PT_BR", Configurations.LANGUAGE_PT_BR.getConfigurationHolder());
        language.addLanguage("ZH_CN", Configurations.LANGUAGE_ZH_CN.getConfigurationHolder());
    }

    private final @NotNull ErrorHandler errorHandler = PlayMoreSoundsCore.getErrorHandler();
    private final @NotNull Logger logger;
    private final @NotNull AddonManager addonManager;
    @Inject
    private PluginContainer container;
    @Inject
    private PluginManager pluginManager;
    @Inject
    private MetricsConfigManager metricsConfigManager;

    @Inject
    public PlayMoreSounds(org.apache.logging.log4j.Logger lf4jLogger, Metrics.Factory metricsFactory)
    {
        logger = new Logger(PMSHelper.isChristmas() ? "&f[&4PlayMoreSounds&f] " : "&6[&9PlayMoreSounds&6] ", lf4jLogger);
        addonManager = new AddonManager(serverPlugins, logger);

        // success can be false if EpicPluginLib version is not supported.
        if (success)
            metricsFactory.make(8393);
    }

    /**
     * Adds a runnable to run when the plugin is disabled. If the plugin was already disabled, then the runnable is run
     * immediately.
     * If a exception is caught, PlayMoreSounds automatically handles it and logs into the data folder.
     *
     * @param runnable Runnable to run on disable.
     */
    public static void addOnDisableRunnable(@NotNull Runnable runnable)
    {
        onDisableRunnables.add(runnable);

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
    public static void addOnEnableRunnable(@NotNull Runnable runnable)
    {
        onEnableRunnables.add(runnable);

        if (enabled) {
            runnable.run();
        }
    }

    /**
     * Adds a runnable to run when the configurations are reloaded.
     * If a exception is caught, PlayMoreSounds automatically handles it and logs into the data folder.
     *
     * @param runnable Runnable to run on configurations reload.
     */
    public static void addOnReloadRunnable(@NotNull Runnable runnable)
    {
        onReloadRunnables.add(runnable);
    }

    /**
     * @return PlayMoreSounds' {@link MessageSender} containing every message from language files.
     */
    public static @NotNull MessageSender getLanguage()
    {
        return language;
    }

    @Listener
    public void onRefreshGame(@SuppressWarnings("unused") RefreshGameEvent event)
    {
        Configurations.getConfigurationLoader().loadConfigurations();

        for (Runnable runnable : onReloadRunnables) {
            try {
                runnable.run();
            } catch (Throwable t) {
                logger.log("&cAn unknown error occurred on PlayMoreSounds reload.");
                PlayMoreSoundsCore.getErrorHandler().report(t, "PMS Reloading Error (Unknown):");
            }
        }
    }

    @Listener
    public void onConstructPlugin(@SuppressWarnings("unused") ConstructPluginEvent event)
    {
        // Checking if PlayMoreSounds was already enabled.
        if (enabled) return;

        try {
            if (!success) return;

            // Loading and registering addons:
            for (PluginContainer plugin : pluginManager.plugins()) {
                serverPlugins.add(plugin.metadata().id());
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

            logger.log("&6-> &eNo listeners loaded.");

            addonManager.startAddons(StartTime.BEFORE_COMMANDS);

            logger.log("&6-> &eCommands not loaded.");
        } catch (Exception e) {
            success = false;
            PlayMoreSoundsCore.getErrorHandler().report(e, "PMSLoadingError (Unknown):");
        } finally {
            if (success) {
                logger.log("&6========================================");
                logger.log("&a PlayMoreSounds is not fully functional");
                logger.log("&aon sponge yet.");
                logger.log("&a " + SoundType.getPresentSoundTypes().size() + " sounds available on " + PlayMoreSoundsCore.getServerVersion());
                logger.log("&6========================================");

                if (metricsConfigManager.collectionState(container) == Tristate.TRUE)
                    logger.log("&ePlayMoreSounds is using bStats as metrics collector.");

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

                addonManager.startAddons(StartTime.END);
            } else {
                logger.log("&6========================================", ConsoleLogger.Level.ERROR);
                logger.log("&c Something went wrong while loading PMS", ConsoleLogger.Level.ERROR);
                logger.log("&c Make sure you read messages before", ConsoleLogger.Level.ERROR);
                logger.log("&creporting.", ConsoleLogger.Level.ERROR);
                logger.log("&6========================================", ConsoleLogger.Level.ERROR);
                logger.log("&4Error log generated on data folder.", ConsoleLogger.Level.WARN);
            }

            for (Runnable runnable : onEnableRunnables) {
                try {
                    runnable.run();
                } catch (Throwable t) {
                    logger.log("&cAn unknown error occurred on PlayMoreSounds startup.");
                    errorHandler.report(t, "PMS Loading Error (Unknown):");
                }
            }

            enabled = true;
        }
    }

    @Listener
    public void onStartedServer(@SuppressWarnings("unused") StartedEngineEvent<Server> event)
    {
        addonManager.startAddons(StartTime.SERVER_LOAD_COMPLETE);
    }

    @Listener
    public void onStoppingServer(@SuppressWarnings("unused") StoppingEngineEvent<Server> event)
    {
        // Checking if PlayMoreSounds was already disabled.
        if (disabled) return;

        addonManager.stopAddons();

        for (Runnable runnable : onDisableRunnables) {
            try {
                runnable.run();
            } catch (Throwable t) {
                logger.log("&cAn unknown error occurred on PlayMoreSounds shutdown.");
                errorHandler.report(t, "PMS Unloading Error (Unknown):");
            }
        }

        disabled = true;
    }
}
