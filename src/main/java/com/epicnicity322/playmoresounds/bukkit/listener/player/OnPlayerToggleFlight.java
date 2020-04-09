package com.epicnicity322.playmoresounds.bukkit.listener.player;

import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class OnPlayerToggleFlight implements Listener
{
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event)
    {
        Player player = event.getPlayer();

        if (player.isFlying()) {
            ConfigurationSection section = PMSHelper.getConfig("sounds").getConfigurationSection("Stop Flying");
            RichSound sound = new RichSound(section);

            if (!event.isCancelled() || !sound.isCancellable()) {
                sound.play(player);
            }
        } else {
            ConfigurationSection section = PMSHelper.getConfig("sounds").getConfigurationSection("Start Flying");
            RichSound sound = new RichSound(section);

            if (!event.isCancelled() || !sound.isCancellable()) {
                sound.play(player);
            }
        }
    }
}
