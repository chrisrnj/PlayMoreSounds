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

package com.epicnicity322.playmoresounds.core.sound;

import com.epicnicity322.playmoresounds.core.util.PMSHelper;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class CoreSound
{
    private final @Nullable ConfigurationSection section;
    private @Nullable SoundType soundType;
    private String sound;
    private float volume;
    private float pitch;
    private long delay;
    private SoundOptions options;

    public CoreSound(@NotNull String sound, float volume, float pitch, long delay, @Nullable SoundOptions options)
    {
        setSound(sound);
        setOptions(options);

        this.volume = volume;
        this.pitch = pitch;
        this.delay = delay;
        section = null;
    }

    /**
     * Creates an instance for {@link CoreSound} based on the keys of a {@link ConfigurationSection}. This is the section
     * where the keys Delay, Options, Pitch, Sound and Volume are. Options are automatically converted based on the rules
     * set on {@link SoundOptions#SoundOptions(ConfigurationSection)}.
     *
     * @param section The section to get the keys for this sound's values.
     * @throws IllegalArgumentException If the section does not contain a 'Sound' key with string as value.
     * @see SoundOptions
     */
    public CoreSound(@NotNull ConfigurationSection section)
    {
        setSound(section.getString("Sound").orElseThrow(() -> new IllegalArgumentException("Section must contain a Sound key.")));

        this.section = section;
        volume = section.getNumber("Volume").orElse(10).floatValue();
        pitch = section.getNumber("Pitch").orElse(1).floatValue();
        delay = section.getNumber("Delay").orElse(0).longValue();

        ConfigurationSection options = section.getConfigurationSection("Options");

        if (options == null)
            setOptions(null);
        else
            this.options = new SoundOptions(options);

    }

    /**
     * Gets the section this sound is stored. This is the section where the values for Delay, Options, Pitch, Sound and
     * Volume are taken.
     *
     * @return The section of this sound, if this is a section sound.
     */
    public @Nullable ConfigurationSection getSection()
    {
        return section;
    }

    /**
     * @return The sound that will be played, null if this is a custom sound.
     */
    public @Nullable SoundType getSoundType()
    {
        return soundType;
    }

    /**
     * Sets the {@link SoundType} to be played. The value of {@link #getSound()} is automatically updated to the
     * minecraft key of this {@link SoundType}.
     *
     * @param soundType The {@link SoundType} to be played.
     * @throws UnsupportedOperationException If the {@link SoundType} is not present on the current minecraft version.
     * @see #setSound(String)
     */
    public void setSoundType(@NotNull SoundType soundType)
    {
        Optional<String> sound = soundType.getSound();

        if (!sound.isPresent())
            throw new UnsupportedOperationException("SoundType is not present on this minecraft version.");

        this.sound = sound.get();
        this.soundType = soundType;
    }

    /**
     * @return The minecraft key of the sound that will be played or the name of a custom sound.
     */
    public @NotNull String getSound()
    {
        return sound;
    }

    /**
     * Sets the sound to be played. This could be a custom sound, a minecraft key sound, or the name of a {@link SoundType}.
     * <p>
     * If this is a {@link SoundType} name, it converts to it using {@link SoundType#valueOf(String)} and updates the
     * value of {@link #getSoundType()}.
     * <p>
     * If this is either a custom sound or minecraft key sound, this will only be set if the string meets the rules of
     * a minecraft key. A {@link SoundType} will not be set in this case.
     *
     * @param sound The custom sound, minecraft key, or {@link SoundType} name.
     * @throws IllegalArgumentException If sound is neither a {@link SoundType} nor a valid minecraft key.
     * @see #setSoundType(SoundType)
     * @see PMSHelper#isNamespacedKey(String)
     */
    public void setSound(@NotNull String sound)
    {
        if (SoundType.getPresentSoundNames().contains(sound)) {
            SoundType type = SoundType.valueOf(sound);

            this.sound = type.getSound().orElse("BLOCK_NOTE_BLOCK_PLING");
            soundType = type;
        } else {
            if (!PMSHelper.isNamespacedKey(sound))
                throw new IllegalArgumentException("Sound is not a valid namespaced key.");

            this.sound = sound;
        }
    }

    /**
     * @return The volume of this sound.
     */
    public float getVolume()
    {
        return volume;
    }

    /**
     * @param volume The volume of this sound to be set.
     */
    public void setVolume(float volume)
    {
        this.volume = volume;
    }

    /**
     * @return The pitch of this sound.
     */
    public float getPitch()
    {
        return pitch;
    }

    /**
     * @param pitch The pitch of this sound to be set.
     */
    public void setPitch(float pitch)
    {
        this.pitch = pitch;
    }

    /**
     * @return The delay in ticks to be waited before playing this sound.
     */
    public long getDelay()
    {
        return delay;
    }

    /**
     * @param delay The delay in ticks to be waited before playing this sound to be set.
     */
    public void setDelay(long delay)
    {
        this.delay = delay;
    }

    /**
     * @return The options to be followed by this sound when played.
     */
    public @NotNull SoundOptions getOptions()
    {
        return options;
    }

    /**
     * Sets the options of this sound.
     *
     * @param options The options or null for default options.
     */
    public void setOptions(@Nullable SoundOptions options)
    {
        if (options == null)
            this.options = new SoundOptions(false, null, null, 0.0, null);
        else
            this.options = options;
    }

    @Override
    public String toString()
    {
        StringBuilder string = new StringBuilder();

        string.append("CoreSound{sound='").append(sound).append('\'')
                .append(", volume=").append(volume)
                .append(", pitch=").append(pitch)
                .append(", delay=").append(delay)
                .append(", options=").append(options);

        if (section != null) {
            // Don't wanna print a big mess with all the nodes of this section
            string.append(", section-path='").append(section.getPath()).append('\'');

            section.getRoot().getFilePath().ifPresent(path -> string.append(", section-root='").append(path.toAbsolutePath()).append('\''));
        }

        string.append('}');

        return string.toString();
    }

    /**
     * Whether the {@link Object} is a {@link CoreSound} and has similar delay, options, pitch, sound and volume.
     *
     * @param o The {@link Object} to compare.
     * @return If the argument is a similar sound to this one.
     * @see #equals(Object)
     */
    public boolean isSimilar(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof CoreSound)) return false;

        CoreSound sound1 = (CoreSound) o;

        return Float.compare(sound1.volume, volume) == 0 &&
                Float.compare(sound1.pitch, pitch) == 0 &&
                delay == sound1.delay &&
                sound.equals(sound1.sound) &&
                options.equals(sound1.options);
    }

    /**
     * Whether the {@link Object} returns true on {@link #isSimilar(Object)} and has the same configuration section.
     *
     * @param o The {@link Object} to compare.
     * @return If the argument is a sound with the same values and origin as this one.
     */
    @Override
    public boolean equals(Object o)
    {
        if (!isSimilar(o)) return false;

        CoreSound sound1 = (CoreSound) o;

        return Objects.equals(sound1.section, section);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(sound, volume, pitch, delay, options, section);
    }
}
