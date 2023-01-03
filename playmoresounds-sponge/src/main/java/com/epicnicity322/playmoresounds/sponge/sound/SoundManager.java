package com.epicnicity322.playmoresounds.sponge.sound;

import com.epicnicity322.playmoresounds.core.sound.ChildSound;
import com.epicnicity322.playmoresounds.core.sound.Options;
import com.epicnicity322.playmoresounds.core.sound.Sound;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.plugin.PluginContainer;

import java.util.*;

public final class SoundManager {
    @NotNull
    private final PluginContainer plugin;

    public SoundManager(@NotNull PluginContainer plugin) {
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
    public static @NotNull Collection<ServerPlayer> getInRange(double radius, @NotNull ServerLocation location) {
        if (radius > 0.0) {
            radius = square(radius);
            var inRadius = new HashSet<ServerPlayer>();

            for (ServerPlayer player : location.world().players()) {
                if (distance(location, player.serverLocation()) <= radius) {
                    inRadius.add(player);
                }
            }

            return inRadius;
        } else if (radius == -1.0) {
            if (Sponge.isServerAvailable()) {
                return Sponge.server().onlinePlayers();
            } else {
                return new HashSet<>();
            }
        } else if (radius < -1.0) {
            return location.world().players();
        } else {
            return new HashSet<>();
        }
    }

    private static double distance(@NotNull ServerLocation loc1, @NotNull ServerLocation loc2) {
        return square(loc1.x() - loc2.x()) + square(loc1.y() - loc2.y()) + square(loc1.z() - loc2.z());
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
     *     <li>The source player is not in {@link GameModes#SPECTATOR} game mode;</li>
     *     <li>The source player does not have {@link PotionEffectTypes#INVISIBILITY} effect and the permission 'playmoresounds.bypass.invisibility';</li>
     *     <li>The listener can see the source player through {@link ServerPlayer#canSee(Entity)} method;</li>
     *     <li>The listener has the {@link Options#permissionToListen()};</li>
     *     <li>The listener has their sounds enabled.</li>
     * </ul>
     *
     * @param sound The sound to be played.
     * @param player The source player that will play the sound.
     * @return A list of sound results for each {@link ChildSound} of this {@link Sound}.
     * @see #play(Sound, ServerPlayer, ServerLocation)
     */
    @NotNull
    public List<SoundResult> play(@NotNull Sound sound, @NotNull ServerPlayer player) {
        return play(sound, player, player.serverLocation());
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
    public List<SoundResult> play(@NotNull Sound sound, @NotNull ServerLocation location) {
        return play(sound, null, location);
    }

    /**
     * Plays a sound to a player in a specific location. If a player is specified, the sound will only be played if this
     * player has the {@link Options#permissionRequired()}.
     * <p>
     * Sounds can be heard by other non-specified players under certain conditions:
     * <ul>
     *     <li>The sound's radius is not 0.0;</li>
     *     <li>If there is a source player and they're not in {@link GameModes#SPECTATOR} game mode;</li>
     *     <li>If there is a source player and they do not have {@link PotionEffectTypes#INVISIBILITY} effect and the permission 'playmoresounds.bypass.invisibility';</li>
     *     <li>If there is a source player and the listener can see them through {@link ServerPlayer#canSee(Entity)} method;</li>
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
    public List<SoundResult> play(@NotNull Sound sound, @Nullable ServerPlayer player, @NotNull ServerLocation location) {
        if (!sound.enabled()) return Collections.emptyList();

        var results = new ArrayList<SoundResult>(sound.childSounds().size());

        for (ChildSound child : sound.childSounds()) {
            Options options = child.options();
            final Collection<ServerPlayer> listeners;

            if (player != null) {
                String permission = options.permissionRequired();

                if (permission != null && !player.hasPermission(permission)) {
                    continue;
                }

                // Sound should only be played to the source player if radius is 0, the game mode is spectator, or if they are valid to be in invisibility mode.
                if (options.radius() == 0.0 || player.gameMode().get().equals(GameModes.SPECTATOR.get()) || ignoreInvisible(player)) {
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
                results.add(new SoundResult(listeners,
                        Sponge.server().scheduler().submit(Task.builder()
                                .execute(() -> prePlay(child, player, listeners, location))
                                .delay(Ticks.of(options.delay()))
                                .plugin(plugin)
                                .build())));
            }
        }

        return results;
    }

    private void prePlay(@NotNull ChildSound child, @Nullable ServerPlayer sourcePlayer, @NotNull Collection<ServerPlayer> listeners, @NotNull ServerLocation soundLocation) {
        Options options = child.options();
        boolean global = options.radius() < 0.0;

        // Playing the sound to the valid listeners.
        for (ServerPlayer listener : listeners) {
            if (//(options.ignoreToggle() || SoundManager.getSoundsState(listener)) &&
                    (options.permissionToListen() == null || listener.hasPermission(options.permissionToListen())) &&
                            (sourcePlayer == null || listener.canSee(sourcePlayer))) {
                ServerLocation location = global ? listener.serverLocation() : soundLocation;

                listener.playSound(net.kyori.adventure.sound.Sound.sound(Key.key(child.sound()), child.category().asKyori(), child.volume(), child.pitch()),
                        new Vector3d(location.x(), location.y(), location.z()));
            }
        }
    }

    private boolean ignoreInvisible(@NotNull ServerPlayer player) {
        if (!player.hasPermission("playmoresounds.bypass.invisibility")) return false;

        for (PotionEffect potionEffect : player.potionEffects()) {
            if (potionEffect.type().equals(PotionEffectTypes.INVISIBILITY.get())) {
                return true;
            }
        }
        return false;
    }

    public record SoundResult(@NotNull Collection<ServerPlayer> listeners, @Nullable ScheduledTask task) {
    }
}
