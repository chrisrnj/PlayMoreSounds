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

package com.epicnicity322.playmoresounds.bukkit.listeners;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.region.RegionManager;
import com.epicnicity322.playmoresounds.bukkit.region.SoundRegion;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionEnterEvent;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionLeaveEvent;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableSound;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundManager;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public final class OnRegionEnterLeave extends PMSListener {
    private @Nullable HashMap<String, BukkitRunnable> loopingRegions;
    private @Nullable PlayableRichSound regionEnterSound;
    private @Nullable PlayableRichSound regionLeaveSound;

    public OnRegionEnterLeave(@NotNull PlayMoreSounds plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getName() {
        return "Region Enter|Region Leave";
    }

    @Override
    public void load() {
        var sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();

        regionEnterSound = getRichSound(sounds.getConfigurationSection("Region Enter"));
        regionLeaveSound = getRichSound(sounds.getConfigurationSection("Region Leave"));

        // OnRegionEnterLeave needs to be always loaded, in case any regions are made.
        if (!isLoaded()) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            setLoaded(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRegionEnter(RegionEnterEvent event) {
        Player player = event.getPlayer();
        SoundRegion region = event.getRegion();
        UUID regionId = region.getId();
        boolean playDefaultSound = regionEnterSound != null;

        // Playing loop sound
        PlayableRichSound loopSound = region.getLoopSound();

        if (loopSound != null && (!event.isCancelled() || !loopSound.isCancellable())) {
            ConfigurationSection loopSection = loopSound.getSection();
            long delay = loopSection.getNumber("Delay").orElse(0).longValue();
            long period = loopSection.getNumber("Period").orElse(0).longValue();
            String loopKey = player.getUniqueId() + ";" + regionId;

            if (loopingRegions == null) loopingRegions = new HashMap<>();

            BukkitRunnable previousRunnable = loopingRegions.put(loopKey, loopSound.playInLoop(player, player::getLocation,
                    delay, period, () -> {
                        for (var currentRegion : RegionManager.getRegions()) {
                            if (currentRegion.getId().equals(regionId)) {
                                // Break only if sound has changed or if player is no longer inside region.
                                return !currentRegion.isInside(player.getLocation()) || !loopSound.equals(currentRegion.getLoopSound());
                            }
                        }

                        // Break if region is not found on the loop.
                        return true;
                    }));

            if (previousRunnable != null && !previousRunnable.isCancelled()) previousRunnable.cancel();
            if (loopSection.getBoolean("Prevent Default Sound").orElse(false)) playDefaultSound = false;
        }

        // Playing enter sound
        PlayableRichSound enterSound = region.getEnterSound();

        if (enterSound != null && (!event.isCancelled() || !enterSound.isCancellable())) {
            enterSound.play(player);
            if (enterSound.getSection().getBoolean("Prevent Default Sound").orElse(false)) return;
        }

        // Playing default enter sound in sounds.yml
        if (playDefaultSound && (!event.isCancelled() || !regionEnterSound.isCancellable())) {
            regionEnterSound.play(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRegionLeave(RegionLeaveEvent event) {
        Player player = event.getPlayer();
        SoundRegion region = event.getRegion();
        boolean online = player.isOnline();

        // Must cancel the BukkitRunnable in case the player left the region or quit the server.
        // If the player is online and the event is cancelled, it means the player didn't actually leave the region through move event.
        if (!online || !event.isCancelled()) {
            String loopKey = player.getUniqueId() + ";" + region.getId();
            BukkitRunnable loopingRunnable = loopingRegions == null ? null : loopingRegions.remove(loopKey);

            if (loopingRunnable != null && !loopingRunnable.isCancelled()) loopingRunnable.cancel();

            // Avoiding the hassle of stopping the sound in case the player is offline.
            if (online) {
                stopOnExit(player, region.getLoopSound());
                stopOnExit(player, region.getEnterSound());
                stopOnExit(player, regionEnterSound);
            }
        }

        PlayableRichSound leaveSound = region.getLeaveSound();

        if (leaveSound != null && (!event.isCancelled() || !leaveSound.isCancellable())) {
            leaveSound.play(player);
            if (leaveSound.getSection().getBoolean("Prevent Default Sound").orElse(false)) return;
        }

        // Playing default leave sound in sounds.yml
        if (regionLeaveSound != null && (!event.isCancelled() || !regionLeaveSound.isCancellable())) {
            regionLeaveSound.play(player);
        }
    }

    private void stopOnExit(@NotNull Player player, @Nullable PlayableRichSound playingSound) {
        if (playingSound == null) return;

        if (playingSound.getSection().getBoolean("Stop On Exit.Enabled").orElse(true)) {
            // SoundManager#stopSounds accepts a collection with the sound names to stop.
            HashSet<String> toStop = new HashSet<>(playingSound.getChildSounds().size());

            for (PlayableSound sound : playingSound.getChildSounds()) {
                toStop.add(sound.getSound());
            }

            SoundManager.stopSounds(player, toStop, playingSound.getSection().getNumber("Stop On Exit.Delay").orElse(0).longValue());
        }
    }
}