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
import com.flowpowered.math.vector.Vector3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public final class SoundManager
{
    /**
     * Gets all players inside a radius range.
     * <p>
     * Radius < -1 - All players in the world.
     * <p>
     * Radius < 0  - All players in the server.
     * <p>
     * Radius > 0  - All players in this range of blocks.
     * <p>
     * Radius = 0  - Empty.
     *
     * @param radius   The range of blocks to get the players.
     * @param location The location to calculate the radius.
     * @return An immutable collection of players in this range.
     */
    public static @NotNull Collection<Player> getInRange(double radius, @NotNull Location<World> location)
    {
        if (radius < -1) {
            return new HashSet<>(location.getExtent().getPlayers());
        } else if (radius < 0) {
            return new HashSet<>(Sponge.getServer().getOnlinePlayers());
        } else if (radius != 0) {
            HashSet<Player> players = new HashSet<>();

            for (Player player : location.getExtent().getPlayers()) {
                if (location.getPosition().distanceSquared(player.getPosition()) <= radius) {
                    players.add(player);
                }
            }

            return players;
        } else {
            return new HashSet<>();
        }
    }

    /**
     * Adds blocks to up, down, right, left, front, back from original sound location based on player's head rotation.
     */
    static @NotNull Vector3d addRelativeLocation(@NotNull Vector3d position, @Nullable Vector3d rotation, @NotNull Map<SoundOptions.Direction, Double> locationToAdd)
    {
        if (!locationToAdd.isEmpty() && rotation != null) {
            Double leftRight = locationToAdd.get(SoundOptions.Direction.LEFT_RIGHT);
            Double frontBack = locationToAdd.get(SoundOptions.Direction.FRONT_BACK);
            Double upDown = locationToAdd.get(SoundOptions.Direction.UP_DOWN);
            double sin = 0;
            double cos = 0;

            if (leftRight != null) {
                double angle = Math.PI * 2 * rotation.getY() / 360;
                sin = Math.sin(angle);
                cos = Math.cos(angle);

                position = position.add(leftRight * cos, 0.0, leftRight * sin);
            }

            if (frontBack != null) {
                if (leftRight == null) {
                    double angle = Math.PI * 2 * rotation.getY() / 360 * -1;
                    sin = Math.sin(angle);
                    cos = Math.cos(angle);
                } else {
                    sin = sin * -1;
                    cos = cos * -1;
                }

                position = position.add(frontBack * sin, 0.0, frontBack * cos);
            }

            if (upDown != null)
                position = position.add(0.0, upDown, 0.0);
        }

        return position;
    }
}
