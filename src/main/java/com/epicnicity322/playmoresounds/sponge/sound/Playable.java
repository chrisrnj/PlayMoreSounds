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

package com.epicnicity322.playmoresounds.sponge.sound;

import com.epicnicity322.playmoresounds.core.sound.SoundOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public interface Playable
{
    /**
     * Plays a sound in a specific location. If your {@link SoundOptions#getRadius()} is 0, then the sound is not played.
     *
     * @param sourceLocation The location where the sound will play. May change depending on {@link SoundOptions#getRelativeLocation()}.
     */
    default void play(@NotNull Location<World> sourceLocation)
    {
        play(null, sourceLocation);
    }

    /**
     * Plays a sound to a specific player. Depending on {@link SoundOptions#getRadius()}, the sound may play to
     * other players too.
     *
     * @param player The player to play the sound.
     */
    default void play(@NotNull Player player)
    {
        play(player, player.getLocation());
    }

    /**
     * Plays a sound to a specific player in a specific location. Depending on {@link SoundOptions#getRadius()}, the
     * sound may play to other players too.
     *
     * @param player         The player to play the sound.
     * @param sourceLocation The location where the sound will play. May change depending on {@link SoundOptions#getRelativeLocation()}.
     */
    void play(@Nullable Player player, @NotNull Location<World> sourceLocation);
}
