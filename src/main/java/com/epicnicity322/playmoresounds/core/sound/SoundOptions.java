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

public class SoundOptions {
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
    public SoundOptions(boolean ignoresDisabled, @Nullable String permissionRequired, @Nullable String permissionToListen,
                        double radius) {
        this.ignoresDisabled = ignoresDisabled;
        this.permissionRequired = permissionRequired != null && permissionRequired.isBlank() ? null : permissionRequired;
        this.permissionToListen = permissionToListen != null && permissionToListen.isBlank() ? null : permissionToListen;
        this.radius = radius;
    }

    /**
     * Create a {@link SoundOptions} based on the keys of a {@link ConfigurationSection}. In PlayMoreSounds configurations,
     * these are in the 'Options' section.
     * The properties are get from the keys 'Ignores Disabled', 'Permission Required', 'Permission To Listen' and 'Radius'.
     * <p>
     * All keys are optional, if any is missing the default value is used.
     *
     * @param section The section where the options are.
     */
    public SoundOptions(@NotNull ConfigurationSection section) {
        this.ignoresDisabled = section.getBoolean("Ignores Disabled").orElse(false);

        String permissionRequired = section.getString("Permission Required").orElse(null),
                permissionToListen = section.getString("Permission To Listen").orElse(null);

        this.permissionRequired = permissionRequired != null && permissionRequired.isBlank() ? null : permissionRequired;
        this.permissionToListen = permissionToListen != null && permissionToListen.isBlank() ? null : permissionToListen;
        this.radius = section.getNumber("Radius").orElse(0).doubleValue();
    }

    /**
     * If Ignores Disabled option is enabled.
     *
     * @return If the sound should ignore if the player has disabled their sounds.
     */
    public boolean ignoresDisabled() {
        return ignoresDisabled;
    }

    public void setIgnoresDisabled(boolean ignoresDisabled) {
        this.ignoresDisabled = ignoresDisabled;
    }

    /**
     * Gets the value of Permission Required option.
     * <p>
     * The Permission Required option allows the sound to play only if the player has this permission.
     *
     * @return The permission the player needs to play the sound.
     */
    public @Nullable String getPermissionRequired() {
        return permissionRequired;
    }

    public void setPermissionRequired(@Nullable String permissionRequired) {
        this.permissionRequired = permissionRequired != null && permissionRequired.isBlank() ? null : permissionRequired;
    }

    /**
     * Gets the value of Permission To Listen option.
     * <p>
     * The Permission To Listen option allows the sound to be played normally, but only who has this permission can hear
     * the sound.
     *
     * @return The permission the player needs to hear the sound.
     */
    public @Nullable String getPermissionToListen() {
        return permissionToListen;
    }

    public void setPermissionToListen(@Nullable String permissionToListen) {
        this.permissionToListen = permissionToListen != null && permissionToListen.isBlank() ? null : permissionToListen;
    }

    /**
     * A radius in blocks where the sound will be heard.
     *
     * @return The radius of the sound.
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Set the radius in blocks the sound will be able to be heard.
     * <ul>
     *     <li>Use 0 to play only to the source player.</li>
     *     <li>Use -1 to play to everyone in the server.</li>
     *     <li>Use -2 to play to everyone in the world.</li>
     * </ul>
     *
     * @param radius The radius to be set.
     */
    public void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * Sets the sound properties to the specified section. Keys set by this method are the same used to create sound
     * options with {@link #SoundOptions(ConfigurationSection)}.
     * <p>
     * Properties with default values are ignored.
     *
     * @param section The section to set the properties.
     */
    public void set(@NotNull ConfigurationSection section) {
        section.set("Ignores Disabled", ignoresDisabled ? true : null);
        section.set("Permission Required", permissionRequired);
        section.set("Permission To Listen", permissionToListen);
        section.set("Radius", radius == 0.0d ? null : radius);
    }

    /**
     * If a {@link SoundOptions} contains the same values of {@link #ignoresDisabled()}, {@link #getRadius()},
     * {@link #getPermissionToListen()} and {@link #getPermissionRequired()}.
     *
     * @param o The {@link SoundOptions} to compare.
     * @return If the {@link SoundOptions} has the same values as this one.
     */
    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof SoundOptions options)) return false;

        return ignoresDisabled == options.ignoresDisabled &&
                Double.compare(radius, options.radius) == 0 &&
                Objects.equals(permissionRequired, options.permissionRequired) &&
                Objects.equals(permissionToListen, options.permissionToListen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ignoresDisabled, permissionRequired, permissionToListen, radius);
    }

    @Override
    public @NotNull String toString() {
        return getClass().getSimpleName() + "{" +
                "ignoresDisabled=" + ignoresDisabled +
                ", permissionRequired='" + permissionRequired + '\'' +
                ", permissionToListen='" + permissionToListen + '\'' +
                ", radius=" + radius +
                '}';
    }
}