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

import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.epicpluginlib.bukkit.logger.Logger;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.region.RegionManager;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionEnterEvent;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundManager;
import com.epicnicity322.playmoresounds.bukkit.util.UpdateManager;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public final class OnPlayerJoin implements Listener
{
    private static final @NotNull MessageSender lang = PlayMoreSounds.getMessageSender();
    private static final @NotNull Logger logger = PlayMoreSounds.getPMSLogger();
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
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                Configuration sounds = Configurations.SOUNDS.getPluginConfig().getConfiguration();
                ConfigurationSection section;

                if (player.hasPlayedBefore())
                    section = sounds.getConfigurationSection("Join Server");
                else
                    section = sounds.getConfigurationSection("First Join");

                if (section != null)
                    new RichSound(section).play(player);
            }
        }, 1);

        // Send update available message.
        if (UpdateManager.isUpdateAvailable())
            if (player.hasPermission("playmoresounds.update.joinmessage"))
                lang.send(player, "&a* PlayMoreSounds has a new update available! *" +
                        "\n&aLink >&7 https://www.spigotmc.org/resources/37429/");

        Configuration config = Configurations.CONFIG.getPluginConfig().getConfiguration();

        // Enabling sounds on login.
        if (config.getBoolean("Enable Sounds On Login").orElse(false))
            SoundManager.toggleSoundsState(player, true);

        // Calling region enter events.
        RegionManager.getAllRegions().stream().filter(region -> region.isInside(location)).forEach(region -> {
            RegionEnterEvent regionEnterEvent = new RegionEnterEvent(region, player, location, location);
            Bukkit.getPluginManager().callEvent(regionEnterEvent);
        });

        // Setting the player's resource pack.
        try {
            if (config.getBoolean("Resource Packs.Request").orElse(false))
                new BukkitRunnable()
                {
                    public void run()
                    {
                        lang.send(player, lang.get("Resource Packs.Request Message"));
                        config.getString("Resource Packs.URL").ifPresent(player::setResourcePack);
                    }
                }.runTaskLater(PlayMoreSounds.getInstance(), 20);
        } catch (Exception ex) {
            logger.log(lang.get("Resource Packs.Error").replace("<player>", player.getName()));
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerResourcePackStatus(PlayerResourcePackStatusEvent event)
    {
        Configuration config = Configurations.CONFIG.getPluginConfig().getConfiguration();
        PlayerResourcePackStatusEvent.Status status = event.getStatus();

        if (config.getBoolean("Resource Packs.Request").orElse(false) &&
                config.getBoolean("Resource Packs.Force.Enabled").orElse(false) &&
                status == PlayerResourcePackStatusEvent.Status.DECLINED ||
                status == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
            if (!config.getBoolean("Resource Packs.Force.Even If Download Fail").orElse(false))
                if (status == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD)
                    return;

            new BukkitRunnable()
            {
                public void run()
                {
                    event.getPlayer().kickPlayer(lang.getColored("Resource Packs.Kick Message"));
                }
            }.runTaskLater(PlayMoreSounds.getInstance(), 20);
        }
    }
}
