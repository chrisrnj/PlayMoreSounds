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

package com.epicnicity322.playmoresounds.core.addons;

import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
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
    final @NotNull Path jar;
    final @NotNull AddonDescription description;
    final PMSAddon addon;

    AddonClassLoader(@NotNull Path jar, @NotNull AddonDescription description) throws InvalidAddonException, MalformedURLException, IllegalAccessException, InstantiationException
    {
        super(new URL[]{jar.toUri().toURL()}, PlayMoreSoundsCore.class.getClassLoader());

        this.jar = jar;
        this.description = description;

        Class<?> main;

        try {
            main = Class.forName(description.getMainClass(), true, this);
        } catch (ClassNotFoundException ex) {
            throw new InvalidAddonException(description.getName() + " addon main class " + description.getMainClass() + " was not found.", ex);
        }

        if (!PMSAddon.class.isAssignableFrom(main)) {
            throw new InvalidAddonException(description.getName() + " addon main class " + description.getMainClass() + " does not extend to PMSAddon.");
        }

        Class<? extends PMSAddon> addonClass = main.asSubclass(PMSAddon.class);

        addon = addonClass.newInstance();
        addon.loaded = true;
    }

    /**
     * Removes all cached classes from the specified {@link AddonClassLoader}.
     *
     * @param classLoader The class loader to remove caches.
     */
    static void clearCaches(@NotNull AddonClassLoader classLoader)
    {
        cacheClasses.entrySet().removeIf(entry -> entry.getValue().getClassLoader() == classLoader);
    }

    /**
     * Removes all cached classes from this class loader.
     */
    static void clearCaches()
    {
        cacheClasses.clear();
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        return findClass(name, true);
    }

    private Class<?> findClass(String name, boolean addons) throws ClassNotFoundException
    {
        Class<?> clazz = cacheClasses.get(name);

        if (clazz != null) return clazz;

        try {
            clazz = super.findClass(name);
            cacheClasses.put(name, clazz);
            return clazz;
        } catch (ClassNotFoundException ignored) {
            // If clazz was not found then continue searching for it.
        }

        // Searching for clazz in other addons.
        if (addons) {
            for (AddonClassLoader loader : AddonManager.addonClassLoaders) {
                if (loader == this) continue;

                try {
                    clazz = loader.findClass(name, false);

                    // This will only break if clazz was found.
                    break;
                } catch (ClassNotFoundException ignored) {
                    // Continue searching on the other addons left.
                }
            }
        }

        if (clazz == null) {
            throw new ClassNotFoundException(description.getName() + " is missing the class " + name + " (Probably from a not specified dependency). Please contact the author(s): " + description.getAuthors());
        } else {
            cacheClasses.put(name, clazz);
            return clazz;
        }
    }

    public @NotNull PMSAddon getAddon()
    {
        return addon;
    }
}
