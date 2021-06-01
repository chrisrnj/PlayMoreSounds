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

import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public abstract class CoreRichSound<T extends CoreSound>
{
    private final @NotNull String name;
    private @Nullable ConfigurationSection section;
    private boolean enabled;
    private boolean cancellable;
    private @NotNull Collection<T> childSounds;

    public CoreRichSound(@NotNull String name, boolean enabled, boolean cancellable, @Nullable Collection<T> childSounds)
    {
        this.name = name;
        this.enabled = enabled;
        this.cancellable = cancellable;
        this.childSounds = childSounds;
    }

    public CoreRichSound(@NotNull ConfigurationSection section)
    {
        this.section = section;
        this.name = section.getPath();
        enabled = section.getBoolean("Enabled").orElse(false);
        cancellable = section.getBoolean("Cancellable").orElse(false);
        childSounds = new HashSet<>();

        ConfigurationSection sounds = section.getConfigurationSection("Sounds");

        if (sounds != null) {
            for (String childSound : sounds.getNodes().keySet()) {
                childSounds.add(newCoreSound(sounds.getConfigurationSection(childSound)));
            }
        }
    }

    protected abstract @NotNull T newCoreSound(@NotNull ConfigurationSection section);

    public @NotNull String getName()
    {
        return name;
    }

    public @Nullable ConfigurationSection getSection()
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

    public @NotNull Collection<T> getChildSounds()
    {
        return childSounds;
    }

    public void setChildSounds(@Nullable Collection<T> childSounds)
    {
        if (childSounds == null)
            this.childSounds = new HashSet<>();
        else
            this.childSounds = childSounds;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CoreRichSound<?> that = (CoreRichSound<?>) o;

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
}
