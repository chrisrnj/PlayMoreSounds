/*
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

package com.epicnicity322.regionshandler;

import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.nbssongplayer.NBSSongPlayer;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.listeners.OnPlayerResourcePackStatus;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableSound;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundManager;
import com.epicnicity322.playmoresounds.bukkit.sound.events.PlayRichSoundEvent;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class RegionsHandler {
    private static @Nullable PlayableRichSound regionEnterSound;
    private static @Nullable PlayableRichSound regionLeaveSound;

    static {
        Runnable runnable = () -> {
            Configuration sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();

            if (sounds.getBoolean("Region Enter.Enabled").orElse(false)) {
                regionEnterSound = new PlayableRichSound(sounds.getConfigurationSection("Region Enter"));
            } else {
                regionEnterSound = null;
            }

            if (sounds.getBoolean("Region Leave.Enabled").orElse(false)) {
                regionLeaveSound = new PlayableRichSound(sounds.getConfigurationSection("Region Leave"));
            } else {
                regionLeaveSound = null;
            }
        };

        runnable.run();
        PlayMoreSounds.onReload(runnable);
    }

    private final @NotNull String pluginName;
    private final @NotNull Listener listener;
    private final @NotNull AtomicBoolean listenerRegistered = new AtomicBoolean(false);
    private final @NotNull InsideChecker insideChecker;
    private final @NotNull HashMap<String, PlayableRichSound> regionSounds = new HashMap<>();
    private final @NotNull HashMap<String, BukkitRunnable> loopingRegions = new HashMap<>();
    private @Nullable Listener resourcePackWaiter;

    public RegionsHandler(@NotNull String pluginName, @NotNull Listener listener, @NotNull InsideChecker insideChecker) {
        this.pluginName = pluginName;
        this.listener = listener;
        this.insideChecker = insideChecker;

        reloadListener();
        PlayMoreSounds.onReload(this::reloadListener);
        // Stopping all looping region sounds
        PlayMoreSounds.onDisable(() -> loopingRegions.forEach((key, runnable) -> {
            if (!runnable.isCancelled()) runnable.cancel();
        }));
    }

    public void reloadListener() {
        regionSounds.clear();
        Configuration regions = Configurations.REGIONS.getConfigurationHolder().getConfiguration();
        Configuration sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();
        ConfigurationSection pluginSection = regions.getConfigurationSection(pluginName);

        if (pluginSection != null) {
            for (Map.Entry<String, Object> node : pluginSection.getNodes().entrySet()) {
                if (!(node.getValue() instanceof ConfigurationSection)) continue;
                ConfigurationSection regionSection = (ConfigurationSection) node.getValue();

                addRegionSound(regionSection, "Enter");
                addRegionSound(regionSection, "Leave");
                addRegionSound(regionSection, "Loop");
            }
        }

        boolean shouldRegister = !regionSounds.isEmpty() || sounds.getBoolean("Region Enter.Enabled").orElse(false) || sounds.getBoolean("Region Leave.Enabled").orElse(false);

        if (shouldRegister) {
            if (!listenerRegistered.getAndSet(true)) {
                Bukkit.getPluginManager().registerEvents(listener, PlayMoreSounds.getInstance());
            }
        } else {
            if (listenerRegistered.getAndSet(false)) {
                HandlerList.unregisterAll(listener);
            }
        }

        // Checking if resource pack waiter should be registered / unregistered.
        if (shouldRegister && Configurations.CONFIG.getConfigurationHolder().getConfiguration().getBoolean("Resource Packs.Request").orElse(false)) {
            if (resourcePackWaiter == null) {
                resourcePackWaiter = new JoinListener();
                Bukkit.getPluginManager().registerEvents(resourcePackWaiter, PlayMoreSounds.getInstance());
            }
        } else {
            if (resourcePackWaiter != null) {
                HandlerList.unregisterAll(resourcePackWaiter);
                resourcePackWaiter = null;
            }
        }
    }

    private void addRegionSound(ConfigurationSection regionSection, String type) {
        if (regionSection.getBoolean(type + ".Enabled").orElse(false)) {
            try {
                regionSounds.put(type + "." + regionSection.getName(), new PlayableRichSound(regionSection.getConfigurationSection(type)));
            } catch (IllegalArgumentException e) {
                PlayMoreSounds.getConsoleLogger().log("[Regions Handler] The region '" + regionSection.getName() + "' of the plugin " + pluginName + " has an invalid sound for " + type + " event in regions.yml configuration, so it was ignored.", ConsoleLogger.Level.WARN);
            }
        }
    }

    public void onEnter(@NotNull Player player, @NotNull String regionId) {
        onEnter(player, regionId, false);
    }

    public void onEnter(@NotNull Player player, @NotNull String regionId, boolean isCancelled) {
        boolean playDefaultSound = regionEnterSound != null;
        String loopKey = player.getUniqueId() + ";" + regionId;
        PlayableRichSound loopSound = regionSounds.get("Loop." + regionId);
        boolean playEnterSound = true;

        // Playing loop sound
        if (loopSound != null && (!isCancelled || !loopSound.isCancellable())) {
            ConfigurationSection loopSection = loopSound.getSection();
            long delay = loopSection.getNumber("Delay").orElse(0).longValue();
            long period = loopSection.getNumber("Period").orElse(0).longValue();

            BukkitRunnable previousRunnable = loopingRegions.put(loopKey, loopSound.playInLoop(player, player::getLocation,
                    delay, period, () -> !player.isOnline() || !insideChecker.isPlayerInside(player, regionId)));

            if (previousRunnable != null && !previousRunnable.isCancelled()) previousRunnable.cancel();

            if (loopSection.getBoolean("Prevent Default Sound").orElse(false))
                playEnterSound = false;
        }

        // Playing enter sound
        if (playEnterSound) {
            PlayableRichSound enterSound = regionSounds.get("Enter." + regionId);

            if (enterSound != null && (!isCancelled || !enterSound.isCancellable())) {
                enterSound.play(player);

                if (enterSound.getSection().getBoolean("Prevent Default Sound").orElse(false))
                    playDefaultSound = false;
            }
        }

        // Playing default sound in sounds.yml
        if (playDefaultSound && (!isCancelled || !regionEnterSound.isCancellable())) {
            regionEnterSound.play(player);
        }
    }

    public void onLeave(@NotNull Player player, @NotNull String regionId) {
        onLeave(player, regionId, false);
    }

    public void onLeave(@NotNull Player player, @NotNull String regionId, boolean isCancelled) {
        // Being cancelled means the player didn't actually leave the region, so the loop keeps playing.
        if (!isCancelled) {
            String loopKey = player.getUniqueId() + ";" + regionId;
            BukkitRunnable loopingRunnable = loopingRegions.remove(loopKey);

            if (loopingRunnable != null && !loopingRunnable.isCancelled()) loopingRunnable.cancel();

            stopOnExit(player, regionSounds.get("Loop." + regionId));
            stopOnExit(player, regionSounds.get("Enter." + regionId));
        }

        boolean playDefaultSound = regionLeaveSound != null;
        PlayableRichSound leaveSound = regionSounds.get("Leave." + regionId);

        if (leaveSound != null && (!isCancelled || !leaveSound.isCancellable())) {
            leaveSound.play(player);

            if (leaveSound.getSection().getBoolean("Prevent Default Sound").orElse(false))
                playDefaultSound = false;
        }

        if (playDefaultSound && (!isCancelled || !regionLeaveSound.isCancellable())) {
            regionLeaveSound.play(player);
        }
    }

    private void stopOnExit(Player player, PlayableRichSound playingSound) {
        if (playingSound == null) return;

        if (playingSound.getSection().getBoolean("Stop On Exit.Enabled").orElse(true)) {
            HashSet<String> toStop = new HashSet<>(playingSound.getChildSounds().size());

            for (PlayableSound sound : playingSound.getChildSounds()) {
                String soundName = sound.getSound();

                if (RegionsHandlerAddon.NBS_SONG_PLAYER && soundName.startsWith("nbs:")) {
                    NBSSongPlayer.stop(player, soundName.substring(4));
                } else {
                    toStop.add(soundName);
                }
            }

            if (!toStop.isEmpty())
                SoundManager.stopSounds(player, toStop, playingSound.getSection().getNumber("Stop On Exit.Delay").orElse(0).longValue());
        }
    }

    public static abstract class InsideChecker {
        protected abstract boolean isPlayerInside(@NotNull Player player, @NotNull String regionId);
    }

    private final class JoinListener implements Listener {
        private final @NotNull HashMap<UUID, HashSet<PlayableRichSound>> toPrevent = new HashMap<>();

        // If you use a plugin that calls "RegionEnterEvent" on PlayerLoginEvent this won't work.
        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            boolean added = false;

            for (Map.Entry<String, PlayableRichSound> regionSound : regionSounds.entrySet()) {
                String key = regionSound.getKey();

                if ((key.startsWith("Loop.") || key.startsWith("Enter.")) && insideChecker.isPlayerInside(player, key.substring(key.indexOf('.') + 1))) {
                    toPrevent.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(regionSound.getValue());
                    added = true;
                }
            }

            if (added)
                OnPlayerResourcePackStatus.waitUntilResourcePackStatus(player, () -> toPrevent.remove(player.getUniqueId()).forEach(sound -> sound.play(player)));
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onPlayRichSound(PlayRichSoundEvent event) {
            Player player = event.getPlayer();
            if (player == null) return;
            HashSet<PlayableRichSound> soundsToPrevent = toPrevent.get(player.getUniqueId());
            if (soundsToPrevent == null) return;
            if (soundsToPrevent.contains(event.getRichSound())) {
                event.setCancelled(true);
            }
        }
    }
}
