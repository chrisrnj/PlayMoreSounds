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

package com.epicnicity322.playmoresounds.core.sound;

import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum SoundCategory
{
    AMBIENT("AMBIENT", "AMBIENT"),
    BLOCK("BLOCK", "BLOCKS"),
    HOSTILE("HOSTILE", "HOSTILE"),
    MASTER("MASTER", "MASTER"),
    MUSIC("MUSIC", "MUSIC"),
    NEUTRAL("NEUTRAL", "NEUTRAL"),
    PLAYER("PLAYER", "PLAYERS"),
    RECORD("RECORD", "RECORDS"),
    VOICE("VOICE", "VOICE"),
    WEATHER("WEATHER", "WEATHER");

    private @Nullable Sound.Source spongeValue;
    private @Nullable org.bukkit.SoundCategory bukkitValue;

    SoundCategory(@NotNull String spongeValue, @NotNull String bukkitValue)
    {
        if (PlayMoreSoundsCore.getPlatform() == PlayMoreSoundsCore.Platform.SPONGE) {
            try {
                this.spongeValue = (Sound.Source) Sound.Source.class.getField(spongeValue).get(null);
            } catch (Exception ignored) {
            }
        } else if (PlayMoreSoundsCore.getPlatform() == PlayMoreSoundsCore.Platform.BUKKIT && StaticFields.bukkitSoundCategories) {
            this.bukkitValue = org.bukkit.SoundCategory.valueOf(bukkitValue);
        }
    }

    /**
     * @return Whether the current platform has sound categories available.
     */
    public static boolean hasSoundCategories()
    {
        return StaticFields.bukkitSoundCategories || PlayMoreSoundsCore.getPlatform() == PlayMoreSoundsCore.Platform.SPONGE;
    }

    public @Nullable org.bukkit.SoundCategory asBukkit()
    {
        return bukkitValue;
    }

    public @Nullable Sound.Source asSponge()
    {
        return spongeValue;
    }

    private static final class StaticFields
    {
        private static final boolean bukkitSoundCategories;

        static {
            boolean bukkitSoundCategories1;

            try {
                Class.forName("org.bukkit.SoundCategory");
                bukkitSoundCategories1 = true;
            } catch (ClassNotFoundException ignored) {
                bukkitSoundCategories1 = false;
            }

            bukkitSoundCategories = bukkitSoundCategories1;
        }
    }
}
