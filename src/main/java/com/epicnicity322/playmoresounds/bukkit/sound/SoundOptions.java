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

import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SoundOptions
{
    private boolean ignoresToggle;
    private @Nullable String permissionToListen;
    private @Nullable String permissionRequired;
    private double radius;
    private @NotNull Map<Direction, Double> relativeLocation = new HashMap<>();

    /**
     * {@link SoundOptions} is used to get the Options of a {@link Sound} more easily.
     *
     * @param ignoresToggle      If a player has toggled their sounds off, the sound will be played anyway.
     * @param permissionToListen The permission the player needs to listen this sound.
     * @param permissionRequired The permission the player needs to play this sound.
     * @param radius             A radius of blocks the sound will be heard. Set 0 to play to only the player, -1 to all
     *                           players online, -2 to all players in the {@link org.bukkit.World}.
     * @param relativeLocation   The location in blocks to be added to the final sound playing location, in relation to
     *                           where the player is looking.
     */
    public SoundOptions(boolean ignoresToggle, @Nullable String permissionToListen, @Nullable String permissionRequired,
                        double radius, @Nullable Map<Direction, Double> relativeLocation)
    {
        setIgnoresToggle(ignoresToggle);
        setPermissionToListen(permissionToListen);
        setPermissionRequired(permissionRequired);
        setRadius(radius);
        setRelativeLocation(relativeLocation);
    }

    /**
     * Create a {@link SoundOptions} based on a configuration section. In PlayMoreSounds this section is named 'Options',
     * it can have the following keys: Permission Required, Permission To Listen, Radius, Ignores Toggle,
     * Relative Location.UP, Relative Location.DOWN, Relative Location.FRONT, Relative Location.BACK,
     * Relative Location.LEFT and Relative Location.RIGHT. All of them are optional, see with more details what key does
     * what on PlayMoreSounds wiki.
     *
     * @param section The section where the options are.
     */
    public SoundOptions(@NotNull ConfigurationSection section)
    {
        setPermissionRequired(section.getString("Permission Required").orElse(null));
        setPermissionToListen(section.getString("Permission To Listen").orElse(null));

        radius = section.getNumber("Radius").orElse(0).doubleValue();
        ignoresToggle = section.getBoolean("Ignores Toggle").orElse(false);

        ConfigurationSection relativeLoc = section.getConfigurationSection("Relative Location");

        if (relativeLoc != null) {
            for (String s : relativeLoc.getNodes().keySet()) {
                try {
                    relativeLocation.put(Direction.valueOf(s.toUpperCase()), relativeLoc.getNumber(s).orElse(0).doubleValue());
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    /**
     * If Ignores Toggle option is enabled.
     *
     * @return If the sound should ignore if the player has disabled their sounds.
     */
    public boolean ignoresToggle()
    {
        return ignoresToggle;
    }

    public void setIgnoresToggle(boolean ignoresToggle)
    {
        this.ignoresToggle = ignoresToggle;
    }

    /**
     * Gets the value of Permission To Listen option.
     * <p>
     * The Permission To Listen option allows the sound to be played normally, but only who has this permission can hear
     * the sound.
     *
     * @return The permission the player needs to hear the sound.
     */
    public @Nullable String getPermissionToListen()
    {
        return permissionToListen;
    }

    public void setPermissionToListen(@Nullable String permissionToListen)
    {
        if (permissionToListen != null && permissionToListen.trim().isEmpty())
            this.permissionToListen = null;
        else
            this.permissionToListen = permissionToListen;
    }

    /**
     * Gets the value of Permission Required option.
     * <p>
     * The Permission Required option allows the sound to play only if the player has this permission.
     *
     * @return The permission the player needs to play the sound.
     */
    public @Nullable String getPermissionRequired()
    {
        return permissionRequired;
    }

    public void setPermissionRequired(@Nullable String permissionRequired)
    {
        if (permissionRequired != null && permissionRequired.trim().isEmpty())
            this.permissionRequired = null;
        else
            this.permissionRequired = permissionRequired;
    }

    /**
     * A radius says who will hear the sound.
     * If greater than 0 then everyone in that range of blocks will hear it,
     * if equal to 0 then the player who played the sound will hear it,
     * if equal to -1 then everyone in the server will hear it,
     * if equal to -2 then everyone in the world will hear it.
     *
     * @return The radius of the sound.
     */
    public double getRadius()
    {
        return radius;
    }

    public void setRadius(double radius)
    {
        this.radius = radius;
    }

    /**
     * Gets the Relative Location option as HashMap.
     *
     * @return The distance to add to the final sound location relative to where the player is looking.
     */
    public @NotNull Map<Direction, Double> getRelativeLocation()
    {
        return relativeLocation;
    }

    public void setRelativeLocation(@Nullable Map<Direction, Double> relativePositions)
    {
        if (relativePositions == null)
            this.relativeLocation = new HashMap<>();
        else
            this.relativeLocation = relativePositions;
    }

    /**
     * If a {@link SoundOptions} contains the same values of {@link #ignoresToggle()}, {@link #getRadius()},
     * {@link #getPermissionToListen()}, {@link #getPermissionRequired()} and {@link #getRelativeLocation()}.
     *
     * @param o The {@link SoundOptions} to compare.
     * @return If the {@link SoundOptions} has the same values as this one.
     */
    @Override
    public boolean equals(@Nullable Object o)
    {
        if (this == o) return true;
        if (!(o instanceof SoundOptions)) return false;

        SoundOptions options = (SoundOptions) o;

        return ignoresToggle() == options.ignoresToggle() &&
                Double.compare(options.getRadius(), getRadius()) == 0 &&
                Objects.equals(getPermissionToListen(), options.getPermissionToListen()) &&
                Objects.equals(getPermissionRequired(), options.getPermissionRequired()) &&
                getRelativeLocation().equals(options.getRelativeLocation());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ignoresToggle(), getPermissionToListen(), getPermissionRequired(), getRadius(), getRelativeLocation());
    }

    @Override
    public @NotNull String toString()
    {
        return "SoundOptions{" +
                "ignoresToggle=" + ignoresToggle +
                ", permissionToListen='" + permissionToListen + '\'' +
                ", permissionRequired='" + permissionRequired + '\'' +
                ", radius=" + radius +
                ", relativeLocation=" + relativeLocation +
                '}';
    }

    public enum Direction
    {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        FRONT,
        BACK
    }
}