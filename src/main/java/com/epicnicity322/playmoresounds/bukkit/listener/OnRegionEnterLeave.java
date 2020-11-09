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

import com.epicnicity322.epicpluginlib.core.util.ObjectUtils;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public final class OnRegionEnterLeave extends PMSListener
{
    private final @NotNull PlayMoreSounds plugin;
    private final @NotNull HashMap<String, BukkitRunnable> regionsInLoop = new HashMap<>();
    private final @NotNull HashMap<String, HashSet<String>> soundsToStop = new HashMap<>();
    private @Nullable RichSound regionEnterSound = null;
    private @Nullable RichSound regionLeaveSound = null;

    public OnRegionEnterLeave(@NotNull PlayMoreSounds plugin)
    {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName()
    {
        return "Region Enter|Region Leave";
    }

    @Override
    public void load()
    {
        Configuration sounds = Configurations.SOUNDS.getPluginConfig().getConfiguration();
        Configuration regions = Configurations.REGIONS.getPluginConfig().getConfiguration();
        ConfigurationSection regionEnterSection = sounds.getConfigurationSection("Region Enter");
        ConfigurationSection regionLeaveSection = sounds.getConfigurationSection("Region Leave");
        ConfigurationSection defaultSection = ObjectUtils.getOrDefault(regionEnterSection, regionLeaveSection);
        ConfigurationSection regionsYAMLSection = regions.getConfigurationSection("PlayMoreSounds");
        boolean load = !soundsToStop.isEmpty();

        if (!load)
            if (defaultSection != null)
                load = defaultSection.getBoolean("Enabled").orElse(false);

        if (!load) {
            if (regionsYAMLSection != null) {
                for (Map.Entry<String, Object> section : regionsYAMLSection.getAbsoluteNodes().entrySet()) {
                    if (section.getKey().endsWith("Enabled")) {
                        if (section.getValue() == Boolean.TRUE) {
                            load = true;
                            break;
                        }
                    }
                }
            }
        }

        if (load) {
            if (regionEnterSection != null)
                regionEnterSound = new RichSound(regionEnterSection);
            if (regionLeaveSection != null)
                regionLeaveSound = new RichSound(regionLeaveSection);

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
    public void onRegionEnter(RegionEnterEvent event)
    {
        Player player = event.getPlayer();
        ConfigurationSection regions = Configurations.REGIONS.getPluginConfig().getConfiguration()
                .getConfigurationSection("PlayMoreSounds");
        SoundRegion region = event.getRegion();
        boolean defaultSound = true;

        String key = region.getId() + ";" + player.getUniqueId();

        if (regionsInLoop.containsKey(key)) {
            regionsInLoop.get(key).cancel();
            regionsInLoop.remove(key);
        }

        if (regions != null) {
            ConfigurationSection loop = regions.getConfigurationSection(region.getName() + ".Loop");
            boolean playEnterSound = true;

            if (loop != null) {
                RichSound loopSound = new RichSound(loop);

                if (loopSound.isEnabled() && (!event.isCancelled() || !loopSound.isCancellable())) {
                    long delay = loop.getNumber("Delay").orElse(0).longValue();
                    long period = loop.getNumber("Period").orElse(0).longValue();

                    regionsInLoop.put(key, loopSound.playInLoop(player, player::getLocation, delay, period, () -> {
                        Configuration updatedRegions = Configurations.REGIONS.getPluginConfig().getConfiguration();

                        return !updatedRegions.getBoolean("PlayMoreSounds." + region.getName() + ".Loop.Enabled").orElse(false)
                                || !RegionManager.getAllRegions().contains(region) || !player.isOnline() || !region.isInside(player.getLocation());
                    }));

                    stopOnExit(player, loop);

                    if (loop.getBoolean("Prevent Other Sounds.Default Sound").orElse(false))
                        defaultSound = false;
                    if (loop.getBoolean("Prevent Other Sounds.Enter Sound").orElse(false))
                        playEnterSound = false;
                }
            }

            if (playEnterSound) {
                ConfigurationSection enter = regions.getConfigurationSection(region.getName() + ".Enter");

                if (enter != null) {
                    RichSound enterSound = new RichSound(enter);

                    if (enterSound.isEnabled()) {
                        if (!event.isCancelled() || !enterSound.isCancellable()) {
                            enterSound.play(player);

                            stopOnExit(player, enter);

                            if (enter.getBoolean("Prevent Default Sound").orElse(false))
                                defaultSound = false;
                        }
                    }
                }
            }
        }

        if (defaultSound && regionEnterSound != null)
            if (!event.isCancelled() || !regionEnterSound.isCancellable()) {
                regionEnterSound.play(player);

                if (regionEnterSound.isEnabled())
                    stopOnExit(player, regionEnterSound.getSection());
            }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRegionLeave(RegionLeaveEvent event)
    {
        Player player = event.getPlayer();

        soundsToStop.entrySet().removeIf(entry -> {
            String key = entry.getKey();

            if (key.startsWith(player.getUniqueId().toString())) {
                long delay = Long.parseLong(key.substring(key.indexOf(";") + 1));

                SoundManager.stopSounds(player, entry.getValue(), delay);
                return true;
            }

            return false;
        });

        SoundRegion region = event.getRegion();
        String key = region.getId() + ";" + player.getUniqueId();

        if (regionsInLoop.containsKey(key)) {
            regionsInLoop.get(key).cancel();
            regionsInLoop.remove(key);
        }

        boolean defaultSound = true;

        ConfigurationSection leave = Configurations.REGIONS.getPluginConfig().getConfiguration().getConfigurationSection("PlayMoreSounds." + region.getName() + ".Leave");

        if (leave != null) {
            RichSound leaveSound = new RichSound(leave);

            if (leaveSound.isEnabled()) {
                if (!event.isCancelled() || !leaveSound.isCancellable()) {
                    leaveSound.play(player);

                    if (leave.getBoolean("Prevent Default Sound").orElse(false))
                        defaultSound = false;
                }
            }
        }

        if (defaultSound && regionLeaveSound != null)
            if (!event.isCancelled() || !regionLeaveSound.isCancellable())
                regionLeaveSound.play(event.getPlayer());
    }

    private void stopOnExit(Player player, ConfigurationSection section)
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
}