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

import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsVersion;
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

public class AddonManager
{
    static final @NotNull Set<AddonClassLoader> addonClassLoaders = ConcurrentHashMap.newKeySet();
    private static final @NotNull AtomicBoolean registered = new AtomicBoolean(false);
    private final @NotNull LoadableHashSet<String> serverPlugins;
    private final @NotNull ConsoleLogger<?> logger;

    public AddonManager(@NotNull LoadableHashSet<String> serverPlugins, @NotNull ConsoleLogger<?> logger)
    {
        this.serverPlugins = serverPlugins;
        this.logger = logger;
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

        var addonsFolder = PlayMoreSoundsCore.getFolder().resolve("Addons");

        if (Files.notExists(addonsFolder))
            Files.createDirectories(addonsFolder);

        var addons = new TreeMap<AddonDescription, Path>((description, t1) -> {
            if (description.getRequiredAddons().contains(t1.getName()) || description.getAddonHooks().contains(t1.getName())) {
                return 1;
            } else {
                return -1;
            }
        });

        var addonNames = new HashSet<String>();

        // Parsing and getting addon descriptions.
        try (var fileStream = Files.list(addonsFolder)) {
            fileStream.filter(file -> file.getFileName().toString().endsWith(".jar")).forEach(jar -> {
                try {
                    var description = new AddonDescription(jar);
                    var name = description.getName();

                    // Checking if an addon with the same name was registered before.
                    if (addonNames.contains(name)) {
                        logger.log("&eTwo addons with the name '" + name + "' were found, only registering the first found.", ConsoleLogger.Level.WARN);
                    } else {
                        String addon = name.toLowerCase().contains("addon") ? name : name + " addon";

                        // Checking if addon api-version is compatible.
                        if (description.getApiVersion().compareTo(PlayMoreSoundsVersion.getVersion()) > 0) {
                            logger.log("&c" + addon + " was made for PlayMoreSounds v" + description.getApiVersion() + ". You are currently on " + PlayMoreSoundsVersion.version + ".", ConsoleLogger.Level.WARN);
                        } else {
                            // Checking if server has the required plugins by this addon.
                            if (serverPlugins.containsAll(description.getRequiredPlugins())) {
                                addons.put(description, jar);
                                addonNames.add(name);
                            } else {
                                logger.log("&c" + addon + " could not be loaded because it depends on the plugin(s): " + description.getRequiredPlugins(), ConsoleLogger.Level.WARN);
                            }
                        }
                    }
                } catch (InvalidAddonException e) {
                    logger.log("&c" + e.getMessage(), ConsoleLogger.Level.WARN);
                } catch (Exception e) {
                    logger.log("&cException while registering the addon '" + jar.getFileName() + "&e': " + e.getMessage(), ConsoleLogger.Level.WARN);
                    PlayMoreSoundsCore.getErrorHandler().report(e, "Path: " + jar.toAbsolutePath() + "\nRegister as addon exception:");
                }
            });
        }

        // Removing addons that are missing dependencies.
        addons.keySet().removeIf(description -> {
            if (!addonNames.containsAll(description.getRequiredAddons())) {
                var name = description.getName();
                logger.log("&c" + (name.toLowerCase().contains("addon") ? name : name + " addon") + " could not be loaded because it depends on the other addon(s): " + description.getRequiredAddons(), ConsoleLogger.Level.WARN);
                addonNames.remove(description.getName());
                return true;
            }

            return false;
        });

        // Instantiating addons main class.
        addons.forEach((description, jar) -> {
            var name = description.getName();

            try {
                addonClassLoaders.add(new AddonClassLoader(jar, description));
            } catch (InvalidAddonException e) {
                logger.log("&c" + e.getMessage(), ConsoleLogger.Level.WARN);
            } catch (Exception e) {
                logger.log("&cException while initializing " + (name.toLowerCase().contains("addon") ? name : name + " addon") + ". Please contact the addon author(s): " + description.getAuthors(), ConsoleLogger.Level.WARN);
                PlayMoreSoundsCore.getErrorHandler().report(e, "Addon Author(s): " + description.getAuthors() + "\nPath: " + jar.toAbsolutePath() + "\nInstantiate main class exception:");
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
        if (addonClassLoaders.isEmpty()) return;

        for (var addon : getAddons())
            if (addon.getDescription().getStartTime() == startTime && !addon.started && !addon.stopped)
                callOnStart(addon);
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
        var name = addon.getDescription().getName();

        logger.log("&eStarting " + name + " v" + addon.getDescription().getVersion() + (name.toLowerCase().contains("addon") ? "." : " addon."));

        try {
            addon.onStart();
            addon.started = true;
            addon.loaded = true;
            AddonEventManager.callLoadUnloadEvent(addon);
        } catch (Exception ex) {
            logger.log("&cException while starting the addon '" + name + "': " + ex.getMessage(), ConsoleLogger.Level.WARN);
            PlayMoreSoundsCore.getErrorHandler().report(ex, "Path: " + addon.getJar() + "\nStart addon exception:");
        }
    }

    /**
     * Stops all registered addons.
     */
    public void stopAddons()
    {
        if (addonClassLoaders.isEmpty()) return;

        for (PMSAddon addon : getAddons())
            if (addon.started && !addon.stopped)
                callOnStop(addon);

        AddonClassLoader.clearCaches();
    }

    /**
     * Stops the specified addon if it was not stopped yet.
     */
    public void stopAddon(@NotNull PMSAddon addon)
    {
        if (addon.started && !addon.stopped) {
            callOnStop(addon);
            AddonClassLoader.clearCaches(addon.getClassLoader());
        }
    }

    private void callOnStop(@NotNull PMSAddon addon)
    {
        var name = addon.getDescription().getName();

        logger.log("&eStopping " + name + " v" + addon.getDescription().getVersion() + (name.toLowerCase().contains("addon") ? "." : " addon."));

        try {
            addon.onStop();
            addon.stopped = true;
        } catch (Exception ex) {
            logger.log("&cException while stopping the addon '" + name + "': " + ex.getMessage(), ConsoleLogger.Level.WARN);
            PlayMoreSoundsCore.getErrorHandler().report(ex, "Path: " + addon.getJar() + "\nStop addon exception:");
        }

        try {
            addon.getClassLoader().close();
            addon.loaded = false;
            AddonEventManager.callLoadUnloadEvent(addon);
        } catch (IOException e) {
            logger.log("&cUnable to close '" + addon + "' addon class loader.", ConsoleLogger.Level.ERROR);
            PlayMoreSoundsCore.getErrorHandler().report(e, "Path: " + addon.getJar() + "\nAddonClassLoader close exception:");
        }
    }

    /**
     * @return An immutable set with all registered addons.
     */
    public @NotNull HashSet<PMSAddon> getAddons()
    {
        var addons = new HashSet<PMSAddon>();

        addonClassLoaders.forEach(classLoader -> addons.add(classLoader.getAddon()));

        return addons;
    }
}
