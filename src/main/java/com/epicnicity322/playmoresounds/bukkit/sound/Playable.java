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

package com.epicnicity322.playmoresounds.bukkit.sound;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Playable
{
    /**
     * Plays a sound in a specific location. If your {@link SoundOptions#getRadius()} is 0, then the sound is not played.
     *
     * @param sourceLocation The location where the sound will play. May change depending on {@link SoundOptions#getRelativeLocation()}.
     * @throws IllegalStateException If the sound has a delay and PlayMoreSounds was not instantiated by bukkit yet.
     */
    default void play(@NotNull Location sourceLocation)
    {
        play(null, sourceLocation);
    }

    /**
     * Plays a sound to a specific player. Depending on {@link SoundOptions#getRadius()}, the sound may be played to
     * other players too.
     *
     * @param player The player to play the sound.
     * @throws IllegalStateException If the sound has a delay and PlayMoreSounds was not instantiated by bukkit yet.
     */
    default void play(@NotNull Player player)
    {
        play(player, player.getLocation());
    }

    /**
     * Plays a sound to a specific player in a specific location. Depending on {@link SoundOptions#getRadius()}, the
     * sound may play to other players.
     *
     * @param player         The player to play the sound.
     * @param sourceLocation The location where the sound will play. May change depending on {@link SoundOptions#getRelativeLocation()}.
     * @throws IllegalStateException If the sound has a delay and PlayMoreSounds was not instantiated by bukkit yet.
     */
    void play(@Nullable Player player, @NotNull Location sourceLocation);
}
