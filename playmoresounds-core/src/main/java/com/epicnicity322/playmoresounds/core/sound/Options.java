/*
 * PlayMoreSounds - A minecraft plugin that manages and plays sounds.
 * Copyright (C) 2023 Christiano Rangel
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

package com.epicnicity322.playmoresounds.core.sound;

import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Options(int delay, double radius, boolean ignoreToggle, @Nullable String permissionRequired,
                      @Nullable String permissionToListen) {
    public static final @NotNull Options DEFAULT_OPTIONS = new Options(0, 0.0, false, null, null);

    public Options(@NotNull ConfigurationSection section) {
        this(section.getNumber("Delay").orElse(0).intValue(), section.getNumber("Radius").orElse(0.0).doubleValue(), section.getBoolean("Ignore Toggle").orElse(false),
                section.getString("Permission Required").orElse(null), section.getString("Permission To Listen").orElse(null));
    }
}
