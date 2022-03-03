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

package com.epicnicity322.playmoresounds.bukkit.sound;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.sound.SoundOptions;
import com.epicnicity322.playmoresounds.core.sound.SoundType;
import com.epicnicity322.playmoresounds.core.util.PMSHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public final class SoundManager
{
    private static final @NotNull HashMap<UUID, Boolean> soundStateCache = new HashMap<>();
    private static NamespacedKey soundState;

    private SoundManager()
    {
    }

    /**
     * Enables or Disables sounds of a {@link Player}.
     * <p>
     * Sounds that have the option {@link SoundOptions#ignoresDisabled()} will be played anyway.
     *
     * @param player The player to toggle the sounds.
     * @param state  The state of sounds: Enabled or Disabled.
     * @throws IllegalStateException If PlayMoreSounds is not instantiated yet.
     */
    public static void toggleSoundsState(@NotNull Player player, boolean state)
    {
        var uuid = player.getUniqueId();

        soundStateCache.put(uuid, state);

        if (soundState == null) {
            if (PlayMoreSounds.getInstance() == null)
                throw new IllegalStateException("PlayMoreSounds must be loaded to use this method.");

            soundState = new NamespacedKey(PlayMoreSounds.getInstance(), "sound_state");
        }

        player.getPersistentDataContainer().set(soundState, PersistentDataType.INTEGER, state ? 1 : 0);
    }

    /**
     * Gets sounds state of a {@link Player}, if they are enabled or disabled.
     *
     * @param player The player to get the state.
     * @return If sounds are enabled or disabled for this player.
     * @throws IllegalStateException If PlayMoreSounds is not instantiated yet.
     */
    public static boolean getSoundsState(@NotNull Player player)
    {
        var uuid = player.getUniqueId();
        Boolean state = soundStateCache.get(uuid);

        if (state == null) {
            if (soundState == null) {
                if (PlayMoreSounds.getInstance() == null)
                    throw new IllegalStateException("PlayMoreSounds must be loaded to use this method.");

                soundState = new NamespacedKey(PlayMoreSounds.getInstance(), "sound_state");
            }

            boolean persistentState = player.getPersistentDataContainer().getOrDefault(soundState, PersistentDataType.INTEGER, 1) == 1;
            soundStateCache.put(uuid, persistentState);
            return persistentState;
        } else {
            return state;
        }
    }

    /**
     * Stops currently playing sounds to the player.
     * <p>
     * Sounds with invalid namespaced keys which {@link PMSHelper#isNamespacedKey(String)} returns false, are ignored.
     *
     * @param player The player to stop the sounds.
     * @param sounds The sounds to stop or null if you want to stop all minecraft sounds, custom sounds are not supported for null.
     * @param delay  The delay to wait before stopping the sounds.
     * @throws IllegalStateException If PlayMoreSounds was not instantiated by bukkit yet.
     */
    public static void stopSounds(@NotNull Player player, @Nullable HashSet<String> sounds, long delay)
    {
        if (PlayMoreSounds.getInstance() == null)
            throw new IllegalStateException("PlayMoreSounds is not loaded.");

        if (sounds != null)
            sounds.removeIf(sound -> !PMSHelper.isNamespacedKey(sound));

        Runnable stopper = () -> {
            if (sounds == null)
                for (SoundType toStop : SoundType.getPresentSoundTypes())
                    // Sounds of #getSoundTypes() are always present.
                    player.stopSound(toStop.getSound().orElse(""));
            else
                for (String sound : sounds)
                    player.stopSound(sound);
        };

        if (delay <= 0) stopper.run();
        else Bukkit.getScheduler().runTaskLater(PlayMoreSounds.getInstance(), stopper, delay);
    }

    /**
     * Gets a collection of players inside a radius range.
     * <ul>
     * <li>Radius == -2 - All players in the world.</li>
     * <li>Radius == -1 - All players in the server.</li>
     * <li>Radius > 0   - All players that have their location's distance in blocks lower than the {@param radius}.</li>
     * <li>Otherwise    - Empty.</li>
     * </ul>
     *
     * @param radius   The range of blocks to get the players.
     * @param location The location to calculate the radius.
     * @return An immutable collection of players in this range.
     */
    public static @NotNull Collection<Player> getInRange(double radius, @NotNull Location location)
    {
        if (radius > 0) {
            radius = square(radius);
            var inRadius = new HashSet<Player>();

            for (var player : location.getWorld().getPlayers()) {
                if (distance(location, player.getLocation()) <= radius) {
                    inRadius.add(player);
                }
            }

            return inRadius;
        } else if (radius == -1) {
            // Creating new HashSet because Bukkit#getOnlinePlayers is not immutable.
            return new HashSet<>(Bukkit.getOnlinePlayers());
        } else if (radius == -2) {
            return location.getWorld().getPlayers();
        } else {
            return new HashSet<>();
        }
    }

    //Avoiding checks for different worlds.
    private static double distance(Location loc1, Location loc2)
    {
        return square(loc1.getX() - loc2.getX()) + square(loc1.getY() - loc2.getY()) + square(loc1.getZ() - loc2.getZ());
    }

    private static double square(double value)
    {
        return value * value;
    }
}
