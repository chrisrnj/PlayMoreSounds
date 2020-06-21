package com.epicnicity322.playmoresounds.core.addons;

import com.epicnicity322.playmoresounds.core.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.addons.events.AddonLoadUnloadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public final class AddonEventManager
{
    private static final @NotNull HashSet<AddonLoadUnloadEvent> registeredLoadUnloadEvents = new HashSet<>();

    private AddonEventManager()
    {
    }

    /**
     * Adds an event to run when an addon is loaded or unloaded.
     *
     * @param event The event to run.
     */
    public static void registerLoadUnloadEvent(AddonLoadUnloadEvent event)
    {
        registeredLoadUnloadEvents.add(event);
    }

    /**
     * Removes the event previously set to run on addon load unload.
     *
     * @param event The event to unregister.
     */
    public static void unregisterLoadUnloadEvent(AddonLoadUnloadEvent event)
    {
        registeredLoadUnloadEvents.remove(event);
    }

    protected static void callLoadUnloadEvent(PMSAddon addon, PlayMoreSounds corePMS)
    {
        for (AddonLoadUnloadEvent e : registeredLoadUnloadEvents)
            try {
                e.onLoadUnload(addon);
            } catch (Exception ex) {
                corePMS.getCoreLogger().log("&eException while calling addon load unload event: " + ex.getMessage());
                corePMS.getCoreErrorLogger().report(ex, "Call addon load unload event exception:");
            }
    }
}
