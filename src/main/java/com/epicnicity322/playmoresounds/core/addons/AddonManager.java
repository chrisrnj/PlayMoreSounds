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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AddonManager
{
    protected static final @NotNull Set<AddonClassLoader> addonClassLoaders = ConcurrentHashMap.newKeySet();
    private static volatile boolean registered = false;
    private final @NotNull PlayMoreSounds corePMS;
    private final @NotNull LoadableHashSet<String> serverPlugins;

    public AddonManager(@NotNull PlayMoreSounds corePMS, @NotNull LoadableHashSet<String> serverPlugins)
    {
        this.corePMS = corePMS;
        this.serverPlugins = serverPlugins;
    }

    private static boolean hasRequiredAddons(Map<String, AddonDescription> addons, AddonDescription addon, LinkedHashSet<AddonDescription> sortedAddons)
    {
        if (addon.getRequiredAddons().isEmpty()) {
            sortedAddons.add(addon);
            return true;
        } else if (addons.keySet().containsAll(addon.getRequiredAddons())) {
            for (String dependency : addon.getRequiredAddons())
                if (!hasRequiredAddons(addons, addons.get(dependency), sortedAddons))
                    return false;

            addons.remove(addon.getName());
            sortedAddons.add(addon);
            return true;
        } else {
            addons.remove(addon.getName());
            return false;
        }
    }

    /**
     * Gets all proper addon jars from Addons folder and loads their classes on PMSAddon Runner thread. Addons can only be registered once.
     *
     * @throws IOException                   If it was not possible to read Addons folder.
     * @throws UnsupportedOperationException If addons were already registered.
     * @throws IllegalStateException         If the server has not registered all plugins yet.
     */
    public synchronized void registerAddons() throws IOException
    {
        if (registered)
            throw new UnsupportedOperationException("Addons were already registered.");
        else
            registered = true;

        if (!serverPlugins.isLoaded())
            throw new IllegalStateException("Can not register addons if the server has not registered all plugins yet.");

        Path addonsFolder = corePMS.getCoreDataFolder().resolve("Addons");

        if (Files.notExists(addonsFolder))
            Files.createDirectories(addonsFolder);

        // The jars in Addons folder
        Set<Path> jarPaths;

        try (Stream<Path> fileStream = Files.list(addonsFolder)) {
            jarPaths = fileStream.filter(file -> file.getFileName().toString().endsWith(".jar")).collect(Collectors.toSet());
        }

        HashMap<AddonDescription, Path> addons = new HashMap<>();
        HashMap<String, AddonDescription> addonNames = new HashMap<>();

        // Parsing and getting addon descriptions.
        for (Path jar : jarPaths) {
            try {
                AddonDescription description = new AddonDescription(jar);
                String name = description.getName();

                // Checking if an addon with the same name was registered before.
                if (addonNames.containsKey(name)) {
                    corePMS.getCoreLogger().log("&eTwo addons with the name '" + name + "' were found, only registering the first one.", ConsoleLogger.Level.WARN);
                } else {
                    String addon = name.toLowerCase().contains("addon") ? name : name + " addon";

                    if (description.getApiVersion().compareTo(PlayMoreSounds.version) > 0)
                        corePMS.getCoreLogger().log("&c" + addon + " was made for PlayMoreSounds v" + description.getApiVersion() + ". You are currently on " + PlayMoreSounds.version + ".", ConsoleLogger.Level.WARN);
                    else {
                        if (serverPlugins.containsAll(description.getRequiredPlugins())) {
                            addons.put(description, jar);
                            addonNames.put(name, description);
                        } else
                            corePMS.getCoreLogger().log("&c" + addon + " depends on the plugin(s): " + description.getRequiredPlugins(), ConsoleLogger.Level.WARN);
                    }
                }
            } catch (InvalidAddonException e) {
                corePMS.getCoreLogger().log("&c" + e.getMessage(), ConsoleLogger.Level.WARN);
            } catch (Exception e) {
                corePMS.getCoreLogger().log("&cException while registering the addon '" + jar.getFileName() + "&e': " + e.getMessage(), ConsoleLogger.Level.ERROR);
                corePMS.getCoreErrorLogger().report(e, "Path: " + jar.toAbsolutePath() + "\nRegister as addon exception:");
            }
        }

        LinkedHashSet<AddonDescription> sortedAddons = new LinkedHashSet<>();

        // Removing the addons that are missing required dependencies.
        addons.entrySet().removeIf(entry -> {
            AddonDescription description = entry.getKey();
            String name = description.getName();

            if (hasRequiredAddons(addonNames, description, sortedAddons)) {
                return false;
            } else {
                corePMS.getCoreLogger().log("&c" + (name.toLowerCase().contains("addon") ? name : name + " addon") + " depends on the other addon(s): " + description.getRequiredAddons(), ConsoleLogger.Level.WARN);
                return true;
            }
        });

        // Instantiating addons main class.
        sortedAddons.forEach(description -> {
            String name = description.getName();
            Path jar = addons.get(description);

            try {
                addonClassLoaders.add(new AddonClassLoader(jar, description));
            } catch (InvalidAddonException e) {
                corePMS.getCoreLogger().log("&c" + e.getMessage(), ConsoleLogger.Level.WARN);
            } catch (Exception e) {
                corePMS.getCoreLogger().log("&cException while initializing the addon '" + name + "': " + e.getMessage(), ConsoleLogger.Level.ERROR);
                corePMS.getCoreErrorLogger().report(e, "Path: " + jar.toAbsolutePath() + "\nInstantiate main class exception:");
            }
        });
    }

    /**
     * Starts unloaded addons that have the specified start time on PMSAddon Runner thread.
     *
     * @param startTime The start time to start the addons.
     */
    public synchronized void startAddons(@NotNull StartTime startTime)
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
    public synchronized void startAddon(@NotNull PMSAddon addon)
    {
        if (!addon.started && !addon.stopped)
            callOnStart(addon);
    }

    private void callOnStart(@NotNull PMSAddon addon)
    {
        String name = addon.getDescription().getName();

        corePMS.getCoreLogger().log("&eStarting " + (name.toLowerCase().contains("addon") ? name + "." : name + " addon."));
        addon.started = true;

        try {
            addon.onStart();
            addon.loaded = true;
            AddonEventManager.callLoadUnloadEvent(addon, corePMS);
        } catch (Exception ex) {
            corePMS.getCoreLogger().log("&cException while starting the addon '" + name + "': " + ex.getMessage(), ConsoleLogger.Level.WARN);
            corePMS.getCoreErrorLogger().report(ex, "Path: " + addon.getJar() + "\nStart addon exception:");
        }
    }

    /**
     * Stops the registered addons on PMSAddon Stopper thread.
     */
    public synchronized void stopAddons()
    {
        new Thread(() -> {
            for (PMSAddon addon : getAddons())
                if (addon.started && !addon.stopped)
                    callOnStop(addon);
        }, "PMSAddon Stopper").start();
    }

    /**
     * Stops the specified addon if it was not stopped yet on the main thread.
     */
    public synchronized void stopAddon(@NotNull PMSAddon addon)
    {
        if (addon.started && !addon.stopped)
            callOnStop(addon);
    }

    private void callOnStop(@NotNull PMSAddon addon)
    {
        String name = addon.getDescription().getName();

        corePMS.getCoreLogger().log("&eStopping " + (name.contains("addon") ? name + "." : name + " addon."));
        addon.stopped = true;

        try {
            addon.onStop();
            addon.loaded = false;
            AddonEventManager.callLoadUnloadEvent(addon, corePMS);
        } catch (Exception ex) {
            corePMS.getCoreLogger().log("&cException while stopping the addon '" + name + "': " + ex.getMessage(), ConsoleLogger.Level.WARN);
            corePMS.getCoreErrorLogger().report(ex, "Path: " + addon.getJar() + "\nStop addon exception:");
        }
    }

    /**
     * @return A immutable set with all registered addons.
     */
    public synchronized @NotNull HashSet<PMSAddon> getAddons()
    {
        HashSet<PMSAddon> addons = new HashSet<>();

        addonClassLoaders.forEach(classLoader -> addons.add(classLoader.getAddon()));

        return addons;
    }
}
