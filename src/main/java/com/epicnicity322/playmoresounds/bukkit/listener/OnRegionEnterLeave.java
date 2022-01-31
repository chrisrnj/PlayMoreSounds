/*
 * PlayMoreSounds - A bukkit plugin that manages and plays sounds.
 * Copyright (C) 2022 Christiano Rangel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.epicpluginlib.core.util.ObjectUtils;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.region.RegionManager;
import com.epicnicity322.playmoresounds.bukkit.region.SoundRegion;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionEnterEvent;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionLeaveEvent;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundManager;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.sound.SoundType;
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
    private @Nullable PlayableRichSound regionEnterSound = null;
    private @Nullable PlayableRichSound regionLeaveSound = null;

    public OnRegionEnterLeave(@NotNull PlayMoreSounds plugin)
    {
        super(plugin);
        this.plugin = plugin;

        PlayMoreSounds.onDisable(() -> regionsInLoop.entrySet().removeIf(entry -> {
            entry.getValue().cancel();
            return true;
        }));
    }

    @Override
    public @NotNull String getName()
    {
        return "Region Enter|Region Leave";
    }

    @Override
    public void load()
    {
        Configuration sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();
        Configuration regions = Configurations.REGIONS.getConfigurationHolder().getConfiguration();
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
                regionEnterSound = new PlayableRichSound(regionEnterSection);
            if (regionLeaveSection != null)
                regionLeaveSound = new PlayableRichSound(regionLeaveSection);

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
        ConfigurationSection regions = Configurations.REGIONS.getConfigurationHolder().getConfiguration()
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
                PlayableRichSound loopSound = new PlayableRichSound(loop);

                if (loopSound.isEnabled() && (!event.isCancelled() || !loopSound.isCancellable())) {
                    long delay = loop.getNumber("Delay").orElse(0).longValue();
                    long period = loop.getNumber("Period").orElse(0).longValue();

                    regionsInLoop.put(key, loopSound.playInLoop(player, player::getLocation, delay, period, () -> {
                        Configuration updatedRegions = Configurations.REGIONS.getConfigurationHolder().getConfiguration();

                        return !updatedRegions.getBoolean("PlayMoreSounds." + region.getName() + ".Loop.Enabled").orElse(false)
                                || !RegionManager.getRegions().contains(region) || !player.isOnline() || !region.isInside(player.getLocation());
                    }));

                    stopOnExit(player, region, loop);

                    if (loop.getBoolean("Prevent Other Sounds.Default Sound").orElse(false))
                        defaultSound = false;
                    if (loop.getBoolean("Prevent Other Sounds.Enter Sound").orElse(false))
                        playEnterSound = false;
                }
            }

            if (playEnterSound) {
                ConfigurationSection enter = regions.getConfigurationSection(region.getName() + ".Enter");

                if (enter != null) {
                    PlayableRichSound enterSound = new PlayableRichSound(enter);

                    if (enterSound.isEnabled()) {
                        if (!event.isCancelled() || !enterSound.isCancellable()) {
                            enterSound.play(player);

                            stopOnExit(player, region, enter);

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
                    stopOnExit(player, region, regionEnterSound.getSection());
            }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRegionLeave(RegionLeaveEvent event)
    {
        Player player = event.getPlayer();
        SoundRegion region = event.getRegion();
        String key = region.getId() + ";" + player.getUniqueId();

        if (regionsInLoop.containsKey(key)) {
            regionsInLoop.get(key).cancel();
            regionsInLoop.remove(key);
        }

        soundsToStop.entrySet().removeIf(entry -> {
            String stopKey = entry.getKey();

            if (stopKey.startsWith(key)) {
                long delay = Long.parseLong(stopKey.substring(stopKey.lastIndexOf(";") + 1));

                SoundManager.stopSounds(player, entry.getValue(), delay);
                return true;
            }

            return false;
        });

        boolean defaultSound = true;

        ConfigurationSection leave = Configurations.REGIONS.getConfigurationHolder().getConfiguration().getConfigurationSection("PlayMoreSounds." + region.getName() + ".Leave");

        if (leave != null) {
            PlayableRichSound leaveSound = new PlayableRichSound(leave);

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
                regionLeaveSound.play(player);
    }

    private void stopOnExit(Player player, SoundRegion region, ConfigurationSection section)
    {
        if (section.getBoolean("Stop On Exit.Enabled").orElse(false)) {
            ConfigurationSection soundsSection = section.getConfigurationSection("Sounds");
            String key = region.getId() + ";" + player.getUniqueId() + ";" + section.getNumber("Stop On Exit.Delay").orElse(0);
            HashSet<String> sounds = soundsToStop.get(key);

            if (sounds == null)
                sounds = new HashSet<>();

            if (soundsSection != null)
                for (String sound : soundsSection.getNodes().keySet()) {
                    String soundToStop = soundsSection.getString(sound + ".Sound").orElse("");

                    sounds.add(SoundType.getPresentSoundNames().contains(soundToStop) ? SoundType.valueOf(soundToStop).getSound().orElse("") : soundToStop);
                }

            soundsToStop.put(key, sounds);
        }
    }
}