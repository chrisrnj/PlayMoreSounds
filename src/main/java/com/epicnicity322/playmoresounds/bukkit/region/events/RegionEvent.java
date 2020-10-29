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
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RegionEvent extends Event implements Cancellable
{
    private static final @NotNull HandlerList handlers = new HandlerList();
    private final @NotNull SoundRegion region;
    private final @NotNull Player player;
    private final @NotNull Location from;
    private final @NotNull Location to;
    private boolean cancelled;

    protected RegionEvent(@NotNull SoundRegion region, @NotNull Location from, @NotNull Location to, @NotNull Player player)
    {
        this.region = region;
        this.from = from;
        this.to = to;
        this.player = player;
    }

    public static @NotNull HandlerList getHandlerList()
    {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return handlers;
    }

    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }

    public @NotNull SoundRegion getRegion()
    {
        return region;
    }

    public @NotNull Player getPlayer()
    {
        return player;
    }

    public @NotNull Location getFrom()
    {
        return from;
    }

    public @NotNull Location getTo()
    {
        return to;
    }
}
