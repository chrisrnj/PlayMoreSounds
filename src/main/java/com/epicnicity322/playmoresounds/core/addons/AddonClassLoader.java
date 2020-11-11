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
    private final @NotNull Path jar;
    private final @NotNull AddonDescription description;

    protected AddonClassLoader(@NotNull Path jar, @NotNull AddonDescription description) throws InvalidAddonException, MalformedURLException
    {
        super(new URL[]{jar.toUri().toURL()}, PlayMoreSounds.class.getClassLoader());

        this.jar = jar;
        this.description = description;

        try {
            Class<?> main = Class.forName(description.getMain(), true, this);
            Class<? extends PMSAddon> addonClass = main.asSubclass(PMSAddon.class);

            addon = addonClass.newInstance();
        } catch (ClassNotFoundException ex) {
            throw new InvalidAddonException("Cannot find main class '" + description.getMain() + "' of the addon '" + description.getName() + "'.", ex);
        } catch (ClassCastException ex) {
            throw new InvalidAddonException("The main class '" + description.getMain() + "' of the addon '" +
                    description.getName() + "' does not extend to PMSAddon.", ex);
        } catch (Exception ex) {
            throw new InvalidAddonException("An error has occurred while instantiating '" + description.getName() + "' addon.", ex);
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
            cacheClasses.put(name, clazz);
            return clazz;
        } catch (ClassNotFoundException ignored) {
            // If clazz was not found then continue searching for it.
        }

        // Searching for clazz in other addons.
        if (addons) {
            for (AddonClassLoader loader : AddonManager.addonClassLoaders)
                try {
                    clazz = loader.findClass(name, false);
                    // This will only break if clazz was found.
                    break;
                } catch (ClassNotFoundException ignored) {
                }
        }

        if (clazz == null) {
            throw new ClassNotFoundException("The addon '" + addon.toString() + "' is missing the class " + name
                    + " (Probably from a not specified dependency.). Please contact the addon author(s): " +
                    addon.getDescription().getAuthors());
        } else {
            cacheClasses.put(name, clazz);
            return clazz;
        }
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
