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

package com.epicnicity322.playmoresounds.bukkit.sound.events;

import com.epicnicity322.playmoresounds.bukkit.sound.PlayableSound;
import com.epicnicity322.playmoresounds.core.sound.SoundOptions;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * When a PlayMoreSounds's sound is about to play but isn't checked yet. This is called before the checks for
 * permission, location and toggle also before the delay is started.
 * You may use this to change the {@link SoundOptions} and check when a sound is about to be played.
 *
 * @see PlaySoundEvent
 */
public class PrePlaySoundEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();
    private final @Nullable Player player;
    private final @NotNull PlayableSound sound;
    private boolean cancelled;
    private @NotNull Location location;

    public PrePlaySoundEvent(@Nullable Player player, @NotNull Location location, @NotNull PlayableSound sound)
    {
        this.player = player;
        this.location = location;
        this.sound = sound;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

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
    public void setCancelled(boolean value)
    {
        cancelled = value;
    }

    /**
     * Gets the player who played the sound.
     *
     * @return The player who played the sound. Null if isn't a player-based sound.
     */
    public @Nullable Player getPlayer()
    {
        return player;
    }

    /**
     * Gets the location where the sound was requested to play.
     * <p>
     * Be aware that this is not the final location. The final location is calculated later when
     * {@link SoundOptions#getRelativeLocation()} are taken into account.
     *
     * @return The requested sound location.
     */
    public @NotNull Location getLocation()
    {
        return location;
    }

    /**
     * Changes the location where the sound was requested to play. If you want to set a precise location, you must
     * remove relativePositions and disable eyeLocation from sound options. To do that just use the method
     * {@link PrePlaySoundEvent#setPreciseLocation(Location)}.
     *
     * @param location The location you want to change to.
     * @see PrePlaySoundEvent#setPreciseLocation(Location)
     */
    public void setLocation(@NotNull Location location)
    {
        this.location = location;
    }

    /**
     * This changes the location where the sound should play precisely by removing Relative Location and Eye Location from
     * SoundOptions.
     *
     * @param preciseLocation The location you want to change to.
     */
    public void setPreciseLocation(@NotNull Location preciseLocation)
    {
        SoundOptions options = getSound().getOptions();

        options.setRelativeLocation(null);

        location = preciseLocation;
    }

    /**
     * Gets the instance used to play the sound. You may use this to change or check what are the sound properties. You
     * can also play the sound again using this instance, making this event be called over and over... It's your choice.
     *
     * @return The instance of the sound.
     */
    public @NotNull PlayableSound getSound()
    {
        return sound;
    }
}
