package com.epicnicity322.playmoresounds.bukkit.listener.player;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.listener.region.RegionEnter;
import com.epicnicity322.playmoresounds.bukkit.region.SoundRegion;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionEnterEvent;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionLeaveEvent;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundType;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

public class PlayerMove implements Listener
{
    public static HashMap<String, BukkitRunnable> SOUNDS_IN_LOOP = new HashMap<>();
    public static HashMap<String, HashSet<String>> STOP_ON_EXIT = new HashMap<>();

    @EventHandler
    public void onMove(PlayerMoveEvent e)
    {
        Location from = e.getFrom();
        Location to = e.getTo();

        if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ()) {
            // PerBiomeSounds code:

            if (!PMSHelper.getConfig("biomes").getKeys(false).isEmpty()) {
                String bfrom = from.getBlock().getBiome().toString();
                String bto = to.getBlock().getBiome().toString();

                if (!bfrom.equals(bto)) {
                    Player player = e.getPlayer();
                    String worldTo = to.getWorld().getName();
                    String worldFrom = from.getWorld().getName();

                    for (String s : new HashSet<>(STOP_ON_EXIT.keySet())) {
                        if (s.startsWith(player.getName())) {
                            long delay = Long.parseLong(s.substring(s.lastIndexOf(";") + 1));

                            PMSHelper.stopSound(player, STOP_ON_EXIT.get(s), delay);
                            STOP_ON_EXIT.remove(s);
                        }
                    }

                    String keyFrom = worldFrom + ";" + bfrom + ";" + player.getName();

                    if (RegionEnter.SOUNDS_IN_LOOP.containsKey(keyFrom)) {
                        RegionEnter.SOUNDS_IN_LOOP.get(keyFrom).cancel();
                        RegionEnter.SOUNDS_IN_LOOP.remove(keyFrom);
                    }

                    if (PMSHelper.getConfig("biomes").contains(worldFrom + "." + bfrom + ".Leave")) {
                        ConfigurationSection section = PMSHelper.getConfig("biomes").getConfigurationSection(
                                worldFrom + "." + bfrom + ".Leave");

                        if (section.getBoolean("Enabled")) {
                            if (!e.isCancelled() || !section.getBoolean("Cancellable")) {
                                new RichSound(section).play(player);
                            }
                        }
                    }

                    boolean playEnter = true;

                    if (PMSHelper.getConfig("biomes").contains(worldTo + "." + bto + ".Loop")) {
                        ConfigurationSection section = PMSHelper.getConfig("biomes").getConfigurationSection(
                                worldTo + "." + bto + ".Loop");

                        if (section.getBoolean("Enabled")) {
                            if (!e.isCancelled() || !section.getBoolean("Cancellable")) {
                                if (section.contains("Sounds")) {
                                    String key = worldTo + ";" + bto + ";" + player.getName();

                                    if (SOUNDS_IN_LOOP.containsKey(key)) {
                                        SOUNDS_IN_LOOP.get(key).cancel();
                                    }

                                    SOUNDS_IN_LOOP.put(key, new BukkitRunnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            ConfigurationSection loopSection = PMSHelper.getConfig("biomes")
                                                    .getConfigurationSection(worldTo + "." + bto + ".Loop");
                                            String currentBiome = player.getLocation().getBlock().getBiome().toString();

                                            if (!loopSection.getBoolean("Enabled") || !currentBiome.equals(bto) ||
                                                    !player.isOnline()) {
                                                cancel();
                                                SOUNDS_IN_LOOP.remove(key);
                                                return;
                                            }

                                            new RichSound(loopSection).play(player);
                                        }
                                    });

                                    SOUNDS_IN_LOOP.get(key).runTaskTimer(PlayMoreSounds.getPlugin(), section.getLong("Delay"),
                                            section.getLong("Period"));

                                    if (section.getBoolean("Stop On Exit.Enabled")) {
                                        HashSet<String> sounds = STOP_ON_EXIT.getOrDefault(player.getName() +
                                                ";" + section.getLong("Stop On Exit.Delay"), new HashSet<>());

                                        for (String s : section.getConfigurationSection("Sounds").getKeys(false)) {
                                            String sound = section.getString("Sounds." + s + ".Sound");

                                            sounds.add(PlayMoreSounds.SOUND_LIST.contains(sound) ? SoundType.valueOf(sound)
                                                    .getSoundOnVersion() : sound);
                                        }

                                        STOP_ON_EXIT.put(player.getName() + ";" + section.getLong("Stop On Exit.Delay"),
                                                sounds);
                                    }
                                }

                                playEnter = !section.getBoolean("Stop Enter Sound");
                            }
                        }
                    }

                    if (playEnter) {
                        if (PMSHelper.getConfig("biomes").contains(worldTo + "." + bto + ".Enter")) {
                            ConfigurationSection section = PMSHelper.getConfig("regions").getConfigurationSection(
                                    worldTo + "." + bto + ".Enter");

                            if (section.getBoolean("Enabled")) {
                                if (!e.isCancelled() || !section.getBoolean("Cancellable")) {
                                    if (section.contains("Sounds")) {
                                        new RichSound(section).play(player);

                                        if (section.getBoolean("Stop On Exit.Enabled")) {
                                            HashSet<String> sounds = STOP_ON_EXIT.getOrDefault(player.getName() + ";" +
                                                    section.getLong("Stop On Exit.Delay"), new HashSet<>());

                                            for (String s : section.getConfigurationSection("Sounds").getKeys(false)) {
                                                String sound = section.getString("Sounds." + s + ".Sound");

                                                sounds.add(PlayMoreSounds.SOUND_LIST.contains(sound) ? SoundType.valueOf(sound)
                                                        .getSoundOnVersion() : sound);
                                            }

                                            STOP_ON_EXIT.put(player.getName() + ";" + section.getLong("Stop On Exit.Delay"),
                                                    sounds);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Region event trigger code:

            try {
                File folder = new File(PlayMoreSounds.getPlugin().getDataFolder(), "Regions");

                if (folder.exists()) {
                    for (File file : folder.listFiles()) {
                        if (file.getName().endsWith(".yml")) {
                            YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);

                            Location P1 = new Location(Bukkit.getWorld(conf.getString("World")),
                                    conf.getDouble("Locations.P1.X"), conf.getDouble("Locations.P1.Y"),
                                    conf.getDouble("Locations.P1.Z"));
                            Location P2 = new Location(Bukkit.getWorld(conf.getString("World")),
                                    conf.getDouble("Locations.P2.X"), conf.getDouble("Locations.P2.Y"),
                                    conf.getDouble("Locations.P2.Z"));

                            int x1 = Math.min(P1.getBlockX(), P2.getBlockX());
                            int y1 = Math.min(P1.getBlockY(), P2.getBlockY());
                            int z1 = Math.min(P1.getBlockZ(), P2.getBlockZ());
                            int x2 = Math.max(P1.getBlockX(), P2.getBlockX());
                            int y2 = Math.max(P1.getBlockY(), P2.getBlockY());
                            int z2 = Math.max(P1.getBlockZ(), P2.getBlockZ());

                            boolean isInFrom = from.getBlockX() >= x1 && from.getBlockX() <= x2 && from.getBlockY() >= y1
                                    && from.getBlockY() <= y2 && from.getBlockZ() >= z1 && from.getBlockZ() <= z2;
                            boolean isInTo = to.getBlockX() >= x1 && to.getBlockX() <= x2 && to.getBlockY() >= y1
                                    && to.getBlockY() <= y2 && to.getBlockZ() >= z1 && to.getBlockZ() <= z2;

                            if (!isInFrom & isInTo) {
                                RegionEnterEvent event = new RegionEnterEvent(
                                        new SoundRegion(conf, file.toPath()), from, to, e.getPlayer());

                                Bukkit.getPluginManager().callEvent(event);

                                if (event.isCancelled()) {
                                    e.setCancelled(true);
                                }
                            } else if (isInFrom & !isInTo) {
                                RegionLeaveEvent event = new RegionLeaveEvent(
                                        new SoundRegion(conf, file.toPath()), from, to, e.getPlayer());

                                Bukkit.getPluginManager().callEvent(event);

                                if (event.isCancelled()) {
                                    e.setCancelled(true);
                                }
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }
}
