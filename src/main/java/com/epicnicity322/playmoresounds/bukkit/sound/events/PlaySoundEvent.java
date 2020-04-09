package com.epicnicity322.playmoresounds.bukkit.sound.events;

import com.epicnicity322.playmoresounds.bukkit.sound.Sound;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundOptions;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

/**
 * This event is called for every player that hears a PlayMoreSounds sound. E.g., if a sound is played with -1 radius,
 * then this event is called for every people online in the server, because all of them hear the sound.
 *
 * @see PrePlaySoundEvent
 */
public class PlaySoundEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private Player sourcePlayer;
    private Player player;
    private HashSet<Player> otherListeners;
    private Location sourceLocation;
    private Location location;
    private Sound sound;

    public PlaySoundEvent(@NotNull Sound sound, @NotNull Player player, @NotNull Location location,
                          @NotNull HashSet<Player> otherListeners, @Nullable Player sourcePlayer,
                          @NotNull Location sourceLocation)
    {
        this.sourcePlayer = sourcePlayer;
        this.player = player;
        this.location = location;
        this.otherListeners = otherListeners;
        this.sourceLocation = sourceLocation;
        this.sound = sound;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    public HandlerList getHandlers()
    {
        return handlers;
    }

    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean value)
    {
        cancelled = value;
    }

    /**
     * Gets the player who played the sound.
     *
     * @return The player who played this sound.
     */
    @Nullable
    public Player getSourcePlayer()
    {
        return sourcePlayer;
    }

    /**
     * Gets the player who heard the sound.
     *
     * @return The player who is listening to this sound.
     */
    @NotNull
    public Player getPlayer()
    {
        return player;
    }

    /**
     * Gets all the other players that are hearing this sound.
     *
     * @return The players that are hearing this sound.
     */
    public HashSet<Player> getOtherListeners()
    {
        return otherListeners;
    }

    /**
     * Gets the location where the sound is played. This is the location where the source player played the sound.
     *
     * @return The location of the sound of the source player.
     */
    public Location getSourceLocation()
    {
        return sourceLocation;
    }

    /**
     * Changes the location where the sound is going to play.
     *
     * @param sourceLocation The location you want to change to.
     */
    public void setSourceLocation(@NotNull Location sourceLocation)
    {
        this.sourceLocation = sourceLocation;
    }

    /**
     * Gets the location where the sound is played. This is the location this sound is being played
     *
     * @return The location of the sound of the source player.
     */
    public Location getLocation()
    {
        return location;
    }

    /**
     * Gets the instance used to play the sound. You can change the {@link SoundOptions} but they wont be taken to
     * account, you may use this only to change the properties of {@link Sound}. You can also play the sound
     * again using this instance, making this event be called over and over... It's your choice.
     *
     * @return The instance of the sound.
     */
    public Sound getSound()
    {
        return sound;
    }
}
