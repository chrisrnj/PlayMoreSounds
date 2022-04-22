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

package com.epicnicity322.playmoresounds.bukkit.region;

import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.YamlConfigurationLoader;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class SoundRegion
{
    public static final @NotNull Pattern ALLOWED_REGION_NAME_CHARS = Pattern.compile("^\\w+$");
    private final @NotNull UUID id;
    private final @Nullable UUID creator;
    private final @NotNull ZonedDateTime creationDate;
    private String name;
    private @Nullable String description;
    private @NotNull Location maxDiagonal;
    private Location minDiagonal;
    private Set<Location> border;
    private @Nullable PlayableRichSound enterSound;
    private @Nullable PlayableRichSound leaveSound;
    private @Nullable PlayableRichSound loopSound;

    /**
     * Loads a sound region from a configuration file. This configuration file must have the name of an {@link UUID} and
     * contain the keys for Name, Creator, Creation Date, Description, World, Diagonals.First and Diagonals.Second (X, Y and Z).
     * <p>
     * If Creator key is not present, the creator is considered {@link org.bukkit.command.ConsoleCommandSender}.
     *
     * @param data The configuration containing all the region data.
     * @throws IllegalArgumentException If data is invalid or missing any of the required keys.
     * @throws NullPointerException     If the world does not exist anymore or is not loaded.
     */
    public SoundRegion(@NotNull Configuration data)
    {
        Supplier<IllegalArgumentException> invalidRegionData = () -> new IllegalArgumentException("The provided data does not contain valid region data.");

        Path path = data.getFilePath().orElseThrow(() -> new IllegalArgumentException("Data is not stored on a real file.")).getFileName();
        String fileName = path.toString();

        id = UUID.fromString(fileName.substring(0, fileName.indexOf(".")));
        creator = data.getString("Creator").map(UUID::fromString).orElse(null);
        creationDate = data.getString("Creation Date").map(ZonedDateTime::parse).orElseThrow(invalidRegionData);
        setName(data.getString("Name").orElseThrow(invalidRegionData));
        description = data.getString("Description").orElse(null);

        World world = Objects.requireNonNull(Bukkit.getWorld(UUID.fromString(data.getString("World").orElseThrow(invalidRegionData))), "The world this region is in does not exist or is not loaded.");
        var max = data.getConfigurationSection("Diagonals.Max");
        var min = data.getConfigurationSection("Diagonals.Min");

        if (max == null) max = data.getConfigurationSection("Diagonals.First");
        if (min == null) min = data.getConfigurationSection("Diagonals.Second");

        if (max == null || min == null) throw invalidRegionData.get();

        maxDiagonal = new Location(world, max.getNumber("X").orElseThrow(invalidRegionData).doubleValue(),
                max.getNumber("Y").orElseThrow(invalidRegionData).doubleValue(), max.getNumber("Z").orElseThrow(invalidRegionData).doubleValue());
        setMinDiagonal(new Location(world, min.getNumber("X").orElseThrow(invalidRegionData).doubleValue(),
                min.getNumber("Y").orElseThrow(invalidRegionData).doubleValue(), min.getNumber("Z").orElseThrow(invalidRegionData).doubleValue()));

        var enter = data.getConfigurationSection("Enter Sound");
        var leave = data.getConfigurationSection("Leave Sound");
        var loop = data.getConfigurationSection("Loop Sound");

        if (enter != null) enterSound = new PlayableRichSound(enter);
        if (leave != null) leaveSound = new PlayableRichSound(leave);
        if (loop != null) loopSound = new PlayableRichSound(loop);
    }

    /**
     * Creates a new sound region.
     *
     * @param name        The name of the region.
     * @param maxDiagonal The {@link Location} of the first diagonal of this region.
     * @param minDiagonal The {@link Location} of the second diagonal of this region.
     * @param creator     The {@link UUID} of the player who created this region, null if it was made by console.
     * @param description The description of this
     */
    public SoundRegion(@NotNull String name, @NotNull Location maxDiagonal, @NotNull Location minDiagonal,
                       @Nullable UUID creator, @Nullable String description)
    {
        if (maxDiagonal.getWorld() == null || minDiagonal.getWorld() == null) {
            throw new IllegalArgumentException("Provided diagonals do not have a world.");
        }

        id = UUID.randomUUID();
        this.creator = creator;
        this.creationDate = ZonedDateTime.now();
        this.description = description;
        this.maxDiagonal = maxDiagonal;
        setName(name);
        setMinDiagonal(minDiagonal);
    }

    /**
     * @return The exact locations of the border blocks of this region.
     */
    private Set<Location> parseBorder()
    {
        var border = new HashSet<Location>();

        double startX = minDiagonal.getX();
        double endX = maxDiagonal.getX() + 1d;
        double startY = minDiagonal.getY();
        double endY = maxDiagonal.getY() + 1d;
        double startZ = minDiagonal.getZ();
        double endZ = maxDiagonal.getZ() + 1d;
        World world = minDiagonal.getWorld();

        for (double x = startX; x <= endX; ++x)
            for (double y = startY; y <= endY; ++y)
                for (double z = startZ; z <= endZ; ++z) {
                    boolean edgeX = x == startX || x == endX;
                    boolean edgeY = y == startY || y == endY;
                    boolean edgeZ = z == startZ || z == endZ;

                    if ((edgeX && edgeY) || (edgeZ && edgeY) || (edgeX && edgeZ))
                        border.add(new Location(world, x, y, z));
                }

        return Collections.unmodifiableSet(border);
    }

    /**
     * Checks if this region is inside the specified location.
     *
     * @param location The location to check if this region is inside.
     */
    public boolean isInside(@NotNull Location location)
    {
        return location.getWorld().equals(minDiagonal.getWorld()) &&
                location.getBlockX() >= minDiagonal.getBlockX() && location.getBlockX() <= maxDiagonal.getBlockX() &&
                location.getBlockY() >= minDiagonal.getBlockY() && location.getBlockY() <= maxDiagonal.getBlockY() &&
                location.getBlockZ() >= minDiagonal.getBlockZ() && location.getBlockZ() <= maxDiagonal.getBlockZ();
    }

    /**
     * Gets the id of this region.
     *
     * @return The id of this region.
     */
    public @NotNull UUID getId()
    {
        return id;
    }

    /**
     * Gets the name of this region.
     *
     * @return the name of this region.
     */
    public @NotNull String getName()
    {
        return name;
    }

    /**
     * Sets the name of this region.
     *
     * @param name The name you want this region to have.
     * @throws IllegalArgumentException If the name is not alpha-numeric.
     */
    public void setName(@NotNull String name)
    {
        if (!ALLOWED_REGION_NAME_CHARS.matcher(name).matches())
            throw new IllegalArgumentException("Specified name is not alpha-numeric.");

        this.name = name;
    }

    /**
     * Gets the time this region was created.
     *
     * @return The time this region was created.
     */
    public @NotNull ZonedDateTime getCreationDate()
    {
        return creationDate;
    }

    /**
     * Gets the max diagonal location of this region.
     *
     * @return An immutable location of a diagonal of this region.
     */
    public @NotNull Location getMaxDiagonal()
    {
        return maxDiagonal.clone();
    }

    /**
     * Calculates the min and max coordinates of this location and second position and updates both.
     *
     * @param loc The location of a diagonal of this sound region.
     */
    public void setMaxDiagonal(@NotNull Location loc)
    {
        var world = minDiagonal.getWorld();

        if (loc.getWorld() != world)
            throw new IllegalArgumentException("First position can not be in a different world than second position.");

        int maxX = Math.max(loc.getBlockX(), maxDiagonal.getBlockX());
        int maxY = Math.max(loc.getBlockY(), maxDiagonal.getBlockY());
        int maxZ = Math.max(loc.getBlockZ(), maxDiagonal.getBlockZ());
        int minX = Math.min(loc.getBlockX(), maxDiagonal.getBlockX());
        int minY = Math.min(loc.getBlockY(), maxDiagonal.getBlockY());
        int minZ = Math.min(loc.getBlockZ(), maxDiagonal.getBlockZ());

        maxDiagonal = new Location(world, maxX, maxY, maxZ);
        minDiagonal = new Location(world, minX, minY, minZ);
        border = parseBorder();
    }

    /**
     * Gets the min diagonal location of this region.
     *
     * @return An immutable location of a diagonal of this region.
     */
    public @NotNull Location getMinDiagonal()
    {
        return minDiagonal.clone();
    }

    /**
     * Calculates the min and max coordinates of this location and max diagonal and updates both.
     *
     * @param location The location of a diagonal of this sound region.
     */
    public void setMinDiagonal(@NotNull Location location)
    {
        var world = maxDiagonal.getWorld();

        if (location.getWorld() != world)
            throw new IllegalArgumentException("Second position can not be in a different world than first position.");

        int maxX = Math.max(location.getBlockX(), maxDiagonal.getBlockX());
        int maxY = Math.max(location.getBlockY(), maxDiagonal.getBlockY());
        int maxZ = Math.max(location.getBlockZ(), maxDiagonal.getBlockZ());
        int minX = Math.min(location.getBlockX(), maxDiagonal.getBlockX());
        int minY = Math.min(location.getBlockY(), maxDiagonal.getBlockY());
        int minZ = Math.min(location.getBlockZ(), maxDiagonal.getBlockZ());

        maxDiagonal = new Location(world, maxX, maxY, maxZ);
        minDiagonal = new Location(world, minX, minY, minZ);
        border = parseBorder();
    }

    /**
     * Gets the coordinates of the border blocks of this region.
     *
     * @return The coordinates of the border of this region.
     */
    public @NotNull Set<Location> getBorder()
    {
        return border;
    }

    /**
     * Gets the {@link UUID} of the player who created this region.
     *
     * @return The region's creator {@link UUID} or null if this region was created by console.
     */
    public @Nullable UUID getCreator()
    {
        return creator;
    }

    /**
     * Gets the description of this region.
     *
     * @return The region's description or null if this region has no description.
     */
    public @Nullable String getDescription()
    {
        return description;
    }

    /**
     * Sets the description of this region.
     *
     * @param description The description you want this region to have.
     */
    public void setDescription(@Nullable String description)
    {
        this.description = description;
    }

    /**
     * @return The sound that plays when a player enters this region.
     */
    public @Nullable PlayableRichSound getEnterSound()
    {
        return enterSound;
    }

    /**
     * Sets the enter sound of this region.
     *
     * @param enterSound The sound to play when a player enters this region.
     */
    public void setEnterSound(@Nullable PlayableRichSound enterSound)
    {
        if (enterSound == null) {
            this.enterSound = null;
            return;
        }

        var section = new Configuration(new YamlConfigurationLoader()).createSection("Enter Sound");
        var oldSection = enterSound.getSection();

        if (oldSection != null) {
            section.set("Prevent Default Sound", oldSection.getBoolean("Prevent Default Sound").orElse(false));
            section.set("Stop On Exit.Enabled", oldSection.getBoolean("Stop On Exit.Enabled").orElse(false));
            section.set("Stop On Exit.Delay", oldSection.getNumber("Stop On Exit.Delay").orElse(0L).longValue());
        }

        enterSound.setAt(section);
        this.enterSound = new PlayableRichSound(section);
    }

    /**
     * @return The sound that plays when a player leaves this region.
     */
    public @Nullable PlayableRichSound getLeaveSound()
    {
        return leaveSound;
    }

    /**
     * Sets the leave sound of this region.
     *
     * @param leaveSound The sound to play when a player leaves this region.
     */
    public void setLeaveSound(@Nullable PlayableRichSound leaveSound)
    {
        if (leaveSound == null) {
            this.leaveSound = null;
            return;
        }

        var section = new Configuration(new YamlConfigurationLoader()).createSection("Leave Sound");
        var oldSection = leaveSound.getSection();

        if (oldSection != null) {
            section.set("Prevent Default Sound", oldSection.getBoolean("Prevent Default Sound").orElse(false));
        }

        leaveSound.setAt(section);
        this.leaveSound = new PlayableRichSound(section);
    }

    /**
     * @return The sound that plays in loop while a player is inside this region.
     */
    public @Nullable PlayableRichSound getLoopSound()
    {
        return loopSound;
    }

    /**
     * Sets the loop sound of this region.
     *
     * @param loopSound The sound to play while a player is in this region.
     */
    public void setLoopSound(@Nullable PlayableRichSound loopSound)
    {
        if (loopSound == null) {
            this.loopSound = null;
            return;
        }

        var section = new Configuration(new YamlConfigurationLoader()).createSection("Loop Sound");
        var oldSection = loopSound.getSection();

        if (oldSection != null) {
            section.set("Delay", oldSection.getNumber("Delay").orElse(0L).longValue());
            section.set("Period", oldSection.getNumber("Period").orElse(100L).longValue());
            section.set("Prevent Default Sound", oldSection.getBoolean("Prevent Default Sound").orElse(false));
            section.set("Stop On Exit.Enabled", oldSection.getBoolean("Stop On Exit.Enabled").orElse(false));
            section.set("Stop On Exit.Delay", oldSection.getNumber("Stop On Exit.Delay").orElse(0L).longValue());
        }

        loopSound.setAt(section);
        this.loopSound = new PlayableRichSound(section);
    }

    /**
     * Checks if the {@link Object} is a {@link SoundRegion} and has the same {@link UUID} as this one.
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof SoundRegion that)) return false;

        return id.equals(that.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id);
    }
}
