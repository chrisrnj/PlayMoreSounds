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
