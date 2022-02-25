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

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import com.epicnicity322.regionshandler.RegionsHandler;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class WorldGuardRegions extends PMSAddon implements Listener {
    private static final @NotNull HashMap<World, com.sk89q.worldedit.world.World> cachedWorlds = new HashMap<>();
    private static RegionContainer container;
    private RegionsHandler handler;

    @Override
    protected void onStart() {
        container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        handler = new RegionsHandler("WorldGuard", this, new RegionsHandler.InsideChecker() {
            @Override
            protected boolean isPlayerInside(@NotNull Player player, @NotNull String regionId) {
                RegionManager manager = getManager(player.getWorld());
                if (manager == null) return false;
                ProtectedRegion region = manager.getRegion(regionId);
                if (region == null) return false;
                Location loc = player.getLocation();

                return region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            }
        });
    }

    private @Nullable RegionManager getManager(World world) {
        com.sk89q.worldedit.world.World weWorld = cachedWorlds.get(world);

        if (weWorld == null) {
            weWorld = BukkitAdapter.adapt(world);
            cachedWorlds.put(world, weWorld);
        }

        return container.get(weWorld);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null) return;
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ())
            return;

        RegionManager manager = getManager(from.getWorld());

        if (manager == null) return;

        for (ProtectedRegion region : manager.getRegions().values()) {
            Player player = event.getPlayer();
            boolean isInFrom = region.contains(from.getBlockX(), from.getBlockY(), from.getBlockZ());
            boolean isInTo = region.contains(to.getBlockX(), to.getBlockY(), to.getBlockZ());

            if (isInFrom & !isInTo)
                handler.onLeave(player, region.getId(), event.isCancelled());
            else if (!isInFrom & isInTo)
                handler.onEnter(player, region.getId(), event.isCancelled());
        }
    }

    private void delay(Runnable runnable) {
        Bukkit.getScheduler().runTaskLater(PlayMoreSounds.getInstance(), runnable, 1);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null) return;

        World fromWorld = from.getWorld();
        World toWorld = to.getWorld();
        Player player = event.getPlayer();
        boolean cancelled = event.isCancelled();

        if (fromWorld == toWorld) {
            RegionManager manager = getManager(fromWorld);
            if (manager == null) return;

            for (ProtectedRegion region : manager.getRegions().values()) {
                boolean isInFrom = region.contains(from.getBlockX(), from.getBlockY(), from.getBlockZ());
                boolean isInTo = region.contains(to.getBlockX(), to.getBlockY(), to.getBlockZ());

                if (isInFrom & !isInTo)
                    delay(() -> handler.onLeave(player, region.getId(), cancelled));
                else if (!isInFrom & isInTo)
                    delay(() -> handler.onEnter(player, region.getId(), cancelled));
            }
        } else {
            RegionManager fromManager = getManager(fromWorld);

            if (fromManager != null) {
                for (ProtectedRegion fromRegion : fromManager.getRegions().values()) {
                    if (fromRegion.contains(from.getBlockX(), from.getBlockY(), from.getBlockZ())) {
                        delay(() -> handler.onLeave(player, fromRegion.getId(), cancelled));
                    }
                }
            }

            RegionManager toManager = getManager(toWorld);

            if (toManager != null) {
                for (ProtectedRegion toRegion : toManager.getRegions().values()) {
                    if (toRegion.contains(to.getBlockX(), to.getBlockY(), to.getBlockZ())) {
                        delay(() -> handler.onEnter(player, toRegion.getId(), cancelled));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();
        RegionManager manager = getManager(loc.getWorld());

        if (manager == null) return;
        for (ProtectedRegion region : manager.getRegions().values()) {
            if (region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
                handler.onEnter(player, region.getId());
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();
        RegionManager manager = getManager(loc.getWorld());

        if (manager == null) return;
        for (ProtectedRegion region : manager.getRegions().values()) {
            if (region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
                handler.onLeave(player, region.getId());
            }
        }
    }
}
