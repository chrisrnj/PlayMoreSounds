package com.epicnicity322.playmoresounds.bukkit.region.events;

import com.epicnicity322.playmoresounds.bukkit.region.SoundRegion;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RegionEnterEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();
    private Location from;
    private Location to;
    private Player p;
    private SoundRegion region;
    private boolean isCancelled;

    public RegionEnterEvent(SoundRegion region, Location from, Location to, Player p)
    {
        this.region = region;
        this.from = from;
        this.to = to;
        this.p = p;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    @Override
    public boolean isCancelled()
    {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean p1)
    {
        isCancelled = p1;
    }

    /**
     * Returns the player who entered the region.
     */
    public Player getPlayer()
    {
        return p;
    }

    /**
     * Returns the SoundRegion that the player entered.
     */
    public SoundRegion getRegion()
    {
        return region;
    }

    /**
     * Returns the Location that the player was before entering the region.
     */
    public Location getFrom()
    {
        return from;
    }

    /**
     * Returns the Location of the player that entered the region.
     */
    public Location getTo()
    {
        return to;
    }
}
