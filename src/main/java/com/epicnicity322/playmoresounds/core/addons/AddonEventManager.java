package com.epicnicity322.playmoresounds.core.addons;

import com.epicnicity322.playmoresounds.core.addons.events.AddonLoadUnloadEvent;

import java.util.HashSet;

public class AddonEventManager
{
    private static final HashSet<AddonLoadUnloadEvent> registeredLoadUnloadEvents = new HashSet<>();

    public static void registerLoadUnloadEvent(AddonLoadUnloadEvent e)
    {
        registeredLoadUnloadEvents.add(e);
    }

    protected static void callLoadUnloadEvent(PMSAddon addon, boolean isLoading)
    {
        for (AddonLoadUnloadEvent e : registeredLoadUnloadEvents) {
            e.onLoadUnload(addon, isLoading);
        }
    }
}
