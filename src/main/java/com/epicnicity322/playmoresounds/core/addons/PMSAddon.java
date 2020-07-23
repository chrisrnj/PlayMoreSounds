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

    protected void init(@NotNull AddonDescription description, @NotNull Path file)
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
