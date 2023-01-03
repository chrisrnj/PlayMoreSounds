/*
 * PlayMoreSounds - A minecraft plugin that manages and plays sounds.
 * Copyright (C) 2023 Christiano Rangel
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

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSoundsPlugin;
import com.epicnicity322.playmoresounds.core.sound.ChildSound;
import com.epicnicity322.playmoresounds.core.sound.Options;
import com.epicnicity322.playmoresounds.core.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class SoundManager {
    @NotNull
    private final PlayMoreSoundsPlugin plugin;

    public SoundManager(@NotNull PlayMoreSoundsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets a collection of players inside a radius range.
     * <ul>
     * <li>Radius = 0  - Empty</li>
     * <li>Radius > 0  - All players in the world that are within a range of blocks the size of the {@param radius}.</li>
     * <li>Radius = -1 - All players in the server.</li>
     * <li>Radius < -1 - All players in the world.</li>
     * </ul>
     *
     * @param radius   The range of blocks to get the players.
     * @param location The location to calculate the radius.
     * @return An immutable collection of players in this range.
     */
    public static @NotNull Collection<Player> getInRange(double radius, @NotNull Location location) {
        if (radius > 0.0) {
            radius = square(radius);
            var inRadius = new HashSet<Player>();

            for (var player : location.getWorld().getPlayers()) {
                if (distance(location, player.getLocation()) <= radius) {
                    inRadius.add(player);
                }
            }

            return inRadius;
        } else if (radius == -1.0) {
            // Creating new HashSet because Bukkit#getOnlinePlayers is not immutable.
            return new HashSet<>(Bukkit.getOnlinePlayers());
        } else if (radius < -1.0) {
            return location.getWorld().getPlayers();
        } else {
            return new HashSet<>();
        }
    }

    private static double distance(@NotNull Location loc1, @NotNull Location loc2) {
        return square(loc1.getX() - loc2.getX()) + square(loc1.getY() - loc2.getY()) + square(loc1.getZ() - loc2.getZ());
    }

    private static double square(double value) {
        return value * value;
    }

    /**
     * Plays a sound to a player. The sound will only be played if this player has the {@link Options#permissionRequired()}.
     * <p>
     * The sound is played at this player's location.
     * <p>
     * Sounds can be heard by other non-specified players under certain conditions:
     * <ul>
     *     <li>The sound's radius is not 0.0;</li>
     *     <li>The source player is not in {@link GameMode#SPECTATOR} game mode;</li>
     *     <li>The source player does not have {@link PotionEffectType#INVISIBILITY} effect and the permission 'playmoresounds.bypass.invisibility';</li>
     *     <li>The listener can see the source player through {@link Player#canSee(Player)} method;</li>
     *     <li>The listener has the {@link Options#permissionToListen()};</li>
     *     <li>The listener has their sounds enabled.</li>
     * </ul>
     *
     * @param sound The sound to be played.
     * @param player The source player that will play the sound.
     * @return A list of sound results for each {@link ChildSound} of this {@link Sound}.
     * @see #play(Sound, Player, Location)
     */
    @NotNull
    public List<SoundResult> play(@NotNull Sound sound, @NotNull Player player) {
        return play(sound, player, player.getLocation());
    }

    /**
     * Plays a sound in a specific location.
     * <p>
     * Sounds can be heard by other players under certain conditions:
     * <ul>
     *     <li>The sound's radius is not 0.0;</li>
     *     <li>The listener has the {@link Options#permissionToListen()};</li>
     *     <li>The listener has their sounds enabled.</li>
     * </ul>
     *
     * @param sound The sound to be played.
     * @param location The location the sound will be played.
     * @return A list of sound results for each {@link ChildSound} of this {@link Sound}.
     * @see SoundResult
     */
    @NotNull
    public List<SoundResult> play(@NotNull Sound sound, @NotNull Location location) {
        return play(sound, null, location);
    }

    /**
     * Plays a sound to a player in a specific location. If a player is specified, the sound will only be played if this
     * player has the {@link Options#permissionRequired()}.
     * <p>
     * Sounds can be heard by other non-specified players under certain conditions:
     * <ul>
     *     <li>The sound's radius is not 0.0;</li>
     *     <li>If there is a source player and they're not in {@link GameMode#SPECTATOR} game mode;</li>
     *     <li>If there is a source player and they do not have {@link PotionEffectType#INVISIBILITY} effect and the permission 'playmoresounds.bypass.invisibility';</li>
     *     <li>If there is a source player and the listener can see them through {@link Player#canSee(Player)} method;</li>
     *     <li>The listener has the {@link Options#permissionToListen()};</li>
     *     <li>The listener has their sounds enabled.</li>
     * </ul>
     *
     *
     * @param sound The sound to be played.
     * @param player The source player that will play the sound.
     * @param location The location the sound will be played.
     * @return A list of sound results for each {@link ChildSound} of this {@link Sound}.
     * @see SoundResult
     */
    @NotNull
    public List<SoundResult> play(@NotNull Sound sound, @Nullable Player player, @NotNull Location location) {
        if (!sound.enabled()) return Collections.emptyList();

        var results = new ArrayList<SoundResult>(sound.childSounds().size());

        for (ChildSound child : sound.childSounds()) {
            Options options = child.options();
            final Collection<Player> listeners;

            if (player != null) {
                String permission = options.permissionRequired();

                if (permission != null && !player.hasPermission(permission)) {
                    continue;
                }

                // Sound should only be played to the source player if radius is 0, the game mode is spectator, or if they are valid to be in invisibility mode.
                if (options.radius() == 0.0 || player.getGameMode() == GameMode.SPECTATOR || (player.hasPotionEffect(PotionEffectType.INVISIBILITY) && player.hasPermission("playmoresounds.bypass.invisibility"))) {
                    listeners = Collections.singleton(player);
                } else {
                    listeners = getInRange(options.radius(), location);
                }
            } else {
                listeners = getInRange(options.radius(), location);
            }

            if (options.delay() == 0) {
                prePlay(child, player, listeners, location);
                results.add(new SoundResult(listeners, null));
            } else {
                results.add(new SoundResult(listeners, plugin.getServer().getScheduler()
                        .runTaskLater(plugin, () -> prePlay(child, player, listeners, location), options.delay())));
            }
        }

        return results;
    }

    private void prePlay(@NotNull ChildSound child, @Nullable Player sourcePlayer, @NotNull Collection<Player> listeners, @NotNull Location soundLocation) {
        Options options = child.options();
        boolean global = options.radius() < 0.0;

        // Playing the sound to the valid listeners.
        for (Player listener : listeners) {
            if (//(options.ignoreToggle() || SoundManager.getSoundsState(listener)) &&
                    (options.permissionToListen() == null || listener.hasPermission(options.permissionToListen())) &&
                            (sourcePlayer == null || listener.canSee(sourcePlayer))) {
                listener.playSound(global ? listener.getLocation() : soundLocation, child.sound(), SoundCategory.valueOf(child.category().asBukkit()), child.volume(), child.pitch());
            }
        }
    }

    /**
     * The result of a played sound.
     * @param listeners The players who listened to the sound.
     * @param task The scheduled task of the sound, in case it has a delay.
     */
    public record SoundResult(@NotNull Collection<Player> listeners, @Nullable BukkitTask task) {
    }
}
