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

package com.epicnicity322.playmoresounds.bukkit.sound;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import com.epicnicity322.playmoresounds.core.sound.SoundOptions;
import com.epicnicity322.playmoresounds.core.sound.SoundType;
import com.epicnicity322.playmoresounds.core.util.PMSHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public final class SoundManager
{
    private static final @NotNull BukkitScheduler scheduler = Bukkit.getScheduler();
    private static final @NotNull HashSet<UUID> disabledSoundsPlayers = new HashSet<>();
    private static NamespacedKey soundState;

    private SoundManager()
    {
    }

    /**
     * Enables or Disables sounds of a {@link Player}.
     * <p>
     * Sounds that have the option {@link SoundOptions#ignoresDisabled()} will be played anyway.
     * If bukkit is running on 1.14+ the sounds will persist a server restart.
     *
     * @param player The player to toggle the sounds.
     * @param state  The state of sounds: Enabled or Disabled.
     * @throws IllegalStateException If the server is running on 1.14+ and PlayMoreSounds is disabled.
     */
    public static void toggleSoundsState(@NotNull Player player, boolean state)
    {
        if (VersionUtils.hasPersistentData()) {
            if (soundState == null) {
                if (PlayMoreSounds.getInstance() == null)
                    throw new IllegalStateException("PlayMoreSounds must be loaded to use this method.");

                soundState = new NamespacedKey(PlayMoreSounds.getInstance(), "sound_state");
            }

            player.getPersistentDataContainer().set(soundState, PersistentDataType.INTEGER, state ? 1 : 0);
        } else {
            if (state)
                disabledSoundsPlayers.remove(player.getUniqueId());
            else
                disabledSoundsPlayers.add(player.getUniqueId());
        }
    }

    /**
     * Gets sounds state of a {@link Player}, if they are enabled or disabled.
     *
     * @param player The player to get the state.
     * @return If sounds are enabled or disabled for this player.
     * @throws IllegalStateException If the server is running on 1.14+ and PlayMoreSounds is disabled.
     */
    public static boolean getSoundsState(@NotNull Player player)
    {
        if (VersionUtils.hasPersistentData()) {
            if (soundState == null) {
                PlayMoreSounds plugin = PlayMoreSounds.getInstance();

                if (plugin == null)
                    throw new IllegalStateException("PlayMoreSounds must be loaded to use this method.");

                soundState = new NamespacedKey(plugin, "sound_state");
            }

            return player.getPersistentDataContainer().getOrDefault(soundState, PersistentDataType.INTEGER, 1) == 1;
        } else {
            return !disabledSoundsPlayers.contains(player.getUniqueId());
        }
    }

    /**
     * Stops the currently playing sounds. If the server is running 1.10.2+, {@link Player#stopSound(String)} method is
     * used, if the server is running an older version, an old glitch of playing lots of sounds is used to stop the sounds.
     * If the server is running a version older than 1.10.2, you can not specify the sounds to stop as all
     * minecraft/resource pack sounds are stopped.
     *
     * @param player The player to stop the sound.
     * @param sounds The sounds to stop, null if you want to stop all minecraft sounds.
     * @param delay  The delay to wait before stopping the sounds.
     * @throws IllegalStateException If PlayMoreSounds was not instantiated by bukkit yet.
     */
    public static void stopSounds(@NotNull Player player, @Nullable HashSet<String> sounds, long delay)
    {
        PlayMoreSounds main = PlayMoreSounds.getInstance();

        if (main == null)
            throw new IllegalStateException("PlayMoreSounds is not loaded.");

        if (sounds != null)
            sounds.removeIf(sound -> !PMSHelper.isNamespacedKey(sound));

        scheduler.runTaskLater(main, () -> {
            if (VersionUtils.hasStopSound())
                if (sounds == null)
                    for (SoundType toStop : SoundType.getPresentSoundTypes())
                        // Sounds of #getSoundTypes() are always present.
                        player.stopSound(toStop.getSound().orElse(""));
                else
                    for (String sound : sounds)
                        player.stopSound(sound);
            else {
                // ENTITY_CHICKEN_HURT is always present.
                String chickenSound = SoundType.ENTITY_CHICKEN_HURT.getSound().orElse("");

                for (int i = 0; i < 70; ++i)
                    player.playSound(player.getLocation(), chickenSound, 1.0E-4f, 1.0f);
            }
        }, delay);
    }

    /**
     * Gets all players inside a radius range.
     * <p>
     * Radius < -1 - All players in the world.
     * <p>
     * Radius < 0  - All players in the server.
     * <p>
     * Radius > 0  - All players in this range of blocks.
     * <p>
     * Radius = 0  - Empty.
     *
     * @param radius   The range of blocks to get the players.
     * @param location The location to calculate the radius.
     * @return An immutable collection of players in this range.
     */
    public static @NotNull Collection<Player> getInRange(double radius, @NotNull Location location)
    {
        if (radius < -1) {
            return location.getWorld().getPlayers();
        } else if (radius < 0) {
            // Creating new HashSet because Bukkit#getOnlinePlayers is not immutable.
            return new HashSet<>(Bukkit.getOnlinePlayers());
        } else if (radius != 0) {
            HashSet<Player> players = new HashSet<>();

            for (Player player : location.getWorld().getPlayers()) {
                if (location.distanceSquared(player.getLocation()) <= radius) {
                    players.add(player);
                }
            }

            return players;
        } else {
            return new HashSet<>();
        }
    }

    /**
     * Adds blocks to up, down, right, left, front, back from original sound location based on pitch and yaw.
     */
    static @NotNull Location addRelativeLocation(@NotNull Location location, @NotNull Map<SoundOptions.Direction, Double> locationToAdd)
    {
        if (!locationToAdd.isEmpty()) {
            location = location.clone();

            Double leftRight = locationToAdd.get(SoundOptions.Direction.LEFT_RIGHT);
            Double frontBack = locationToAdd.get(SoundOptions.Direction.FRONT_BACK);
            Double upDown = locationToAdd.get(SoundOptions.Direction.UP_DOWN);
            double sin = 0;
            double cos = 0;

            if (leftRight != null) {
                double angle = Math.PI * 2 * location.getYaw() / 360;
                sin = Math.sin(angle);
                cos = Math.cos(angle);

                location.add(leftRight * cos, 0.0, leftRight * sin);
            }

            if (frontBack != null) {
                if (leftRight == null) {
                    double angle = Math.PI * 2 * location.getYaw() / 360 * -1;
                    sin = Math.sin(angle);
                    cos = Math.cos(angle);
                } else {
                    sin = sin * -1;
                    cos = cos * -1;
                }

                location.add(frontBack * sin, 0.0, frontBack * cos);
            }

            if (upDown != null)
                location.add(0.0, upDown, 0.0);
        }

        return location;
    }
}
