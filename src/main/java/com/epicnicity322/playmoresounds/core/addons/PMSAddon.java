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

public abstract class PMSAddon
{
    protected volatile boolean started = false;
    protected volatile boolean stopped = false;
    protected volatile boolean loaded = false;
    private AddonDescription description;
    private Path jar;

    protected PMSAddon()
    {
        ClassLoader classLoader = getClass().getClassLoader();

        if (!(classLoader instanceof AddonClassLoader))
            throw new UnsupportedOperationException("PMSAddon requires an AddonClassLoader");

        ((AddonClassLoader) classLoader).init(this);
    }

    final void init(@NotNull AddonDescription description, @NotNull Path file)
    {
        this.description = description;
        this.jar = file;
    }

    public final AddonDescription getDescription()
    {
        return description;
    }

    public final Path getJar()
    {
        return jar;
    }

    /**
     * @return If this addon was already started once.
     */
    public final boolean hasStarted()
    {
        return started;
    }

    /**
     * @return If this addon was already stopped once.
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
     * @throws IllegalStateException If this addon was already started.
     */
    protected void onStart()
    {
        if (started)
            throw new IllegalStateException(getDescription().getName() + " has already started.");
    }

    /**
     * When your addon is stopped by PMS.
     *
     * @throws IllegalStateException If this addon was already stopped.
     */
    protected void onStop()
    {
        if (stopped)
            throw new IllegalStateException(getDescription().getName() + " was already stopped.");
    }

    @Override
    public String toString()
    {
        return getDescription().getName();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof PMSAddon)) return false;

        PMSAddon pmsAddon = (PMSAddon) o;

        return getJar().equals(pmsAddon.getJar());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getJar());
    }
}
