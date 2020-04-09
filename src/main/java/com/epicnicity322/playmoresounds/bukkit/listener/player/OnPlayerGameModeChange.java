package com.epicnicity322.playmoresounds.bukkit.listener.player;

import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

public class OnPlayerGameModeChange implements Listener
{
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event)
    {
        String gameMode = event.getNewGameMode().toString();
        Player player = event.getPlayer();

        if (PMSHelper.getConfig("gamemodes").contains(gameMode)) {
            ConfigurationSection section = PMSHelper.getConfig("gamemodes").getConfigurationSection(gameMode);
            RichSound sound = new RichSound(section);

            if (sound.isEnabled()) {
                if (!event.isCancelled() || !sound.isCancellable()) {
                    sound.play(player);

                    // Stops the sound below from playing.
                    if (section.getBoolean("Stop Other Sounds"))
                        return;
                }
            }
        }

        ConfigurationSection section = PMSHelper.getConfig("sounds").getConfigurationSection("Game Mode Change");
        RichSound sound = new RichSound(section);

        if (!event.isCancelled() || !sound.isCancellable()) {
            sound.play(event.getPlayer());
        }
    }
}
