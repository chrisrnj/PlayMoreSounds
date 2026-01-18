/*
 * PlayMoreSounds - A Minecraft plugin that manages and plays sounds.
 * Copyright (C) 2023-2026 Christiano Rangel
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

package com.epicnicity322.playmoresounds.core.sound;

import com.epicnicity322.epicpluginlib.core.EpicPluginLib;
import com.epicnicity322.playmoresounds.core.location.Pos;
import com.epicnicity322.playmoresounds.core.location.WorldPos;
import com.epicnicity322.playmoresounds.core.util.PlayerUtil;
import com.epicnicity322.playmoresounds.core.util.SoundsToggleState;
import com.epicnicity322.playmoresounds.core.util.TaskFactory;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Provides methods for playing sounds, getting audience and toggling sound states.
 */
public final class SoundManager<P extends Audience & Sound.Emitter, E extends Sound.Emitter> {
    /**
     * Whether sounds should be scheduled to play at the same thread as an entity.
     */
    //TODO:
    private static final boolean SCHEDULE_SAME_THREAD_AS_ENTITY = EpicPluginLib.Platform.isFolia();
    /**
     * This value is used for global server radius.
     */
    private static final double RADIUS_SERVER_GLOBAL = PMSSound.Options.Radius.SERVER_GLOBAL.radius();
    /**
     * Values lower than this will count as global world radius.
     */
    private static final double RADIUS_WORLD_GLOBAL_LOWER_THAN = RADIUS_SERVER_GLOBAL;
    /**
     * This value is used for non-global world radius, must be lower than {@link #RADIUS_WORLD_GLOBAL_LOWER_THAN}.
     */
    private static final double RADIUS_WORLD_LOCAL = PMSSound.Options.Radius.WORLD_LOCAL.radius();

    private final @NotNull PlayerUtil<P, E> playerUtil;
    private final @NotNull SoundsToggleState<P> toggleStates;
    private final @NotNull TaskFactory<E> taskFactory;

    public SoundManager(@NotNull PlayerUtil<P, E> playerUtil, @NotNull SoundsToggleState<P> toggleStates, @NotNull TaskFactory<E> taskFactory) {
        this.playerUtil = playerUtil;
        this.toggleStates = toggleStates;
        this.taskFactory = taskFactory;
    }

    /**
     * Gets a collection of players inside a radius range.
     * <ul>
     * <li>Radius = 0  - Empty</li>
     * <li>Radius > 0  - All players in the world that are within a range of blocks the size of the {@param radius}.</li>
     * <li>Radius = -1 - All players in the server.</li>
     * <li>Radius < -1 - All players in the world.</li>
     * <li>Radius = -3 - Plays to all players in the world, but locally.</li>
     * </ul>
     *
     * @param radius The range of blocks to get the players.
     * @param pos    The location to calculate the radius.
     * @return A collection of players in this radius.
     * @see com.epicnicity322.playmoresounds.core.sound.PMSSound.Options.Radius
     */
    @NotNull
    public Collection<? extends P> radiusAudience(double radius, @NotNull WorldPos pos) {
        if (radius == RADIUS_SERVER_GLOBAL) {
            return playerUtil.onlinePlayers();
        } else if (radius < RADIUS_WORLD_GLOBAL_LOWER_THAN) {
            return playerUtil.worldPlayers(pos.world());
        } else {
            return playerUtil.playersInRange(radius, pos);
        }
    }

    private @NotNull TaskFactory.Task delayedTask(@Nullable P player, @NotNull Emitter<E> emitter, @NotNull Runnable task, long delay) {
        if (player != null) return taskFactory.delayedFor(playerUtil.asEntity(player), task, delay);
        else if (emitter.emitter() != null) return taskFactory.delayedFor(emitter.emitter(), task, delay);
        else return taskFactory.delayedLocal(emitter.location(), task, delay);
    }

    /**
     * Plays a following sound to a player. The sound will only be played if this player has the
     * {@link PMSSound.Options#permissionToPlay()}.
     * <p>
     * Sounds can be heard by other non-specified players under certain conditions:
     * <ul>
     *     <li>The sound's radius is not 0.0;</li>
     *     <li>The source player is not in spectator game mode;</li>
     *     <li>The source player does not have invisibility effect and the permission 'playmoresounds.bypass.invisibility';</li>
     *     <li>The listener is in range of the defined {@link #radiusAudience(double, WorldPos) radius}.</li>
     *     <li>The listener can see the source player;</li>
     *     <li>The listener has the {@link PMSSound.Options#permissionToListen()};</li>
     *     <li>The listener has their sounds enabled.</li>
     * </ul>
     *
     * @param soundList The sound to be played.
     * @param player    The source player that will play the sound.
     * @see #play(PMSSoundList, Audience, Emitter)
     */
    public void play(@NotNull PMSSoundList soundList, @NotNull P player) {
        play(soundList, player, playerUtil.asEmitter(player));
    }

    /**
     * Plays a sound at a specific location.
     * <p>
     * Sounds can be heard by players under certain conditions:
     * <ul>
     *     <li>The sound's radius is not 0.0;</li>
     *     <li>The listener is in range of the defined {@link #radiusAudience(double, WorldPos) radius}.</li>
     *     <li>The listener has the {@link PMSSound.Options#permissionToListen()};</li>
     *     <li>The listener has their sounds enabled.</li>
     * </ul>
     *
     * @param soundList The sound to be played.
     * @param emitter   The emitter of the sound, where it will be played.
     * @see #playAndGetResult(PMSSoundList, Audience, Emitter)
     */
    public void play(@NotNull PMSSoundList soundList, @NotNull Emitter<E> emitter) {
        if (!soundList.enabled()) return;

        for (PMSSound sound : soundList.sounds()) {
            PMSSound.Options options = sound.options();

            if (options.delay() == 0) playSound(sound, null, emitter, false);
            else delayedTask(null, emitter, () -> playSound(sound, null, emitter, false), options.delay());
        }
    }

    /**
     * Plays a sound to a player in a specific location. The sound will only be played if the source player has the
     * {@link PMSSound.Options#permissionToPlay()}.
     * <p>
     * Sounds can be heard by other non-specified players under certain conditions:
     * <ul>
     *     <li>The sound's radius is not 0.0;</li>
     *     <li>The source player is not in spectator game mode;</li>
     *     <li>The source player does not have invisibility effect and the permission 'playmoresounds.bypass.invisibility';</li>
     *     <li>The listener is in range of the defined {@link #radiusAudience(double, WorldPos) radius}.</li>
     *     <li>The listener can see the source player;</li>
     *     <li>The listener has the {@link PMSSound.Options#permissionToListen()};</li>
     *     <li>The listener has their sounds enabled.</li>
     * </ul>
     *
     * @param soundList The sound to be played.
     * @param player    The source player that will play the sound.
     * @param emitter   The emitter of the sound, where it will be played.
     * @see #playAndGetResult(PMSSoundList, Audience, Emitter)
     */
    public void play(@NotNull PMSSoundList soundList, @NotNull P player, @NotNull Emitter<E> emitter) {
        if (!soundList.enabled()) return;

        boolean othersHear = !playerUtil.isSpectator(player) && (!playerUtil.hasInvisibility(player) || !playerUtil.hasPermission(player, "playmoresounds.bypass.invisibility"));

        for (PMSSound sound : soundList.sounds()) {
            PMSSound.Options options = sound.options();
            String permission = options.permissionToPlay();
            if (permission != null && !playerUtil.hasPermission(player, permission)) continue;

            // Other players should not be allowed to hear if: the radius is 0, the source player is in spectator game
            //mode, or if the source player is valid to be in invisibility mode.
            boolean onlySource = !othersHear || options.radius() == 0;

            if (options.delay() == 0) playSound(sound, player, emitter, onlySource);
            else delayedTask(player, emitter, () -> playSound(sound, player, emitter, onlySource), options.delay());
        }
    }

    /**
     * Plays a sound to a player in a specific location. If a player is specified, the sound will only be played if this
     * player has the {@link PMSSound.Options#permissionToPlay()}.
     * <p>
     * Sounds can be heard by other non-specified players under certain conditions:
     * <ul>
     *     <li>The sound's radius is not 0.0;</li>
     *     <li>If there is a source player, and they're not in spectator game mode;</li>
     *     <li>If there is a source player, and they do not have invisibility effect and the permission 'playmoresounds.bypass.invisibility';</li>
     *     <li>The listener is in range of the defined {@link #radiusAudience(double, WorldPos) radius}.</li>
     *     <li>If there is a source player and the listener can see them;</li>
     *     <li>The listener has the {@link PMSSound.Options#permissionToListen()};</li>
     *     <li>The listener has their sounds enabled.</li>
     * </ul>
     *
     * @param soundList The sound to be played.
     * @param player    The source player that will play the sound.
     * @param emitter   The emitter of the sound, where it will be played.
     * @return A list of sound results for each {@link PMSSound} of this {@link PMSSoundList}.
     * @see SoundResult
     */
    @NotNull
    public List<SoundResult<? extends P>> playAndGetResult(@NotNull PMSSoundList soundList, @Nullable P player, @NotNull Emitter<E> emitter) {
        if (!soundList.enabled()) return Collections.emptyList();

        var results = new ArrayList<SoundResult<? extends P>>(soundList.sounds().size());
        boolean othersHear = player == null || (!playerUtil.isSpectator(player)
                && (!playerUtil.hasInvisibility(player) || !playerUtil.hasPermission(player, "playmoresounds.bypass.invisibility")));

        for (PMSSound child : soundList.sounds()) {
            PMSSound.Options options = child.options();

            // Other players should not be allowed to hear if: the radius is 0, the source player is in spectator game
            //mode, or if the source player is valid to be in invisibility mode.
            boolean onlySource = !othersHear || options.radius() == 0;

            if (player != null) {
                String permission = options.permissionToPlay();
                if (permission != null && !playerUtil.hasPermission(player, permission)) continue;
            }

            CompletableFuture<Collection<? extends P>> listeners = new CompletableFuture<>();

            if (options.delay() == 0) {
                listeners.complete(playSound(child, player, emitter, onlySource));
                results.add(new SoundResult<>(listeners, null));
            } else {
                results.add(new SoundResult<>(listeners, delayedTask(player, emitter, () -> listeners.complete(playSound(child, player, emitter, onlySource)), options.delay())));
            }
        }

        return results;
    }

    private @NotNull Collection<? extends P> playSound(@NotNull PMSSound pmsSound, @Nullable P sourcePlayer, @NotNull Emitter<E> soundLocation, boolean sourceListener) {
        Sound sound = pmsSound.sound();
        PMSSound.Options options = pmsSound.options();
        E emitter = soundLocation.emitter();
        WorldPos wPos = soundLocation.location();
        Pos pos = wPos == null ? null : wPos.pos();
        Pos offset = options.relativePosition();
        boolean global = options.radius() < 0.0 && options.radius() != RADIUS_WORLD_LOCAL;

        Collection<? extends P> listeners;

        if (sourceListener) listeners = Collections.singletonList(sourcePlayer);
        else listeners = radiusAudience(options.radius(), wPos != null ? wPos : playerUtil.wPos(emitter));

        if (offset == null) {
            Sound.Emitter newEmitter;
            if (global) {
                pos = null;
                newEmitter = Sound.Emitter.self();
            } else newEmitter = emitter;

            for (P listener : listeners) {
                validateListenerThenPlay(listener, sound, options, sourcePlayer, pos, newEmitter);
            }
        } else {
            if (!global) {
                if (pos == null)
                    pos = playerUtil.wPos(emitter).pos().addRelativePosition(offset, playerUtil.yaw(emitter));
                else pos = pos.addRelativePosition(offset, sourcePlayer != null ? playerUtil.yaw(sourcePlayer) : 0);
            }

            for (P listener : listeners) {
                Pos newPos = global ? playerUtil.pos(listener).addRelativePosition(offset, playerUtil.yaw(listener)) : pos;

                validateListenerThenPlay(listener, sound, options, sourcePlayer, newPos, null);
            }
        }

        return listeners;
    }

    @Contract("_,_,_,_,null,null -> fail")
    private void validateListenerThenPlay(@NotNull P listener, @NotNull Sound sound, @NotNull PMSSound.Options options, @Nullable P sourcePlayer, Pos pos, Sound.Emitter emitter) {
        if ((options.ignoreToggleState() || toggleStates.state(listener))
                && (options.permissionToListen() == null || playerUtil.hasPermission(listener, options.permissionToListen()))
                && (sourcePlayer == null || playerUtil.canSee(listener, sourcePlayer))) {

            if (pos == null) listener.playSound(sound, emitter);
            else listener.playSound(sound, pos.x(), pos.y(), pos.z());
        }
    }

    /**
     * The result of a played sound.
     *
     * @param listeners A future that is completed once the listeners from a sound are obtained. Immutability for the underlying list is not guaranteed.
     * @param task      The scheduled task of a delayed sound, null if there's no delay.
     */
    public record SoundResult<P extends Audience & Sound.Emitter>(
            @NotNull CompletableFuture<Collection<? extends P>> listeners, @Nullable TaskFactory.Task task) {
    }
}
