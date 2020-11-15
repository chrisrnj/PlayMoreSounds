/*
 * Copyright (c) 2020 Christiano Rangel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
