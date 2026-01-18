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

package com.epicnicity322.playmoresounds.core.util;

import com.epicnicity322.playmoresounds.core.location.Pos;
import com.epicnicity322.playmoresounds.core.location.WorldPos;
import com.epicnicity322.playmoresounds.core.sound.Emitter;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public interface PlayerUtil<P, E extends Sound.Emitter> {

    /**
     * Gets a collection with all players in the specified world.
     *
     * @param world The name of the world.
     * @return A collection with all players in the world.
     */
    @NotNull Collection<? extends P> worldPlayers(@NotNull String world);

    /**
     * Gets a collection with all online players in the server.
     *
     * @return A collection with all online players.
     */
    @NotNull Collection<? extends P> onlinePlayers();

    /**
     * Gets a collection of players that are in range from the provided coordinates.
     *
     * @param radius The number in blocks to calculate the range.
     * @param pos    The position to calculate the range.
     * @return A collection with all players in range of the location, or an empty collection in case there is none.
     */
    @NotNull Collection<? extends P> playersInRange(double radius, @NotNull WorldPos pos);

    /**
     * Tests whether the player has the specified permission.
     *
     * @param player     The player to check the permission.
     * @param permission The permission to be checked.
     * @return Whether the player is allowed the permission.
     */
    boolean hasPermission(@NotNull P player, @NotNull String permission);

    /**
     * Tests whether the player is in spectator game mode.
     *
     * @param player The player to check the game mode.
     * @return Whether the player is a spectator.
     */
    boolean isSpectator(@NotNull P player);

    /**
     * Tests whether the player has invisibility potion effect.
     *
     * @param player The player to check potion effects.
     * @return Whether the player has invisibility.
     */
    boolean hasInvisibility(@NotNull P player);

    /**
     * Tests whether the first player can see the second one. Useful for testing vanish status of players.
     *
     * @param player1 The player that will see.
     * @param player2 The player to be seen.
     * @return Whether the first player sees the second.
     */
    boolean canSee(@NotNull P player1, @NotNull P player2);

    /**
     * Gets the current world and coordinates of an entity.
     *
     * @param entity The entity to get the location.
     * @return The world position of an entity.
     */
    @NotNull WorldPos wPos(@NotNull E entity);

    /**
     * Gets the current coordinates of a player.
     *
     * @param player The player to get the location.
     * @return The coordinates of a player.
     */
    @NotNull Pos pos(@NotNull P player);

    /**
     * Gets the yaw, aka the head facing coordinate of a player.
     *
     * @param player The player to get the yaw.
     * @return The yaw of this player.
     */
    default float yaw(@NotNull P player) {
        return yaw(asEntity(player));
    }

    /**
     * Gets the yaw, aka the head facing coordinate of an entity.
     *
     * @param entity The entity to get the yaw.
     * @return The yaw of this entity.
     */
    float yaw(@NotNull E entity);

    @NotNull UUID uuid(@NotNull P player);

    /**
     * Gets the sound emitter for the entity generic using a player argument.
     *
     * @param player The player to get the emitter from.
     * @return An emitter for this player, using the entity generic.
     */
    default @NotNull Emitter<E> asEmitter(@NotNull P player) {
        return Emitter.follow(asEntity(player));
    }

    /**
     * Gets the entity generic from the provided player argument.
     *
     * @param player The player to get as entity.
     * @return An entity instance of this player.
     */
    @NotNull E asEntity(@NotNull P player);

    /**
     * Saves a sound toggle state on a player's persistent data.
     *
     * @param dataHolder The entity holding the persistent data container.
     * @param value      The state to be saved.
     */
    void saveStateOnPersistentData(@NotNull P dataHolder, boolean value);

    /**
     * Reads the sound toggle state from a player's persistent data.
     *
     * @param dataHolder The entity holding the persistent data container.
     * @return The boolean saved on persistent data, null if not available.
     */
    @Nullable Boolean getStateOnPersistentData(@NotNull P dataHolder);
}
