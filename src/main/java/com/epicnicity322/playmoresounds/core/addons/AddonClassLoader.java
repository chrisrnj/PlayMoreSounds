package com.epicnicity322.playmoresounds.core.addons;

import com.epicnicity322.playmoresounds.core.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.addons.exceptions.InvalidAddonException;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.HashMap;

public class AddonClassLoader extends URLClassLoader
{
    private static final HashMap<String, Class<?>> cacheClasses = new HashMap<>();
    protected PMSAddon addon;
    private PlayMoreSounds pms;
    private AddonDescription description;
    private Path jar;

    protected AddonClassLoader(@NotNull PlayMoreSounds pms, @NotNull AddonDescription description, @NotNull Path jar)
            throws MalformedURLException, InvalidAddonException
    {
        super(new URL[]{jar.toUri().toURL()}, pms.getClass().getClassLoader());

        this.pms = pms;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        return findClass(name, true);
    }

    protected Class<?> findClass(String name, boolean addons) throws ClassNotFoundException
    {
        if (cacheClasses.containsKey(name)) {
            return cacheClasses.get(name);
        }

        Class<?> clazz = null;

        try {
            clazz = super.findClass(name);
        } catch (Exception ignored) {
        }

        if (clazz == null) {
            if (addons) {
                for (AddonClassLoader loader : pms.getAddonManager().getAddonClassLoaders()) {
                    if (loader != this) {
                        try {
                            clazz = loader.findClass(name, false);
                        } catch (ClassNotFoundException ignored) {
                        }
                    }
                }
            }

            if (clazz == null) {
                if (addon == null) {
                    throw new ClassNotFoundException(name);
                } else {
                    throw new ClassNotFoundException("The addon '" + addon.toString() + "' is missing the class " + name
                            + " (Probably from a not specified dependency.). Please contact the addon author(s): " +
                            addon.getDescription().getAuthors());
                }
            }
        }

        cacheClasses.put(name, clazz);
        return clazz;
    }

    protected synchronized void init(@NotNull PMSAddon addon)
    {
        if (this.addon != null) {
            throw new IllegalArgumentException("Addon is already initialized.");
        }

        addon.init(description, jar);
    }

    public PMSAddon getAddon()
    {
        return addon;
    }
}
