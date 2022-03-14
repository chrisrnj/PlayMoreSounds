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

import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class RichSound<T extends Sound>
{
    private final @NotNull String name;
    private final @NotNull Collection<T> childSounds;
    private final @NotNull Collection<T> unmodifiableChildSounds;
    private @Nullable ConfigurationSection section;
    private boolean enabled;
    private boolean cancellable;

    public RichSound(@NotNull String name, boolean enabled, boolean cancellable, @Nullable Collection<T> childSounds)
    {
        if (name.isBlank()) throw new IllegalArgumentException("Rich Sound name can't be blank.");

        // Checking if there are two child sounds with the same name in the collection.
        var currentNames = new HashSet<String>();
        for (var sound : childSounds) {
            if (!currentNames.add(sound.getId())) {
                throw new IllegalArgumentException("Child Sounds collection has two sounds with the same ID.");
            }
        }

        this.name = name;
        this.enabled = enabled;
        this.cancellable = cancellable;
        this.childSounds = childSounds;
        this.unmodifiableChildSounds = Collections.unmodifiableCollection(childSounds);
    }

    public RichSound(@NotNull ConfigurationSection section)
    {
        this.section = section;
        this.name = section.getPath();
        this.enabled = section.getBoolean("Enabled").orElse(false);
        this.cancellable = section.getBoolean("Cancellable").orElse(false);
        this.childSounds = new ArrayList<>();
        this.unmodifiableChildSounds = Collections.unmodifiableCollection(childSounds);

        var sounds = section.getConfigurationSection("Sounds");

        if (sounds != null) {
            var currentNames = new HashSet<String>();

            for (Map.Entry<String, Object> node : sounds.getNodes().entrySet()) {
                if (!(node.getValue() instanceof ConfigurationSection childSoundSection)) continue;
                // A RichSound can not have two sounds with the same ID.
                if (!currentNames.add(node.getKey())) continue;

                childSounds.add(newCoreSound(childSoundSection));
            }
        }
    }

    protected abstract @NotNull T newCoreSound(@NotNull ConfigurationSection section);

    public @NotNull String getName()
    {
        return name;
    }

    public final @Nullable ConfigurationSection getSection()
    {
        return section;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean isCancellable()
    {
        return cancellable;
    }

    public void setCancellable(boolean cancellable)
    {
        this.cancellable = cancellable;
    }

    public final @NotNull Collection<T> getChildSounds()
    {
        return unmodifiableChildSounds;
    }

    public final @Nullable T getChildSound(@NotNull String id)
    {
        for (var sound : childSounds) {
            if (sound.getId().equals(id)) {
                return sound;
            }
        }
        return null;
    }

    public final void addChildSound(@NotNull T childSound)
    {
        if (getChildSound(childSound.getId()) == null) childSounds.add(childSound);
    }

    public final void removeChildSound(@NotNull T childSound)
    {
        childSounds.remove(childSound);
    }

    public final void removeChildSound(@NotNull String id)
    {
        childSounds.removeIf(sound -> sound.getId().equals(id));
    }

    /**
     * Sets the properties of this rich sound to a configuration. Keys set by this method are the same used to create a
     * rich sound with {@link #RichSound(ConfigurationSection)}.
     * <p>
     * If {@link #getName()} is blank, the properties are applied to the configuration's root, otherwise a section with
     * the name is created and properties are applied there.
     * <p>
     * Default properties are ignored.
     *
     * @param configuration The configuration to apply properties and child sounds.
     * @return The section the properties were applied.
     */
    public @NotNull ConfigurationSection set(@NotNull Configuration configuration)
    {
        ConfigurationSection section;

        if (name.isBlank()) {
            section = configuration;
        } else {
            section = Objects.requireNonNullElseGet(configuration.getConfigurationSection(name), () -> configuration.createSection(name));
        }

        section.set("Enabled", enabled);
        section.set("Cancellable", cancellable);

        ConfigurationSection sounds = Objects.requireNonNullElseGet(section.getConfigurationSection("Sounds"), () -> section.createSection("Sounds"));

        for (T childSound : childSounds) childSound.set(sounds);
        return section;
    }

    @Override
    public boolean equals(@Nullable Object o)
    {
        if (this == o) return true;
        if (!(o instanceof RichSound<?> that)) return false;

        return enabled == that.enabled
                && cancellable == that.cancellable
                && name.equals(that.name)
                && Objects.equals(section, that.section)
                && childSounds.equals(that.childSounds);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, section, enabled, cancellable, childSounds);
    }

    @Override
    public @NotNull String toString()
    {
        StringBuilder string = new StringBuilder();

        string.append(getClass().getSimpleName()).append("{").append("name='").append(name).append('\'');

        if (section != null) {
            section.getRoot().getFilePath().ifPresent(path -> string.append(", section-root='").append(path.toAbsolutePath()).append('\''));
            if (section.getParent() != null) string.append(", section-path='").append(section.getPath()).append('\'');
        }

        string.append(", enabled=").append(enabled)
                .append(", cancellable=").append(cancellable)
                .append(", childSounds=").append(childSounds)
                .append('}');

        return string.toString();
    }
}
