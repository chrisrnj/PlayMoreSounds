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

import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.playmoresounds.core.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.addons.exceptions.InvalidAddonException;
import com.epicnicity322.playmoresounds.core.util.LoadableHashSet;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class AddonManager
{
    static final @NotNull Set<AddonClassLoader> addonClassLoaders = ConcurrentHashMap.newKeySet();
    private static final @NotNull AtomicBoolean registered = new AtomicBoolean(false);
    private final @NotNull PlayMoreSounds corePMS;
    private final @NotNull LoadableHashSet<String> serverPlugins;

    public AddonManager(@NotNull PlayMoreSounds corePMS, @NotNull LoadableHashSet<String> serverPlugins)
    {
        this.corePMS = corePMS;
        this.serverPlugins = serverPlugins;
    }

    /**
     * Gets all proper addon jars from Addons folder and loads their classes on PMSAddon Runner thread. Addons can only be registered once.
     *
     * @throws IOException                   If it was not possible to read Addons folder.
     * @throws UnsupportedOperationException If addons were already registered.
     * @throws IllegalStateException         If the server has not registered all plugins yet.
     */
    public void registerAddons() throws IOException
    {
        if (registered.getAndSet(true))
            throw new UnsupportedOperationException("Addons were already registered.");

        if (!serverPlugins.isLoaded())
            throw new IllegalStateException("Can not register addons if the server has not registered all plugins yet.");

        Path addonsFolder = corePMS.getCoreDataFolder().resolve("Addons");

        if (Files.notExists(addonsFolder))
            Files.createDirectories(addonsFolder);

        TreeMap<AddonDescription, Path> addons = new TreeMap<>((description, t1) -> {
            if (description.getRequiredAddons().contains(t1.getName()) || description.getAddonHooks().contains(t1.getName())) {
                return 1;
            } else {
                return -1;
            }
        });

        HashSet<String> addonNames = new HashSet<>();

        // Parsing and getting addon descriptions.
        try (Stream<Path> fileStream = Files.list(addonsFolder)) {
            fileStream.filter(file -> file.getFileName().toString().endsWith(".jar")).forEach(jar -> {
                try {
                    AddonDescription description = new AddonDescription(jar);
                    String name = description.getName();

                    // Checking if an addon with the same name was registered before.
                    if (addonNames.contains(name)) {
                        corePMS.getCoreLogger().log("&eTwo addons with the name '" + name + "' were found, only registering the first one.", ConsoleLogger.Level.WARN);
                    } else {
                        String addon = name.toLowerCase().contains("addon") ? name : name + " addon";

                        // Checking if addon api-version is compatible.
                        if (description.getApiVersion().compareTo(PlayMoreSounds.version) > 0) {
                            corePMS.getCoreLogger().log("&c" + addon + " was made for PlayMoreSounds v" + description.getApiVersion() + ". You are currently on " + PlayMoreSounds.version + ".", ConsoleLogger.Level.WARN);
                        } else {
                            // Checking if server has the required plugins by this addon.
                            if (serverPlugins.containsAll(description.getRequiredPlugins())) {
                                addons.put(description, jar);
                                addonNames.add(name);
                            } else {
                                corePMS.getCoreLogger().log("&c" + addon + " depends on the plugin(s): " + description.getRequiredPlugins(), ConsoleLogger.Level.WARN);
                            }
                        }
                    }
                } catch (InvalidAddonException e) {
                    corePMS.getCoreLogger().log("&c" + e.getMessage(), ConsoleLogger.Level.WARN);
                } catch (Exception e) {
                    corePMS.getCoreLogger().log("&cException while registering the addon '" + jar.getFileName() + "&e': " + e.getMessage(), ConsoleLogger.Level.WARN);
                    corePMS.getCoreErrorLogger().report(e, "Path: " + jar.toAbsolutePath() + "\nRegister as addon exception:");
                }
            });
        }

        // Removing addons that are missing dependencies.
        addons.keySet().removeIf(description -> {
            if (!addonNames.containsAll(description.getRequiredAddons())) {
                corePMS.getCoreLogger().log("&c" + description.getName() + " depends on the other addon(s): " + description.getRequiredAddons(), ConsoleLogger.Level.WARN);
                addonNames.remove(description.getName());
                return true;
            }

            return false;
        });

        // Instantiating addons main class.
        addons.forEach((description, jar) -> {
            String name = description.getName();

            try {
                addonClassLoaders.add(new AddonClassLoader(corePMS, jar, description));
            } catch (InvalidAddonException e) {
                corePMS.getCoreLogger().log("&c" + e.getMessage(), ConsoleLogger.Level.WARN);
            } catch (Exception e) {
                corePMS.getCoreLogger().log("&cException while initializing " + name + " addon. Please contact the addon author(s): " + description.getAuthors(), ConsoleLogger.Level.WARN);
                corePMS.getCoreErrorLogger().report(e, "Addon Author(s): " + description.getAuthors() + "\nPath: " + jar.toAbsolutePath() + "\nInstantiate main class exception:");
            }
        });
    }

    /**
     * Starts unloaded addons that have the specified start time on PMSAddon Runner thread.
     *
     * @param startTime The start time to start the addons.
     */
    public void startAddons(@NotNull StartTime startTime)
    {
        if (!addonClassLoaders.isEmpty()) {
            HashSet<PMSAddon> toStart = new HashSet<>();

            for (PMSAddon addon : getAddons())
                if (!addon.started && !addon.stopped && addon.getDescription().getStartTime() == startTime)
                    toStart.add(addon);

            if (!toStart.isEmpty())
                new Thread(() -> {
                    for (PMSAddon addon : toStart)
                        callOnStart(addon);
                }, "PMSAddon Runner").start();
        }
    }

    /**
     * Starts the specified addon on the main thread if it was not started yet.
     *
     * @param addon The addon to start.
     */
    public void startAddon(@NotNull PMSAddon addon)
    {
        if (!addon.started && !addon.stopped)
            callOnStart(addon);
    }

    private void callOnStart(@NotNull PMSAddon addon)
    {
        String name = addon.getDescription().getName();

        corePMS.getCoreLogger().log("&eStarting " + name + " v" + addon.getDescription().getVersion() + (name.toLowerCase().contains("addon") ? "." : " addon."));

        try {
            addon.onStart();
            addon.started = true;
            AddonEventManager.callLoadUnloadEvent(addon, corePMS);
        } catch (Exception ex) {
            corePMS.getCoreLogger().log("&cException while starting the addon '" + name + "': " + ex.getMessage(), ConsoleLogger.Level.WARN);
            corePMS.getCoreErrorLogger().report(ex, "Path: " + addon.getJar() + "\nStart addon exception:");
        }
    }

    /**
     * Stops the registered addons on PMSAddon Stopper thread.
     */
    public void stopAddons()
    {
        if (!addonClassLoaders.isEmpty()) {
            new Thread(() -> {
                for (PMSAddon addon : getAddons())
                    if (addon.started && !addon.stopped)
                        callOnStop(addon);
            }, "PMSAddon Stopper").start();
        }
    }

    /**
     * Stops the specified addon if it was not stopped yet on the main thread.
     */
    public void stopAddon(@NotNull PMSAddon addon)
    {
        if (addon.started && !addon.stopped)
            callOnStop(addon);
    }

    private void callOnStop(@NotNull PMSAddon addon)
    {
        String name = addon.getDescription().getName();

        corePMS.getCoreLogger().log("&eStopping " + name + " v" + addon.getDescription().getVersion() + (name.toLowerCase().contains("addon") ? "." : " addon."));

        try {
            addon.onStop();
            addon.stopped = true;
            addon.loaded = false;
            AddonEventManager.callLoadUnloadEvent(addon, corePMS);
        } catch (Exception ex) {
            corePMS.getCoreLogger().log("&cException while stopping the addon '" + name + "': " + ex.getMessage(), ConsoleLogger.Level.WARN);
            corePMS.getCoreErrorLogger().report(ex, "Path: " + addon.getJar() + "\nStop addon exception:");
        }
    }

    /**
     * @return An immutable set with all registered addons.
     */
    public @NotNull HashSet<PMSAddon> getAddons()
    {
        HashSet<PMSAddon> addons = new HashSet<>();

        addonClassLoaders.forEach(classLoader -> addons.add(classLoader.getAddon()));

        return addons;
    }
}
