package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.region.RegionManager;
import com.epicnicity322.playmoresounds.bukkit.region.SoundRegion;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionLeaveEvent;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class OnPlayerQuit implements Listener
{
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();

        try {
            Location location = player.getLocation();
            SoundRegion region = RegionManager.getRegion(location);

            if (region != null) {
                RegionLeaveEvent regionLeaveEvent = new RegionLeaveEvent(region, location, location, player);

                Bukkit.getPluginManager().callEvent(regionLeaveEvent);
            }
        } catch (Exception ignored) {
        }

        if (player.isBanned()) {
            ConfigurationSection section = PMSHelper.getConfig("sounds").getConfigurationSection(
                    "Player Ban");

            new RichSound(section).play(player);
        } else {
            ConfigurationSection section = PMSHelper.getConfig("sounds").getConfigurationSection(
                    "Leave Server");

            new RichSound(section).play(player);
        }
    }
}
