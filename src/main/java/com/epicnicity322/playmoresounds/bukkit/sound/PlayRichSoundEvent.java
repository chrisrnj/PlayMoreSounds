/*
 * PlayMoreSounds - A bukkit plugin that manages and plays sounds.
 * Copyright (C) 2022 Christiano Rangel
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

package com.epicnicity322.playmoresounds.bukkit.sound;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Called when a rich sound is played. Rich sounds can have multiple inner sounds.
 *
 * @see PlaySoundEvent
 */
public class PlayRichSoundEvent extends Event implements Cancellable {
    private static final @NotNull HandlerList handlers = new HandlerList();
    private final @Nullable Player player;
    private final @NotNull PlayableRichSound richSound;
    @NotNull Location location;
    private boolean cancelled;

    public PlayRichSoundEvent(@Nullable Player player, @NotNull Location location, @NotNull PlayableRichSound richSound) {
        this.player = player;
        this.location = location;
        this.richSound = richSound;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    /**
     * This is the source player. This player may be null because the sound may not be played by a player.
     *
     * @return The source player.
     */
    public @Nullable Player getSourcePlayer() {
        return player;
    }

    /**
     * This is NOT the final location of the sound. This is the source location, the location where the sound was
     * asked to play. But this location may change depending on the sound's radius.
     *
     * @return The source location of the sound.
     */
    public final @NotNull Location getLocation() {
        return location.clone();
    }

    /**
     * Sets the location of this sound, this may not be the final location of the sound depending if the sound's radius
     * is global.
     *
     * @param location The location the sound should play, if this is not a global sound.
     * @throws IllegalArgumentException If the new location is in a different world than the previous.
     */
    public void setLocation(@NotNull Location location) {
        if (!Objects.equals(this.location.getWorld(), location.getWorld()))
            throw new IllegalArgumentException("New location world is not the same as previous location's world.");

        this.location = location;
    }

    /**
     * The sound that will be played.
     *
     * @return The rich sound object that will play.
     */
    public @NotNull PlayableRichSound getRichSound() {
        return richSound;
    }
}
