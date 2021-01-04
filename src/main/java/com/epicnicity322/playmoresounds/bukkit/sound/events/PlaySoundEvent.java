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

package com.epicnicity322.playmoresounds.bukkit.sound.events;

import com.epicnicity322.playmoresounds.bukkit.sound.Sound;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundOptions;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * This event is called for every player that hears a PlayMoreSounds sound. E.g., if a sound is played with -1 radius,
 * then this event is called for every people online in the server, because all of them hear the sound.
 *
 * @see PrePlaySoundEvent
 */
public class PlaySoundEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();
    private final @Nullable Player sourcePlayer;
    private final @NotNull Player player;
    private final @NotNull Collection<Player> otherListeners;
    private final @NotNull Location sourceLocation;
    private final @NotNull Sound sound;
    private boolean cancelled;
    private @NotNull Location location;

    public PlaySoundEvent(@NotNull Sound sound, @NotNull Player player, @NotNull Location location,
                          @NotNull Collection<Player> otherListeners, @Nullable Player sourcePlayer,
                          @NotNull Location sourceLocation)
    {
        this.sourcePlayer = sourcePlayer;
        this.player = player;
        this.location = location;
        this.otherListeners = otherListeners;
        this.sourceLocation = sourceLocation;
        this.sound = sound;
    }

    public static HandlerList getHandlerList()
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
    public void setCancelled(boolean value)
    {
        cancelled = value;
    }

    /**
     * Gets the player who played the sound.
     *
     * @return The player who played this sound, null if this sound was not played by a player.
     */
    public @Nullable Player getSourcePlayer()
    {
        return sourcePlayer;
    }

    /**
     * Gets the player who heard the sound.
     *
     * @return The player who is listening to this sound.
     */
    public @NotNull Player getPlayer()
    {
        return player;
    }

    /**
     * Gets all the other players that are hearing this sound.
     *
     * @return The players that are hearing this sound.
     */
    public @NotNull Collection<Player> getOtherListeners()
    {
        return otherListeners;
    }

    /**
     * Gets the location where the sound is played. This is the location where the source player played the sound.
     *
     * @return The location of the sound of the source player.
     */
    public @NotNull Location getSourceLocation()
    {
        return sourceLocation;
    }

    /**
     * Gets the location where the sound is played. This is the location the player that hears the sound will hear the sound
     *
     * @return The location of the sound of the hearing player.
     */
    public @NotNull Location getLocation()
    {
        return location;
    }

    /**
     * Changes the location where the sound of the hearing player will play.
     *
     * @param location The location you want to change to.
     * @throws IllegalArgumentException If the location is in a different world.
     */
    public void setLocation(@NotNull Location location)
    {
        if (!location.getWorld().equals(this.location.getWorld()))
            throw new IllegalArgumentException("Can't set location to a different world");

        this.location = location;
    }

    /**
     * Gets the instance used to play the sound. You can change the {@link SoundOptions} but they wont be taken to
     * account as this event is called after the options were applied.
     *
     * @return The instance of the sound.
     */
    public @NotNull Sound getSound()
    {
        return sound;
    }
}
