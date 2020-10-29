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
import com.epicnicity322.epicpluginlib.core.util.ObjectUtils;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.region.RegionManager;
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
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

public class OnPlayerInteract implements Listener
{
    private static final @NotNull MessageSender lang = PlayMoreSounds.getMessageSender();
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

    // getItemInHand() is deprecated but is only used if you are running on older version of bukkit.
    @SuppressWarnings(value = "deprecation")
    // Other region plugins may cancel the event, so priority is set to high. If you wanted to play a sound on this event
    //you should use HIGHEST.
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.useInteractedBlock() != Event.Result.DENY) {
            Player player = event.getPlayer();

            if (player.hasPermission("playmoresounds.region.select.wand")) {
                ItemStack item;

                if (VersionUtils.hasOffHand())
                    item = player.getInventory().getItemInMainHand();
                else
                    item = player.getItemInHand();

                UUID uuid = player.getUniqueId();

                if (item.isSimilar(RegionManager.getWand())) {
                    Action action = event.getAction();

                    if (action.toString().endsWith("BLOCK")) {
                        Location clicked = event.getClickedBlock().getLocation();
                        Location[] diagonals;

                        if (selectedDiagonals.containsKey(uuid))
                            diagonals = selectedDiagonals.get(uuid);
                        else
                            diagonals = new Location[2];

                        event.setUseInteractedBlock(Event.Result.DENY);
                        event.setUseItemInHand(Event.Result.DENY);

                        if (!player.hasPermission("playmoresounds.region.select.overlap")) {
                            if (RegionManager.getAllRegions().stream().anyMatch(region ->
                                    !Objects.equals(region.getCreator(), player.getUniqueId()) && region.isInside(clicked))) {
                                lang.send(player, lang.get("Region.Select.Error.Overlap"));
                                return;
                            }
                        }

                        int i = 0;

                        if (action == Action.RIGHT_CLICK_BLOCK)
                            i = 1;

                        if (!Objects.equals(diagonals[i], clicked)) {
                            diagonals[i] = clicked;
                            selectedDiagonals.put(uuid, diagonals);
                            lang.send(player, lang.get("Region.Set.Select.Position." + (i == 0 ? "First" : "Second"))
                                    .replace("<w>", clicked.getWorld().getName())
                                    .replace("<x>", Integer.toString(clicked.getBlockX()))
                                    .replace("<y>", Integer.toString(clicked.getBlockY()))
                                    .replace("<z>", Integer.toString(clicked.getBlockZ())));
                        }
                    }
                }
            }
        }
    }
}
