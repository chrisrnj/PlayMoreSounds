/*
 * PlayMoreSounds - A Minecraft plugin that manages and plays sounds.
 * Copyright (C) 2026 Christiano Rangel
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

package com.epicnicity322.playmoresounds.core.sound;

import com.epicnicity322.playmoresounds.core.location.Pos;
import com.epicnicity322.playmoresounds.core.location.WorldPos;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * An emitter is an entity on which the sound will follow its position.
 * <p>
 * This class is useful for specifying entity emitters, or a static location on which the sound will play.
 *
 * @param <E> An entity that extends {@link Sound.Emitter}
 */
public record Emitter<E extends Sound.Emitter>(WorldPos location, E emitter) {

    @Contract("null,null -> fail")
    public Emitter {
        if (location == null && emitter == null) throw new NullPointerException("No emitter provided.");
    }

    /**
     * Specify a static location to play the sound.
     *
     * @param location The location the sound will be played.
     * @return An emitter with a static location.
     */
    public static <E extends Sound.Emitter> @NotNull Emitter<E> loc(@NotNull WorldPos location) {
        return new Emitter<>(location, null);
    }

    /**
     * Specify a static location to play the sound.
     *
     * @param world The name of the world.
     * @param x     X coordinate.
     * @param y     Y coordinate.
     * @param z     Z coordinate.
     * @return An emitter with a static location.
     */
    public static <E extends Sound.Emitter> @NotNull Emitter<E> loc(@NotNull String world, double x, double y, double z) {
        return new Emitter<>(new WorldPos(world, new Pos(x, y, z)), null);
    }

    /**
     * Specify an entity to follow while playing the sound.
     *
     * @param emitter The entity to follow.
     * @param <E>     An entity that extends {@link Sound.Emitter}
     * @return An emitter with an entity to follow.
     */
    public static <E extends Sound.Emitter> @NotNull Emitter<E> follow(@NotNull E emitter) {
        return new Emitter<>(null, emitter);
    }
}
