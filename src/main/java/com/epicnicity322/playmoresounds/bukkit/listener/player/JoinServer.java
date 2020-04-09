package com.epicnicity322.playmoresounds.bukkit.listener.player;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.region.RegionManager;
import com.epicnicity322.playmoresounds.bukkit.region.SoundRegion;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionEnterEvent;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import com.epicnicity322.playmoresounds.bukkit.util.UpdateManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

public class JoinServer implements Listener
{
    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        Player p = e.getPlayer();

        try {
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if (p.isOnline()) {
                        if (p.hasPlayedBefore()) {
                            ConfigurationSection section = PMSHelper.getConfig("sounds").getConfigurationSection(
                                    "Join Server");

                            new RichSound(section).play(e.getPlayer());
                        } else {
                            ConfigurationSection section = PMSHelper.getConfig("sounds").getConfigurationSection(
                                    "First Join");

                            new RichSound(section).play(e.getPlayer());
                        }
                    }
                }
            }.runTaskLater(PlayMoreSounds.getPlugin(), 1);
        } catch (Exception ignored) {
        }

        if (UpdateManager.AVAILABLE) {
            if (p.hasPermission("playmoresounds.update.joinmessage")) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&a* PlayMoreSounds has a new update available! *" +
                                "\n&aLink >&7 https://www.spigotmc.org/resources/37429/"));
            }
        }

        if (p.getName().equalsIgnoreCase("console")) {
            for (Player operator : Bukkit.getOnlinePlayers()) {
                if (operator.isOp()) {
                    PlayMoreSounds.MESSAGE_SENDER.send(operator, true,
                            PlayMoreSounds.MESSAGE_SENDER.get("General.Console Joined"));
                }
            }

            PlayMoreSounds.LOGGER.log(PlayMoreSounds.MESSAGE_SENDER.getColored("General.Console Joined"),
                    Level.WARNING);
        }

        if (PMSHelper.getConfig("config").getBoolean("Enable Sounds After Re-Login")) {
            PlayMoreSounds.IGNORED_PLAYERS.remove(p.getName());
        }

        try {
            SoundRegion region = RegionManager.getRegion(p.getLocation());

            if (region != null) {
                RegionEnterEvent event = new RegionEnterEvent(region, p.getLocation(), p.getLocation(), p);

                Bukkit.getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    new BukkitRunnable()
                    {
                        public void run()
                        {
                            p.kickPlayer(PlayMoreSounds.MESSAGE_SENDER.getColored("Region.Entry Not Allowed"));

                            for (Player op : Bukkit.getOnlinePlayers()) {
                                if (op.isOp()) {
                                    PlayMoreSounds.MESSAGE_SENDER.send(op, true,
                                            PlayMoreSounds.MESSAGE_SENDER.get("Region.Player Cant Enter").replace(
                                                    "<player>", p.getName()));
                                }
                            }

                            PlayMoreSounds.MESSAGE_SENDER.send(Bukkit.getConsoleSender(), true, PlayMoreSounds
                                    .MESSAGE_SENDER.get("Region.Player Cant Enter").replace("<player>", p
                                            .getName()));
                        }
                    }.runTaskLater(PlayMoreSounds.getPlugin(), 60);
                }
            }
        } catch (Exception ignored) {
        }

        try {
            if (PMSHelper.getConfig("config").getBoolean("Resource Packs.Request")) {
                new BukkitRunnable()
                {
                    public void run()
                    {
                        PlayMoreSounds.MESSAGE_SENDER.send(p, true, PlayMoreSounds.MESSAGE_SENDER.get(
                                "Resource Packs.Request Message"));
                        p.setResourcePack(PMSHelper.getConfig("config").getString("Resource Packs.URL"));
                    }
                }.runTaskLater(PlayMoreSounds.getPlugin(), 20);
            }
        } catch (Exception ex) {
            PlayMoreSounds.MESSAGE_SENDER.send(Bukkit.getConsoleSender(), true, PlayMoreSounds.MESSAGE_SENDER
                    .get("Resource Packs.Error").replace("<player>", p.getName()));
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onStatus(PlayerResourcePackStatusEvent e)
    {
        if (PMSHelper.getConfig("config").getBoolean("Resource Packs.Request")) {
            if (PMSHelper.getConfig("config").getBoolean("Resource Packs.Force.Enabled")) {
                if (e.getStatus().equals(PlayerResourcePackStatusEvent.Status.DECLINED) | e.getStatus().equals(
                        PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD)) {
                    if (!PMSHelper.getConfig("config").getBoolean(
                            "Resource Packs.Force.Even If Download Fail")) {
                        if (e.getStatus().equals(PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD)) {
                            return;
                        }
                    }

                    new BukkitRunnable()
                    {
                        public void run()
                        {
                            e.getPlayer().kickPlayer(PlayMoreSounds.MESSAGE_SENDER.getColored(
                                    "ResourcePacks.KickMessage"));
                        }
                    }.runTaskLater(PlayMoreSounds.getPlugin(), 20);
                }
            }
        }
    }
}
