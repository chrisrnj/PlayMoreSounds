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

import com.epicnicity322.epicpluginlib.bukkit.logger.Logger;
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.core.util.PathUtils;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import com.epicnicity322.playmoresounds.core.config.Configurations;
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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;

public final class WorldGuardRegions extends PMSAddon implements Listener
{
    private static final @NotNull HashMap<World, com.sk89q.worldedit.world.World> cachedWorlds = new HashMap<>();
    private static RegionContainer container;
    private static RegionsHandler handler;

    @Override
    protected void onStart()
    {
        Logger logger = PlayMoreSounds.getConsoleLogger();

        if (!Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            logger.log("[WorldGuard Regions] Addon could not be started because WorldGuard plugin failed to enable.", ConsoleLogger.Level.ERROR);
            PlayMoreSounds.getAddonManager().stopAddon(this);
            return;
        }

        container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        handler = new RegionsHandler("WorldGuard", this, new RegionsHandler.InsideChecker()
        {
            @Override
            protected boolean isPlayerInside(@NotNull Player player, @NotNull String regionId)
            {
                RegionManager manager = getManager(player.getWorld());
                if (manager == null) return false;
                ProtectedRegion region = manager.getRegion(regionId);
                if (region == null) return false;
                Location loc = player.getLocation();

                return region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            }
        });

        // Adding example to regions.yml.
        if (!Configurations.REGIONS.getConfigurationHolder().getConfiguration().contains("WorldGuard")) {
            String data = "\n\n# WorldGuard Regions example:\n" +
                    "WorldGuard:\n" +
                    "  # Create a section with the region name, in this example we're using spawn.\n" +
                    "  spawn:\n" +
                    "    # Loop sound for spawn region.\n" +
                    "    Loop:\n" +
                    "      Enabled: true\n" +
                    "      # The time in ticks to wait before playing the sound again in loop.\n" +
                    "      Period: 100\n" +
                    "      # The delay to wait before playing the FIRST sound.\n" +
                    "      Delay: 0\n" +
                    "      # When a plugin makes so the player can't enter the region, the sound won't play.\n" +
                    "      Cancellable: true\n" +
                    "      # Prevents the default Region Enter sound set in sounds.yml from playing when someone enters this region.\n" +
                    "      Prevent Default Sound: true\n" +
                    "      Stop On Exit:\n" +
                    "        # If you set to play a long sound, it will stop from playing on exit.\n" +
                    "        Enabled: true\n" +
                    "        # The delay to wait before stopping the sound.\n" +
                    "        Delay: 0\n" +
                    "      Sounds:\n" +
                    "        '1':\n" +
                    "          # Your Sound Type or Custom Sound name here.\n" +
                    "          # NBS Song Player addon is also supported to play nbs songs.\n" +
                    "          Sound: BLOCK_NOTE_BLOCK_HAT\n" +
                    "          # Make sure to set a big volume so the sound is heard in the whole region.\n" +
                    "          # Volume 1 is heard as far as 15 blocks.\n" +
                    "          Volume: 1000\n" +
                    "    # Enter sound for spawn region. You can set both a welcoming sound and a looping sound to play\n" +
                    "    #at the same time, but Enter will only play once when someone enters the region.\n" +
                    "    Enter:\n" +
                    "      Enabled: true\n" +
                    "      Cancellable: true\n" +
                    "      Prevent Default Sound: true\n" +
                    "      Stop On Exit:\n" +
                    "        Enabled: true\n" +
                    "        Delay: 0\n" +
                    "      Sounds:\n" +
                    "        '1':\n" +
                    "          Sound: BLOCK_NOTE_BLOCK_PLING\n" +
                    "          Volume: 1000\n" +
                    "    # Leave sound for spawn region.\n" +
                    "    Leave:\n" +
                    "      Enabled: true\n" +
                    "      Cancellable: true\n" +
                    "      # Prevents the default Region Enter sound set in sounds.yml from playing when someone leaves this region.\n" +
                    "      Prevent Default Sound: true\n" +
                    "      Stop On Exit:\n" +
                    "        Enabled: true\n" +
                    "        Delay: 0\n" +
                    "      Sounds:\n" +
                    "        '1':\n" +
                    "          Sound: BLOCK_NOTE_BLOCK_PLING\n" +
                    "          Volume: 1000\n" +
                    "\n" +
                    "  # If you want to add sounds for another region, just copy the example above and paste below, make sure to rename\n" +
                    "  #the section to match your desired region name. Names are case sensitive, keep that in mind.\n" +
                    "  # Another example:\n" +
                    "  shop:\n" +
                    "    Enter:\n" +
                    "      Enabled: true\n" +
                    "      Sounds:\n" +
                    "        '1':\n" +
                    "          Sound: ENTITY_PLAYER_LEVELUP\n" +
                    "\n" +
                    "# PlayMoreSounds has much more options for sounds, like sound categories, delays and permissions. To see how to\n" +
                    "#use those options, visit the file sounds.yml.\n" +
                    "# The only option I advise to not use in region sounds is Radius, because it would make so players could hear\n" +
                    "#each others region enter/leave events.";

            try {
                PathUtils.write(data, Configurations.REGIONS.getConfigurationHolder().getPath());
                logger.log("[WorldGuard Regions] &eAdded example WorldGuard region sound to regions.yml. Do /pms rl if you want to use it.");
            } catch (IOException e) {
                logger.log("[WorldGuard Regions] Failed to add example to regions.yml configuration. You can find a tutorial on how to set region sounds in PlayMoreSounds forum: https://playmoresounds.freeforums.net/thread/15/worldguard-regions", ConsoleLogger.Level.WARN);
            }
        }
    }

    private @Nullable RegionManager getManager(World world)
    {
        com.sk89q.worldedit.world.World weWorld = cachedWorlds.get(world);

        if (weWorld == null) {
            weWorld = BukkitAdapter.adapt(world);
            cachedWorlds.put(world, weWorld);
        }

        return container.get(weWorld);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event)
    {
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

    private void delay(Runnable runnable)
    {
        Bukkit.getScheduler().runTaskLater(PlayMoreSounds.getInstance(), runnable, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event)
    {
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
    public void onPlayerJoin(PlayerJoinEvent event)
    {
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
    public void onPlayerLeave(PlayerQuitEvent event)
    {
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
