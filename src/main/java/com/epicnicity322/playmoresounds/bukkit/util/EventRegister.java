package com.epicnicity322.playmoresounds.bukkit.util;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.listener.*;
import com.epicnicity322.playmoresounds.bukkit.listener.player.*;
import com.epicnicity322.playmoresounds.bukkit.listener.region.RegionEnter;
import com.epicnicity322.playmoresounds.bukkit.listener.region.RegionLeave;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class EventRegister
{
    /**
     * Shows the events that are enabled in the moment.
     */
    public static HashSet<String> REGISTERED_EVENTS = new HashSet<>();
    private static LinkedHashMap<String, Listener> eventListeners = new LinkedHashMap<>();
    /**
     * Shows all events related to classes.
     * Events that starts with ! are special events that contains 2 or more triggers.
     */
    public static final Map<String, Listener> EVENT_LISTENERS = Collections.unmodifiableMap(eventListeners);

    static {
        eventListeners.put("Bed Enter", new OnPlayerBedEnter());
        eventListeners.put("Bed Leave", new OnPlayerBedLeave());
        eventListeners.put("Change Held Item", new OnPlayerItemHeld());
        eventListeners.put("Change Level", new OnPlayerLevelChange());
        eventListeners.put("Crafting Extract", new OnCraftItem());
        eventListeners.put("Furnace Extract", new OnFurnaceExtract());
        eventListeners.put("!Game Mode Change", new OnPlayerGameModeChange());
        eventListeners.put("!Player Chat", new PlayerChat());
        eventListeners.put("!Send Command", new SendCommand());
        eventListeners.put("!Player Death", new PlayerDeath());
        eventListeners.put("Drop Item", new OnPlayerDropItem());
        eventListeners.put("!Flying", new OnPlayerToggleFlight());
        eventListeners.put("Player Kicked", new PlayerKicked());
        eventListeners.put("!Region Enter", new RegionEnter());
        eventListeners.put("!Region Leave", new RegionLeave());
    }

    /**
     * Registers all sound events.
     */
    public static int registerPMSEvents()
    {
        int amount = 0;

        for (String event : EVENT_LISTENERS.keySet()) {
            if (event.startsWith("!")) {
                switch (event) {
                    case "!Game Mode Change":
                        amount = amount + soundAndFileRegistration(event, "gamemodes", PMSHelper.getConfig("gamemodes"));

                        break;

                    case "!Player Chat":
                        amount = amount + soundAndFileRegistration(event, "chat", PMSHelper.getConfig("chat"));

                        break;

                    case "!Send Command":
                        amount = amount + soundAndFileRegistration(event, "commands", PMSHelper.getConfig("commands"));

                        break;

                    case "!Player Death":
                        amount = amount + soundAndFileRegistration(event, "deathtypes", PMSHelper.getConfig("deathtypes"));

                        break;

                    case "!Flying":
                        amount = amount + twoSoundsRegistration(event, "Start Flying", "Stop Flying");

                        break;

                    case "!Region Enter":
                    case "!Region Leave":
                        amount = amount + (soundAndFileRegistration(event, "regions", PMSHelper.getConfig("regions")) == 1 ? 2 : 0);

                        break;
                }
            } else {
                if (PMSHelper.getConfig("sounds").getBoolean(event + ".Enabled")) {
                    if (!REGISTERED_EVENTS.contains(event)) {
                        Bukkit.getPluginManager().registerEvents(EVENT_LISTENERS.get(event), PlayMoreSounds.getPlugin());
                        REGISTERED_EVENTS.add(event);
                        amount++;
                    }
                } else {
                    if (REGISTERED_EVENTS.contains(event)) {
                        HandlerList.unregisterAll(EVENT_LISTENERS.get(event));
                        REGISTERED_EVENTS.remove(event);
                    }
                }
            }
        }

        return amount;
    }

    /**
     * Checks if a sound event is enabled in a specific file and in the main sounds file.
     */
    private static int soundAndFileRegistration(String event, String filename, FileConfiguration fileConf)
    {
        boolean configCheck = true;

        File file = new File(PlayMoreSounds.getPlugin().getDataFolder(), "Sounds/" + filename + ".yml");

        if (file.exists()) {
            if (fileConf.getKeys(true).size() == 0) {
                configCheck = false;
            }
        } else {
            configCheck = false;
        }

        if (PMSHelper.getConfig("sounds").getBoolean(event.substring(1) + ".Enabled") | configCheck) {
            if (!REGISTERED_EVENTS.contains(event.substring(1))) {
                Bukkit.getPluginManager().registerEvents(EVENT_LISTENERS.get(event), PlayMoreSounds.getPlugin());
                REGISTERED_EVENTS.add(event.substring(1));

                return 1;
            }
        } else if (!PMSHelper.getConfig("sounds").getBoolean(event.substring(1) + ".Enabled") & !configCheck) {
            if (REGISTERED_EVENTS.contains(event.substring(1))) {
                HandlerList.unregisterAll(EVENT_LISTENERS.get(event));
                REGISTERED_EVENTS.remove(event.substring(1));
            }
        }

        return 0;
    }

    /**
     * Checks if two sounds are enabled in the main sounds file.
     */
    private static int twoSoundsRegistration(String event, String sound1, String sound2)
    {
        if (PMSHelper.getConfig("sounds").getConfigurationSection(sound1).getBoolean("Enabled") |
                PMSHelper.getConfig("sounds").getConfigurationSection(sound2).getBoolean("Enabled")) {
            if (!REGISTERED_EVENTS.contains(event)) {
                Bukkit.getPluginManager().registerEvents(EVENT_LISTENERS.get(event), PlayMoreSounds.getPlugin());
                REGISTERED_EVENTS.add(event);

                return 2;
            }
        } else if (!PMSHelper.getConfig("sounds").getConfigurationSection(sound1).getBoolean("Enabled") &&
                !PMSHelper.getConfig("sounds").getConfigurationSection(sound2).getBoolean("Enabled")) {
            if (REGISTERED_EVENTS.contains(event)) {
                HandlerList.unregisterAll(EVENT_LISTENERS.get(event));
                REGISTERED_EVENTS.remove(event);
            }
        }

        return 0;
    }
}
