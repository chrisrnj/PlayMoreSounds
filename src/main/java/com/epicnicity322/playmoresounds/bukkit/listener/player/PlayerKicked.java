package com.epicnicity322.playmoresounds.bukkit.listener.player;

import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;

public class PlayerKicked implements Listener
{

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKick(PlayerKickEvent e)
    {
        ConfigurationSection section = PMSHelper.getConfig("sounds").getConfigurationSection("Player Kicked");
        RichSound sound = new RichSound(section);

        if (!e.isCancelled() || !sound.isCancellable()) {
            sound.play(e.getPlayer());
        }
    }
}
