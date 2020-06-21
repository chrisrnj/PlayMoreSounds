package com.epicnicity322.playmoresounds.core.addons;

import com.epicnicity322.playmoresounds.core.PlayMoreSounds;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AddonManager
{
    protected static final @NotNull HashSet<AddonClassLoader> addonClassLoaders = new HashSet<>();
    private final @NotNull PlayMoreSounds corePMS;

    public AddonManager(@NotNull PlayMoreSounds corePMS)
    {
        this.corePMS = corePMS;
    }

    /**
     * Gets all proper addon jars from Addons folder and loads their classes. Addons can only be registered once.
     *
     * @throws IOException If it was not possible to read Addons folder.
     */
    public void registerAddons() throws IOException
    {
        if (!addonClassLoaders.isEmpty())
            throw new IllegalStateException("Addons are already registered");

        Path addonsFolder = corePMS.getCoreDataFolder().resolve("Addons");

        if (!Files.exists(addonsFolder))
            Files.createDirectories(addonsFolder);

        Set<Path> jars;

        try (Stream<Path> paths = Files.list(addonsFolder)) {
            jars = paths.filter(file -> file.getFileName().toString().endsWith(".jar")).collect(Collectors.toSet());
        }

        Thread addonRunner = new Thread(() -> jars.forEach(jar -> {
            try {
                addonClassLoaders.add(new AddonClassLoader(new AddonDescription(jar), jar));
            } catch (Exception e) {
                corePMS.getCoreLogger().log("&eException while initializing the addon '" + jar.getFileName() + "&e': " + e.getMessage());
                corePMS.getCoreErrorLogger().report(e, "Path: " + jar.toAbsolutePath() + "\nRegister as addon exception:");
            }
        }), "PMSAddon Runner");

        addonRunner.start();

        try {
            addonRunner.join();
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Starts unloaded addons that have the specified start time.
     *
     * @param startTime The start time to start the addons.
     */
    public void startAddons(@NotNull StartTime startTime)
    {
        if (startTime == StartTime.HOOK_ADDONS || startTime == StartTime.HOOK_PLUGINS)
            throw new IllegalArgumentException("Hooking addons are started automatically.");

        new Thread(() -> {
            for (PMSAddon addon : getAddons())
                if (!addon.started && !addon.stopped)
                    if (addon.getDescription().getStartTime() == startTime)
                        callOnStart(addon);
        }, "PMSAddon Runner").start();
    }

    /**
     * Starts the specified addon if it was not started yet.
     *
     * @param addon The addon to start.
     */
    public void startAddon(@NotNull PMSAddon addon)
    {
        if (!addon.started && !addon.stopped) {
            Thread addonRunner = new Thread(() -> callOnStart(addon), "PMSAddon Runner");
            StartTime startTime = addon.getDescription().getStartTime();

            addonRunner.start();

            if (startTime == StartTime.HOOK_PLUGINS || startTime == StartTime.HOOK_ADDONS)
                try {
                    addonRunner.join();
                } catch (InterruptedException ignored) {
                }
        }
    }

    private void callOnStart(@NotNull PMSAddon addon)
    {
        String name = addon.getDescription().getName();

        corePMS.getCoreLogger().log("&eStarting " + (name.contains("addon") ? name + "." : name + " addon."));

        try {
            addon.onStart();
        } catch (Exception ex) {
            corePMS.getCoreLogger().log("&eException while starting the addon '" + name + "&e': " + ex.getMessage());
            corePMS.getCoreErrorLogger().report(ex, "Path: " + addon.getJar() + "\nStart addon exception:");
        }

        addon.started = true;
        addon.loaded = true;
        AddonEventManager.callLoadUnloadEvent(addon, corePMS);
    }

    /**
     * Stops the current registered addons.
     */
    public void stopAddons()
    {
        new Thread(() -> {
            for (PMSAddon addon : getAddons()) {
                if (addon.started && !addon.stopped) {
                    StartTime startTime = addon.getDescription().getStartTime();

                    // HOOK_PLUGINS and HOOK_ADDONS are called automatically when the hooked addon or plugin is disabled.
                    if (startTime != StartTime.HOOK_PLUGINS && startTime != StartTime.HOOK_ADDONS)
                        callOnStop(addon);
                }
            }
        }, "PMSAddon Runner").start();
    }

    /**
     * Stops the specified addon if it was not stopped yet.
     */
    public void stopAddon(@NotNull PMSAddon addon)
    {
        if (addon.started && !addon.stopped) {
            Thread addonRunner = new Thread(() -> callOnStop(addon), "PMSAddon Runner");
            StartTime startTime = addon.getDescription().getStartTime();

            addonRunner.start();

            if (startTime == StartTime.HOOK_PLUGINS || startTime == StartTime.HOOK_ADDONS)
                try {
                    addonRunner.join();
                } catch (InterruptedException ignored) {
                }
        }
    }

    private void callOnStop(@NotNull PMSAddon addon)
    {
        String name = addon.getDescription().getName();

        corePMS.getCoreLogger().log("&eStopping " + (name.contains("addon") ? name + "." : name + " addon."));

        try {
            addon.onStop();
        } catch (Exception ex) {
            corePMS.getCoreLogger().log("&eException while stopping the addon '" + name + "&e': " + ex.getMessage());
            corePMS.getCoreErrorLogger().report(ex, "Path: " + addon.getJar() + "\nStop addon exception:");
        }

        addon.stopped = true;
        addon.loaded = false;
        AddonEventManager.callLoadUnloadEvent(addon, corePMS);
    }

    /**
     * @return A set with all registered addons.
     */
    public @NotNull HashSet<PMSAddon> getAddons()
    {
        HashSet<PMSAddon> addons = new HashSet<>();

        addonClassLoaders.forEach(classLoader -> addons.add(classLoader.getAddon()));

        return addons;
    }
}
