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

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

public final class OnPlayerTeleport implements Listener
{
    private final @NotNull PlayMoreSounds main;
    private final @NotNull BukkitScheduler scheduler;

    public OnPlayerTeleport(@NotNull PlayMoreSounds main)
    {
        this.main = main;
        scheduler = Bukkit.getScheduler();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event)
    {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        if (!event.isCancelled())
            OnPlayerMove.callRegionEnterLeaveEvents(event, player, from, to);

        OnPlayerMove.checkBiomeEnterLeaveSounds(event, player, from, to);

        if (event.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND) {
            ConfigurationSection section = Configurations.SOUNDS.getPluginConfig().getConfiguration()
                    .getConfigurationSection("Teleport");

            if (section != null) {
                RichSound sound = new RichSound(section);

                if (sound.isEnabled() && (!event.isCancelled() || !sound.isCancellable()))
                    scheduler.runTaskLater(main, () -> sound.play(player), 1);
            }
        }
    }
}
