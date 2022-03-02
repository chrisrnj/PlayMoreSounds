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

package com.epicnicity322.playmoresounds.core.sound;

import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SoundOptions
{
    private boolean ignoresDisabled;
    private @Nullable String permissionToListen;
    private @Nullable String permissionRequired;
    private double radius;

    /**
     * {@link SoundOptions} is used to get the Options of a {@link Sound} more easily.
     *
     * @param ignoresDisabled    If a player has toggled their sounds off, the sound will be played anyway.
     * @param permissionToListen The permission the player needs to listen this sound.
     * @param permissionRequired The permission the player needs to play this sound.
     * @param radius             A radius of blocks the sound will be heard. Set 0 to play to only the player, -1 to all
     *                           players online, -2 to all players in the {@link org.bukkit.World}.
     */
    public SoundOptions(boolean ignoresDisabled, @Nullable String permissionToListen, @Nullable String permissionRequired,
                        double radius)
    {
        setIgnoresDisabled(ignoresDisabled);
        setPermissionToListen(permissionToListen);
        setPermissionRequired(permissionRequired);
        setRadius(radius);
    }

    /**
     * Create a {@link SoundOptions} based on a configuration section. In PlayMoreSounds this section is named 'Options',
     * it can have the following keys: Permission Required, Permission To Listen, Radius, Ignores Disabled,
     * All of them are optional, see with more details what key does what on PlayMoreSounds wiki.
     *
     * @param section The section where the options are.
     */
    public SoundOptions(@NotNull ConfigurationSection section)
    {
        setPermissionRequired(section.getString("Permission Required").orElse(null));
        setPermissionToListen(section.getString("Permission To Listen").orElse(null));
        setRadius(section.getNumber("Radius").orElse(0).doubleValue());
        ignoresDisabled = section.getBoolean("Ignores Disabled").orElse(false);
    }

    /**
     * If Ignores Disabled option is enabled.
     *
     * @return If the sound should ignore if the player has disabled their sounds.
     */
    public boolean ignoresDisabled()
    {
        return ignoresDisabled;
    }

    public void setIgnoresDisabled(boolean ignoresDisabled)
    {
        this.ignoresDisabled = ignoresDisabled;
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
     * If a {@link SoundOptions} contains the same values of {@link #ignoresDisabled()}, {@link #getRadius()},
     * {@link #getPermissionToListen()} and {@link #getPermissionRequired()}.
     *
     * @param o The {@link SoundOptions} to compare.
     * @return If the {@link SoundOptions} has the same values as this one.
     */
    @Override
    public boolean equals(@Nullable Object o)
    {
        if (this == o) return true;
        if (!(o instanceof SoundOptions options)) return false;

        return ignoresDisabled() == options.ignoresDisabled() &&
                Double.compare(options.getRadius(), getRadius()) == 0 &&
                Objects.equals(getPermissionToListen(), options.getPermissionToListen()) &&
                Objects.equals(getPermissionRequired(), options.getPermissionRequired());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ignoresDisabled(), getPermissionToListen(), getPermissionRequired(), getRadius());
    }

    @Override
    public @NotNull String toString()
    {
        return "SoundOptions{" +
                "ignoresDisabled=" + ignoresDisabled +
                ", permissionToListen='" + permissionToListen + '\'' +
                ", permissionRequired='" + permissionRequired + '\'' +
                ", radius=" + radius +
                '}';
    }
}