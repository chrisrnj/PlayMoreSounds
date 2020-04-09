package com.epicnicity322.playmoresounds.bukkit.listener.plugin;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.addons.AddonManager;
import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import com.epicnicity322.playmoresounds.core.addons.StartTime;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;

public class PluginEnableDisable implements Listener
{
    private static HashSet<String> enabledPlugins = new HashSet<>();

    public PluginEnableDisable()
    {
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin.isEnabled()) {
                enabledPlugins.add(plugin.getName());
            }
        }
    }

    @EventHandler
    public void onEnable(PluginEnableEvent e)
    {
        AddonManager manager = PlayMoreSounds.getPlugin().getAddonManager();

        enabledPlugins.add(e.getPlugin().getName());

        for (PMSAddon addon : manager.getRegisteredAddons()) {
            if (!addon.isLoaded() && addon.getDescription().getStartTime() == StartTime.HOOK_PLUGINS &&
                    enabledPlugins.containsAll(addon.getDescription().getHookPlugins())) {
                manager.loadAddon(addon);
            }
        }
    }

    @EventHandler
    public void onDisable(PluginDisableEvent e)
    {
        AddonManager manager = PlayMoreSounds.getPlugin().getAddonManager();

        enabledPlugins.remove(e.getPlugin().getName());

        for (PMSAddon addon : manager.getRegisteredAddons()) {
            if (addon.isLoaded() && addon.getDescription().getStartTime() == StartTime.HOOK_PLUGINS &&
                    !enabledPlugins.containsAll(addon.getDescription().getHookPlugins())) {
                manager.unloadAddon(addon);
                manager.unregisterAddon(addon);
            }
        }
    }
}
