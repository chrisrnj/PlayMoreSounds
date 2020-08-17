package com.epicnicity322.playmoresounds.core.addons;

import com.epicnicity322.playmoresounds.core.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.addons.exceptions.InvalidAddonException;
import com.epicnicity322.playmoresounds.core.util.LoadableHashSet;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
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
            throw new UnsupportedOperationException("Addons are already registered.");
        else
            registered = true;

        if (!serverPlugins.isLoaded())
            throw new IllegalStateException("Can not register addons if the server has not registered all plugins yet.");

        Path addonsFolder = corePMS.getCoreDataFolder().resolve("Addons");

        if (!Files.exists(addonsFolder))
            Files.createDirectories(addonsFolder);

        Set<Path> jars;

        try (Stream<Path> paths = Files.list(addonsFolder)) {
            jars = paths.filter(file -> file.getFileName().toString().endsWith(".jar")).collect(Collectors.toSet());
        }

        Thread addonRegister = new Thread(() -> {
            HashMap<Path, AddonDescription> descriptions = new HashMap<>();
            HashSet<String> addonNames = new HashSet<>();

            // Parsing and getting addon descriptions.
            for (Path jar : jars) {
                try {
                    AddonDescription description = new AddonDescription(jar);
                    String name = description.getName();

                    // Checking if an addon with the same name was registered before.
                    if (addonNames.contains(name)) {
                        corePMS.getCoreLogger().log("&eTwo addons with the name '" + name + "' were found, only initializing the first one.");
                    } else {
                        String addon = name.toLowerCase().contains("addon") ? name : name + " addon";

                        if (description.getApiVersion().compareTo(PlayMoreSounds.version) > 0)
                            corePMS.getCoreLogger().log("&e" + addon + " was made for PlayMoreSounds v" + description.getApiVersion() + ". You are currently on " + PlayMoreSounds.version + ".");
                        else {
                            if (serverPlugins.containsAll(description.getRequiredPlugins())) {
                                descriptions.put(jar, description);
                                addonNames.add(name);
                            } else
                                corePMS.getCoreLogger().log("&e" + addon + " depends on the plugin(s): " + description.getRequiredPlugins());
                        }
                    }
                } catch (InvalidAddonException e) {
                    corePMS.getCoreLogger().log("&e" + e.getMessage());
                } catch (Exception e) {
                    corePMS.getCoreLogger().log("&eException while initializing the addon '" + jar.getFileName() + "&e': " + e.getMessage());
                    corePMS.getCoreErrorLogger().report(e, "Path: " + jar.toAbsolutePath() + "\nRegister as addon exception:");
                }
            }

            // Instantiating addons main class.
            descriptions.forEach((jar, description) -> {
                String name = description.getName();

                try {
                    if (addonNames.containsAll(description.getRequiredAddons()))
                        addonClassLoaders.add(new AddonClassLoader(description, jar));
                    else
                        corePMS.getCoreLogger().log("&e" + (name.toLowerCase().contains("addon") ? name : name + " addon") + " depends on the other addon(s): " + description.getRequiredAddons());
                } catch (Exception e) {
                    corePMS.getCoreLogger().log("&eException while initializing the addon '" + name + "&e': " + e.getMessage());
                    corePMS.getCoreErrorLogger().report(e, "Path: " + jar.toAbsolutePath() + "\nRegister as addon exception:");
                }
            });
        }, "PMSAddon Runner");

        addonRegister.start();

        try {
            addonRegister.join();
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Starts unloaded addons that have the specified start time on PMSAddon Runner thread.
     *
     * @param startTime The start time to start the addons.
     */
    public synchronized void startAddons(@NotNull StartTime startTime)
    {
        if (startTime == StartTime.HOOK_ADDONS || startTime == StartTime.HOOK_PLUGINS)
            throw new IllegalArgumentException("Hooking addons are started automatically.");

        if (!addonClassLoaders.isEmpty())
            new Thread(() -> {
                for (PMSAddon addon : getAddons())
                    if (!addon.started && !addon.stopped && addon.getDescription().getStartTime() == startTime)
                        callOnStart(addon);
            }, "PMSAddon Runner").start();
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
            corePMS.getCoreLogger().log("&eException while starting the addon '" + name + "&e': " + ex.getMessage());
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
            corePMS.getCoreLogger().log("&eException while stopping the addon '" + name + "&e': " + ex.getMessage());
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
