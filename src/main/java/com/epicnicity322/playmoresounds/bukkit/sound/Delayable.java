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

package com.epicnicity322.playmoresounds.bukkit.sound;

import com.epicnicity322.playmoresounds.core.sound.SoundOptions;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface Delayable extends Playable {
    @Override
    default void play(@Nullable Player player, @NotNull Location sourceLocation) {
        playDelayable(player, sourceLocation);
    }

    default @NotNull PlayResult<?> playDelayable(@NotNull Location sourceLocation) {
        return playDelayable(null, sourceLocation);
    }

    default @NotNull PlayResult<?> playDelayable(@NotNull Player player) {
        return playDelayable(player, player.getLocation());
    }

    /**
     * Plays a sound to a specific player in a specific location. Depending on {@link SoundOptions#getRadius()}, the
     * sound may play to other players.
     *
     * @param player         The player to play the sound.
     * @param sourceLocation The location where the sound will play.
     * @return A {@link BukkitTask} if the sound was tasked to be played with a delay greater than 0.
     */
    @NotNull PlayResult<?> playDelayable(@Nullable Player player, @NotNull Location sourceLocation);

    interface PlayResult<T> {
        @NotNull T get();
    }

    record ChildPlayResult(@NotNull Collection<Player> listeners,
                           @Nullable BukkitTask delayedTask) implements PlayResult<ChildPlayResult> {
        public @NotNull ChildPlayResult get() {
            return this;
        }
    }

    record RichPlayResult(@NotNull Collection<Player> listeners,
                          @Nullable List<BukkitTask> delayedTasks) implements PlayResult<RichPlayResult> {
        public @NotNull RichPlayResult get() {
            return this;
        }
    }
}
