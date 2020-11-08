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

package com.epicnicity322.playmoresounds.bukkit.region;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import com.epicnicity322.yamlhandler.YamlConfigurationLoader;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Pattern;

public class SoundRegion
{
    private static final @NotNull YamlConfigurationLoader loader = new YamlConfigurationLoader();
    private static final @NotNull Pattern allowedRegionNameChars = Pattern.compile("^[A-Za-z0-9_]+$");
    protected final @NotNull Path save;
    private final @NotNull UUID id;
    private final @Nullable UUID creator;
    private final @NotNull ZonedDateTime creationDate;
    private @NotNull String name;
    private @Nullable String description;
    private @NotNull Location maxDiagonal;
    private @NotNull Location minDiagonal;
    private @NotNull Set<Location> border;

    protected SoundRegion(@NotNull Path path)
    {
        try {
            Configuration region = loader.load(path);
            String fileName = path.getFileName().toString();

            id = UUID.fromString(fileName.substring(0, fileName.indexOf(".")));
            creator = region.getString("Creator").map(UUID::fromString).orElse(null);
            creationDate = region.getString("Creation Date").map(ZonedDateTime::parse).orElse(
                    Files.readAttributes(path, BasicFileAttributes.class).creationTime().toInstant().atZone(ZoneId.systemDefault()));
            setName(region.getString("Name").get());
            description = region.getString("Description").orElse(null);

            World world = Bukkit.getWorld(UUID.fromString(region.getString("World").get()));
            ConfigurationSection diagonals = region.getConfigurationSection("Diagonals");
            ConfigurationSection first = diagonals.getConfigurationSection("First");
            ConfigurationSection second = diagonals.getConfigurationSection("Second");

            maxDiagonal = new Location(world, first.getNumber("X").get().doubleValue(),
                    first.getNumber("Y").get().doubleValue(), first.getNumber("Z").get().doubleValue());
            setMinDiagonal(new Location(world, second.getNumber("X").get().doubleValue(),
                    second.getNumber("Y").get().doubleValue(), second.getNumber("Z").get().doubleValue()));
            save = path;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Path is not pointing to a valid region file.", ex);
        }
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
        id = UUID.randomUUID();
        this.creator = creator;
        this.creationDate = ZonedDateTime.now();
        this.description = description;
        this.maxDiagonal = maxDiagonal;
        setName(name);
        setMinDiagonal(minDiagonal);
        save = PlayMoreSounds.getFolder().resolve("Data").resolve("Regions").resolve(id.toString() + ".yml");
    }

    private Set<Location> parseBorder()
    {
        HashSet<Location> border = new HashSet<>();

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
        if (!allowedRegionNameChars.matcher(name).matches())
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
     * @return A diagonal of this region.
     */
    public @NotNull Location getMaxDiagonal()
    {
        return maxDiagonal;
    }

    /**
     * Calculates the min and max coordinates of this location and second position and updates both.
     *
     * @param loc The location of a diagonal of this sound region.
     */
    public void setMaxDiagonal(@NotNull Location loc)
    {
        World world = minDiagonal.getWorld();

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
     * @return A diagonal of this region.
     */
    public @NotNull Location getMinDiagonal()
    {
        return minDiagonal;
    }

    /**
     * Calculates the min and max coordinates of this location and max diagonal and updates both.
     *
     * @param location The location of a diagonal of this sound region.
     */
    public void setMinDiagonal(@NotNull Location location)
    {
        World world = maxDiagonal.getWorld();

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
     * Saves this region into a file in PlayMoreSounds folder.
     */
    public void save() throws IOException
    {
        // Checking if a region with the same name already exists.
        for (SoundRegion region : RegionManager.getAllRegions())
            if (region.getName().equals(getName()) && region != this)
                throw new IllegalStateException("A region with the same name already exists.");

        Configuration region = new Configuration(loader);

        region.set("Name", name);
        region.set("World", maxDiagonal.getWorld().getUID().toString());

        if (creator != null)
            region.set("Creator", creator.toString());

        region.set("Creation Date", creationDate.toString());
        region.set("Description", description);
        region.set("Diagonals.First.X", maxDiagonal.getBlockX());
        region.set("Diagonals.First.Y", maxDiagonal.getBlockY());
        region.set("Diagonals.First.Z", maxDiagonal.getBlockZ());
        region.set("Diagonals.Second.X", minDiagonal.getBlockX());
        region.set("Diagonals.Second.Y", minDiagonal.getBlockY());
        region.set("Diagonals.Second.Z", minDiagonal.getBlockZ());

        delete();
        region.save(save);
        RegionManager.regions.add(this);
    }

    /**
     * Deletes the file holding this region in PlayMoreSounds folder if exists.
     */
    public void delete() throws IOException
    {
        Files.deleteIfExists(save);
        RegionManager.regions.remove(this);
    }

    /**
     * Checks if object is a {@link SoundRegion} and if the {@link SoundRegion} has the same creator, name, description
     * and diagonal locations.
     *
     * @param o The object to check.
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof SoundRegion)) return false;

        SoundRegion that = (SoundRegion) o;

        return Objects.equals(getCreator(), that.getCreator()) &&
                getName().equals(that.getName()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                getMaxDiagonal().equals(that.getMaxDiagonal()) &&
                getMinDiagonal().equals(that.getMinDiagonal());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getCreator(), getName(), getDescription(), getMaxDiagonal(), getMinDiagonal());
    }
}
