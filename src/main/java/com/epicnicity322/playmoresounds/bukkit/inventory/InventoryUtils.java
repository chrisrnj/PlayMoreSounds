/*
 * PlayMoreSounds - A bukkit plugin that manages and plays sounds.
 * Copyright (C) 2021 Christiano Rangel
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

package com.epicnicity322.playmoresounds.bukkit.inventory;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class InventoryUtils
{
    private static @Nullable NamespacedKey button;

    static {
        PlayMoreSounds.addOnInstanceRunnable(plugin -> button = new NamespacedKey(plugin, "button"));
    }

    private InventoryUtils()
    {
    }

    public static @NotNull NamespacedKey getButton()
    {
        if (button == null)
            throw new IllegalStateException("PlayMoreSounds is not loaded.");

        return button;
    }
}
