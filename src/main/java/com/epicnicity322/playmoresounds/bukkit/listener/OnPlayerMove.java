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
