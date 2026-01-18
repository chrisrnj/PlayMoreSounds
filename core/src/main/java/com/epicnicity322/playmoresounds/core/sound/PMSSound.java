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

import com.epicnicity322.playmoresounds.core.location.Pos;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import io.soabase.recordbuilder.core.RecordBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A sound from PlayMoreSounds, containing multiple options to be tweaked before play time.
 *
 * @param sound   The adventure API sound.
 * @param options The options of how this sound should be played.
 */
@RecordBuilder
public record PMSSound(@NotNull Sound sound, @NotNull PMSSound.Options options) {
    /**
     * Creates a sound from the values of a configuration section.
     * <p>
     * The nodes read from this section are:
     * <ul>
     *     <li>Sound</li>
     *     <li>Volume</li>
     *     <li>Pitch</li>
     *     <li>Category</li>
     *     <li>Options</li>
     * </ul>
     * <p>
     * All values except 'Sound' are optional, using default values when absent.
     *
     * @param section The section to obtain the sound properties from.
     * @throws java.util.NoSuchElementException            If 'Sound' key is not present.
     * @throws net.kyori.adventure.key.InvalidKeyException If 'Sound' key has an invalid namespace value.
     */
    public PMSSound(@NotNull ConfigurationSection section) {
        this(Sound.sound()
                        .type(findSoundKey(section))
                        .volume(findVolume(section.getNumber("Volume").orElse(1).floatValue()))
                        .pitch(section.getNumber("Pitch").orElse(1).floatValue())
                        .source(findCategory(section.getString("Category").orElse("MASTER")))
                        .build(),
                findOptions(section.getConfigurationSection("Options")));
    }

    @SuppressWarnings("PatternValidation") // let it throw
    @NotNull
    private static Key findSoundKey(@NotNull ConfigurationSection section) {
        String value = section.getString("Sound").orElseThrow();

        try {
            value = SoundKey.valueOf(value).onCurrentVersion();
        } catch (IllegalArgumentException ignored) {
        }

        return Key.key(value);
    }

    @NotNull
    private static Sound.Source findCategory(@NotNull String category) {
        return Objects.requireNonNullElse(Sound.Source.NAMES.value(category), Sound.Source.MASTER);
    }

    private static float findVolume(float volume) {
        if (volume < 0) return Float.MAX_VALUE;
        return volume;
    }

    @NotNull
    private static PMSSound.Options findOptions(@Nullable ConfigurationSection options) {
        if (options == null) return Options.DEFAULT;
        return new Options(options);
    }

    /**
     * Extra options attributed to sounds. Not necessarily have to do with the sounds themselves, but how they're played.
     *
     * @param delay              The delay in ticks to wait before playing the sound.
     * @param radius             The radius in which other players will have the sound played to.
     * @param ignoreToggleState  Whether to ignore the '/pms toggle' state when playing this sound.
     * @param permissionToPlay   The permission a player needs to play the sound.
     * @param permissionToListen The permission a player needs in order to listen to the played sound.
     * @param relativePosition   A {@link Pos} with values for left, up, and front, respectively, which will be added to the sound's location, relative to where the entity's facing.
     */
    @RecordBuilder
    public record Options(long delay, double radius, boolean ignoreToggleState, @Nullable String permissionToPlay,
                          @Nullable String permissionToListen, @Nullable Pos relativePosition) {
        public static final @NotNull PMSSound.Options DEFAULT = new Options(0, 0, false, null, null, null);

        /**
         * Instantiates sound Options from a configuration section.
         * <p>
         * Nodes read from the section:
         * <ul>
         *     <li>Delay</li>
         *     <li>Radius</li>
         *     <li>Ignore Toggle State</li>
         *     <li>Permission To Play</li>
         *     <li>Permission To Listen</li>
         *     <li>Relative Position.Left</li>
         *     <li>Relative Position.Up</li>
         *     <li>Relative Position.Front</li>
         * </ul>
         * <p>
         * All values are optional, using {@link #DEFAULT}'s values when absent.
         *
         * @param section The section to get the options from.
         */
        public Options(@NotNull ConfigurationSection section) {
            this(section.getNumber("Delay").orElse(DEFAULT.delay).longValue(),
                    findRadius(section),
                    section.getBoolean("Ignore Toggle State").orElse(DEFAULT.ignoreToggleState),
                    section.getString("Permission To Play").orElse(DEFAULT.permissionToPlay),
                    section.getString("Permission To Listen").orElse(DEFAULT.permissionToListen),
                    findRelativePosition(section.getConfigurationSection("Relative Position")));
        }

        private static double findRadius(@NotNull ConfigurationSection section) {
            Object radius = section.getObject("Radius").orElse(DEFAULT.radius);
            if (radius instanceof Number n) return n.doubleValue();

            try {
                return Radius.valueOf(radius.toString()).radius();
            } catch (IllegalArgumentException e) {
                return 0;
            }
        }

        @Contract("null -> null")
        private static @Nullable Pos findRelativePosition(@Nullable ConfigurationSection section) {
            if (section == null) return DEFAULT.relativePosition;

            Pos relativePosition = new Pos(section.getNumber("Left").orElse(0).doubleValue(),
                    section.getNumber("Up").orElse(0).doubleValue(),
                    section.getNumber("Front").orElse(0).doubleValue());

            return relativePosition.equals(Pos.ZERO) ? null : relativePosition;
        }

        /**
         * A radius defines who to play the sound, and whether it's global or not.
         */
        public enum Radius {
            DEFAULT(0),
            SERVER_GLOBAL(-1),
            WORLD_GLOBAL(-2),
            WORLD_LOCAL(-3);

            private final double radius;

            Radius(double radius) {
                this.radius = radius;
            }

            public double radius() {
                return radius;
            }
        }
    }
}
