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

package com.epicnicity322.playmoresounds.core.addons;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Objects;

public class PMSAddon
{
    volatile boolean started = false;
    volatile boolean stopped = false;
    volatile boolean loaded = false;
    private AddonDescription description;
    private Path jar;

    protected PMSAddon()
    {
        ClassLoader classLoader = getClass().getClassLoader();

        if (!(classLoader instanceof AddonClassLoader))
            throw new UnsupportedOperationException(this.getClass().getName() + " can only be instantiated by " + AddonClassLoader.class.getName());

        ((AddonClassLoader) classLoader).init(this);
    }

    final void init(@NotNull AddonDescription description, @NotNull Path file)
    {
        this.description = description;
        this.jar = file;
    }

    /**
     * @return The description file of this addon.
     */
    public final @NotNull AddonDescription getDescription()
    {
        return description;
    }

    /**
     * @return The path to this addon jar.
     */
    public final @NotNull Path getJar()
    {
        return jar;
    }

    /**
     * @return If this addon has already started once.
     */
    public final boolean hasStarted()
    {
        return started;
    }

    /**
     * @return If this addon has already stopped once.
     */
    public final boolean hasStopped()
    {
        return stopped;
    }

    /**
     * @return If this addon is loaded.
     */
    public final boolean isLoaded()
    {
        return loaded;
    }

    /**
     * When your addon is started by PMS.
     *
     * @throws IllegalStateException If this addon was already started before.
     */
    protected void onStart()
    {
        if (started)
            throw new IllegalStateException(toString() + " has already started.");
    }

    /**
     * When your addon is stopped by PMS.
     *
     * @throws IllegalStateException If this addon was already stopped before.
     */
    protected void onStop()
    {
        if (stopped)
            throw new IllegalStateException(toString() + " has already stopped.");
    }

    /**
     * @return This addon name from {@link AddonDescription#getName()}.
     */
    @Override
    public String toString()
    {
        return getDescription().getName();
    }

    /**
     * @return If the object is an addon and if has the same jar path as this.
     */
    @Override
    public boolean equals(Object otherAddon)
    {
        if (this == otherAddon) return true;
        if (!(otherAddon instanceof PMSAddon)) return false;

        PMSAddon pmsAddon = (PMSAddon) otherAddon;

        return getJar().equals(pmsAddon.getJar());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getJar());
    }
}
