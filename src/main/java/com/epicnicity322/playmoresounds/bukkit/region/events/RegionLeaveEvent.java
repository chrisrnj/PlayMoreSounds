/*
 * PlayMoreSounds - A bukkit plugin that manages and plays sounds.
 * Copyright (C) 2021 Christiano Rangel
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

package com.epicnicity322.playmoresounds.bukkit.region.events;

import com.epicnicity322.playmoresounds.bukkit.region.SoundRegion;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * When a player leaves a PlayMoreSounds sound region.
 */
public class RegionLeaveEvent extends RegionEvent
{
    public RegionLeaveEvent(@NotNull SoundRegion region, @NotNull Player player, @NotNull Location from, @NotNull Location to)
    {
        super(region, from, to, player);
    }

    /**
     * @return true if the player did not leave the region.
     */
    @Override
    public boolean isCancelled()
    {
        return super.isCancelled();
    }

    /**
     * Prevents the player from leaving this region.
     *
     * @param cancelled If the player should not leave the region.
     */
    @Override
    public void setCancelled(boolean cancelled)
    {
        super.setCancelled(cancelled);
    }

    /**
     * @return The {@link SoundRegion} the player left.
     */
    public @NotNull SoundRegion getRegion()
    {
        return super.getRegion();
    }

    /**
     * @return The player who left the region.
     */
    public @NotNull Player getPlayer()
    {
        return super.getPlayer();
    }

    /**
     * @return The {@link Location} where the player was before leaving the region.
     */
    public @NotNull Location getFrom()
    {
        return super.getFrom();
    }

    /**
     * @return The {@link Location} where the player left the region.
     */
    public @NotNull Location getTo()
    {
        return super.getTo();
    }
}
