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

import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.region.RegionManager;
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
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public final class OnRegionEnterLeave extends PMSListener
{
    private final @NotNull HashMap<String, PlayableRichSound> regionSounds = new HashMap<>();
    private final @NotNull HashMap<String, BukkitRunnable> loopingRegions = new HashMap<>();
    private @Nullable PlayableRichSound regionEnterSound = null;
    private @Nullable PlayableRichSound regionLeaveSound = null;

    public OnRegionEnterLeave(@NotNull PlayMoreSounds plugin)
    {
        super(plugin);

        PlayMoreSounds.onDisable(() -> loopingRegions.entrySet().removeIf(entry -> {
            if (!entry.getValue().isCancelled()) entry.getValue().cancel();
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
        regionSounds.clear();

        var sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();
        var regions = Configurations.REGIONS.getConfigurationHolder().getConfiguration();
        ConfigurationSection regionsSection = regions.getConfigurationSection("PlayMoreSounds");

        if (regionsSection != null) {
            for (Map.Entry<String, Object> node : regionsSection.getNodes().entrySet()) {
                if (!(node.getValue() instanceof ConfigurationSection regionSection)) continue;

                addRegionSound(regionSection, "Enter");
                addRegionSound(regionSection, "Leave");
                addRegionSound(regionSection, "Loop");
            }
        }

        regionEnterSound = getRichSound(sounds.getConfigurationSection("Region Enter"));
        regionLeaveSound = getRichSound(sounds.getConfigurationSection("Region Leave"));

        if (!regionSounds.isEmpty() || regionEnterSound != null || regionLeaveSound != null) {
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

    private void addRegionSound(ConfigurationSection regionSection, String type)
    {
        if (regionSection.getBoolean(type + ".Enabled").orElse(false)) {
            try {
                regionSounds.put(type + "." + regionSection.getName(), new PlayableRichSound(regionSection.getConfigurationSection(type)));
            } catch (IllegalArgumentException e) {
                PlayMoreSounds.getConsoleLogger().log("The sound region '" + regionSection.getName() + "' has an invalid sound for " + type + " event in regions.yml configuration, so it was ignored.", ConsoleLogger.Level.WARN);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRegionEnter(RegionEnterEvent event)
    {
        var player = event.getPlayer();
        var regionName = event.getRegion().getName();

        boolean playDefaultSound = regionEnterSound != null;
        boolean playEnterSound = true;

        String loopKey = player.getUniqueId() + ";" + regionName;
        PlayableRichSound loopSound = regionSounds.get("Loop." + regionName);

        // Playing loop sound
        if (loopSound != null && (!event.isCancelled() || !loopSound.isCancellable())) {
            ConfigurationSection loopSection = loopSound.getSection();
            long delay = loopSection.getNumber("Delay").orElse(0).longValue();
            long period = loopSection.getNumber("Period").orElse(0).longValue();

            BukkitRunnable previousRunnable = loopingRegions.put(loopKey, loopSound.playInLoop(player, player::getLocation,
                    delay, period, () -> {
                        if (!player.isOnline()) return true;

                        for (var region : RegionManager.getRegions()) {
                            if (region.getName().equals(regionName)) {
                                return !region.isInside(player.getLocation());
                            }
                        }

                        return true;
                    }));

            if (previousRunnable != null && !previousRunnable.isCancelled()) previousRunnable.cancel();

            if (loopSection.getBoolean("Prevent Default Sound").orElse(false))
                playEnterSound = false;
        }

        // Playing enter sound
        if (playEnterSound) {
            PlayableRichSound enterSound = regionSounds.get("Enter." + regionName);

            if (enterSound != null && (!event.isCancelled() || !enterSound.isCancellable())) {
                enterSound.play(player);

                if (enterSound.getSection().getBoolean("Prevent Default Sound").orElse(false))
                    playDefaultSound = false;
            }
        }

        // Playing default sound in sounds.yml
        if (playDefaultSound && (!event.isCancelled() || !regionEnterSound.isCancellable())) {
            regionEnterSound.play(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRegionLeave(RegionLeaveEvent event)
    {
        var player = event.getPlayer();
        var regionName = event.getRegion().getName();
        // Being cancelled means the player didn't actually leave the region, so the loop keeps playing.
        if (!event.isCancelled()) {
            String loopKey = player.getUniqueId() + ";" + regionName;
            BukkitRunnable loopingRunnable = loopingRegions.remove(loopKey);

            if (loopingRunnable != null && !loopingRunnable.isCancelled()) loopingRunnable.cancel();

            stopOnExit(player, regionSounds.get("Loop." + regionName));
            stopOnExit(player, regionSounds.get("Enter." + regionName));
        }

        boolean playDefaultSound = regionLeaveSound != null;
        PlayableRichSound leaveSound = regionSounds.get("Leave." + regionName);

        if (leaveSound != null && (!event.isCancelled() || !leaveSound.isCancellable())) {
            leaveSound.play(player);

            if (leaveSound.getSection().getBoolean("Prevent Default Sound").orElse(false))
                playDefaultSound = false;
        }

        if (playDefaultSound && (!event.isCancelled() || !regionLeaveSound.isCancellable())) {
            regionLeaveSound.play(player);
        }
    }

    private void stopOnExit(Player player, PlayableRichSound playingSound)
    {
        if (playingSound == null) return;

        if (playingSound.getSection().getBoolean("Stop On Exit.Enabled").orElse(true)) {
            HashSet<String> toStop = new HashSet<>(playingSound.getChildSounds().size());

            for (PlayableSound sound : playingSound.getChildSounds()) {
                toStop.add(sound.getSound());
            }

            SoundManager.stopSounds(player, toStop, playingSound.getSection().getNumber("Stop On Exit.Delay").orElse(0).longValue());
        }
    }
}