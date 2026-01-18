/*
 * PlayMoreSounds - A Minecraft plugin that manages and plays sounds.
 * Copyright (C) 2023-2026 Christiano Rangel
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A list containing multiple {@link PMSSound}s. The sounds are divided in groups to allow for randomization, when
 * there's more than one group, the group that plays sounds is chosen randomly.
 *
 * @param enabled     Whether the sound list should be played.
 * @param cancellable Whether the sound list should not play if the related event is cancelled.
 * @param soundGroups An immutable list containing lists of sounds to play, the list of sounds that will play is chosen randomly.
 */
public record PMSSoundList(boolean enabled, boolean cancellable, @NotNull List<List<PMSSound>> soundGroups) {
    /**
     * @throws NullPointerException If any of the lists has null elements.
     * @see PMSSoundList
     */
    public PMSSoundList {
        // Guarantee immutability.
        soundGroups = soundGroups.stream().map(List::copyOf).toList();
    }

    /**
     * Creates a sound list using a single flat list.
     * <p>
     * If random is enabled, this constructor only allows for one sound to be selected at play time. For multiple sounds,
     * you need to use {@link #PMSSoundList(boolean, boolean, List) the constructor that allows passing groups}.
     *
     * @param enabled     Whether the sound list should be played.
     * @param cancellable Whether the sound list should not play if the related event is cancelled.
     * @param random      If true, all sounds in the list will have individual groups, otherwise all sounds will share a single group.
     * @param sounds      The sounds of this PMSSoundList.
     * @throws NullPointerException If the list has null elements.
     * @see PMSSoundList
     */
    public PMSSoundList(boolean enabled, boolean cancellable, boolean random, @NotNull List<PMSSound> sounds) {
        this(enabled, cancellable,
                // If random is true with this constructor, every single sound must have its own group.
                random ? sounds.stream().map(List::of).toList() : List.of(sounds));
    }

    /**
     * Creates a sound list from the values of a configuration section.
     * <p>
     * The nodes read from the section are:
     * <ul>
     *     <li>Enabled</li>
     *     <li>Cancellable</li>
     *     <li>Random</li>
     *     <li>Sounds</li>
     * </ul>
     *
     * @param section The section to obtain the sound list from.
     * @throws java.util.NoSuchElementException            If a sound section is missing the 'Sound' key.
     * @throws net.kyori.adventure.key.InvalidKeyException If a sound contains a 'Sound' key with an invalid namespace value.
     */
    public PMSSoundList(@NotNull ConfigurationSection section) {
        this(section.getBoolean("Enabled").orElse(false), section.getBoolean("Cancellable").orElse(false),
                getSoundGroups(section.getConfigurationSection("Sounds"), section.getBoolean("Random").orElse(false)));
    }

    private static @NotNull List<List<PMSSound>> getSoundGroups(@Nullable ConfigurationSection soundsSection, boolean random) {
        if (soundsSection == null) return Collections.emptyList();

        Collection<Object> values = soundsSection.getNodes().values();

        if (random) {
            List<List<PMSSound>> soundGroups = new ArrayList<>(values.size());

            for (Object value : values) {
                if (!(value instanceof ConfigurationSection section)) continue;

                if (section.contains("Sound")) {
                    // Group with single sound.
                    soundGroups.add(List.of(new PMSSound(section)));
                } else {
                    // Group with multiple sounds.
                    soundGroups.add(getSounds(section));
                }
            }

            return soundGroups;
        } else {
            // No random, single group for all sounds.
            return List.of(getSounds(soundsSection));
        }
    }

    private static @NotNull List<PMSSound> getSounds(@NotNull ConfigurationSection soundsSection) {
        Collection<Object> values = soundsSection.getNodes().values();
        List<PMSSound> sounds = new ArrayList<>(values.size());

        for (Object value : values) {
            if (!(value instanceof ConfigurationSection section)) continue;
            sounds.add(new PMSSound(section));
        }
        return sounds;
    }

    /**
     * Whether the sounds from this sound list are being obtained through a randomized way.
     * <p>
     * This is tested by checking if {@code soundGroups.size() > 1}.
     *
     * @return true if this sound list is randomized.
     */
    public boolean random() {
        return soundGroups.size() > 1;
    }

    /**
     * Obtains a list of sounds to be played in this sound list.
     * <p>
     * If there is more than one sound group available, a random one is picked through
     * {@code random.nextInt(soundGroups.size())}.
     *
     * @return A list of sounds to be played immediately by this sound list.
     */
    public @NotNull List<PMSSound> sounds() {
        if (soundGroups.size() == 1) return soundGroups.get(0);
        else {
            if (soundGroups.isEmpty()) return Collections.emptyList();
            return soundGroups.get(ThreadLocalRandom.current().nextInt(soundGroups.size()));
        }
    }
}
