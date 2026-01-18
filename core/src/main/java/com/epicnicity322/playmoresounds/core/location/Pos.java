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
 * A record composed by 3 doubles used for storing the values of a 3D coordinate.
 *
 * @param x X coordinate.
 * @param y Y coordinate.
 * @param z Z coordinate.
 */
public record Pos(double x, double y, double z) {
    public static @NotNull Pos ZERO = new Pos(0, 0, 0);

    /**
     * Gets the distance squared between two coordinates.
     *
     * @return The square of the number of blocks between the two points.
     */
    public static double distanceSquared(double x1, double y1, double z1, double x2, double y2, double z2) {
        return square(x1 - x2) + square(y1 - y2) + square(z1 - z2);
    }

    private static double square(double value) {
        return value * value;
    }

    /**
     * Adds blocks to the left, up and front relative to the entity's head position.
     *
     * @param relativePosition A Pos composed of the number to add to the left, up and front, respectively.
     * @param yaw              The yaw of the entity's head.
     * @return A new Pos with the relative position added.
     */
    public @NotNull Pos addRelativePosition(@NotNull Pos relativePosition, float yaw) {
        if (ZERO.equals(relativePosition)) return relativePosition;

        double radians = Math.toRadians(yaw);
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);

        double newX = x;
        double newY = y + relativePosition.y;
        double newZ = z;

        // Left/right
        newX += relativePosition.x * cos;
        newZ += relativePosition.x * sin;

        // Forward/backward
        newX += relativePosition.z * sin;
        newZ += relativePosition.z * cos;

        return new Pos(newX, newY, newZ);
    }

    /**
     * Gets the X of the chunk containing this coordinate in a world.
     *
     * @return The X coordinate of this coordinate's chunk.
     */
    public int chunkX() {
        return (int) x >> 4;
    }

    /**
     * Gets the Z of the chunk containing this coordinate in a world.
     *
     * @return The Z coordinate of this coordinate's chunk.
     */
    public int chunkZ() {
        return (int) z >> 4;
    }

    /**
     * Gets the distance squared of this coordinate to another coordinate. To get the distance in blocks, use
     * {@link Math#sqrt(double)} on the returned value.
     *
     * @param other The other coordinate to compare to.
     * @return The square of the number of blocks between the two points.
     */
    public double distanceSquared(@NotNull Pos other) {
        return distanceSquared(other.x, other.y, other.z);
    }

    /**
     * Gets the distance squared of this coordinate to another coordinate. To get the distance in blocks, use
     * {@link Math#sqrt(double)} on the returned value.
     *
     * @param x2 X coordinate.
     * @param y2 Y coordinate.
     * @param z2 Z coordinate.
     * @return The square of the number of blocks between the two points.
     */
    public double distanceSquared(double x2, double y2, double z2) {
        return distanceSquared(x, y, z, x2, y2, z2);
    }
}
