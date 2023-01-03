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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record Sound(boolean enabled, boolean cancellable, @NotNull List<ChildSound> childSounds) {
    public Sound(@NotNull ConfigurationSection section) {
        this(section.getBoolean("Enabled").orElse(false), section.getBoolean("Cancellable").orElse(false), getChildSounds(section.getConfigurationSection("Sounds")));
    }

    @NotNull
    private static List<ChildSound> getChildSounds(@Nullable ConfigurationSection sounds) {
        if (sounds == null) return Collections.emptyList();

        ArrayList<ChildSound> childSounds = new ArrayList<>();

        sounds.getNodes().forEach((name, section) -> {
            if (section instanceof ConfigurationSection childSection) {
                childSounds.add(new ChildSound(childSection));
            }
        });

        return Collections.unmodifiableList(childSounds);
    }
}
