package com.epicnicity322.playmoresounds.bukkit.sound;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SoundOptions
{
    private boolean ignoresToggle;
    private boolean eyeLocation;
    private String permissionToListen;
    private String permissionRequired;
    private double radius;
    private Map<Direction, Double> relativeLocation = new HashMap<>();

    /**
     * SoundOptions is used to get the Options of a PMSSound more easily.
     *
     * @param ignoresToggle      If a player has toggle their sounds to off, should the sound be played anyway?
     * @param eyeLocation        If the sound should be played in player's eye location.
     * @param permissionToListen The permission the player needs to has to listen to this sound.
     * @param permissionRequired The permission the player needs to has to play this sound.
     * @param radius             Greater than 0 to a range in blocks that the sound will be heard, 0 to the player, -1 to
     *                           everyone online or -2 to everyone in the world.
     * @param relativeLocation   This position will be added to the final sound location relative to where the player is looking.
     */
    public SoundOptions(boolean ignoresToggle, boolean eyeLocation, @Nullable String permissionToListen,
                        @Nullable String permissionRequired, double radius, @Nullable Map<Direction, Double> relativeLocation)
    {
        setIgnoresToggle(ignoresToggle);
        setIfEyeLocation(eyeLocation);
        setPermissionToListen(permissionToListen);
        setPermissionRequired(permissionRequired);
        setRadius(radius);
        setRelativeLocation(relativeLocation);
    }

    /**
     * Create a SoundOptions based on a configuration section. In PlayMoreSounds this section is named 'Options', they
     * must have the following keys: Permission Required, Permission To Listen, Radius, Eye Location, Ignores Toggle,
     * Relative Location.UP, Relative Location.DOWN, Relative Location.FRONT, Relative Location.BACK,
     * Relative Location.LEFT and Relative Location.RIGHT. All of them are optional, see from more details what key does
     * what on PlayMoreSounds wiki.
     *
     * @param section The section where the options are.
     */
    public SoundOptions(@NotNull ConfigurationSection section)
    {
        Validate.notNull(section, "section is null");

        if (section.contains("Permission Required")) {
            setPermissionRequired(section.getString("Permission Required"));
        }

        if (section.contains("Permission To Listen")) {
            setPermissionToListen(section.getString("Permission To Listen"));
        }

        radius = section.getDouble("Radius");
        eyeLocation = section.getBoolean("Eye Location");
        ignoresToggle = section.getBoolean("Ignores Toggle");

        if (section.contains("Relative Location")) {
            ConfigurationSection relativeLoc = section.getConfigurationSection("Relative Location");

            for (String s : relativeLoc.getKeys(false)) {
                try {
                    relativeLocation.put(Direction.valueOf(s.toUpperCase()), relativeLoc.getDouble(s));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    /**
     * Checks if Ignores Toggle option is enabled.
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
     * Checks if Eye Location option is enabled.
     *
     * @return If the sound should be played in the player's eye location.
     */
    public boolean isEyeLocation()
    {
        return eyeLocation;
    }

    /**
     * Changes the value of Eye Location option.
     *
     * @param eyeLocation If the sound should be played in player's eye location.
     */
    public void setIfEyeLocation(boolean eyeLocation)
    {
        this.eyeLocation = eyeLocation;
    }

    /**
     * Gets the value of Permission Listen option.
     * <p>
     * The Permission Listen option allows the sound to be played normally, but only those who have this permission can
     * hear the sound.
     *
     * @return The permission the player needs to hear the sound.
     */
    @Nullable
    public String getPermissionToListen()
    {
        return permissionToListen;
    }

    public void setPermissionToListen(@Nullable String permissionToListen)
    {
        this.permissionToListen = permissionToListen;

        if (permissionToListen != null && permissionToListen.equals("")) {
            this.permissionToListen = null;
        }
    }

    /**
     * Gets the value of Permission Required option.
     * <p>
     * The Permission Required option only allows the sound to play if the player has this permission, unlike the
     * Permission Listen option that plays the sound anyway, but only those who have permission to listen can hear it.
     *
     * @return The permission the player needs to play the sound.
     */
    @Nullable
    public String getPermissionRequired()
    {
        return permissionRequired;
    }

    public void setPermissionRequired(@Nullable String permissionRequired)
    {
        this.permissionRequired = permissionRequired;

        if (permissionRequired != null && permissionRequired.equals("")) {
            this.permissionRequired = null;
        }
    }

    /**
     * A radius says who will hear the sound. If it's greater than 0 then everyone in that range of block will hear it,
     * if it's 0 then the player who played the sound will hear it, if it's -1 then everyone in the server will hear it,
     * if it's -2 then everyone in the world will hear it.
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
    @NotNull
    public Map<Direction, Double> getRelativeLocation()
    {
        return relativeLocation;
    }

    public void setRelativeLocation(@Nullable Map<Direction, Double> relativePositions)
    {
        if (relativePositions == null) {
            this.relativeLocation = new HashMap<>();
        } else {
            this.relativeLocation = relativePositions;
        }
    }

    /**
     * If a SoundOptions contains the same values of {@link #ignoresToggle()}, {@link #isEyeLocation()},
     * {@link #getRadius()}, {@link #getPermissionToListen()}, {@link #getPermissionRequired()} and
     * {@link #getRelativeLocation()}.
     *
     * @param o The SoundOptions to compare.
     * @return If the SoundOptions has the same values as this one.
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof SoundOptions)) return false;

        SoundOptions options = (SoundOptions) o;

        return ignoresToggle() == options.ignoresToggle() &&
                isEyeLocation() == options.isEyeLocation() &&
                Double.compare(options.getRadius(), getRadius()) == 0 &&
                Objects.equals(getPermissionToListen(), options.getPermissionToListen()) &&
                Objects.equals(getPermissionRequired(), options.getPermissionRequired()) &&
                Objects.equals(getRelativeLocation(), options.getRelativeLocation());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ignoresToggle(), isEyeLocation(), getPermissionToListen(), getPermissionRequired(), getRadius(), getRelativeLocation());
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