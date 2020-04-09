package com.epicnicity322.playmoresounds.core.addons;

import com.epicnicity322.playmoresounds.core.PlayMoreSounds;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class AddonManager
{
    private static final HashSet<AddonClassLoader> addonClassLoaders = new HashSet<>();
    private AddonRunner runner;
    private PlayMoreSounds pms;

    public AddonManager(PlayMoreSounds pms, HashSet<String> pluginNames)
    {
        runner = new AddonRunner(pms, pluginNames);
        this.pms = pms;
    }

    /**
     * Gets all proper addon jars from Addons folder and loads their classes. Addons can only be registered once.
     */
    public void registerAddons()
    {
        if (!addonClassLoaders.isEmpty()) {
            throw new IllegalStateException("Addons are already registered");
        }

        File addonsFolder = pms.getFolder().resolve("Addons").toFile();

        if (!addonsFolder.exists()) {
            if (!addonsFolder.mkdir()) {
                pms.getPMSLogger().log("&eUnable to create Addons folder in PlayMoreSounds data folder.", Level.WARNING);
                return;
            }
        }

        File[] jars = addonsFolder.listFiles((dir, name) -> name.endsWith(".jar"));

        if (jars != null) {
            for (File jar : jars) {
                try {
                    addonClassLoaders.add(new AddonClassLoader(pms, new AddonDescription(jar), jar.toPath()));
                } catch (Exception e) {
                    pms.getPMSLogger().log("&eException while parsing the addon '" + jar.getName() + "&e': " +
                            e.getMessage(), Level.SEVERE);
                    pms.getErrorLogger().report(e, "Path: " + jar.getAbsolutePath() + "\nRegister as addon exception:");
                }
            }
        }
    }

    /**
     * Removes unloaded addons from memory.
     *
     * @see #unregisterAddon(PMSAddon)
     */
    public void unregisterAddons()
    {
        for (AddonClassLoader loader : getAddonClassLoaders()) {
            if (!loader.getAddon().isLoaded()) {
                try {
                    loader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                loader.addon = null;
                addonClassLoaders.remove(loader);
            }
        }
    }

    /**
     * Removes an addon from memory whether is loaded or not.
     *
     * @param addon The addon you want to unregister.
     */
    public void unregisterAddon(PMSAddon addon)
    {
        for (AddonClassLoader loader : getAddonClassLoaders()) {
            if (loader.getAddon().equals(addon)) {
                try {
                    loader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                loader.addon = null;
                addonClassLoaders.remove(loader);
                break;
            }
        }
    }

    /**
     * Loads all addons in a specific time and prints messages to console.
     *
     * @param startTime Loads only addons that have this start time.
     */
    public void loadAddons(StartTime startTime)
    {
        HashSet<PMSAddon> addons = new HashSet<>();

        for (PMSAddon addon : getRegisteredAddons()) {
            if (!addon.isLoaded()) {
                if (startTime == StartTime.SERVER_LOAD_COMPLETE || addon.getDescription().getStartTime() == startTime) {
                    addons.add(addon);
                }
            }
        }

        runner.toStart.addAll(addons);
        runner.start();

        if (!startTime.name().startsWith("END") && !startTime.name().startsWith("HOOK")
                && startTime != StartTime.SERVER_LOAD_COMPLETE && runner.isAlive()) {
            try {
                runner.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Unloads all loaded addons that don't have the start-time set to HOOK_PLUGINS or HOOK_ADDONS and prints a message
     * to console.
     */
    public void unloadAddons()
    {
        for (PMSAddon addon : getRegisteredAddons()) {
            if (!addon.getDescription().getStartTime().name().startsWith("HOOK")) {
                try {
                    unloadAddon(addon);
                } catch (IllegalStateException ignored) {
                }
            }
        }
    }

    /**
     * Loads an addon and prints a message to console.
     *
     * @param addon The addon that you want to load.
     * @throws IllegalStateException If the addon is already loaded.
     */
    public void loadAddon(PMSAddon addon)
    {
        if (addon.isLoaded()) {
            throw new IllegalStateException("addon is already loaded.");
        } else {
            StartTime startTime = addon.getDescription().getStartTime();

            runner.toStart.add(addon);
            runner.start();

            if (!startTime.name().startsWith("END") && !startTime.name().startsWith("HOOK")
                    && startTime != StartTime.SERVER_LOAD_COMPLETE && runner.isAlive()) {
                try {
                    runner.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Unloads an addon and prints a message to console.
     *
     * @param addon The addon you want to unload.
     * @throws IllegalStateException If the addon is already unloaded.
     */
    public void unloadAddon(PMSAddon addon)
    {
        if (addon.isLoaded()) {
            new Thread("PMSAddon Runner")
            {
                @Override
                public void run()
                {
                    try {
                        AddonEventManager.callLoadUnloadEvent(addon, false);
                        addon.onStop();
                        pms.getPMSLogger().log("&4-&e The addon " + addon.toString() + "&e v" +
                                addon.getDescription().getVersion() + "&e was stopped.", Level.INFO);
                    } catch (Exception e) {
                        pms.getPMSLogger().log("&cSomething went wrong while stopping the addon " +
                                addon.toString(), Level.SEVERE);
                        e.printStackTrace();
                    }
                    addon.loaded = false;
                }
            }.start();
        } else {
            throw new IllegalStateException("addon is already unloaded");
        }
    }

    /**
     * Gets an addon main instance by name.
     *
     * @param name The name of the addon.
     * @return null if no addon was found with that name.
     */
    public PMSAddon getAddon(String name)
    {
        for (PMSAddon addon : getRegisteredAddons()) {
            if (addon.toString().equals(name)) {
                return addon;
            }
        }

        return null;
    }

    public Set<AddonClassLoader> getAddonClassLoaders()
    {
        return Collections.unmodifiableSet(addonClassLoaders);
    }

    public HashSet<PMSAddon> getRegisteredAddons()
    {
        HashSet<PMSAddon> addons = new HashSet<>();

        for (AddonClassLoader loader : getAddonClassLoaders()) {
            addons.add(loader.getAddon());
        }

        return addons;
    }

    public HashSet<String> getRegisteredAddonsNames()
    {
        HashSet<String> addons = new HashSet<>();

        for (AddonClassLoader loader : getAddonClassLoaders()) {
            addons.add(loader.getAddon().toString());
        }

        return addons;
    }
}
