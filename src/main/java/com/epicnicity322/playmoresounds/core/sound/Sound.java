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

import com.epicnicity322.playmoresounds.core.util.PMSHelper;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class Sound
{
    private static final @NotNull HashMap<String, SoundCategory> categories = new HashMap<>(SoundCategory.values().length);

    static {
        for (SoundCategory category : SoundCategory.values()) {
            categories.put(category.name(), category);
        }
    }

    private final @NotNull String id;
    private final @Nullable ConfigurationSection section;
    private @Nullable SoundType soundType;
    private @NotNull String sound;
    private @NotNull SoundCategory category;
    private float volume;
    private float pitch;
    private long delay;
    private @NotNull SoundOptions options;

    /**
     * Creates a sound with random {@link #getId()}.
     *
     * @see #Sound(String, SoundCategory, float, float, long, SoundOptions)
     */
    public Sound(@NotNull String sound, @Nullable SoundCategory category, float volume, float pitch, long delay, @Nullable SoundOptions options)
    {
        this(null, sound, category, volume, pitch, delay, options);
    }

    /**
     * A sound that can be played.
     *
     * @param id       The id of this sound. If null or a blank string, a random string is get.
     * @param sound    The sound to be played. {@link SoundType#getPresentSoundNames()} contains this string, it is automatically converted to the version dependent sound.
     * @param category The category of this sound. Null for default to {@link SoundCategory#MASTER}.
     * @param volume   The volume of this sound.
     * @param pitch    The pitch of this sound, values greater than 2 or lower than 0 have no difference.
     * @param delay    The delay to wait before playing this sound.
     * @param options  The options of this sound, null for default values.
     * @throws IllegalArgumentException If sound parameter is not a {@link SoundType} nor a valid Minecraft namespaced key.
     * @see PMSHelper#isNamespacedKey(String)
     */
    public Sound(@Nullable String id, @NotNull String sound, @Nullable SoundCategory category, float volume, float pitch, long delay, @Nullable SoundOptions options)
    {
        this.soundType = parseSound(sound);

        if (soundType != null) {
            this.sound = soundType.getSound().orElse("block.note_block.pling");
        } else {
            this.sound = sound;
        }

        this.section = null;
        this.id = id == null || id.isBlank() ? PMSHelper.getRandomString(6) : id;
        this.category = Objects.requireNonNullElse(category, SoundCategory.MASTER);
        this.delay = delay;
        this.options = Objects.requireNonNullElseGet(options, () -> new SoundOptions(false, null, null, 0.0));
        this.pitch = pitch;
        this.volume = volume;
    }

    /**
     * Creates a {@link Sound} based on the keys of a {@link ConfigurationSection}.
     * The properties are get from the keys 'Category', 'Delay', 'Pitch', 'Sound' and 'Volume'. {@link SoundOptions} are
     * automatically get from the section 'Options'.
     * <p>
     * The only required key is 'Sound', all the others are optional and default values are used in case any is missing.
     *
     * @param section The section to get the keys for this Sound's values.
     * @throws IllegalArgumentException If the section does not have a parent (or is a {@link com.epicnicity322.yamlhandler.Configuration}).
     * @throws IllegalArgumentException If the section does not contain a 'Sound' string key, or if the sound is not a valid namespaced key.
     * @see SoundOptions
     */
    public Sound(@NotNull ConfigurationSection section)
    {
        if (section.getParent() == null)
            throw new IllegalArgumentException("Section parameter must not be a Configuration.");

        String sound = section.getString("Sound").orElseThrow(() -> new IllegalArgumentException("Section must contain a Sound key."));
        this.soundType = parseSound(sound);

        if (soundType != null) {
            this.sound = soundType.getSound().orElse("block.note_block.pling");
        } else {
            this.sound = sound;
        }

        this.section = section;
        this.id = section.getName();

        this.category = categories.getOrDefault(section.getString("Category").orElse("MASTER").toUpperCase(Locale.ROOT), SoundCategory.MASTER);
        this.delay = section.getNumber("Delay").orElse(0).longValue();
        this.pitch = section.getNumber("Pitch").orElse(1).floatValue();
        this.volume = section.getNumber("Volume").orElse(10).floatValue();

        var options = section.getConfigurationSection("Options");

        // Assigning default options in case the sound is missing them.
        if (options == null)
            this.options = new SoundOptions(false, null, null, 0.0);
        else
            this.options = new SoundOptions(options);
    }

    /**
     * Validates if sound parameter is a {@link SoundType} or a valid namespaced key.
     *
     * @param sound The sound to validate.
     * @return The sound type if one was found.
     * @throws IllegalArgumentException If sound is neither a {@link SoundType} nor a valid namespaced key.
     * @see PMSHelper#isNamespacedKey(String)
     */
    private @Nullable SoundType parseSound(@NotNull String sound)
    {
        // Checking if sound should be transformed to SoundType.
        if (SoundType.getPresentSoundNames().contains(sound)) {
            return SoundType.valueOf(sound);
        } else {
            if (!PMSHelper.isNamespacedKey(sound)) {
                throw new IllegalArgumentException("Sound is not a valid namespaced key.");
            }
            return null;
        }
    }

    /**
     * Gets the ID of this sound, can be any type of string.
     * <p>
     * The ID is the same as the section name, in case this is a {@link ConfigurationSection} sound.
     *
     * @return The ID of this sound.
     */
    public @NotNull String getId()
    {
        return id;
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

        if (sound.isEmpty())
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
     * If this is either a custom sound or minecraft key sound, this will only be set if the string meets the rules of a
     * namespaced key.
     *
     * @param sound The custom sound, minecraft key, or {@link SoundType} name.
     * @throws IllegalArgumentException If sound is neither a {@link SoundType} nor a valid namespaced key.
     * @see #setSoundType(SoundType)
     * @see PMSHelper#isNamespacedKey(String)
     */
    public void setSound(@NotNull String sound)
    {
        this.soundType = parseSound(sound);

        if (soundType != null) {
            this.sound = soundType.getSound().orElse("block.note_block.pling");
        } else {
            this.sound = sound;
        }
    }

    /**
     * The category this sound should be played as, irrelevant if the current version does not support categories.
     *
     * @return The category of this sound.
     */
    public @NotNull SoundCategory getCategory()
    {
        return category;
    }

    /**
     * Sets the category this sound should be played as, use null to set it to {@link SoundCategory#MASTER}.
     *
     * @param category The category this sound should be played.
     */
    public void setCategory(@Nullable SoundCategory category)
    {
        this.category = Objects.requireNonNullElse(category, SoundCategory.MASTER);
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
        this.options = Objects.requireNonNullElseGet(options, () -> new SoundOptions(false, null, null, 0.0));
    }

    /**
     * Sets the properties of this sound to the specified section.
     * <p>
     * A configuration section will be created with the name of {@link #getId()}, if it doesn't already exists. And the
     * properties will be applied to it. Keys set by this method are the same used to create a sound with {@link #Sound(ConfigurationSection)}.
     * <p>
     * Default properties are ignored.
     * <p>
     * {@link SoundOptions} are set automatically to 'Options' section through {@link SoundOptions#set(ConfigurationSection)}
     *
     * @param section The section to set the properties.
     * @return The configuration section with this sound's properties.
     */
    public @NotNull ConfigurationSection set(@NotNull ConfigurationSection section)
    {
        var soundSection = Objects.requireNonNullElseGet(section.getConfigurationSection(id), () -> section.createSection(id));
        var optionsSection = Objects.requireNonNullElseGet(soundSection.getConfigurationSection("Options"), () -> soundSection.createSection("Options"));
        options.set(optionsSection);

        if (pitch != 1.0f || soundSection.contains("Pitch")) soundSection.set("Pitch", pitch);
        soundSection.set("Sound", soundType != null ? soundType.name() : sound);
        if (volume != 10.0f || soundSection.contains("Volume")) soundSection.set("Volume", volume);

        return soundSection;
    }

    @Override
    public @NotNull String toString()
    {
        StringBuilder string = new StringBuilder();

        string.append(getClass().getSimpleName()).append("{id='").append(id).append('\'');

        if (section != null) {
            section.getRoot().getFilePath().ifPresent(path -> string.append(", section-root='").append(path.toAbsolutePath()).append('\''));
            // Sounds always have a path, because a check for ConfigurationSection#getParent is made on constructor.
            string.append(", section-path='").append(section.getPath()).append('\'');
        }

        string.append(", sound='").append(sound).append('\'')
                .append(", category=").append(category.name())
                .append(", volume=").append(volume)
                .append(", pitch=").append(pitch)
                .append(", delay=").append(delay)
                .append(", options=").append(options)
                .append('}');

        return string.toString();
    }

    /**
     * Whether the {@link Object} is a {@link Sound} and has similar delay, options, pitch, sound and volume.
     *
     * @param o The {@link Object} to compare.
     * @return If the argument is a similar sound to this one.
     * @see #equals(Object)
     */
    public boolean isSimilar(@Nullable Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Sound sound1)) return false;

        return delay == sound1.delay &&
                category == sound1.category &&
                Float.compare(sound1.volume, volume) == 0 &&
                Float.compare(sound1.pitch, pitch) == 0 &&
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
        if (this == o) return true;
        if (!(o instanceof Sound sound1)) return false;

        if (!isSimilar(sound1)) return false;

        return id.equals(sound1.id) &&
                Objects.equals(sound1.section, section);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, sound, volume, pitch, delay, category, options, section);
    }
}
