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

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class SoundManager {
    private static final @NotNull HashMap<UUID, Boolean> soundStateCache = new HashMap<>();
    private static final Key<Value<Integer>> soundState = Key.from(ResourceKey.of("playmoresounds", "sound_state"), Integer.class);

    private SoundManager() {
    }

    public static void toggleSoundsState(@NotNull ServerPlayer player, boolean state) {
        var uuid = player.uniqueId();

        soundStateCache.put(uuid, state);
        player.tryOffer(soundState, state ? 1 : 0);
    }

    public static boolean getSoundsState(@NotNull ServerPlayer player) {
        var uuid = player.uniqueId();
        Boolean state = soundStateCache.get(uuid);

        if (state == null) {
            boolean persistentState = player.getInt(soundState).orElse(1) == 1;

            soundStateCache.put(uuid, persistentState);
            return persistentState;
        } else {
            return state;
        }
    }

    public static @NotNull Collection<ServerPlayer> getInRange(double radius, @NotNull ServerLocation location) {
        if (radius > 0) {
            radius = square(radius);
            var inRadius = new HashSet<ServerPlayer>();

            for (ServerPlayer player : location.world().players()) {
                if (distance(location, player.serverLocation()) <= radius) {
                    inRadius.add(player);
                }
            }

            return inRadius;
        } else if (radius == -1) {
            return Sponge.server().onlinePlayers();
        } else if (radius == -2) {
            return location.world().players();
        } else {
            return new HashSet<>();
        }
    }

    private static double distance(ServerLocation loc1, ServerLocation loc2) {
        return square(loc1.x() - loc2.x()) + square(loc1.y() - loc2.y()) + square(loc1.z() - loc2.z());
    }

    private static double square(double value) {
        return value * value;
    }
}
