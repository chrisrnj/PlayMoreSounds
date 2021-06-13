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
    private final @NotNull AddonClassLoader classLoader;
    private final @NotNull Path jar;
    private final @NotNull AddonDescription description;
    volatile boolean started = false;
    volatile boolean stopped = false;
    volatile boolean loaded = false;

    protected PMSAddon()
    {
        ClassLoader classLoader = getClass().getClassLoader();
        String name = this.getClass().getName();

        if (!(classLoader instanceof AddonClassLoader))
            throw new UnsupportedOperationException(name + " can only be instantiated by " + AddonClassLoader.class.getName());

        this.classLoader = (AddonClassLoader) classLoader;

        if (this.classLoader.addon != null)
            throw new IllegalStateException(name + " is a singleton and was already instantiated.");

        jar = this.classLoader.jar;
        description = this.classLoader.description;
    }

    @NotNull AddonClassLoader getClassLoader()
    {
        return classLoader;
    }

    /**
     * @return The path to this addon jar.
     */
    public final @NotNull Path getJar()
    {
        return jar;
    }

    /**
     * @return The description file of this addon.
     */
    public final @NotNull AddonDescription getDescription()
    {
        return description;
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
            throw new IllegalStateException(this + " was already started.");
    }

    /**
     * When your addon is stopped by PMS.
     *
     * @throws IllegalStateException If this addon was already stopped before.
     */
    protected void onStop()
    {
        if (stopped)
            throw new IllegalStateException(this + " was already stopped.");
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

        return jar.equals(pmsAddon.jar);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(jar);
    }
}
