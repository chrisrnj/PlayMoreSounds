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

import com.epicnicity322.playmoresounds.core.sound.PMSSound;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class that provides methods for setting and getting sound toggle states of players.
 *
 * @param <P> The player class.
 */
public final class SoundsToggleState<P> {
    private static final @NotNull Set<UUID> enabledCache = ConcurrentHashMap.newKeySet();
    private final @NotNull PlayerUtil<P, ?> playerUtil;

    public SoundsToggleState(@NotNull PlayerUtil<P, ?> playerUtil) {
        this.playerUtil = playerUtil;
    }

    /**
     * Toggles the sounds of a player. If sounds are disabled, the player will not hear PlayMoreSounds' sounds, unless
     * the sound has {@link PMSSound.Options#ignoreToggleState()} enabled.
     *
     * @param player The player to toggle the sounds.
     * @param state  Whether the sounds are enabled or disabled.
     */
    public void setState(@NotNull P player, boolean state) {
        UUID uuid = playerUtil.uuid(player);

        if (state) enabledCache.add(uuid);
        else enabledCache.remove(uuid);
        playerUtil.saveStateOnPersistentData(player, state);
    }

    /**
     * Gets the state of sounds of a player.
     *
     * @param player The player to check if sounds are enabled.
     * @return Whether sounds are enabled for this player.
     */
    public boolean state(@NotNull P player) {
        UUID uuid = playerUtil.uuid(player);
        if (enabledCache.contains(uuid)) return true;

        Boolean state = playerUtil.getStateOnPersistentData(player);
        if (state == null || state) {
            enabledCache.add(uuid);
            return true;
        } else return false;
    }

    /**
     * Removes a player from the cached list of player sound toggle states.
     * <p>
     * Generally, it should be called when the player logs off the server.
     *
     * @param player The player to remove from cache.
     */
    public void clearCache(@NotNull UUID player) {
        enabledCache.remove(player);
    }
}
