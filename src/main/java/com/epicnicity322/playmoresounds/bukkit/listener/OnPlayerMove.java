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

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class OnPlayerMove implements Listener
{
    protected static void callRegionEnterLeaveEvents(Cancellable event, Player player, Location from, Location to)
    {
        Stream<SoundRegion> regions = RegionManager.getAllRegions().stream();
        Set<SoundRegion> fromRegions = regions.filter(region -> region.isInside(from)).collect(Collectors.toSet());
        Set<SoundRegion> toRegions = regions.filter(region -> region.isInside(to)).collect(Collectors.toSet());

        for (SoundRegion fromRegion : fromRegions) {
            if (!toRegions.contains(fromRegion)) {
                RegionLeaveEvent regionLeaveEvent = new RegionLeaveEvent(fromRegion, player, from, to);
                Bukkit.getPluginManager().callEvent(regionLeaveEvent);

                if (regionLeaveEvent.isCancelled())
                    event.setCancelled(true);
            }
        }

        for (SoundRegion toRegion : toRegions) {
            if (!fromRegions.contains(toRegion)) {
                RegionEnterEvent regionEnterEvent = new RegionEnterEvent(toRegion, player, from, to);
                Bukkit.getPluginManager().callEvent(regionEnterEvent);

                if (regionEnterEvent.isCancelled())
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event)
    {
        if (!event.isCancelled())
            callRegionEnterLeaveEvents(event, event.getPlayer(), event.getFrom(), event.getTo());


        //TODO: Run biome leave and enter sounds.
    }
}
