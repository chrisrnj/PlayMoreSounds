package com.epicnicity322.playmoresounds.bukkit.sound;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class SoundManager
{
    private static final @NotNull HashSet<UUID> ignoredPlayers = new HashSet<>();
    private static @NotNull Set<String> soundList = new HashSet<>();
    private static @NotNull Set<SoundType> soundTypes = new HashSet<>();

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
     * Gets the names of the players that won't hear the sounds played by PlayMoreSounds.
     *
     * @return The players that won't hear sounds played by PlayMoreSounds.
     */
    public static @NotNull HashSet<UUID> getIgnoredPlayers()
    {
        return ignoredPlayers;
    }

    public static void stopSounds(@NotNull Player player, @Nullable HashSet<String> sounds, long delay)
    {
        if (PlayMoreSounds.getInstance() == null)
            throw new IllegalStateException("PlayMoreSounds must be loaded before using this method.");

        Bukkit.getScheduler().runTaskLater(PlayMoreSounds.getInstance(), () -> {
            if (VersionUtils.hasStopSound())
                if (sounds == null)
                    for (SoundType toStop : SoundManager.getSoundTypes())
                        // Sounds of #getSoundTypes() are always present.
                        player.stopSound(toStop.getSound().get());
                else
                    for (String sound : sounds)
                        player.stopSound(sound);
            else {
                // ENTITY_CHICKEN_HURT is always present.
                String chickenSound = SoundType.ENTITY_CHICKEN_HURT.getSound().get();

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
    public static @NotNull HashSet<Player> getInRange(double radius, @NotNull Location location)
    {
        HashSet<Player> players = new HashSet<>();

        if (radius < -1)
            players.addAll(location.getWorld().getPlayers());
        else if (radius < 0)
            players.addAll(Bukkit.getOnlinePlayers());
        else if (radius != 0)
            for (Player world : location.getWorld().getPlayers())
                if (location.distance(world.getLocation()) <= radius)
                    players.add(world);

        return players;
    }

    /**
     * Adds blocks to up, down, right, left, front, back from original sound location based on pitch and yaw.
     */
    protected static Location addRelativeLocation(Location soundLoc, Map<SoundOptions.Direction, Double> locations)
    {
        if (!locations.isEmpty()) {
            double front = locations.getOrDefault(SoundOptions.Direction.FRONT, 0.0);
            double back = locations.getOrDefault(SoundOptions.Direction.BACK, 0.0);
            double up = locations.getOrDefault(SoundOptions.Direction.UP, 0.0);
            double down = locations.getOrDefault(SoundOptions.Direction.DOWN, 0.0);
            double right = locations.getOrDefault(SoundOptions.Direction.RIGHT, 0.0);
            double left = locations.getOrDefault(SoundOptions.Direction.LEFT, 0.0);

            double FB = 0;

            if (front != 0 | back != 0)
                FB = front
                        + (Double.toString(back).startsWith("-") ? Double.parseDouble(Double.toString(back).substring(1))
                        : Double.parseDouble("-" + back));

            double UD = 0;

            if (up != 0 | down != 0)
                UD = up + (Double.toString(down).startsWith("-") ? Double.parseDouble(Double.toString(down).substring(1))
                        : Double.parseDouble("-" + down));

            double LR = 0;

            if (right != 0 | left != 0)
                LR = left
                        + (Double.toString(right).startsWith("-") ? Double.parseDouble(Double.toString(right).substring(1))
                        : Double.parseDouble("-" + right));

            double newX = soundLoc.getX();
            double newZ = soundLoc.getZ();

            float yaw = soundLoc.getYaw();

            if (FB != 0) {
                newX = (newX + (FB * Math.cos(Math.toRadians(yaw + 90))));
                newZ = (newZ + (FB * Math.sin(Math.toRadians(yaw + 90))));
            }
            if (LR != 0) {
                newX = (newX + (LR * Math.cos(Math.toRadians(yaw))));
                newZ = (newZ + (LR * Math.sin(Math.toRadians(yaw))));
            }

            return new Location(soundLoc.getWorld(), newX, soundLoc.getY() + UD, newZ);
        }
        return soundLoc;
    }
}
