/*
 * PlayMoreSounds - A bukkit plugin that manages and plays sounds.
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

package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.epicpluginlib.core.util.ObjectUtils;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.region.RegionManager;
import com.epicnicity322.playmoresounds.bukkit.region.SoundRegion;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public record OnPlayerInteract() implements Listener
{
    private static final @NotNull HashMap<UUID, Location[]> selectedDiagonals = new HashMap<>();
    private static final @NotNull UUID console = UUID.randomUUID();

    /**
     * Gets the selected diagonals from this player.
     *
     * @param player The uuid of the player you want to get the selections from.
     * @return The locations the player selected or null if this player didn't select any location.
     */
    public static @Nullable Location[] getSelectedDiagonals(@Nullable UUID player)
    {
        return selectedDiagonals.get(ObjectUtils.getOrDefault(player, console));
    }

    /**
     * Sets a player selection to the specified location.
     *
     * @param player   The player to set or null if you want to set to console.
     * @param location The location to set the selection or null to remove the selection.
     * @param diagonal If this is the first diagonal. Set to false if you want to set the second.
     */
    public static void selectDiagonal(@Nullable UUID player, @Nullable Location location, boolean diagonal)
    {
        Location[] selection = getSelectedDiagonals(player);

        if (selection == null)
            selection = new Location[2];

        selection[diagonal ? 0 : 1] = location;
        selectedDiagonals.put(ObjectUtils.getOrDefault(player, console), selection);
    }

    // Other region plugins may cancel the event, so priority is set to high. If you wanted to play a sound on this event
    //you should use HIGHEST.
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if ((event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK)
                || event.useInteractedBlock() == Event.Result.DENY) return;

        Player player = event.getPlayer();

        if (!player.hasPermission("playmoresounds.region.select.wand")) return;

        ItemStack item = event.getItem();

        if (item == null || !item.isSimilar(RegionManager.getWand())) return;

        MessageSender lang = PlayMoreSounds.getLanguage();
        UUID uuid = player.getUniqueId();
        Location clicked = event.getClickedBlock().getLocation();

        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);

        if (!player.hasPermission("playmoresounds.region.select.overlap")) {
            for (SoundRegion region : RegionManager.getRegions()) {
                if (!Objects.equals(region.getCreator(), uuid) && region.isInside(clicked)) {
                    lang.send(player, lang.get("Region.Select.Error.Overlap"));
                    return;
                }
            }
        }

        Location[] diagonals = selectedDiagonals.computeIfAbsent(uuid, k -> new Location[2]);

        int i = 0;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
            i = 1;

        if (!Objects.equals(diagonals[i], clicked)) {
            diagonals[i] = clicked;
            lang.send(player, lang.get("Region.Set.Select.Position." + (i == 0 ? "First" : "Second"))
                    .replace("<w>", clicked.getWorld().getName())
                    .replace("<x>", Integer.toString(clicked.getBlockX()))
                    .replace("<y>", Integer.toString(clicked.getBlockY()))
                    .replace("<z>", Integer.toString(clicked.getBlockZ())));
        }
    }
}
