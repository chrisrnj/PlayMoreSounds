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
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class OnPlayerResourcePackStatus implements Listener
{
    private static final @NotNull MessageSender lang = PlayMoreSounds.getMessageSender();

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
