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

package com.epicnicity322.playmoresounds.core.util;

import java.util.HashSet;

/**
 * A {@link HashSet} you can use to tell if it was populated or not.
 *
 * @param <E> The element of the {@link HashSet}.
 */
public class LoadableHashSet<E> extends HashSet<E>
{
    private boolean loaded = false;

    /**
     * @return If this set was already populated.
     */
    public synchronized boolean isLoaded()
    {
        return loaded;
    }

    /**
     * Set if this set is populated.
     */
    public synchronized void setLoaded(boolean loaded)
    {
        this.loaded = loaded;
    }
}
