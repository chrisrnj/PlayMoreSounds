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
import com.epicnicity322.playmoresounds.bukkit.region.SoundRegion;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionEnterEvent;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.bukkit.util.UpdateManager;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.google.common.io.BaseEncoding;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class OnPlayerJoin implements Listener {
    private static final @NotNull Cancellable cancellableDummy = new Cancellable() {
        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public void setCancelled(boolean cancel) {
        }
    };
    private static @Nullable PlayableRichSound firstJoin;
    private static @Nullable PlayableRichSound joinServer;
    private static byte[] resourcePackHash;

    static {
        Runnable soundUpdater = () -> {
            var config = Configurations.CONFIG.getConfigurationHolder().getConfiguration();
            var sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();
            firstJoin = PMSListener.getRichSound(sounds.getConfigurationSection("First Join"));
            joinServer = PMSListener.getRichSound(sounds.getConfigurationSection("Join Server"));

            if (config.getBoolean("Resource Packs.Request").orElse(false) && !config.getString("Resource Packs.URL").orElse("").isEmpty()) {
                String hexadecimalHash = config.getString("Resource Packs.Hash").orElse("");

                if (hexadecimalHash.length() != 40) {
                    PlayMoreSounds.getConsoleLogger().log("The provided resource pack hash is invalid.", ConsoleLogger.Level.WARN);
                } else {
                    try {
                        // A little rant: Why the hell bukkit uses byte[] as parameter for Player#setResourcePack just so they encode the bytes again, the packet for
                        //sending resource packs use String for the hash, so why shouldn't the bukkit method do as well?
                        resourcePackHash = BaseEncoding.base16().decode(hexadecimalHash);
                    } catch (IllegalArgumentException e) {
                        PlayMoreSounds.getConsoleLogger().log("The provided resource pack hash is invalid.", ConsoleLogger.Level.WARN);
                    }
                }
            }
        };

        PlayMoreSounds.onInstance(soundUpdater);
        PlayMoreSounds.onEnable(soundUpdater); // Make sure to load once all configurations have been reloaded.
        PlayMoreSounds.onReload(soundUpdater);
    }

    public OnPlayerJoin() {
        if (PlayMoreSounds.getInstance() == null) throw new IllegalStateException("PlayMoreSounds is not loaded.");
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        var lang = PlayMoreSounds.getLanguage();
        var player = event.getPlayer();
        var location = player.getLocation();

        // Playing join sound
        if (player.hasPlayedBefore()) {
            if (joinServer != null) joinServer.play(player);
        } else if (firstJoin != null) firstJoin.play(player);

        // Send update available message.
        if (UpdateManager.isUpdateAvailable() && player.hasPermission("playmoresounds.update.joinmessage"))
            lang.send(player, false, "&2* &aPlayMoreSounds has a new update available!\n&2* &aDownload it using &7&n/pms update download&a command.");

        var config = Configurations.CONFIG.getConfigurationHolder().getConfiguration();

        // Calling region enter event.
        for (SoundRegion region : RegionManager.getRegions()) {
            if (!region.isInside(location)) continue;

            var regionEnterEvent = new RegionEnterEvent(region, player, location, location);

            // Checking if event should be played only when player accepts resource pack.
            if (config.getBoolean("Resource Packs.Request").orElse(false)) {
                OnPlayerResourcePackStatus.waitUntilResourcePackStatus(player, () -> Bukkit.getPluginManager().callEvent(regionEnterEvent));
            } else {
                Bukkit.getPluginManager().callEvent(regionEnterEvent);
            }
        }

        // Calling biome enter event.
        String biome = location.getBlock().getBiome().name();
        String world = location.getWorld().getName();
        var biomesConfig = Configurations.BIOMES.getConfigurationHolder().getConfiguration();

        if (biomesConfig.getBoolean(world + "." + biome + "." + "Enter.Enabled").orElse(false) || biomesConfig.getBoolean(world + "." + biome + "." + "Loop.Enabled").orElse(false)) {
            // Checking if event should be played only when player accepts resource pack.
            if (config.getBoolean("Resource Packs.Request").orElse(false)) {
                OnPlayerResourcePackStatus.waitUntilResourcePackStatus(player, () -> OnPlayerMove.checkBiomeEnterLeaveSounds(cancellableDummy, player, location, location, false));
            } else {
                OnPlayerMove.checkBiomeEnterLeaveSounds(cancellableDummy, player, location, location, false);
            }
        }

        String url = config.getString("Resource Packs.URL").orElse("");

        // Setting the player's resource pack.
        if (config.getBoolean("Resource Packs.Request").orElse(false) && !url.isEmpty())
            Bukkit.getScheduler().runTaskLater(PlayMoreSounds.getInstance(), () -> player.setResourcePack(url, resourcePackHash, lang.getColored("Resource Packs.Request Message")), 3);
    }
}
