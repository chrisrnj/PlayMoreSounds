/*
 * PlayMoreSounds - A bukkit plugin that manages and plays sounds.
 * Copyright (C) 2021 Christiano Rangel
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

public enum StartTime
{
    /**
     * Before PlayMoreSounds has loaded commands.
     */
    BEFORE_COMMANDS,
    /**
     * Before PlayMoreSounds has loaded configurations.
     */
    BEFORE_CONFIGURATIONS,
    /**
     * Before PlayMoreSounds has loaded its sound listeners.
     */
    BEFORE_LISTENERS,
    /**
     * When PlayMoreSounds finishes starting up.
     */
    END,
    /**
     * When the server finishes the load of all plugins.
     */
    SERVER_LOAD_COMPLETE
}
