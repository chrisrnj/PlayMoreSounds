package com.epicnicity322.playmoresounds.bukkit.sound;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Map;

public class SoundManager
{
    /**
     * Gets all players in radius range, if radius = -1, returns all players on the server, if radius = -2 returns all
     * players on the world.
     *
     * @param radius   The range of blocks the players are in.
     * @param location The source location.
     * @return A set of players inside this range.
     */
    public static HashSet<Player> getInRange(double radius, Location location)
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
