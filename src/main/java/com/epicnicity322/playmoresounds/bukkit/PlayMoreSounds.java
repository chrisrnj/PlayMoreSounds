package com.epicnicity322.playmoresounds.bukkit;

import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.epicpluginlib.bukkit.logger.Logger;
import com.epicnicity322.epicpluginlib.bukkit.reflection.ReflectionUtil;
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.core.logger.ErrorLogger;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.playmoresounds.bukkit.command.CommandLoader;
import com.epicnicity322.playmoresounds.bukkit.listener.*;
import com.epicnicity322.playmoresounds.bukkit.util.ListenerRegister;
import com.epicnicity322.playmoresounds.bukkit.util.UpdateManager;
import com.epicnicity322.playmoresounds.core.addons.AddonEventManager;
import com.epicnicity322.playmoresounds.core.addons.AddonManager;
import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import com.epicnicity322.playmoresounds.core.addons.StartTime;
import com.epicnicity322.playmoresounds.core.addons.events.AddonLoadUnloadEvent;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.logging.Level;

public final class PlayMoreSounds extends JavaPlugin implements com.epicnicity322.playmoresounds.core.PlayMoreSounds
{
    private static final @NotNull HashSet<Runnable> onDisableRunnables = new HashSet<>();
    private static final @NotNull HashSet<Runnable> onEnableRunnables = new HashSet<>();
    private static final @NotNull HashSet<Runnable> onInstanceRunnables = new HashSet<>();
    private static final @NotNull PluginManager pm = Bukkit.getPluginManager();
    private static final @NotNull Path folder = Paths.get("plugins").resolve("PlayMoreSounds");
    private static final @NotNull Logger logger = new Logger("&6[&9PlayMoreSounds&6] ");
    private static final @NotNull MessageSender messageSender = new MessageSender(Configurations.CONFIG.getPluginConfig(),
            Configurations.LANGUAGE_EN_US.getPluginConfig());
    private static @Nullable PlayMoreSounds instance;
    private static ErrorLogger errorLogger;

    static {
        // Creating data folder.
        if (Files.notExists(folder)) {
            try {
                Files.createDirectories(folder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        messageSender.addLanguage("EN_US", Configurations.LANGUAGE_EN_US.getPluginConfig());
        messageSender.addLanguage("ES_LA", Configurations.LANGUAGE_ES_LA.getPluginConfig());
        messageSender.addLanguage("PT_BR", Configurations.LANGUAGE_PT_BR.getPluginConfig());
        messageSender.addLanguage("ZH_CN", Configurations.LANGUAGE_ZH_CN.getPluginConfig());

        errorLogger = new ErrorLogger(folder, "PlayMoreSounds", getVersion().getVersion(),
                Collections.singleton("Epicnicity322"), "https://www.spigotmc.org/resources/37429/", null);
    }

    private final @NotNull Path jar = getFile().toPath();
    private final @NotNull AddonManager addonManager;

    public PlayMoreSounds()
    {
        instance = this;

        PluginDescriptionFile descriptionFile = getDescription();

        errorLogger = new ErrorLogger(folder, descriptionFile.getName(), version.toString(), descriptionFile.getAuthors(),
                descriptionFile.getWebsite(), getLogger());

        HashSet<String> pluginNames = new HashSet<>();

        for (Plugin plugin : Bukkit.getPluginManager().getPlugins())
            pluginNames.add(plugin.getName());

        addonManager = new AddonManager(this, pluginNames);

        try {
            addonManager.registerAddons();

            AddonEventManager.registerLoadUnloadEvent(new AddonLoadUnloadEvent()
            {
                private final @NotNull HashSet<String> startedAddons = new HashSet<>();

                @Override
                public void onLoadUnload(@NotNull PMSAddon addon)
                {
                    if (addon.isLoaded()) {
                        startedAddons.add(addon.getDescription().getName());

                        for (PMSAddon toStart : addonManager.getAddons())
                            if (addon.getDescription().getStartTime() == StartTime.HOOK_ADDONS)
                                if (startedAddons.containsAll(toStart.getDescription().getHookAddons()))
                                    addonManager.startAddon(toStart);
                    } else
                        for (PMSAddon toStop : addonManager.getAddons())
                            if (addon.getDescription().getStartTime() == StartTime.HOOK_ADDONS)
                                if (toStop.getDescription().getHookAddons().contains(addon.getDescription().getName()))
                                    addonManager.stopAddon(toStop);
                }
            });
        } catch (IllegalStateException ignored) {
            // Only thrown if addons were registered before.
        } catch (IOException ex) {
            logger.log("&cFailed to register addons.");
            errorLogger.report(ex, "Addon registration error:");
        }

        new Thread(() -> {
            for (Runnable runnable : onInstanceRunnables)
                try {
                    runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
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
    public @NotNull ConsoleLogger<?, ?> getCoreLogger()
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
        boolean success = true;

        try {
            HashSet<String> enabledPlugins = new HashSet<>();

            for (Plugin plugin : Bukkit.getPluginManager().getPlugins())
                if (plugin.isEnabled())
                    enabledPlugins.add(plugin.getName());

            Bukkit.getPluginManager().registerEvents(new Listener()
            {
                @EventHandler
                public void onPluginEnable(PluginEnableEvent event)
                {
                    Plugin plugin = event.getPlugin();

                    enabledPlugins.add(plugin.getName());

                    for (PMSAddon addon : addonManager.getAddons()) {
                        if (addon.getDescription().getStartTime() == StartTime.HOOK_PLUGINS)
                            if (enabledPlugins.containsAll(addon.getDescription().getHookPlugins()))
                                addonManager.startAddon(addon);
                    }
                }
            }, this);

            Bukkit.getPluginManager().registerEvents(new Listener()
            {
                @EventHandler
                public void onPluginDisable(PluginDisableEvent event)
                {
                    for (PMSAddon addon : addonManager.getAddons())
                        if (addon.getDescription().getStartTime() == StartTime.HOOK_PLUGINS)
                            if (addon.getDescription().getHookPlugins().contains(event.getPlugin().getName()))
                                addonManager.stopAddon(addon);
                }
            }, this);

            addonManager.startAddons(StartTime.BEFORE_CONFIGURATION);

            Configurations.getConfigLoader().loadConfigurations();
            logger.log("&6-> &eConfigurations loaded.");

            addonManager.startAddons(StartTime.BEFORE_EVENTS);

            // Registering all listeners:

            // Registering region wand tool listener.
            pm.registerEvents(new OnPlayerInteract(), this);
            // Registering region enter event caller.
            pm.registerEvents(new OnPlayerJoin(this), this);
            // Registering region enter and leave event caller.
            pm.registerEvents(new OnPlayerMove(), this);
            // Registering region leave event caller.
            pm.registerEvents(new OnPlayerQuit(), this);
            // Registering region enter and leave event caller.
            pm.registerEvents(new OnPlayerTeleport(), this);
            //TODO: Design better way to register OnRegionEnterLeave listener
            pm.registerEvents(new OnRegionEnterLeave(this), this);
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
            logger.log("&6============================================");

            if (success) {
                logger.log("&aPlayMoreSounds has been enabled");
                logger.log("&aVersion " + ReflectionUtil.getNmsVersion() + " detected");
                logger.log("&6============================================");
                addonManager.startAddons(StartTime.END);

                Bukkit.getScheduler().runTaskLater(this, () -> {
                    addonManager.startAddons(StartTime.SERVER_LOAD_COMPLETE);

                    // Starting all not started addons.
                    for (PMSAddon addon : addonManager.getAddons())
                        // Making sure to not start another thread unnecessarily
                        if (!addon.hasStarted())
                            addonManager.startAddon(addon);
                }, 1);

                new Thread(() -> {
                    for (Runnable runnable : onEnableRunnables)
                        try {
                            runnable.run();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }).start();
            } else {
                logger.log("&cSomething went wrong while loading PMS");
                logger.log("&cMake sure you read messages before reporting");
                logger.log("&6============================================");
                logger.log("&4ERROR.LOG generated, please check.");
                logger.log("Plugin disabled.", Level.SEVERE);
                Bukkit.getPluginManager().disablePlugin(this);
            }
        }
    }

    @Override
    public void onDisable()
    {
        if (addonManager != null)
            addonManager.stopAddons();

        new Thread(() -> {
            for (Runnable runnable : onDisableRunnables)
                try {
                    runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }).start();
    }
}