package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;

public class OnPlayerLevelChange implements Listener
{
    @EventHandler
    public void onPlayerLevelChange(PlayerLevelChangeEvent event)
    {
        ConfigurationSection section = PMSHelper.getConfig("sounds").getConfigurationSection("Change Level");

        new RichSound(section).play(event.getPlayer());
    }
}
