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
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.region.RegionManager;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionEnterEvent;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundManager;
import com.epicnicity322.playmoresounds.bukkit.util.UpdateManager;
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public final class OnPlayerJoin implements Listener
{
    static final @NotNull HashSet<RegionEnterEvent> playersInRegionWaitingToLoadResourcePack = new HashSet<>();
    private static @Nullable PlayableRichSound firstJoin;
    private static @Nullable PlayableRichSound joinServer;

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

        // Getting all regions in the location.
        RegionManager.getRegions().stream().filter(region -> region.isInside(location)).forEach(region -> {
            RegionEnterEvent regionEnterEvent = new RegionEnterEvent(region, player, location, location);

            // Checking if event should be added to playersInRegionWaitingToLoadResourcePack.
            if (config.getBoolean("Resource Packs.Request").orElse(false)) {
                playersInRegionWaitingToLoadResourcePack.add(regionEnterEvent);
            }

            // Calling the event.
            Bukkit.getPluginManager().callEvent(regionEnterEvent);
        });

        // Setting the player's resource pack.
        if (VersionUtils.supportsResourcePacks() && config.getBoolean("Resource Packs.Request").orElse(false))
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    lang.send(player, lang.get("Resource Packs.Request Message"));
                    config.getString("Resource Packs.URL").ifPresent(player::setResourcePack);
                } catch (Exception ex) {
                    PlayMoreSounds.getConsoleLogger().log(lang.get("Resource Packs.Error").replace("<player>", player.getName()));
                    ex.printStackTrace();
                }
            }, 20);
    }
}
