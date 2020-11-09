/*
 * Copyright (c) 2020 Christiano Rangel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.epicpluginlib.core.config.PluginConfig;
import com.epicnicity322.playmoresounds.bukkit.region.RegionManager;
import com.epicnicity322.playmoresounds.bukkit.region.SoundRegion;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionEnterEvent;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionLeaveEvent;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundManager;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundType;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public final class OnPlayerMove implements Listener
{
    private static final @NotNull PluginConfig biomes = Configurations.BIOMES.getPluginConfig();
    private static final @NotNull HashMap<UUID, BukkitRunnable> biomesInLoop = new HashMap<>();
    private static final @NotNull HashMap<String, HashSet<String>> soundsToStop = new HashMap<>();

    protected static void callRegionEnterLeaveEvents(Cancellable event, Player player, Location from, Location to)
    {
        for (SoundRegion region : RegionManager.getAllRegions()) {
            boolean isInFrom = region.isInside(from);
            boolean isInTo = region.isInside(to);

            if (isInFrom && !isInTo) {
                RegionLeaveEvent regionLeaveEvent = new RegionLeaveEvent(region, player, from, to);
                Bukkit.getPluginManager().callEvent(regionLeaveEvent);

                if (regionLeaveEvent.isCancelled())
                    event.setCancelled(true);
            } else if (!isInFrom && isInTo) {
                RegionEnterEvent regionEnterEvent = new RegionEnterEvent(region, player, from, to);
                Bukkit.getPluginManager().callEvent(regionEnterEvent);

                if (regionEnterEvent.isCancelled())
                    event.setCancelled(true);
            }
        }
    }

    protected static void checkBiomeEnterLeaveSounds(Cancellable event, Player player, Location from, Location to)
    {
        // Playing sounds for biomes.yml.
        Configuration biomesConfiguration = biomes.getConfiguration();

        if (biomesConfiguration.contains(from.getWorld().getName()) || !biomesInLoop.isEmpty()) {
            Biome fromBiome = from.getBlock().getBiome();
            Biome toBiome = to.getBlock().getBiome();

            if (fromBiome != toBiome) {
                soundsToStop.entrySet().removeIf(entry -> {
                    String key = entry.getKey();

                    if (key.startsWith(player.getUniqueId().toString())) {
                        long delay = Long.parseLong(key.substring(key.indexOf(";") + 1));

                        SoundManager.stopSounds(player, entry.getValue(), delay);
                        return true;
                    }

                    return false;
                });

                UUID key = player.getUniqueId();

                if (biomesInLoop.containsKey(key)) {
                    biomesInLoop.get(key).cancel();
                    biomesInLoop.remove(key);
                }

                ConfigurationSection loop = biomesConfiguration.getConfigurationSection(to.getWorld().getName() + '.' + toBiome.name() + ".Loop");
                ConfigurationSection leave = biomesConfiguration.getConfigurationSection(from.getWorld().getName() + '.' + fromBiome.name() + ".Leave");
                boolean playEnterSound = true;

                if (loop != null) {
                    RichSound loopSound = new RichSound(loop);

                    if (loopSound.isEnabled() && (!event.isCancelled() || !loopSound.isCancellable())) {
                        long delay = loop.getNumber("Delay").orElse(0).longValue();
                        long period = loop.getNumber("Period").orElse(0).longValue();

                        biomesInLoop.put(key, loopSound.playInLoop(player, player::getLocation, delay, period, () -> {
                            Configuration updatedBiomes = biomes.getConfiguration();

                            return !updatedBiomes.getBoolean(loop.getPath() + ".Enabled").orElse(false)
                                    || !player.isOnline() || !player.getWorld().equals(to.getWorld())
                                    || player.getLocation().getBlock().getBiome() != toBiome;
                        }));

                        stopOnExit(player, loop);

                        if (loop.getBoolean("Prevent Enter Sound").orElse(false))
                            playEnterSound = false;
                    }
                }

                if (playEnterSound) {
                    ConfigurationSection enter = biomesConfiguration.getConfigurationSection(to.getWorld().getName() + '.' + toBiome.name() + ".Enter");

                    if (enter != null) {
                        RichSound enterSound = new RichSound(enter);

                        if (!event.isCancelled() || !enterSound.isCancellable()) {
                            enterSound.play(player);

                            if (enterSound.isEnabled())
                                stopOnExit(player, enter);
                        }
                    }
                }

                if (leave != null) {
                    RichSound leaveSound = new RichSound(leave);

                    if (!event.isCancelled() || !leaveSound.isCancellable())
                        leaveSound.play(player);
                }
            }
        }
    }

    private static void stopOnExit(Player player, ConfigurationSection section)
    {
        if (section.getBoolean("Stop On Exit.Enabled").orElse(false)) {
            String key = player.getUniqueId() + ";" + section.getNumber("Stop On Exit.Delay").orElse(0);
            HashSet<String> sounds = soundsToStop.getOrDefault(key, new HashSet<>());
            ConfigurationSection soundsSection = section.getConfigurationSection("Sounds");

            if (soundsSection != null)
                for (String sound : soundsSection.getNodes().keySet()) {
                    String soundToStop = soundsSection.getString(sound + ".Sound").orElse("");

                    sounds.add(SoundManager.getSoundList().contains(soundToStop) ? SoundType.valueOf(soundToStop).getSound().orElse("") : soundToStop);
                }

            soundsToStop.put(key, sounds);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event)
    {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
            Player player = event.getPlayer();

            // Calling region events.
            if (!event.isCancelled())
                callRegionEnterLeaveEvents(event, player, from, to);

            checkBiomeEnterLeaveSounds(event, player, from, to);
        }
    }
}
