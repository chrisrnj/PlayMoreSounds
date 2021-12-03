/*
 * Copyright (C) 2021 Christiano Rangel
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

package com.epicnicity322.regionshandler;

import com.epicnicity322.epicpluginlib.core.util.ObjectUtils;
import com.epicnicity322.nbssongplayer.NBSSongPlayer;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundManager;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.sound.SoundType;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Supplier;

public class RegionsHandler
{
    private static @Nullable PlayableRichSound regionEnterSound;
    private static @Nullable PlayableRichSound regionLeaveSound;

    static {
        Runnable runnable = () -> {
            Configuration sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();
            ConfigurationSection regionEnterSection = sounds.getConfigurationSection("Region Enter");
            ConfigurationSection regionLeaveSection = sounds.getConfigurationSection("Region Leave");

            if (regionEnterSection != null && regionEnterSection.getBoolean("Enabled").orElse(false))
                regionEnterSound = new PlayableRichSound(regionEnterSection);

            if (regionLeaveSection != null && regionLeaveSection.getBoolean("Enabled").orElse(false))
                regionLeaveSound = new PlayableRichSound(regionLeaveSection);
        };

        runnable.run();
        PlayMoreSounds.onReload(runnable);
    }

    private final @NotNull String pluginName;
    private final @NotNull HashMap<String, BukkitRunnable> regionsInLoop = new HashMap<>();
    private final @NotNull HashMap<String, HashSet<String>> soundsToStop = new HashMap<>();
    private boolean registered = false;

    public RegionsHandler(@NotNull String pluginName, @NotNull Listener listener)
    {
        this.pluginName = pluginName;

        Runnable runnable = () -> {
            Configuration regions = Configurations.REGIONS.getConfigurationHolder().getConfiguration();
            ConfigurationSection regionsYAMLSection = regions.getConfigurationSection(pluginName);
            boolean load = !soundsToStop.isEmpty() || ObjectUtils.getOrDefault(regionEnterSound, regionLeaveSound) != null;

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
                if (!registered) {
                    Bukkit.getPluginManager().registerEvents(listener, PlayMoreSounds.getInstance());
                    registered = true;
                }
            } else {
                if (registered) {
                    HandlerList.unregisterAll(listener);
                    registered = false;
                }
            }
        };

        runnable.run();
        PlayMoreSounds.onReload(runnable);

        PlayMoreSounds.onDisable(() -> regionsInLoop.entrySet().removeIf(entry -> {
            entry.getValue().cancel();
            return true;
        }));
    }

    public void onEnter(Player player, String regionId, Supplier<Boolean> stopper, Cancellable cancellable)
    {
        ConfigurationSection regions = Configurations.REGIONS.getConfigurationHolder().getConfiguration().getConfigurationSection(pluginName);
        boolean defaultSound = true;

        String key = regionId + ";" + player.getUniqueId();

        if (regionsInLoop.containsKey(key)) {
            regionsInLoop.get(key).cancel();
            regionsInLoop.remove(key);
        }

        if (regions != null) {
            ConfigurationSection loop = regions.getConfigurationSection(regionId + ".Loop");
            boolean playEnterSound = true;

            if (loop != null) {
                PlayableRichSound loopSound = new PlayableRichSound(loop);

                if (loopSound.isEnabled() && (cancellable == null || !cancellable.isCancelled() || !loopSound.isCancellable())) {
                    long delay = loop.getNumber("Delay").orElse(0).longValue();
                    long period = loop.getNumber("Period").orElse(0).longValue();

                    regionsInLoop.put(key, loopSound.playInLoop(player, player::getLocation, delay, period, stopper));

                    stopOnExit(player, regionId, loop);

                    if (loop.getBoolean("Prevent Other Sounds.Default Sound").orElse(false))
                        defaultSound = false;
                    if (loop.getBoolean("Prevent Other Sounds.Enter Sound").orElse(false))
                        playEnterSound = false;
                }
            }

            if (playEnterSound) {
                ConfigurationSection enter = regions.getConfigurationSection(regionId + ".Enter");

                if (enter != null) {
                    PlayableRichSound enterSound = new PlayableRichSound(enter);

                    if (enterSound.isEnabled()) {
                        if (cancellable == null || !cancellable.isCancelled() || !enterSound.isCancellable()) {
                            enterSound.play(player);

                            stopOnExit(player, regionId, enter);

                            if (enter.getBoolean("Prevent Default Sound").orElse(false))
                                defaultSound = false;
                        }
                    }
                }
            }
        }

        if (defaultSound && regionEnterSound != null)
            if (cancellable == null || !cancellable.isCancelled() || !regionEnterSound.isCancellable()) {
                regionEnterSound.play(player);

                if (regionEnterSound.isEnabled())
                    stopOnExit(player, regionId, regionEnterSound.getSection());
            }
    }

    public void onLeave(Player player, String regionId, Cancellable cancellable)
    {
        String stopKey = regionId + ";" + player.getUniqueId() + ";";

        soundsToStop.entrySet().removeIf(entry -> {
            String key = entry.getKey();

            if (key.startsWith(stopKey)) {
                long delay = Long.parseLong(key.substring(key.lastIndexOf(";") + 1));

                HashSet<String> value = entry.getValue();

                if (PlayMoreSounds.getInstance() != null && PlayMoreSounds.getInstance().getAddonManager().getAddons().stream().anyMatch(addon -> addon.toString().equals("NBS Song Player"))) {
                    if (value != null)
                        value.removeIf(sound -> {
                            if (sound.startsWith("nbs:") && PlayMoreSounds.getAddonManager().getAddons().stream().anyMatch(addon -> addon.toString().equals("NBS Song Player"))) {
                                NBSSongPlayer.stop(player, sound.substring(4));
                                return true;
                            }

                            return false;
                        });
                }

                SoundManager.stopSounds(player, value, delay);
                return true;
            }

            return false;
        });

        String key = regionId + ";" + player.getUniqueId();

        if (regionsInLoop.containsKey(key)) {
            regionsInLoop.get(key).cancel();
            regionsInLoop.remove(key);
        }

        boolean defaultSound = true;

        ConfigurationSection leave = Configurations.REGIONS.getConfigurationHolder().getConfiguration().getConfigurationSection(pluginName + "." + regionId + ".Leave");

        if (leave != null) {
            PlayableRichSound leaveSound = new PlayableRichSound(leave);

            if (leaveSound.isEnabled()) {
                if (cancellable == null || !cancellable.isCancelled() || !leaveSound.isCancellable()) {
                    leaveSound.play(player);

                    if (leave.getBoolean("Prevent Default Sound").orElse(false))
                        defaultSound = false;
                }
            }
        }

        if (defaultSound && regionLeaveSound != null)
            if (cancellable == null || !cancellable.isCancelled() || !regionLeaveSound.isCancellable())
                regionLeaveSound.play(player);
    }

    private void stopOnExit(Player player, String regionId, ConfigurationSection section)
    {
        if (section.getBoolean("Stop On Exit.Enabled").orElse(false)) {
            String key = regionId + ";" + player.getUniqueId() + ";" + section.getNumber("Stop On Exit.Delay").orElse(0);
            HashSet<String> sounds = soundsToStop.getOrDefault(key, new HashSet<>());
            ConfigurationSection soundsSection = section.getConfigurationSection("Sounds");

            if (soundsSection != null)
                for (String sound : soundsSection.getNodes().keySet()) {
                    String soundToStop = soundsSection.getString(sound + ".Sound").orElse("");

                    sounds.add(SoundType.getPresentSoundNames().contains(soundToStop) ? SoundType.valueOf(soundToStop).getSound().orElse("") : soundToStop);
                }

            soundsToStop.put(key, sounds);
        }
    }
}
