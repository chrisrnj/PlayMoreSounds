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

package com.epicnicity322.redprotectregions;

import br.net.fabiozumbi12.RedProtect.Bukkit.API.events.EnterExitRegionEvent;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import com.epicnicity322.epicpluginlib.bukkit.logger.Logger;
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.core.util.PathUtils;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.regionshandler.RegionsHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public final class RedProtectRegions extends PMSAddon implements Listener
{
    private RegionsHandler handler;

    @Override
    protected void onStart()
    {
        Logger logger = PlayMoreSounds.getConsoleLogger();

        if (!Bukkit.getPluginManager().isPluginEnabled("RedProtect")) {
            logger.log("[RedProtect Regions] Addon could not be started because RedProtect plugin failed to enable.", ConsoleLogger.Level.ERROR);
            PlayMoreSounds.getAddonManager().stopAddon(this);
            return;
        }

        handler = new RegionsHandler("RedProtect", this, new RegionsHandler.InsideChecker()
        {
            @Override
            protected boolean isPlayerInside(@NotNull Player player, @NotNull String regionId)
            {
                Region region = RedProtect.get().getAPI().getRegion(player.getLocation());
                if (region == null) return false;
                return region.getID().equals(regionId);
            }
        });

        // Adding example to regions.yml.
        if (!Configurations.REGIONS.getConfigurationHolder().getConfiguration().contains("RedProtect")) {
            String data = "\n\n# RedProtect Regions example:\n" +
                    "RedProtect:\n" +
                    "  # Create a section with the ID of the regions you want to add a sound.\n" +
                    "  # ATTENTION: RedProtect's region IDs are the region's name followed by the world's name, with a @ separating\n" +
                    "  #them. So if you want to play a sound for the region with name 'spawn' that is in the world 'world', you\n" +
                    "  #have to create a section like this:\n" +
                    "  spawn@world: #<region name>@<world name>\n" +
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
                    "  #the section to match your desired region ID. Region and world names are case sensitive, keep that in mind.\n" +
                    "  # Another example:\n" +
                    "  shop@TownWorld:\n" +
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
                logger.log("[RedProtect Regions] &eAdded example RedProtect region sound to regions.yml. Do /pms rl if you want to use it.");
            } catch (IOException e) {
                logger.log("[RedProtect Regions] Failed to add example to regions.yml configuration. You can find a tutorial on how to set region sounds in PlayMoreSounds forum: https://playmoresounds.freeforums.net/thread/16/redprotect-regions", ConsoleLogger.Level.WARN);
            }
        }
    }

    @EventHandler
    public void onEnterExitRegion(EnterExitRegionEvent event)
    {
        Region enterRegion = event.getEnteredRegion();
        Region exitRegion = event.getExitedRegion();
        Player player = event.getPlayer();

        if (enterRegion != null) {
            handler.onEnter(player, enterRegion.getID(), event.isCancelled());
        }
        if (exitRegion != null) {
            handler.onLeave(player, exitRegion.getID(), event.isCancelled());
        }
    }
}
