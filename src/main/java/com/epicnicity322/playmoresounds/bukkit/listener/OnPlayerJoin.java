/*
 * PlayMoreSounds - A bukkit plugin that manages and plays sounds.
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
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class OnPlayerJoin implements Listener
{
    private static final @NotNull MessageSender lang = PlayMoreSounds.getLanguage();
    private static final @NotNull BukkitScheduler scheduler = Bukkit.getScheduler();
    private static @Nullable PlayableRichSound firstJoin;
    private static @Nullable PlayableRichSound joinServer;

    static {
        Runnable soundUpdater = () -> {
            Configuration sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();
            ConfigurationSection firstJoinSection = sounds.getConfigurationSection("First Join");
            ConfigurationSection joinServerSection = sounds.getConfigurationSection("Join Server");

            if (firstJoinSection != null) {
                firstJoin = new PlayableRichSound(firstJoinSection);

                if (!firstJoin.isEnabled())
                    firstJoin = null;
            }
            if (joinServerSection != null) {
                joinServer = new PlayableRichSound(joinServerSection);

                if (!joinServer.isEnabled())
                    joinServer = null;
            }
        };

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
        Player player = event.getPlayer();
        Location location = player.getLocation();

        // Playing join sound
        if (player.hasPlayedBefore()) {
            if (joinServer != null) joinServer.play(player);
            else if (firstJoin != null) firstJoin.play(player);
        }

        // Send update available message.
        if (UpdateManager.isUpdateAvailable() && player.hasPermission("playmoresounds.update.joinmessage"))
            lang.send(player, "&a* PlayMoreSounds has a new update available! *\n&aLink >&7 https://www.spigotmc.org/resources/37429/");

        Configuration config = Configurations.CONFIG.getConfigurationHolder().getConfiguration();

        // Enabling sounds on login.
        if (config.getBoolean("Enable Sounds On Login").orElse(false))
            SoundManager.toggleSoundsState(player, true);

        // Calling region enter events.
        RegionManager.getRegions().stream().filter(region -> region.isInside(location)).forEach(region -> {
            RegionEnterEvent regionEnterEvent = new RegionEnterEvent(region, player, location, location);
            Bukkit.getPluginManager().callEvent(regionEnterEvent);
        });

        // Setting the player's resource pack.
        if (VersionUtils.supportsResourcePacks()) {
            try {
                if (config.getBoolean("Resource Packs.Request").orElse(false))
                    scheduler.runTaskLater(plugin, () -> {
                        lang.send(player, lang.get("Resource Packs.Request Message"));
                        config.getString("Resource Packs.URL").ifPresent(player::setResourcePack);
                    }, 20);
            } catch (Exception ex) {
                PlayMoreSounds.getConsoleLogger().log(lang.get("Resource Packs.Error").replace("<player>", player.getName()));
                ex.printStackTrace();
            }
        }
    }
}
