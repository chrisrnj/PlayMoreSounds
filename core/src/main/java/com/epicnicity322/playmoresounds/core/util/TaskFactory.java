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

import com.epicnicity322.playmoresounds.core.location.WorldPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TaskFactory<E> {
    @NotNull Task delayedGlobal(@NotNull Runnable runnable, long delay);

    @NotNull Task delayedLocal(@NotNull WorldPos worldPos, @NotNull Runnable runnable, long delay);

    default @NotNull Task delayedFor(@NotNull E entity, @NotNull Runnable runnable, long delay) {
        return delayedFor(entity, runnable, null, delay);
    }

    @NotNull Task delayedFor(@NotNull E entity, @NotNull Runnable runnable, @Nullable Runnable retired, long delay);

    interface Task {
        void cancel();
    }
}
