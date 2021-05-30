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

package com.epicnicity322.playmoresounds.sponge;

import com.epicnicity322.epicpluginlib.core.EpicPluginLib;
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.epicpluginlib.sponge.logger.Logger;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsVersion;
import com.epicnicity322.playmoresounds.core.addons.AddonManager;
import com.epicnicity322.playmoresounds.core.addons.StartTime;
import com.epicnicity322.playmoresounds.core.util.LoadableHashSet;
import com.epicnicity322.playmoresounds.core.util.PMSHelper;
import com.epicnicity322.playmoresounds.sponge.listeners.OnClientConnection;
import com.google.inject.Inject;
import org.bstats.sponge.Metrics;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.metric.MetricsConfigManager;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;

@Plugin(id = "playmoresounds",
        name = "PlayMoreSounds",
        version = PlayMoreSoundsVersion.version,
        description = "Plays sounds at player events.",
        dependencies = @Dependency(id = "epicpluginlib"))
public final class PlayMoreSounds
{
    private static final @NotNull HashSet<Runnable> onDisableRunnables = new HashSet<>();
    private static final @NotNull HashSet<Runnable> onEnableRunnables = new HashSet<>();
    private static final @NotNull LoadableHashSet<String> serverPlugins = new LoadableHashSet<>();
    private static final @NotNull Random random = new Random();
    private static boolean success = true;
    private static boolean enabled = false;
    private static boolean disabled = false;

    static {
        if (EpicPluginLib.version.compareTo(new Version("2.0")) < 0) {
            success = false;

            addOnEnableRunnable(() -> {
                Optional<PluginContainer> plugin = Sponge.getGame().getPluginManager().getPlugin("playmoresounds");

                // EpicPluginLib didn't had platform independent message leveling before 1.6.1 so I'm using l4fj logger.
                if (plugin.isPresent())
                    plugin.get().getLogger().error("You are running an old version of EpicPluginLib, make sure you are using the latest one.");
                else
                    System.out.println("You are running an old version of EpicPluginLib, make sure you are using the latest one.");
            });
        }
    }

    private final @NotNull String gameVersion;
    private final @NotNull Logger logger;
    private final @NotNull AddonManager addonManager;
    @Inject
    private PluginContainer container;
    @Inject
    private PluginManager pluginManager;
    @Inject
    private MetricsConfigManager metricsConfigManager;

    @Inject
    public PlayMoreSounds(Game game, org.slf4j.Logger lf4jLogger, Metrics.Factory metricsFactory)
    {
        logger = new Logger(PMSHelper.isChristmas() ? "&f[&4PlayMoreSounds&f] " : "&6[&9PlayMoreSounds&6] ", lf4jLogger);
        gameVersion = game.getPlatform().getContainer(Platform.Component.GAME).getVersion().orElse("0");
        addonManager = new AddonManager(serverPlugins, logger);

        // success can be false if EpicPluginLib version is not supported.
        if (success)
            metricsFactory.make(8393);
    }

    /**
     * Adds a runnable to run when PlayMoreSounds is disabled by bukkit.
     *
     * @param runnable The runnable to run on disable.
     */
    public static void addOnDisableRunnable(@NotNull Runnable runnable)
    {
        if (disabled)
            runnable.run();
        else
            onDisableRunnables.add(runnable);
    }

    /**
     * Adds a runnable to run when PlayMoreSounds is enabled.
     *
     * @param runnable The runnable to run on enable.
     */
    public static void addOnEnableRunnable(@NotNull Runnable runnable)
    {
        if (enabled)
            runnable.run();
        else
            onEnableRunnables.add(runnable);
    }

    @Listener
    public void onGameInitialization(@SuppressWarnings("unused") GameInitializationEvent event)
    {
        try {
            if (!success)
                return;

            for (PluginContainer plugin : pluginManager.getPlugins())
                serverPlugins.add(plugin.getId());

            serverPlugins.setLoaded(true);

            try {
                addonManager.registerAddons();
            } catch (UnsupportedOperationException ignored) {
                // Only thrown if addons were registered before.
            } catch (IOException ex) {
                logger.log("&cFailed to register addons.");
                PlayMoreSoundsCore.getErrorHandler().report(ex, "Addon registration error:");
            }

            addonManager.startAddons(StartTime.BEFORE_CONFIGURATIONS);

            logger.log("&6-> &eConfigurations not loaded.");

            addonManager.startAddons(StartTime.BEFORE_LISTENERS);

            Sponge.getEventManager().registerListeners(this, new OnClientConnection());

            logger.log("&6-> &eOne listener loaded.");

            addonManager.startAddons(StartTime.BEFORE_COMMANDS);

            logger.log("&6-> &eCommands not loaded.");
        } catch (Exception e) {
            success = false;
            PlayMoreSoundsCore.getErrorHandler().report(e, "PMSLoadingError (Unknown):");
        } finally {
            if (success) {
                logger.log("&6============================================");
                logger.log("&a PlayMoreSounds is not fully functional on");
                logger.log("&asponge yet.");
                logger.log("&a 000 sounds available on " + gameVersion);
                logger.log("&6============================================");

                if (metricsConfigManager.getCollectionState(container) == Tristate.TRUE)
                    logger.log("&ePlayMoreSounds is using bStats as metrics collector.");

                LocalDateTime now = LocalDateTime.now();

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

                if (!onEnableRunnables.isEmpty())
                    new Thread(() -> {
                        for (Runnable runnable : onEnableRunnables)
                            try {
                                runnable.run();
                            } catch (Exception e) {
                                logger.log("&cAn unknown error occurred on PlayMoreSounds startup.");
                                PlayMoreSoundsCore.getErrorHandler().report(e, "PMS Loading Error (Unknown):");
                            }
                    }).start();

                enabled = true;
            } else {
                logger.log("&6============================================", ConsoleLogger.Level.ERROR);
                logger.log("&cSomething went wrong while loading PMS", ConsoleLogger.Level.ERROR);
                logger.log("&cMake sure you read messages before reporting", ConsoleLogger.Level.ERROR);
                logger.log("&6============================================", ConsoleLogger.Level.ERROR);
                logger.log("&4Error log generated on data folder.", ConsoleLogger.Level.WARN);
            }
        }
    }

    @Listener
    public void onGameStartedServer(@SuppressWarnings("unused") GameStartedServerEvent event)
    {
        addonManager.startAddons(StartTime.SERVER_LOAD_COMPLETE);
    }

    @Listener
    public void onGameStopping(@SuppressWarnings("unused") GameStoppingEvent event)
    {
        addonManager.stopAddons();

        if (!onDisableRunnables.isEmpty())
            new Thread(() -> {
                for (Runnable runnable : onDisableRunnables)
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        logger.log("&cAn unknown error occurred on PlayMoreSounds shutdown.");
                        PlayMoreSoundsCore.getErrorHandler().report(e, "PMS Unloading Error (Unknown):");
                    }
            }).start();

        disabled = true;
    }
}
