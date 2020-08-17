package com.epicnicity322.playmoresounds.bukkit;

import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.epicpluginlib.bukkit.logger.Logger;
import com.epicnicity322.epicpluginlib.bukkit.reflection.ReflectionUtil;
import com.epicnicity322.epicpluginlib.core.EpicPluginLib;
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.core.logger.ErrorLogger;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.playmoresounds.bukkit.command.CommandLoader;
import com.epicnicity322.playmoresounds.bukkit.listener.*;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundManager;
import com.epicnicity322.playmoresounds.bukkit.util.ListenerRegister;
import com.epicnicity322.playmoresounds.bukkit.util.UpdateManager;
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import com.epicnicity322.playmoresounds.core.addons.AddonDescription;
import com.epicnicity322.playmoresounds.core.addons.AddonManager;
import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import com.epicnicity322.playmoresounds.core.addons.StartTime;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.util.LoadableHashSet;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public final class PlayMoreSounds extends JavaPlugin implements com.epicnicity322.playmoresounds.core.PlayMoreSounds
{
    private static final @NotNull HashSet<Runnable> onDisableRunnables = new HashSet<>();
    private static final @NotNull HashSet<Runnable> onEnableRunnables = new HashSet<>();
    private static final @NotNull HashSet<Runnable> onInstanceRunnables = new HashSet<>();
    private static final @NotNull LoadableHashSet<String> serverPlugins = new LoadableHashSet<>();
    private static final @NotNull PluginManager pm = Bukkit.getPluginManager();
    private static final @NotNull Path folder = Paths.get("plugins").resolve("PlayMoreSounds");
    private static final @NotNull MessageSender messageSender = new MessageSender(Configurations.CONFIG.getPluginConfig(),
            Configurations.LANGUAGE_EN_US.getPluginConfig());
    private static final @NotNull HashSet<String> enabledPlugins = new HashSet<>();
    private static @NotNull Logger logger = new Logger("&6[&9PlayMoreSounds&6] ");
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

        // Checking if EpicPluginLib is on 1.6.1 by checking if version field exists because this field was added on 1.6.1.
        if (ReflectionUtil.getField(EpicPluginLib.class, "version") == null) {
            success = false;

            // There were no logger level on version 1.6, so I'm just using bukkit logger.
            addOnEnableRunnable(() -> Bukkit.getLogger().severe("You are running an old version of EpicPluginLib, make sure you are using the latest one."));
        } else {
            messageSender.addLanguage("EN_US", Configurations.LANGUAGE_EN_US.getPluginConfig());
            messageSender.addLanguage("ES_LA", Configurations.LANGUAGE_ES_LA.getPluginConfig());
            messageSender.addLanguage("PT_BR", Configurations.LANGUAGE_PT_BR.getPluginConfig());
            messageSender.addLanguage("ZH_CN", Configurations.LANGUAGE_ZH_CN.getPluginConfig());
        }
    }

    private final @NotNull Path jar = getFile().toPath();
    private final @NotNull AddonManager addonManager;
    private final @NotNull HashMap<PMSAddon, HashSet<String>> hookingPluginAddons = new HashMap<>();
    private final @NotNull Listener addonHookingPluginStarter = new Listener()
    {
        @EventHandler
        public void onPluginEnable(PluginEnableEvent event)
        {
            enabledPlugins.add(event.getPlugin().getName());

            HashSet<PMSAddon> toStart = new HashSet<>();

            for (Map.Entry<PMSAddon, HashSet<String>> addonHookingPlugin : hookingPluginAddons.entrySet()) {
                HashSet<String> hookingPlugins = addonHookingPlugin.getValue();

                if (!hookingPlugins.isEmpty() && enabledPlugins.containsAll(hookingPlugins)) {
                    // Adding addon to toStart set so addons can start all in one thread.
                    toStart.add(addonHookingPlugin.getKey());
                }
            }

            if (!toStart.isEmpty())
                new Thread(() -> {
                    for (PMSAddon addon : toStart)
                        addonManager.startAddon(addon);
                }, "PMSAddon Runner").start();
        }
    };

    public PlayMoreSounds()
    {
        instance = this;
        logger = new Logger(logger.getPrefix(), getLogger());

        PluginDescriptionFile descriptionFile = getDescription();

        errorLogger = new ErrorLogger(folder, descriptionFile.getName(), getVersion().getVersion(), descriptionFile.getAuthors(),
                descriptionFile.getWebsite(), getLogger());
        addonManager = new AddonManager(this, serverPlugins);

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

            for (Plugin plugin : pm.getPlugins())
                if (plugin.isEnabled())
                    enabledPlugins.add(plugin.getName());

            for (PMSAddon addon : addonManager.getAddons()) {
                AddonDescription description = addon.getDescription();

                if (description.getStartTime() == StartTime.HOOK_PLUGINS) {
                    HashSet<String> hookPlugins = new HashSet<>();

                    for (String plugin : description.getHookPlugins())
                        if (!enabledPlugins.contains(plugin) && serverPlugins.contains(plugin))
                            hookPlugins.add(plugin);

                    hookingPluginAddons.put(addon, hookPlugins);
                }
            }

            pm.registerEvents(addonHookingPluginStarter, this);

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
            pm.registerEvents(new OnPlayerTeleport(this), this);
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
            if (success) {
                logger.log("&6============================================");
                logger.log("&aPlayMoreSounds has been enabled");
                logger.log("&a" + SoundManager.getSoundTypes().size() + " sounds available on " + VersionUtils.getBukkitVersion());
                logger.log("&6============================================");
                //logger.log("&ePlayMoreSounds is collecting anonymous data using bStats. If you don't want to send data, edit bStats configuration.");

                addonManager.startAddons(StartTime.END);

                Bukkit.getScheduler().runTaskLater(this, () -> {
                    // Starting addons that hook to plugins that are not present in the server.
                    if (!hookingPluginAddons.isEmpty())
                        new Thread(() -> {
                            for (Map.Entry<PMSAddon, HashSet<String>> addonHookingPlugin : hookingPluginAddons.entrySet())
                                if (addonHookingPlugin.getValue().isEmpty())
                                    addonManager.startAddon(addonHookingPlugin.getKey());
                        }, "PMSAddon Runner").start();

                    addonManager.startAddons(StartTime.SERVER_LOAD_COMPLETE);
                }, 1);
            } else {
                logger.log("&6============================================");
                logger.log("&cSomething went wrong while loading PMS");
                logger.log("&cMake sure you read messages before reporting");
                logger.log("&6============================================");
                getLogger().severe("Plugin disabled.");
                Bukkit.getPluginManager().disablePlugin(this);
            }

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