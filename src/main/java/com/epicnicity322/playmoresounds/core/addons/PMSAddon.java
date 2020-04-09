package com.epicnicity322.playmoresounds.core.addons;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Objects;

public abstract class PMSAddon
{
    protected boolean loaded;
    private AddonDescription description;
    private Path jar;

    protected PMSAddon()
    {
        ClassLoader classLoader = getClass().getClassLoader();

        if (!(classLoader instanceof AddonClassLoader)) {
            throw new IllegalStateException("PMSAddon requires AddonClassLoader");
        }

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

    public final boolean isLoaded()
    {
        return loaded;
    }

    /**
     * When your addon is loaded by PMS.
     */
    public abstract void onStart();

    /**
     * When your addon is unloaded by PMS.
     */
    public void onStop()
    {
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
