package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class OnPlayerTeleport implements Listener
{
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event)
    {
        Player player = event.getPlayer();

        OnPlayerMove.callRegionEnterLeaveEvents(event, player, event.getFrom(), event.getTo());

        if (event.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND) {
            ConfigurationSection section = Configurations.SOUNDS.getPluginConfig().getConfiguration()
                    .getConfigurationSection("Teleport");

            if (section != null) {
                RichSound sound = new RichSound(section);

                if (!event.isCancelled() || !sound.isCancellable())
                    sound.play(event.getPlayer());
            }
        }
    }
}
