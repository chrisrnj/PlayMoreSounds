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

import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.epicpluginlib.bukkit.reflection.ReflectionUtil;
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.region.RegionManager;
import com.epicnicity322.playmoresounds.bukkit.region.SoundRegion;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionEnterEvent;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundManager;
import com.epicnicity322.playmoresounds.bukkit.util.UpdateManager;
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.google.common.io.BaseEncoding;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class OnPlayerJoin implements Listener
{
    private static final @NotNull Cancellable cancellableDummy = new Cancellable()
    {
        @Override public boolean isCancelled()
        {
            return false;
        }

        @Override public void setCancelled(boolean cancel)
        {
        }
    };
    private static final boolean hasFancyResourcePackMessage = ReflectionUtil.getMethod(Player.class, "setResourcePack", String.class, byte[].class, String.class) != null;
    private static final boolean hasResourcePackHash = ReflectionUtil.getMethod(Player.class, "setResourcePack", String.class, byte[].class) != null;
    private static @Nullable PlayableRichSound firstJoin;
    private static @Nullable PlayableRichSound joinServer;
    private static byte[] resourcePackHash;

    static {
        Runnable soundUpdater = () -> {
            Configuration sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();

            if (sounds.getBoolean("First Join.Enabled").orElse(false))
                firstJoin = new PlayableRichSound(sounds.getConfigurationSection("First Join"));
            else
                firstJoin = null;

            if (sounds.getBoolean("Join Server.Enabled").orElse(false))
                joinServer = new PlayableRichSound(sounds.getConfigurationSection("Join Server"));
            else
                joinServer = null;

            Configuration config = Configurations.CONFIG.getConfigurationHolder().getConfiguration();

            if (hasResourcePackHash && config.getBoolean("Resource Packs.Request").orElse(false) && !config.getString("Resource Packs.URL").orElse("").isEmpty()) {
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

        // Not running it immediately because PlayableRichSound requires PlayMoreSounds loaded if delay > 0.
        PlayMoreSounds.onInstance(soundUpdater);
        PlayMoreSounds.onReload(soundUpdater);
    }

    private final @NotNull PlayMoreSounds plugin;

    public OnPlayerJoin(@NotNull PlayMoreSounds plugin)
    {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        MessageSender lang = PlayMoreSounds.getLanguage();
        Player player = event.getPlayer();
        Location location = player.getLocation();

        // Playing join sound
        if (player.hasPlayedBefore()) {
            if (joinServer != null) joinServer.play(player);
        } else if (firstJoin != null) firstJoin.play(player);

        // Send update available message.
        if (UpdateManager.isUpdateAvailable() && player.hasPermission("playmoresounds.update.joinmessage"))
            lang.send(player, false, "&2* &aPlayMoreSounds has a new update available!\n&2* &aDownload it using &7&n/pms update download&a command.");

        Configuration config = Configurations.CONFIG.getConfigurationHolder().getConfiguration();

        // Enabling sounds on login.
        if (config.getBoolean("Enable Sounds On Login").orElse(false))
            SoundManager.toggleSoundsState(player, true);

        // Calling region enter event.
        for (SoundRegion region : RegionManager.getRegions()) {
            if (!region.isInside(location)) continue;

            RegionEnterEvent regionEnterEvent = new RegionEnterEvent(region, player, location, location);

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
        Configuration biomesConfig = Configurations.BIOMES.getConfigurationHolder().getConfiguration();

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
        if (VersionUtils.supportsResourcePacks() && config.getBoolean("Resource Packs.Request").orElse(false) && !url.isEmpty())
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    if (hasFancyResourcePackMessage) {
                        player.setResourcePack(url, resourcePackHash, lang.getColored("Resource Packs.Request Message"));
                    } else {
                        lang.send(player, false, lang.get("Resource Packs.Request Message"));

                        if (hasResourcePackHash)
                            player.setResourcePack(url, resourcePackHash);
                        else
                            player.setResourcePack(url);
                    }
                } catch (Exception ex) {
                    PlayMoreSounds.getConsoleLogger().log(lang.get("Resource Packs.Error").replace("<player>", player.getName()));
                    ex.printStackTrace();
                }
            }, 3);
    }
}
