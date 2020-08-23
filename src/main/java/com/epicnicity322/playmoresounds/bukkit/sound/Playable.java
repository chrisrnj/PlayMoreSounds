package com.epicnicity322.playmoresounds.bukkit.sound;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Playable
{
    /**
     * Plays a sound in a specific location. If your {@link SoundOptions#getRadius()} is 0, then the sound is not played.
     *
     * @param sourceLocation The location where the sound will play. May change depending on
     *                       {@link SoundOptions#getRelativeLocation()} and {@link SoundOptions#isEyeLocation()}.
     * @throws IllegalStateException If the sound has a delay and PlayMoreSounds was not instantiated by bukkit yet.
     */
    default void play(@NotNull Location sourceLocation)
    {
        play(null, sourceLocation);
    }

    /**
     * Plays a sound to a specific player. Depending on your {@link SoundOptions#getRadius()}, the sound may be played to
     * other players too.
     *
     * @param player The player to play the sound.
     * @throws IllegalStateException If the sound has a delay and PlayMoreSounds was not instantiated by bukkit yet.
     */
    default void play(@NotNull Player player)
    {
        play(player, player.getLocation());
    }

    /**
     * Plays a sound to a specific player in a specific location. Depending on {@link SoundOptions#getRadius()}, the
     * sound may play to other players at different locations.
     *
     * @param player         The player to play the sound.
     * @param sourceLocation The location where the sound will play. May change depending on
     *                       {@link SoundOptions#getRelativeLocation()} and {@link SoundOptions#isEyeLocation()}.
     * @throws IllegalStateException If the sound has a delay and PlayMoreSounds was not instantiated by bukkit yet.
     */
    void play(@Nullable Player player, @NotNull Location sourceLocation);
}
