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

package com.epicnicity322.playmoresounds.bukkit.sound;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

public final class SoundManager
{
    private static final @NotNull BukkitScheduler scheduler = Bukkit.getScheduler();
    private static final @NotNull HashSet<UUID> disabledSoundsPlayers = new HashSet<>();
    private static final @NotNull Pattern invalidSoundCharacters = Pattern.compile("[^a-z0-9/._-]");
    private static @NotNull Set<String> soundList = new HashSet<>();
    private static @NotNull Set<SoundType> soundTypes = new HashSet<>();
    private static NamespacedKey soundState;

    static {
        for (SoundType type : SoundType.values()) {
            if (type.getSound().isPresent()) {
                soundTypes.add(type);
                soundList.add(type.name());
            }
        }

        soundTypes = Collections.unmodifiableSet(soundTypes);
        soundList = Collections.unmodifiableSet(soundList);
    }

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
                PlayMoreSounds plugin = PlayMoreSounds.getInstance();

                if (plugin == null)
                    throw new IllegalStateException("PlayMoreSounds must be loaded to use this method.");

                soundState = new NamespacedKey(plugin, "sound_state");
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
     * Gets all {@link SoundType} names that are available in the version bukkit is running.
     *
     * @return The sounds available in this version.
     */
    public static @NotNull Set<String> getSoundList()
    {
        return soundList;
    }

    /**
     * Gets all {@link SoundType} that are available in the version bukkit is running.
     *
     * @return The sounds available in this version.
     */
    public static @NotNull Set<SoundType> getSoundTypes()
    {
        return soundTypes;
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
            sounds.removeIf(sound -> invalidSoundCharacters.matcher(sound).find());

        scheduler.runTaskLater(main, () -> {
            if (VersionUtils.hasStopSound())
                if (sounds == null)
                    for (SoundType toStop : SoundManager.getSoundTypes())
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
     * Gets all players in radius range, if radius = -1, returns all players on the server, if radius = -2 returns all
     * players on the world.
     *
     * @param radius   The range of blocks the players are in.
     * @param location The source location.
     * @return A set of players inside this range.
     */
    public static @NotNull Collection<Player> getInRange(double radius, @NotNull Location location)
    {
        if (radius < -1) {
            return location.getWorld().getPlayers();
        } else if (radius < 0) {
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
    protected static @NotNull Location addRelativeLocation(@NotNull Location location, @NotNull Map<SoundOptions.Direction, Double> locationToAdd)
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
