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
