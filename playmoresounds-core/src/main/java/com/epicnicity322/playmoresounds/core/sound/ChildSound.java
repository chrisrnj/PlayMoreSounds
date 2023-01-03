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

import com.epicnicity322.playmoresounds.core.util.PMSUtils;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ChildSound(@NotNull String sound, @NotNull SoundCategory category, float volume, float pitch,
                         @NotNull Options options) {
    public ChildSound {
        if (!PMSUtils.isNamespacedKey(sound)) {
            throw new IllegalArgumentException("Sound '" + sound + "' is not a valid namespaced key!");
        }
    }

    public ChildSound(@NotNull ConfigurationSection section) {
        this(section.getString("Sound").orElseThrow(), findCategory(section.getString("Category").orElse("MASTER")),
                findVolume(section.getNumber("Volume").orElse(10f).floatValue()), section.getNumber("Pitch").orElse(1f).floatValue(),
                findOptions(section.getConfigurationSection("Options")));
    }

    private static @NotNull SoundCategory findCategory(@NotNull String category) {
        try {
            return SoundCategory.valueOf(category);
        } catch (IllegalArgumentException e) {
            return SoundCategory.MASTER;
        }
    }

    private static float findVolume(float volume) {
        if (volume == -1f) return Float.MAX_VALUE;
        return volume;
    }

    private static @NotNull Options findOptions(@Nullable ConfigurationSection options) {
        if (options == null) return Options.DEFAULT_OPTIONS;
        return new Options(options);
    }
}
