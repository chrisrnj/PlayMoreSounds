/*
 * PlayMoreSounds - A Minecraft plugin that manages and plays sounds.
 * Copyright (C) 2026 Christiano Rangel
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

package com.epicnicity322.playmoresounds.core.util;

import com.epicnicity322.playmoresounds.core.listener.PMSListener;

/**
 * All sorts of utility methods to be used abstract of platform.
 */
public interface PlatformUtil {

    /**
     * Registers events of a listener, according to the current platform.
     *
     * @param listener The listener class to register events.
     * @param <L>      A PMSListener.
     */
    <L extends PMSListener> void registerEvents(L listener);

    /**
     * Unregisters the events of a listener.
     *
     * @param listener The listener class to unregister events.
     * @param <L>      A PMSListener.
     */
    <L extends PMSListener> void unregisterEvents(L listener);
}
