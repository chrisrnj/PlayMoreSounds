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

package com.epicnicity322.playmoresounds.bukkit.util;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public abstract class UniqueRunnable implements Runnable
{
    private final @NotNull UUID uniqueId;

    public UniqueRunnable(@NotNull UUID uniqueId)
    {
        this.uniqueId = uniqueId;
    }

    public @NotNull UUID getUniqueId()
    {
        return uniqueId;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UniqueRunnable that = (UniqueRunnable) o;
        return uniqueId.equals(that.uniqueId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(uniqueId);
    }
}
