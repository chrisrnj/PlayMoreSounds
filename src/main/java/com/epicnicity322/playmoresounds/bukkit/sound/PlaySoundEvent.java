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

import com.epicnicity322.playmoresounds.core.sound.SoundOptions;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This event is called before a sound is played.
 */
public class PlaySoundEvent extends Event implements Cancellable
{
    private static final @NotNull HandlerList handlers = new HandlerList();
    private final @NotNull PlayableSound sound;
    private final @Nullable Player sourcePlayer;
    private final @NotNull Collection<Player> listeners;
    private final @NotNull Collection<Player> unmodifiableListeners;
    @NotNull Location location;
    private boolean cancelled;
    private boolean global;

    public PlaySoundEvent(@NotNull PlayableSound sound, @Nullable Player sourcePlayer, @NotNull Location location,
                          @NotNull Collection<Player> listeners, boolean global)
    {
        this.sound = sound;
        this.sourcePlayer = sourcePlayer;
        this.location = location;
        this.listeners = listeners;
        this.unmodifiableListeners = Collections.unmodifiableCollection(listeners);
        this.global = global;
    }

    public static @NotNull HandlerList getHandlerList()
    {
        return handlers;
    }

    /**
     * @return Whether the event is cancelled and the sound is not playing.
     */
    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }

    /**
     * Cancels this event, making so the sound does not play to the {@link #getListeners()}.
     *
     * @param cancelled If the sound should play.
     */
    @Override
    public void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return handlers;
    }

    /**
     * @return The sound that will be played.
     */
    public @NotNull PlayableSound getSound()
    {
        return sound;
    }

    /**
     * The source player that caused the sound to play. The source player may or may not hear the sound depending on
     * {@link #validateListener(Player)} result.
     *
     * @return The source player or null if this sound was not played by a player.
     */
    public @Nullable Player getSourcePlayer()
    {
        return sourcePlayer;
    }

    /**
     * The location the sound was asked to play. If {@link #playingGlobally()} is true, this location is ignored and
     * the sound will play at all listeners locations.
     *
     * @return The source location of the sound.
     */
    public final @NotNull Location getLocation()
    {
        return location.clone();
    }

    /**
     * Sets the location of this sound, this location will be ignored in case {@link #playingGlobally()} is true.
     *
     * @param location The location the sound should play, if this is not a global sound.
     * @throws IllegalArgumentException If the new location is in a different world than the previous.
     */
    public void setLocation(@NotNull Location location)
    {
        if (!Objects.equals(this.location.getWorld(), location.getWorld()))
            throw new IllegalArgumentException("New location world is not the same as previous location world.");

        this.location = location;
    }

    /**
     * The listeners of this sound. All players in this collection are players that are in the sound specified radius
     * ({@link SoundOptions#getRadius()}), but it doesn't mean all players in this collection will hear the sound,
     * because they may or may not be valid. Use {@link #validateListener(Player)} to validate if the player is a valid listener.
     * <p>
     * Even though the sound has a radius that's not 0, only the source player may be in this collection in case they are
     * in spectator mode or have invisibility effect and the permission 'playmoresounds.bypass.invisibility'.
     * <p>
     * This method does not return valid listeners because they are only validated right before the sound is played, in
     * order to avoid reiteration of the collection.
     *
     * @return An unmodifiable collection with the players in range to hear the sound.
     * @see #getValidListeners()
     */
    public @NotNull Collection<Player> getListeners()
    {
        return unmodifiableListeners;
    }

    /**
     * Adds a player to the collection of players that should listen to the sound. If {@link #validateListener(Player)}
     * returns false for this player, then they are not added and the sound is left unchanged.
     * <p>
     * Adding a valid listener that is in another world from {@link #getLocation()} makes the sound play globally.
     * <p>
     * Once the sound is set to play globally, it can not be undone.
     *
     * @param listener The player to add.
     * @see #getListeners()
     * @see #validateListener(Player)
     */
    public void addListener(@NotNull Player listener)
    {
        if (!validateListener(listener)) return;
        if (!location.getWorld().equals(listener.getWorld())) {
            global = true;
        }

        // Since #getListeners can or can not be a Set, performing remove method to avoid duplicates.
        listeners.remove(listener);
        listeners.add(listener);
    }

    /**
     * Removes a player from the collection of players that should listen to the sound.
     * <p>
     * Use this as your own way of validating a listener.
     *
     * @param listener The player to remove.
     * @see #getListeners()
     */
    public void removeListener(@NotNull Player listener)
    {
        listeners.remove(listener);
    }

    /**
     * Validates all listeners from {@link #getListeners()} through {@link #validateListener(Player)} method and adds
     * them to a {@link HashSet}.
     *
     * @return An immutable set containing all valid listeners of the playing sound.
     * @see #getListeners()
     */
    public @NotNull HashSet<Player> getValidListeners()
    {
        return listeners.stream().filter(this::validateListener).collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Whether this sound is playing at each of the listeners location, this happens when radius is -1 or -2, or any of
     * the players in {@link #getListeners()} are not in the sound's world.
     *
     * @return If the sound is playing globally to each listener location.
     */
    public final boolean playingGlobally()
    {
        return global;
    }

    /**
     * Validates if this player should hear the sound, that is if they have the sound's permission to listen, can see the
     * source player through {@link Player#canSee(Player)} (in case there is a source player), and if this sounds doesn't
     * ignore toggle and the player has their sounds toggled on.
     * <p>
     * This method is meant to be used in {@link #getListeners()} players, as it does not check for radius distance.
     * <p>
     * Even though the source player ({@link #getSourcePlayer()}) has played the sound, it doesn't mean they are valid to hear it.
     *
     * @param listener The player to validate as a listener for the sound.
     * @return If the player is a valid listener of {@link #getSound()}.
     */
    public boolean validateListener(@NotNull Player listener)
    {
        SoundOptions options = getSound().getOptions();

        return (options.ignoresDisabled() || SoundManager.getSoundsState(listener))
                && (options.getPermissionToListen() == null || listener.hasPermission(options.getPermissionToListen()))
                && (sourcePlayer == null || listener.canSee(sourcePlayer));
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof PlaySoundEvent that)) return false;

        return cancelled == that.cancelled &&
                global == that.global &&
                sound.equals(that.sound) &&
                Objects.equals(sourcePlayer, that.sourcePlayer) &&
                location.equals(that.location) &&
                listeners.equals(that.listeners);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(cancelled, sound, sourcePlayer, location, listeners, global);
    }
}
