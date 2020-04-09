package com.epicnicity322.playmoresounds.bukkit.sound;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Playable
{
    /**
     * Plays a sound in a specific location. If your {@link SoundOptions#getRadius()} is 0, then the sound is not played
     * at all. Also, this does not handle {@link Player#canSee(Player)} and Invisibility effects since there is no player
     * to check.
     *
     * @param sourceLocation The location where the sound will take into account. May change depending
     *                       on {@link SoundOptions#getRelativeLocation()} and {@link SoundOptions#isEyeLocation()}.
     */
    void play(@NotNull Location sourceLocation);

    /**
     * Plays a sound to a specific player. Depending on your {@link SoundOptions#getRadius()}, the sound may be played to
     * other players too.
     *
     * @param player The player who triggered the sound.
     */
    void play(@NotNull Player player);

    /**
     * Plays a sound to a specific player in a specific location. Depending on your {@link SoundOptions#getRadius()},
     * the sound may be played to other players too at different locations.
     *
     * @param player         The player who triggered the sound.
     * @param sourceLocation The location where the sound will take into account. May change depending
     *                       on {@link SoundOptions#getRelativeLocation()} and {@link SoundOptions#isEyeLocation()}.
     */
    void play(@Nullable Player player, @NotNull Location sourceLocation);
}
