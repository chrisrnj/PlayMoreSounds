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
import com.epicnicity322.playmoresounds.core.addons.events.AddonLoadUnloadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public final class AddonEventManager {
    private static final @NotNull HashSet<AddonLoadUnloadEvent> registeredLoadUnloadEvents = new HashSet<>();

    private AddonEventManager() {
    }

    /**
     * Adds an event to run when an addon is loaded or unloaded successfully.
     *
     * @param event The event to run.
     */
    public static void registerLoadUnloadEvent(@NotNull AddonLoadUnloadEvent event) {
        registeredLoadUnloadEvents.add(event);
    }

    /**
     * Removes the event previously set to run on addon load unload.
     *
     * @param event The event to unregister.
     */
    public static void unregisterLoadUnloadEvent(@NotNull AddonLoadUnloadEvent event) {
        registeredLoadUnloadEvents.remove(event);
    }

    static void callLoadUnloadEvent(PMSAddon addon, ConsoleLogger<?> logger) {
        for (AddonLoadUnloadEvent event : registeredLoadUnloadEvents)
            try {
                event.onLoadUnload(addon);
            } catch (Throwable t) {
                logger.log("&eException while calling addon load unload event: " + t.getMessage());
                PlayMoreSoundsCore.getErrorHandler().report(t, "Call addon load unload event exception:");
            }
    }
}
