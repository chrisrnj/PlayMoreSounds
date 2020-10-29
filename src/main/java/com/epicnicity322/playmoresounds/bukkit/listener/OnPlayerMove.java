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

import com.epicnicity322.playmoresounds.bukkit.region.RegionManager;
import com.epicnicity322.playmoresounds.bukkit.region.SoundRegion;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionEnterEvent;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionLeaveEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public final class OnPlayerMove implements Listener
{
    protected static void callRegionEnterLeaveEvents(Cancellable event, Player player, Location from, Location to)
    {
        for (SoundRegion region : RegionManager.getAllRegions()) {
            boolean isInFrom = region.isInside(from);
            boolean isInTo = region.isInside(to);

            if (isInFrom && !isInTo) {
                RegionLeaveEvent regionLeaveEvent = new RegionLeaveEvent(region, player, from, to);
                Bukkit.getPluginManager().callEvent(regionLeaveEvent);

                if (regionLeaveEvent.isCancelled())
                    event.setCancelled(true);
            } else if (!isInFrom && isInTo) {
                RegionEnterEvent regionEnterEvent = new RegionEnterEvent(region, player, from, to);
                Bukkit.getPluginManager().callEvent(regionEnterEvent);

                if (regionEnterEvent.isCancelled())
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event)
    {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
            if (!event.isCancelled())
                callRegionEnterLeaveEvents(event, event.getPlayer(), from, to);

            //TODO: Run biome leave and enter sounds.
        }
    }
}
