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

package com.epicnicity322.playmoresounds.sponge.sound;

import com.epicnicity322.playmoresounds.core.sound.SoundOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;

public interface Playable
{
    /**
     * Plays a sound in a specific location. If the {@link SoundOptions#getRadius()} is lower than 0, the location is
     * only used to get the world to play sounds globally.
     * <p>
     * Sounds with radius 0 are ignored because there's no one to listen.
     *
     * @param sourceLocation The location where the sound will play.
     */
    default void play(@NotNull ServerLocation sourceLocation)
    {
        play(null, sourceLocation);
    }

    /**
     * Plays a sound to a specific player. Depending on {@link SoundOptions#getRadius()}, the sound may be played to
     * other players too.
     *
     * @param player The player to play the sound.
     */
    default void play(@NotNull ServerPlayer player)
    {
        play(player, player.serverLocation());
    }

    /**
     * Plays a sound to a specific player in a specific location. Depending on {@link SoundOptions#getRadius()}, the
     * sound may play to other players.
     *
     * @param player         The player to play the sound.
     * @param sourceLocation The location where the sound will play. May change depending on {@link SoundOptions#getRadius()}.
     */
    void play(@Nullable ServerPlayer player, @NotNull ServerLocation sourceLocation);
}