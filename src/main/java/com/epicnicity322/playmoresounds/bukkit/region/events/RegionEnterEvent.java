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
 * When a player enters a PlayMoreSounds sound region.
 */
public class RegionEnterEvent extends RegionEvent
{
    public RegionEnterEvent(@NotNull SoundRegion region, @NotNull Player player, @NotNull Location from, @NotNull Location to)
    {
        super(region, from, to, player);
    }

    /**
     * @return true if the player did not enter this region.
     */
    @Override
    public boolean isCancelled()
    {
        return super.isCancelled();
    }

    /**
     * Prevents the player from entering this region.
     *
     * @param cancelled If the player should not enter the region.
     */
    @Override
    public void setCancelled(boolean cancelled)
    {
        super.setCancelled(cancelled);
    }

    /**
     * @return The {@link SoundRegion} the player entered.
     */
    public @NotNull SoundRegion getRegion()
    {
        return super.getRegion();
    }

    /**
     * @return The player who entered the region.
     */
    public @NotNull Player getPlayer()
    {
        return super.getPlayer();
    }

    /**
     * @return The {@link Location} where the player was before entering the region.
     */
    public @NotNull Location getFrom()
    {
        return super.getFrom();
    }

    /**
     * @return The {@link Location} where the player entered the region.
     */
    public @NotNull Location getTo()
    {
        return super.getTo();
    }
}
