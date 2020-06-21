package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.region.RegionManager;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionLeaveEvent;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class OnPlayerQuit implements Listener
{
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        Location location = player.getLocation();

        RegionManager.getAllRegions().stream().filter(region -> region.isInside(location)).forEach(region -> {
            RegionLeaveEvent regionLeaveEvent = new RegionLeaveEvent(region, player, location, location);
            Bukkit.getPluginManager().callEvent(regionLeaveEvent);
        });

        Configuration sounds = Configurations.SOUNDS.getPluginConfig().getConfiguration();
        ConfigurationSection section;

        if (player.isBanned())
            section = sounds.getConfigurationSection("Player Ban");
        else
            section = sounds.getConfigurationSection("Leave Server");

        if (section != null)
            new RichSound(section).play(player);
    }
}
