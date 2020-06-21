package com.epicnicity322.playmoresounds.core.addons;

import com.epicnicity322.playmoresounds.core.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.addons.exceptions.InvalidAddonException;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.HashMap;

public final class AddonClassLoader extends URLClassLoader
{
    private static final @NotNull HashMap<String, Class<?>> cacheClasses = new HashMap<>();
    private final @NotNull PMSAddon addon;
    private final @NotNull AddonDescription description;
    private final @NotNull Path jar;

    protected AddonClassLoader(@NotNull AddonDescription description, @NotNull Path jar)
            throws MalformedURLException, InvalidAddonException, IllegalAccessException, InstantiationException
    {
        super(new URL[]{jar.toUri().toURL()}, PlayMoreSounds.class.getClassLoader());

        this.description = description;
        this.jar = jar;

        try {
            Class<?> main = Class.forName(description.getMain(), true, this);
            Class<? extends PMSAddon> addonClass = main.asSubclass(PMSAddon.class);

            addon = addonClass.newInstance();
        } catch (ClassNotFoundException ex) {
            throw new InvalidAddonException("Cannot find main class '" + description.getMain() + "'.", ex);
        } catch (ClassCastException ex) {
            throw new InvalidAddonException("The main class '" + description.getMain() + "' of the addon '" +
                    description.getName() + "' does not extend to PMSAddon.", ex);
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        return findClass(name, true);
    }

    private Class<?> findClass(String name, boolean addons) throws ClassNotFoundException
    {
        if (cacheClasses.containsKey(name))
            return cacheClasses.get(name);

        Class<?> clazz = null;

        try {
            clazz = super.findClass(name);
        } catch (ClassNotFoundException ignored) {
            // Let clazz be null if not found.
        }

        if (clazz == null) {
            // Searching the class in other addons.
            if (addons)
                for (AddonClassLoader loader : AddonManager.addonClassLoaders)
                    try {
                        clazz = loader.findClass(name, false);
                        // This will only break if the class was found.
                        break;
                    } catch (ClassNotFoundException ignored) {
                    }

            if (clazz == null)
                if (addon == null)
                    throw new ClassNotFoundException(name);
                else
                    throw new ClassNotFoundException("The addon '" + addon.toString() + "' is missing the class " + name
                            + " (Probably from a not specified dependency.). Please contact the addon author(s): " +
                            addon.getDescription().getAuthors());
        }

        cacheClasses.put(name, clazz);
        return clazz;
    }

    @Override
    protected void finalize() throws Throwable
    {
        cacheClasses.clear();
        super.finalize();
    }

    protected synchronized void init(@NotNull PMSAddon addon)
    {
        if (this.addon != null)
            throw new UnsupportedOperationException("Addon is already initialized.");

        addon.init(description, jar);
    }

    public @NotNull PMSAddon getAddon()
    {
        return addon;
    }
}
