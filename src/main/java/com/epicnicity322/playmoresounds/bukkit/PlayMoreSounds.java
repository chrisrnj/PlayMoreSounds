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

package com.epicnicity322.playmoresounds.bukkit;

import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.epicpluginlib.bukkit.logger.Logger;
import com.epicnicity322.epicpluginlib.core.EpicPluginLib;
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.core.logger.ErrorLogger;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.playmoresounds.bukkit.command.CommandLoader;
import com.epicnicity322.playmoresounds.bukkit.listener.*;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundManager;
import com.epicnicity322.playmoresounds.bukkit.util.ListenerRegister;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import com.epicnicity322.playmoresounds.bukkit.util.UpdateManager;
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import com.epicnicity322.playmoresounds.core.addons.AddonManager;
import com.epicnicity322.playmoresounds.core.addons.StartTime;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.util.LoadableHashSet;
import org.bstats.bukkit.MetricsLite;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

public final class PlayMoreSounds extends JavaPlugin implements com.epicnicity322.playmoresounds.core.PlayMoreSounds
{
    private static final @NotNull HashSet<Runnable> onDisableRunnables = new HashSet<>();
    private static final @NotNull HashSet<Runnable> onEnableRunnables = new HashSet<>();
    private static final @NotNull HashSet<Runnable> onInstanceRunnables = new HashSet<>();
    private static final @NotNull LoadableHashSet<String> serverPlugins = new LoadableHashSet<>();
    private static final @NotNull PluginManager pluginManager = Bukkit.getPluginManager();
    private static final @NotNull Path folder = Paths.get("plugins").resolve("PlayMoreSounds");
    private static final @NotNull MessageSender messageSender = new MessageSender(Configurations.CONFIG.getPluginConfig(),
            Configurations.LANGUAGE_EN_US.getPluginConfig());
    private static final @NotNull Random random = new Random();
    private static @NotNull Logger logger = new Logger(PMSHelper.isChristmas() ? "&f[&4PlayMoreSounds&f] " : "&6[&9PlayMoreSounds&6] ");
    private static @Nullable PlayMoreSounds instance;
    private static @NotNull ErrorLogger errorLogger;
    private static boolean success = true;

    static {
        // Creating data folder.
        if (Files.notExists(folder)) {
            try {
                Files.createDirectories(folder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        errorLogger = new ErrorLogger(folder, "PlayMoreSounds", getVersion().getVersion(),
                Collections.singleton("Epicnicity322"), "https://www.spigotmc.org/resources/37429/", Bukkit.getLogger());

        // Checking if EpicPluginLib is outdated.
        if (EpicPluginLib.version.compareTo(new Version("1.6.5")) < 0) {
            success = false;

            addOnEnableRunnable(() -> logger.log("You are running an old version of EpicPluginLib, make sure you are using the latest one.", ConsoleLogger.Level.ERROR));
        } else {
            // Continue initializing PMS.
            messageSender.addLanguage("EN_US", Configurations.LANGUAGE_EN_US.getPluginConfig());
            messageSender.addLanguage("ES_LA", Configurations.LANGUAGE_ES_LA.getPluginConfig());
            messageSender.addLanguage("PT_BR", Configurations.LANGUAGE_PT_BR.getPluginConfig());
            messageSender.addLanguage("ZH_CN", Configurations.LANGUAGE_ZH_CN.getPluginConfig());
        }
    }

    private final @NotNull Path jar = getFile().toPath();
    private final @NotNull AddonManager addonManager;

    public PlayMoreSounds()
    {
        instance = this;
        logger = new Logger(logger.getPrefix(), getLogger());

        PluginDescriptionFile descriptionFile = getDescription();

        errorLogger = new ErrorLogger(folder, descriptionFile.getName(), getVersion().getVersion(), descriptionFile.getAuthors(),
                descriptionFile.getWebsite(), getLogger());
        addonManager = new AddonManager(this, serverPlugins);

        if (!onInstanceRunnables.isEmpty())
            new Thread(() -> {
                for (Runnable runnable : onInstanceRunnables)
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        logger.log("&cAn unknown error occurred on PlayMoreSounds initialization.");
                        errorLogger.report(e, "PMSInitializationError (Unknown):");
                    }
            }).start();
    }

    /**
     * Adds a runnable to run when PlayMoreSounds is disabled by bukkit.
     *
     * @param runnable The runnable to run on disable.
     */
    public static void addOnDisableRunnable(@NotNull Runnable runnable)
    {
        onDisableRunnables.add(runnable);
    }

    /**
     * Adds a runnable to run when PlayMoreSounds is enabled by bukkit.
     *
     * @param runnable The runnable to run on enable.
     */
    public static void addOnEnableRunnable(@NotNull Runnable runnable)
    {
        onEnableRunnables.add(runnable);
    }

    /**
     * Adds a runnable to run when PlayMoreSounds is instantiated by bukkit.
     *
     * @param runnable The runnable to run when PlayMoreSounds is instantiated.
     */
    public static void addOnInstanceRunnable(@NotNull Runnable runnable)
    {
        if (getInstance() != null)
            runnable.run();

        onInstanceRunnables.add(runnable);
    }

    /**
     * @return An instance of this class, null if it wasn't instantiated by bukkit yet.
     */
    public static @Nullable PlayMoreSounds getInstance()
    {
        return instance;
    }

    public static @NotNull Logger getPMSLogger()
    {
        return logger;
    }

    public static @NotNull MessageSender getMessageSender()
    {
        return messageSender;
    }

    public static @NotNull Path getFolder()
    {
        return folder;
    }

    public static @NotNull ErrorLogger getErrorLogger()
    {
        return errorLogger;
    }

    /**
     * Gets the running version of PlayMoreSounds.
     */
    public static @NotNull Version getVersion()
    {
        return version;
    }

    @Override
    public @NotNull Path getJar()
    {
        return jar;
    }

    @Override
    public @NotNull Path getCoreDataFolder()
    {
        return folder;
    }

    @Override
    public @NotNull ErrorLogger getCoreErrorLogger()
    {
        return errorLogger;
    }

    @Override
    public @NotNull ConsoleLogger<?> getCoreLogger()
    {
        return logger;
    }

    @Override
    public @NotNull AddonManager getAddonManager()
    {
        return addonManager;
    }

    @Override
    public void onEnable()
    {
        try {
            if (!success)
                return;

            for (Plugin plugin : Bukkit.getPluginManager().getPlugins())
                serverPlugins.add(plugin.getName());

            serverPlugins.setLoaded(true);

            try {
                addonManager.registerAddons();
            } catch (UnsupportedOperationException ignored) {
                // Only thrown if addons were registered before.
            } catch (IOException ex) {
                logger.log("&cFailed to register addons.");
                errorLogger.report(ex, "Addon registration error:");
            }

            addonManager.startAddons(StartTime.BEFORE_CONFIGURATIONS);

            Configurations.getConfigLoader().loadConfigurations();
            logger.log("&6-> &eConfigurations loaded.");

            addonManager.startAddons(StartTime.BEFORE_LISTENERS);

            // Registering all listeners:

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
            TimeTrigger.load();
            ListenerRegister.loadListeners();

            logger.log("&6-> &eListeners loaded.");

            addonManager.startAddons(StartTime.BEFORE_COMMANDS);

            CommandLoader.loadCommands();

            logger.log("&6-> &eCommands loaded.");

            UpdateManager.loadUpdater();
        } catch (Exception e) {
            success = false;
            errorLogger.report(e, "PMSLoadingError (Unknown):");
        } finally {
            if (success) {
                logger.log("&6============================================");
                logger.log("&aPlayMoreSounds has been enabled");
                logger.log("&a" + SoundManager.getSoundTypes().size() + " sounds available on " + VersionUtils.getBukkitVersion());
                logger.log("&6============================================");

                LocalDateTime now = LocalDateTime.now();

                if (now.getMonth() == Month.OCTOBER && now.getDayOfMonth() == 31) {
                    boolean bool = random.nextBoolean();

                    if (bool)
                        logger.log("&6H&ea&6p&ep&6y&e H&6a&el&6l&eo&6w&ee&6e&en&6!");
                    else
                        logger.log("&6T&er&6i&ec&6k&e o&6r&e T&6r&ee&6a&et&6?");
                }

                if (PMSHelper.isChristmas()) {
                    boolean bool = random.nextBoolean();

                    if (bool)
                        logger.log("&cMerry Christmas!");
                    else
                        logger.log("&cHappy Christmas!");
                }

                MetricsLite metrics = new MetricsLite(this, 7985);

                if (metrics.isEnabled())
                    logger.log("&ePlayMoreSounds is using bStats. If you don't want to send anonymous data, edit bStats configuration.");

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

            if (!onEnableRunnables.isEmpty())
                new Thread(() -> {
                    for (Runnable runnable : onEnableRunnables)
                        try {
                            runnable.run();
                        } catch (Exception e) {
                            logger.log("&cAn unknown error occurred on PlayMoreSounds startup.");
                            errorLogger.report(e, "PMSLoadingError (Unknown):");
                        }
                }).start();
        }
    }

    @Override
    public void onDisable()
    {
        addonManager.stopAddons();

        if (!onDisableRunnables.isEmpty())
            new Thread(() -> {
                for (Runnable runnable : onDisableRunnables)
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        logger.log("&cAn unknown error occurred on PlayMoreSounds shutdown.");
                        errorLogger.report(e, "PMSUnloadingError (Unknown):");
                    }
            }).start();
    }
}