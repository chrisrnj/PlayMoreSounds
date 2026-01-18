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

package com.epicnicity322.playmoresounds.core.location;

import org.jetbrains.annotations.NotNull;

/**
 * A coordinate position in a world.
 *
 * @param world The given name of the world.
 * @param pos   The X, Y, Z coordinate.
 * @apiNote String is used instead of UUID because most platforms maintain a map with world name keys, and world object
 * values. This allows for O(1) lookup of the world object using its name.
 */
public record WorldPos(@NotNull String world, @NotNull Pos pos) {
    public WorldPos(@NotNull String world, double x, double y, double z) {
        this(world, new Pos(x, y, z));
    }
}
