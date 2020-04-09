package com.epicnicity322.playmoresounds.bukkit;

import com.epicnicity322.epicpluginlib.config.ConfigManager;
import com.epicnicity322.epicpluginlib.config.LoadOutput;
import com.epicnicity322.epicpluginlib.config.type.ConfigType;
import com.epicnicity322.epicpluginlib.lang.MessageSender;
import com.epicnicity322.epicpluginlib.logger.ErrorLogger;
import com.epicnicity322.epicpluginlib.logger.Logger;
import com.epicnicity322.playmoresounds.bukkit.command.CommandLoader;
import com.epicnicity322.playmoresounds.bukkit.inventory.RichSoundInventory;
import com.epicnicity322.playmoresounds.bukkit.inventory.SoundInventory;
import com.epicnicity322.playmoresounds.bukkit.listener.InventoryListener;
import com.epicnicity322.playmoresounds.bukkit.listener.OnPlayerArmorStandManipulate;
import com.epicnicity322.playmoresounds.bukkit.listener.OnPlayerQuit;
import com.epicnicity322.playmoresounds.bukkit.listener.player.JoinServer;
import com.epicnicity322.playmoresounds.bukkit.listener.player.PlayerHit;
import com.epicnicity322.playmoresounds.bukkit.listener.player.PlayerMove;
import com.epicnicity322.playmoresounds.bukkit.listener.player.Teleport;
import com.epicnicity322.playmoresounds.bukkit.listener.plugin.PluginEnableDisable;
import com.epicnicity322.playmoresounds.bukkit.listener.world.WorldTiming;
import com.epicnicity322.playmoresounds.bukkit.region.selector.AreaSelector;
import com.epicnicity322.playmoresounds.bukkit.region.selector.InappropriateEvents;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundType;
import com.epicnicity322.playmoresounds.bukkit.util.EventRegister;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import com.epicnicity322.playmoresounds.bukkit.util.Storage;
import com.epicnicity322.playmoresounds.bukkit.util.UpdateManager;
import com.epicnicity322.playmoresounds.core.addons.AddonManager;
import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import com.epicnicity322.playmoresounds.core.addons.StartTime;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.logging.Level;

public class PlayMoreSounds extends JavaPlugin implements com.epicnicity322.playmoresounds.core.PlayMoreSounds
{
    public static HashSet<String> IGNORED_PLAYERS = new HashSet<>();
    public static LinkedHashSet<String> SOUND_LIST = new LinkedHashSet<>();
    public static String[] LANG_VERSION = {"3.0.0#13"};
    public static String PMS_VERSION = "3.0.0";
    public static File JAR;
    public static String BUKKIT_VERSION = Bukkit.getBukkitVersion();
    public static Path DATA_FOLDER;
    public static ErrorLogger ERROR_LOGGER;
    public static Logger LOGGER;
    public static ConfigManager CONFIG;
    public static MessageSender MESSAGE_SENDER;
    public static boolean HAS_STOP_SOUND = !BUKKIT_VERSION.startsWith("1.7") & !BUKKIT_VERSION.startsWith("1.8") &
            !BUKKIT_VERSION.startsWith("1.9-") & !BUKKIT_VERSION.startsWith("1.9.1-") &
            !BUKKIT_VERSION.startsWith("1.9.2-");
    public static boolean HAS_PERSISTENT_DATA_CONTAINER = BUKKIT_VERSION.startsWith("1.14") ||
            BUKKIT_VERSION.startsWith("1.15") || BUKKIT_VERSION.startsWith("1.16");
    private static PluginManager pm = Bukkit.getPluginManager();
    private static HashSet<String> plugins = new HashSet<>();
    private static PlayMoreSounds PLUGIN;

    public static PlayMoreSounds getPlugin()
    {
        return PLUGIN;
    }

    @Override
    public void onEnable()
    {
        boolean success = true;

        try {
            PLUGIN = this;
            DATA_FOLDER = getDataFolder().toPath();
            ERROR_LOGGER = new ErrorLogger(getDescription(), DATA_FOLDER, null);
            LOGGER = new Logger("&6[&9PlayMoreSounds&6] ", null);
            JAR = getFile();

            if (!getDataFolder().exists()) {
                if (!getDataFolder().mkdir()) {
                    LOGGER.log("&cUnable to create PlayMoreSounds data folder.", Level.SEVERE);
                    success = false;
                    return;
                }
            }

            File soundsFolder = new File(getDataFolder(), "Sounds");

            if (!soundsFolder.exists()) {
                if (!soundsFolder.mkdir()) {
                    LOGGER.log("&eUnable to create Sounds folder in PlayMoreSounds data folder.", Level.WARNING);
                }
            }

            Storage.loadTypes();

            for (SoundType s : SoundType.values()) {
                if (s.getSoundOnVersion() != null) {
                    SOUND_LIST.add(s.name());
                }
            }

            LOGGER.log(Bukkit.getConsoleSender(), "&6-> &e&n" + SOUND_LIST.size() + "&e sounds loaded.");

            for (Plugin plugin : pm.getPlugins()) {
                plugins.add(plugin.getName());
            }

            getAddonManager().registerAddons();

            getAddonManager().loadAddons(StartTime.BEFORE_CONFIGURATION);

            CONFIG = new ConfigManager(this, Storage.getTypes());
            ERROR_LOGGER.setConfigManager(CONFIG);

            HashMap<ConfigType, LoadOutput> result = CONFIG.loadConfig();

            for (ConfigType type : result.keySet()) {
                LoadOutput output = result.get(type);

                switch (output.getResult()) {
                    case ERROR_EXTRACTION:
                        LOGGER.log("&cSomething went wrong while creating " + type.getName() + ".",
                                Level.SEVERE);
                        ERROR_LOGGER.report(output.getException(), "Config creation error (" + type.getName() +
                                "):");

                        if (type.equals(Storage.TYPES.get("config")) || type.equals(Storage.TYPES.get("sounds"))) {
                            success = false;
                            return;
                        }

                        break;
                    case ERROR_LOAD:
                        LOGGER.log("&cSomething went wrong while loading " + type.getName() + ".",
                                Level.SEVERE);
                        ERROR_LOGGER.report(output.getException(), "Config load error (" + type.getName() + "):");

                        if (type.equals(Storage.TYPES.get("config")) || type.equals(Storage.TYPES.get("sounds"))) {
                            success = false;
                            return;
                        }

                        break;
                    case RESTORED_OLD:
                        LOGGER.log("&cYour " + type.getName() +
                                " was outdated so it was automatically restored.", Level.INFO);
                }
            }

            CONFIG.loadLanguage(new HashSet<>(Arrays.asList(LANG_VERSION)), Storage.getHardCodedLang());
            LOGGER.setMainConfig(PMSHelper.getConfig("config"));

            MESSAGE_SENDER = new MessageSender(CONFIG);
            LOGGER.log("&6-> &eConfiguration loaded.", Level.INFO);

            getAddonManager().loadAddons(StartTime.BEFORE_EVENTS);

            pm.registerEvents(new InventoryListener(), this);
            pm.registerEvents(new OnPlayerArmorStandManipulate(), this);
            pm.registerEvents(new PlayerHit(), this);
            pm.registerEvents(new JoinServer(), this);
            pm.registerEvents(new PlayerMove(), this);
            pm.registerEvents(new Teleport(), this);
            pm.registerEvents(new OnPlayerQuit(), this);
            WorldTiming.time();

            LOGGER.log("&6-> &e&n" + PMSHelper.addZerosToLeft((EventRegister.registerPMSEvents() + 8),
                    2) + "&e events loaded.", Level.INFO);

            CommandLoader.loadCommands();

            pm.registerEvents(new AreaSelector(), this);
            pm.registerEvents(new InappropriateEvents(), this);
            pm.registerEvents(new PluginEnableDisable(), this);

            UpdateManager.loadUpdater();

            getAddonManager().loadAddons(StartTime.END);

            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    getAddonManager().loadAddons(StartTime.SERVER_LOAD_COMPLETE);
                }
            }.runTaskLater(this, 1);
        } catch (Exception e) {
            success = false;
            ERROR_LOGGER.report(e, "PMSLoadingError (Unknown):");
        } finally {
            try {
                getAddonManager().loadAddons(StartTime.END_PERSISTENT);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (success) {
                LOGGER.log("&6============================================", Level.INFO);
                LOGGER.log("&aPlayMoreSounds has been enabled", Level.INFO);
                LOGGER.log("&aVersion " + BUKKIT_VERSION + " detected", Level.INFO);
                LOGGER.log("&6============================================", Level.INFO);
            } else {
                LOGGER.log("&6============================================", Level.SEVERE);
                LOGGER.log("&cSomething went wrong while loading PMS", Level.SEVERE);
                LOGGER.log("&cPlease report this error to the developer", Level.SEVERE);
                LOGGER.log("&6============================================", Level.SEVERE);
                LOGGER.log("&4ERROR.LOG generated, please check.", Level.SEVERE);
                LOGGER.log("&4Plugin disabled.", Level.SEVERE);
                Bukkit.getPluginManager().disablePlugin(this);
            }
        }
    }

    @Override
    public void onDisable()
    {
        for (String playerName : SoundInventory.openInventories.keySet()) {
            Player player = Bukkit.getPlayer(playerName);

            player.closeInventory();
        }
        for (String playerName : RichSoundInventory.openInventories.keySet()) {
            Player player = Bukkit.getPlayer(playerName);

            player.closeInventory();
        }

        new Thread(() -> {
            LOGGER.log("&eDisabling addons...", Level.INFO);
            int amount = 0;

            for (PMSAddon addon : getAddonManager().getRegisteredAddons()) {
                try {
                    addon.onStop();
                    LOGGER.log("&4-&e The addon " + addon.toString() + "&e was stopped.", Level.INFO);
                } catch (Exception e) {
                    LOGGER.log("&cSomething went wrong while disabling the addon " + addon.toString(),
                            Level.SEVERE);
                    e.printStackTrace();
                }

                ++amount;
            }

            if (amount == 0) {
                LOGGER.log("&eNo addons found.", Level.INFO);
            } else {
                LOGGER.log("&e" + amount + " addons were disabled.", Level.INFO);
            }

            getAddonManager().unregisterAddons();
            System.gc();
        }, "PMSAddon Runner").start();
    }

    @Override
    public ErrorLogger getErrorLogger()
    {
        return ERROR_LOGGER;
    }

    @Override
    public Logger getPMSLogger()
    {
        return LOGGER;
    }

    @Override
    public Path getFolder()
    {
        return DATA_FOLDER;
    }

    @Override
    public AddonManager getAddonManager()
    {
        return new AddonManager(this, plugins);
    }
}
