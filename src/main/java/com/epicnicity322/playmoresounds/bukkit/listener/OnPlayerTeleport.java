package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

public final class OnPlayerTeleport implements Listener
{
    private final @NotNull PlayMoreSounds main;
    private final @NotNull BukkitScheduler scheduler;

    public OnPlayerTeleport(@NotNull PlayMoreSounds main)
    {
        this.main = main;
        scheduler = Bukkit.getScheduler();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event)
    {
        Player player = event.getPlayer();

        if (!event.isCancelled())
            OnPlayerMove.callRegionEnterLeaveEvents(event, player, event.getFrom(), event.getTo());

        if (event.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND) {
            ConfigurationSection section = Configurations.SOUNDS.getPluginConfig().getConfiguration()
                    .getConfigurationSection("Teleport");

            if (section != null) {
                RichSound sound = new RichSound(section);

                if (sound.isEnabled() && (!event.isCancelled() || !sound.isCancellable()))
                    scheduler.runTaskLater(main, () -> sound.play(event.getPlayer()), 1);
            }
        }
    }
}
