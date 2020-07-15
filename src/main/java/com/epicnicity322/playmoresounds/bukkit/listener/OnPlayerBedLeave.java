package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.jetbrains.annotations.NotNull;

public final class OnPlayerBedLeave extends PMSListener
{
    private final @NotNull PlayMoreSounds plugin;
    private RichSound bedLeave;
    private RichSound wakeUp;

    public OnPlayerBedLeave(@NotNull PlayMoreSounds plugin)
    {
        super(plugin);

        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName()
    {
        return "Bed Leave|Wake Up";
    }

    @Override
    public void load()
    {
        Configuration sounds = Configurations.SOUNDS.getPluginConfig().getConfiguration();
        ConfigurationSection leave = sounds.getConfigurationSection("Bed Leave");
        ConfigurationSection wake = sounds.getConfigurationSection("Wake Up");
        boolean leaveEnabled = leave == null ? false : leave.getBoolean("Enabled").orElse(false);
        boolean wakeEnabled = wake == null ? false : wake.getBoolean("Enabled").orElse(false);

        if (leaveEnabled || wakeEnabled) {
            if (leaveEnabled)
                bedLeave = new RichSound(leave);

            if (wakeEnabled)
                wakeUp = new RichSound(wake);

            if (!isLoaded()) {
                Bukkit.getPluginManager().registerEvents(this, plugin);
                setLoaded(true);
            }
        } else {
            if (isLoaded()) {
                HandlerList.unregisterAll(this);
                setLoaded(false);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerBedLeave(PlayerBedLeaveEvent event)
    {
        Player player = event.getPlayer();

        if (bedLeave != null)
            bedLeave.play(player);

        if (wakeUp != null) {
            long time = player.getWorld().getTime();

            if (time < 300)
                wakeUp.play(player);
        }
    }
}
