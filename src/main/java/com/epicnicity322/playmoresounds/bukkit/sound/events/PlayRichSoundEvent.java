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

package com.epicnicity322.playmoresounds.bukkit.sound.events;

import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.core.sound.SoundOptions;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a config sound is played. Rich sounds can only be played through configuration sections.
 *
 * @see HearSoundEvent
 */
public class PlayRichSoundEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();
    private final @Nullable Player player;
    private final @NotNull PlayableRichSound richSound;
    private boolean cancelled;
    private @NotNull Location location;

    public PlayRichSoundEvent(@Nullable Player player, @NotNull Location location, @NotNull PlayableRichSound richSound)
    {
        this.player = player;
        this.location = location;
        this.richSound = richSound;
    }

    public static HandlerList getHandlerList()
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

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return handlers;
    }

    /**
     * This is the source player. This player may be null because the sound may not be played by a player.
     *
     * @return The source player.
     */
    public @Nullable Player getPlayer()
    {
        return player;
    }

    /**
     * This is NOT the final location of the sound. This is the source location, the location where the sound was
     * asked to play. But this location may change depending on radius and on {@link SoundOptions}.
     *
     * @return The source location of the sound.
     */
    public @NotNull Location getLocation()
    {
        return location;
    }

    /**
     * This won't be the final location of the sound. This is the source location, the location where the sound was
     * asked to play. But this location may change depending on radius and on {@link SoundOptions}.
     *
     * @param location The location that you want the sound to take as source.
     */
    public void setLocation(@NotNull Location location)
    {
        this.location = location;
    }

    /**
     * This is the instance of the configuration sound as object. You may use this to check which section was used to play
     * the sound and change the sounds that will play.
     *
     * @return The rich sound object that will play.
     */
    public @NotNull PlayableRichSound getRichSound()
    {
        return richSound;
    }
}
