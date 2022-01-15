/*
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

package com.epicnicity322.worldguardregions;

import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.regionshandler.RegionsHandler;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class WorldGuardRegions extends PMSAddon implements Listener
{
    private static final @NotNull HashSet<WGRegionEnterEvent> playersInRegionWaitingToLoadResourcePack = new HashSet<>();
    private static RegionContainer container;
    private RegionsHandler handler;

    @Override
    protected void onStart()
    {
        container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        handler = new RegionsHandler("WorldGuard", this);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event)
    {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
            RegionManager manager = container.get(BukkitAdapter.adapt(from.getWorld()));
            Player player = event.getPlayer();

            if (manager != null)
                for (ProtectedRegion region : manager.getRegions().values()) {
                    boolean isInFrom = region.contains(from.getBlockX(), from.getBlockY(), from.getBlockZ());
                    boolean isInTo = region.contains(to.getBlockX(), to.getBlockY(), to.getBlockZ());

                    if (isInFrom & !isInTo)
                        handler.onLeave(player, region.getId(), event);
                    else if (!isInFrom & isInTo)
                        enter(player, region, from.getWorld());
                }
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event)
    {
        Location from = event.getFrom();
        Location to = event.getTo();
        Player player = event.getPlayer();

        try {
            if (from.getWorld() == to.getWorld()) {
                Collection<ProtectedRegion> regions = container.get(BukkitAdapter.adapt(from.getWorld())).getRegions().values();

                for (ProtectedRegion region : regions) {
                    boolean isInFrom = region.contains(from.getBlockX(), from.getBlockY(), from.getBlockZ());
                    boolean isInTo = region.contains(to.getBlockX(), to.getBlockY(), to.getBlockZ());

                    if (isInFrom & !isInTo)
                        handler.onLeave(player, region.getId(), event);
                    else if (!isInFrom & isInTo)
                        enter(player, region, from.getWorld());
                }
            } else {
                HashMap<ProtectedRegion, World> regions = new HashMap<>();

                container.get(BukkitAdapter.adapt(from.getWorld())).getRegions().values().forEach(region -> regions.put(region, from.getWorld()));
                container.get(BukkitAdapter.adapt(to.getWorld())).getRegions().values().forEach(region -> regions.put(region, to.getWorld()));

                for (Map.Entry<ProtectedRegion, World> regionAndWorld : regions.entrySet()) {
                    ProtectedRegion region = regionAndWorld.getKey();
                    boolean isInFrom = regionAndWorld.getValue().equals(from.getWorld()) && region.contains(from.getBlockX(), from.getBlockY(), from.getBlockZ());
                    boolean isInTo = regionAndWorld.getValue().equals(to.getWorld()) && region.contains(to.getBlockX(), to.getBlockY(), to.getBlockZ());

                    if (isInFrom & !isInTo)
                        handler.onLeave(player, region.getId(), event);
                    else if (!isInFrom & isInTo)
                        enter(player, region, regionAndWorld.getValue());
                }
            }
        } catch (NullPointerException ignored) {
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Location location = event.getPlayer().getLocation();
        World world = location.getWorld();
        RegionManager manager = container.get(BukkitAdapter.adapt(world));
        Player player = event.getPlayer();

        if (manager != null)
            for (ProtectedRegion region : manager.getRegions().values())
                if (region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
                    WGRegionEnterEvent regionEvent = new WGRegionEnterEvent(player, region, world);

                    // Checking if event should be added to playersInRegionWaitingToLoadResourcePack.
                    if (Configurations.CONFIG.getConfigurationHolder().getConfiguration().getBoolean("Resource Packs.Request").orElse(false)) {
                        playersInRegionWaitingToLoadResourcePack.add(regionEvent);
                    }

                    enter(regionEvent);
                }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event)
    {
        Location location = event.getPlayer().getLocation();
        RegionManager manager = container.get(BukkitAdapter.adapt(location.getWorld()));
        Player player = event.getPlayer();

        if (manager != null)
            for (ProtectedRegion region : manager.getRegions().values())
                if (region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                    handler.onLeave(player, region.getId(), null);
    }

    @EventHandler
    public void onPlayerResourcePackStatus(PlayerResourcePackStatusEvent event)
    {
        if (!Configurations.CONFIG.getConfigurationHolder().getConfiguration().getBoolean("Resource Packs.Request").orElse(false)) {
            return;
        }

        PlayerResourcePackStatusEvent.Status status = event.getStatus();
        Player player = event.getPlayer();

        if (status == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            HashSet<WGRegionEnterEvent> removedRegionEvents = new HashSet<>();

            playersInRegionWaitingToLoadResourcePack.removeIf(regionEvent -> {
                if (player.equals(regionEvent.player)) {
                    removedRegionEvents.add(regionEvent);
                    return true;
                }

                return false;
            });

            for (WGRegionEnterEvent regionEvent : removedRegionEvents) {
                enter(regionEvent);
            }
        }
    }

    private void enter(WGRegionEnterEvent event)
    {
        if (playersInRegionWaitingToLoadResourcePack.contains(event)) return;

        enter(event.player, event.region, event.regionWorld);
    }

    private void enter(Player player, ProtectedRegion region, World regionWorld)
    {
        handler.onEnter(player, region.getId(), () -> {
            Location loc = player.getLocation();
            RegionManager updatedManager = container.get(BukkitAdapter.adapt(regionWorld));

            return !region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()) ||
                    !loc.getWorld().equals(regionWorld) || !player.isOnline() || updatedManager == null ||
                    !updatedManager.hasRegion(region.getId());
        }, null);
    }

    private static class WGRegionEnterEvent
    {
        private final @NotNull Player player;
        private final @NotNull ProtectedRegion region;
        private final @NotNull World regionWorld;

        public WGRegionEnterEvent(@NotNull Player player, @NotNull ProtectedRegion region, @NotNull World regionWorld)
        {
            this.player = player;
            this.region = region;
            this.regionWorld = regionWorld;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WGRegionEnterEvent that = (WGRegionEnterEvent) o;
            return player.equals(that.player) && region.equals(that.region) && regionWorld.equals(that.regionWorld);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(player, region, regionWorld);
        }
    }
}
