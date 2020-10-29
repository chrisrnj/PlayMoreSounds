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
