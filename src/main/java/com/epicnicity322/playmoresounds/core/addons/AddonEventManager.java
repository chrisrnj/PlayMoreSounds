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
     * Adds an event to run when an addon is loaded or unloaded successfully.
     *
     * @param event The event to run.
     */
    public static void registerLoadUnloadEvent(@NotNull AddonLoadUnloadEvent event)
    {
        registeredLoadUnloadEvents.add(event);
    }

    /**
     * Removes the event previously set to run on addon load unload.
     *
     * @param event The event to unregister.
     */
    public static void unregisterLoadUnloadEvent(@NotNull AddonLoadUnloadEvent event)
    {
        registeredLoadUnloadEvents.remove(event);
    }

    protected static void callLoadUnloadEvent(PMSAddon addon, PlayMoreSounds corePMS)
    {
        for (AddonLoadUnloadEvent event : registeredLoadUnloadEvents)
            try {
                event.onLoadUnload(addon);
            } catch (Exception ex) {
                corePMS.getCoreLogger().log("&eException while calling addon load unload event: " + ex.getMessage());
                corePMS.getCoreErrorLogger().report(ex, "Call addon load unload event exception:");
            }
    }
}
