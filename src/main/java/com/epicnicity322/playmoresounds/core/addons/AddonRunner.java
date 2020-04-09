package com.epicnicity322.playmoresounds.core.addons;

import com.epicnicity322.playmoresounds.core.PlayMoreSounds;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.logging.Level;

public class AddonRunner extends Thread
{
    @NotNull
    protected HashSet<PMSAddon> toStart = new HashSet<>();
    private PlayMoreSounds pms;
    private HashSet<String> plugins = new HashSet<>();

    public AddonRunner(PlayMoreSounds pms, HashSet<String> pluginNames)
    {
        super("PMSAddon Runner");
        this.pms = pms;
        plugins.addAll(pluginNames);
    }

    @Override
    public synchronized void run()
    {
        if (toStart != null) {
            for (PMSAddon addon : toStart) {
                if (plugins.containsAll(addon.getDescription().getRequiredPlugins())) {
                    if (pms.getAddonManager().getRegisteredAddonsNames().containsAll(addon.getDescription().getRequiredAddons())) {
                        try {
                            addon.loaded = true;
                            AddonEventManager.callLoadUnloadEvent(addon, true);
                            addon.onStart();
                            pms.getPMSLogger().log("&2+&e The addon " + addon.toString() +
                                    "&e v" + addon.getDescription().getVersion() + "&e was started.", Level.INFO);
                        } catch (Exception e) {
                            pms.getPMSLogger().log("&cSomething went wrong while starting the addon " + addon.toString() +
                                    "&c. Please report the generated log at PlayMoreSounds folder to the addon author(s): " +
                                    addon.getDescription().getAuthors().toString(), Level.SEVERE);
                            pms.getErrorLogger().report(e, "PMSAddon start error (" + addon.toString() + "):");
                        }
                    } else {
                        pms.getPMSLogger().log("&cThe addon " + addon.toString() +
                                "&c requires the following addons installed to work: " + addon.getDescription()
                                .getRequiredAddons().toString(), Level.SEVERE);
                    }
                } else {
                    pms.getPMSLogger().log("&cThe addon " + addon.toString() +
                            "&c requires the following plugins installed to work: " + addon.getDescription()
                            .getRequiredPlugins().toString(), Level.SEVERE);
                }
            }
        }

        toStart = new HashSet<>();
    }
}
