package com.epicnicity322.playmoresounds.bukkit.listener.player;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.region.SoundRegion;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionEnterEvent;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionLeaveEvent;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public class Teleport implements Listener
{
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeleport(final PlayerTeleportEvent e)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Location from = e.getFrom();
                Location to = e.getTo();

                //TODO: Optimize Region Events caller code.
                File folder = new File(PlayMoreSounds.getPlugin().getDataFolder(), "Regions");

                if (folder.exists()) {
                    for (File file : folder.listFiles()) {
                        if (file.getName().endsWith(".yml")) {
                            YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);

                            Location P1 = new Location(Bukkit.getWorld(conf.getString("World")), conf.getDouble("Locations.P1.X"), conf.getDouble("Locations.P1.Y"), conf.getDouble("Locations.P1.Z"));
                            Location P2 = new Location(Bukkit.getWorld(conf.getString("World")), conf.getDouble("Locations.P2.X"), conf.getDouble("Locations.P2.Y"), conf.getDouble("Locations.P2.Z"));

                            int x1 = Math.min(P1.getBlockX(), P2.getBlockX());
                            int y1 = Math.min(P1.getBlockY(), P2.getBlockY());
                            int z1 = Math.min(P1.getBlockZ(), P2.getBlockZ());
                            int x2 = Math.max(P1.getBlockX(), P2.getBlockX());
                            int y2 = Math.max(P1.getBlockY(), P2.getBlockY());
                            int z2 = Math.max(P1.getBlockZ(), P2.getBlockZ());

                            boolean isInFrom = from.getBlockX() >= x1 & from.getBlockX() <= x2 & from.getBlockY() >= y1 & from.getBlockY() <= y2
                                    & from.getBlockZ() >= z1 & from.getBlockZ() <= z2;
                            boolean isInTo = to.getBlockX() >= x1 & to.getBlockX() <= x2 & to.getBlockY() >= y1 & to.getBlockY() <= y2
                                    & to.getBlockZ() >= z1 & to.getBlockZ() <= z2;

                            if (!isInFrom & isInTo) {
                                RegionEnterEvent event = new RegionEnterEvent(new SoundRegion(conf, file.toPath()), from, to, e.getPlayer());

                                Bukkit.getPluginManager().callEvent(event);

                                if (event.isCancelled()) {
                                    e.setCancelled(true);
                                }
                            } else if (isInFrom & !isInTo) {
                                RegionLeaveEvent event = new RegionLeaveEvent(new SoundRegion(conf, file.toPath()), from, to, e.getPlayer());

                                Bukkit.getPluginManager().callEvent(event);

                                if (event.isCancelled()) {
                                    e.setCancelled(true);
                                }
                            }
                        }
                    }
                }

                if (e.getCause().equals(PlayerTeleportEvent.TeleportCause.COMMAND)) {
                    ConfigurationSection section = PMSHelper.getConfig("sounds").getConfigurationSection("Teleport");
                    RichSound sound = new RichSound(section);

                    if (!e.isCancelled() || !sound.isCancellable()) {
                        sound.play(e.getPlayer());
                    }
                }
            }
        }.runTaskLater(PlayMoreSounds.getPlugin(), 1);
    }
}
