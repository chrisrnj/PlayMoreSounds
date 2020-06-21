package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.epicpluginlib.bukkit.logger.Logger;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.region.RegionManager;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionEnterEvent;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundManager;
import com.epicnicity322.playmoresounds.bukkit.util.UpdateManager;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public final class OnPlayerJoin implements Listener
{
    private static final @NotNull MessageSender lang = PlayMoreSounds.getMessageSender();
    private static final @NotNull Logger logger = PlayMoreSounds.getPMSLogger();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        Location location = player.getLocation();

        // Playing join sound
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (player.isOnline()) {
                    Configuration sounds = Configurations.SOUNDS.getPluginConfig().getConfiguration();
                    ConfigurationSection section;

                    if (player.hasPlayedBefore())
                        section = sounds.getConfigurationSection("Join Server");
                    else
                        section = sounds.getConfigurationSection("First Join");

                    if (section != null)
                        new RichSound(section).play(player);
                }
            }
        }.runTaskLater(PlayMoreSounds.getInstance(), 1);

        // Send update available message.
        if (UpdateManager.isUpdateAvailable())
            if (player.hasPermission("playmoresounds.update.joinmessage"))
                lang.send(player, "&a* PlayMoreSounds has a new update available! *" +
                        "\n&aLink >&7 https://www.spigotmc.org/resources/37429/");

        Configuration config = Configurations.CONFIG.getPluginConfig().getConfiguration();

        // Enabling sounds on login.
        if (config.getBoolean("Enable Sounds After Re-Login").orElse(true))
            SoundManager.getIgnoredPlayers().remove(player.getUniqueId());

        // Calling region enter events.
        RegionManager.getAllRegions().stream().filter(region -> region.isInside(location)).forEach(region -> {
            RegionEnterEvent regionEnterEvent = new RegionEnterEvent(region, player, location, location);
            Bukkit.getPluginManager().callEvent(regionEnterEvent);
        });

        // Setting the player's resource pack.
        try {
            if (config.getBoolean("Resource Packs.Request").orElse(false))
                new BukkitRunnable()
                {
                    public void run()
                    {
                        lang.send(player, lang.get("Resource Packs.Request Message"));
                        config.getString("Resource Packs.URL").ifPresent(player::setResourcePack);
                    }
                }.runTaskLater(PlayMoreSounds.getInstance(), 20);
        } catch (Exception ex) {
            logger.log(lang.get("Resource Packs.Error").replace("<player>", player.getName()));
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerResourcePackStatus(PlayerResourcePackStatusEvent event)
    {
        Configuration config = Configurations.CONFIG.getPluginConfig().getConfiguration();
        PlayerResourcePackStatusEvent.Status status = event.getStatus();

        if (config.getBoolean("Resource Packs.Request").orElse(false) &&
                config.getBoolean("Resource Packs.Force.Enabled").orElse(false) &&
                status == PlayerResourcePackStatusEvent.Status.DECLINED ||
                status == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
            if (!config.getBoolean("Resource Packs.Force.Even If Download Fail").orElse(false))
                if (status == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD)
                    return;

            new BukkitRunnable()
            {
                public void run()
                {
                    event.getPlayer().kickPlayer(lang.getColored("Resource Packs.Kick Message"));
                }
            }.runTaskLater(PlayMoreSounds.getInstance(), 20);
        }
    }
}
