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
