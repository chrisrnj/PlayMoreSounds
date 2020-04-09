package com.epicnicity322.playmoresounds.bukkit.region;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class SoundRegion
{
    private String id;
    private Location P1;
    private Location P2;
    private Path file;
    private long creationDate;
    private String description;
    private Location tpPoint;
    private ZoneId zone;
    private String creator;
    private String owner;

    public SoundRegion(String id, Location P1, Location P2, String creator)
    {
        this.id = id.toLowerCase();
        this.P1 = P1;
        this.P2 = P2;
        creationDate = System.currentTimeMillis();
        zone = ZoneId.systemDefault();
        description = "Regular sound region.";
        tpPoint = P1;
        this.creator = creator;
        owner = creator;
    }

    public SoundRegion(String id, Location P1, Location P2, String creator, String owner)
    {
        this.id = id.toLowerCase();
        this.P1 = P1;
        this.P2 = P2;
        this.creator = creator;
        this.owner = owner;
        creationDate = System.currentTimeMillis();
        zone = ZoneId.systemDefault();
        description = "Regular sound region.";
        tpPoint = P1;
    }

    public SoundRegion(String id, Location P1, Location P2, String creator, Location tpPoint)
    {
        this.id = id.toLowerCase();
        this.P1 = P1;
        this.P2 = P2;
        this.creator = creator;
        owner = creator;
        this.tpPoint = tpPoint;
        creationDate = System.currentTimeMillis();
        zone = ZoneId.systemDefault();
        description = "Regular sound region.";
    }

    public SoundRegion(String id, Location P1, Location P2, String creator, String owner, Location tpPoint,
                       String description)
    {
        this.id = id.toLowerCase();
        this.P1 = P1;
        this.P2 = P2;
        this.creator = creator;
        this.description = description;
        this.tpPoint = tpPoint;
        this.owner = owner;
        creationDate = System.currentTimeMillis();
        zone = ZoneId.systemDefault();
    }

    public SoundRegion(YamlConfiguration savedRegionConfig, Path saveFile) throws NullPointerException
    {
        World w = Bukkit.getWorld(savedRegionConfig.getString("World"));

        id = savedRegionConfig.getString("Name").toLowerCase();
        P1 = new Location(w, savedRegionConfig.getDouble("Locations.P1.X"),
                savedRegionConfig.getDouble("Locations.P1.Y"), savedRegionConfig.getDouble("Locations.P1.Z"));
        P2 = new Location(w, savedRegionConfig.getDouble("Locations.P2.X"),
                savedRegionConfig.getDouble("Locations.P2.Y"), savedRegionConfig.getDouble("Locations.P2.Z"));
        creator = savedRegionConfig.getString("Creator");
        creationDate = savedRegionConfig.getLong("CreationDate");
        zone = ZoneId.of(savedRegionConfig.getString("ZoneId"));
        description = savedRegionConfig.getString("Description");
        owner = savedRegionConfig.getString("Owner");
        tpPoint = new Location(w, savedRegionConfig.getDouble("TeleportPoint.X"),
                savedRegionConfig.getDouble("TeleportPoint.Y"), savedRegionConfig.getDouble("TeleportPoint.Z"),
                (float) savedRegionConfig.getDouble("TeleportPoint.Yaw"),
                (float) savedRegionConfig.getDouble("TeleportPoint.Pitch"));
        file = saveFile;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String name)
    {
        id = name.toLowerCase();
    }

    public String getName()
    {
        return getId();
    }

    public void setName(String name)
    {
        setId(name);
    }

    public Path getSavePath()
    {
        return file;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Long getCreationDateMillis()
    {
        return creationDate;
    }

    public String getCreationDate()
    {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(creationDate), zone)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getCreationDate(String datePattern) throws IllegalArgumentException
    {
        if (datePattern == null) {
            datePattern = "";
        }

        return LocalDateTime.ofInstant(Instant.ofEpochMilli(creationDate), zone)
                .format(DateTimeFormatter.ofPattern(datePattern));
    }

    public Location getPosition1()
    {
        return P1;
    }

    public void setPosition1(Location loc)
    {
        if (loc.getWorld() == getWorld()) {
            P1 = loc;
        } else {
            throw new IllegalArgumentException("Specified location must be in the same world of the region!");
        }
    }

    public Location getPosition2()
    {
        return P2;
    }

    public void setPosition2(Location loc)
    {
        if (loc.getWorld() == getWorld()) {
            P2 = loc;
        } else {
            throw new IllegalArgumentException("Specified location must be in the same world of the region!");
        }
    }

    public String getCreatorName()
    {
        return creator;
    }

    public String getOwnerName()
    {
        return owner;
    }

    public Location getTeleportPoint()
    {
        return tpPoint;
    }

    public void setTeleportPoint(Location loc)
    {
        tpPoint = loc;
    }

    public World getWorld()
    {
        return P1.getWorld();
    }

    public void setWorld(World world)
    {
        P1 = new Location(world, P1.getX(), P1.getY(), P1.getZ(), P1.getYaw(), P1.getPitch());
        P2 = new Location(world, P2.getX(), P2.getY(), P2.getZ(), P2.getYaw(), P2.getPitch());
    }

    public void setOwnership(String owner)
    {
        this.owner = owner;
    }

    public void renameTo(String string)
    {
        id = string;
    }

    public void save(Path file) throws IOException
    {
        if (!Files.exists(file)) {
            Files.createFile(file);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file.toFile());

        config.set("Name", id);
        config.set("World", getWorld().getName());
        config.set("CreationDate", creationDate);
        config.set("Creator", creator);
        config.set("Description", description);
        config.set("Owner", owner);
        config.set("Locations.P1.X", P1.getX());
        config.set("Locations.P1.Y", P1.getY());
        config.set("Locations.P1.Z", P1.getZ());
        config.set("Locations.P2.X", P2.getX());
        config.set("Locations.P2.Y", P2.getY());
        config.set("Locations.P2.Z", P2.getZ());
        config.set("TeleportPoint.X", tpPoint.getX());
        config.set("TeleportPoint.Y", tpPoint.getY());
        config.set("TeleportPoint.Z", tpPoint.getZ());
        config.set("TeleportPoint.Yaw", tpPoint.getYaw());
        config.set("TeleportPoint.Pitch", tpPoint.getPitch());
        config.set("ZoneId", zone.getId());

        config.save(file.toFile());
        this.file = file;
    }

    public void delete() throws IOException
    {
        if (file != null) {
            if (Files.exists(file)) {
                Files.delete(file);
            }
        }
    }

    public String toString(String separator)
    {
        return "{ID=" + id + separator + "World=" + getWorld() + separator + "CreationDate=" + getCreationDateMillis()
                + separator + "Creator=" + creator + separator + "Description=" + getDescription() + separator
                + "Owner=" + owner + separator + "P1=" + P1 + separator + "P2=" + P2 + separator + "TeleportPoint="
                + getTeleportPoint() + separator + "ZoneId=" + zone.getId() + "}";
    }

    @Override
    public String toString()
    {
        return "{ID=" + id + ",World=" + getWorld() + ",CreationDate=" + getCreationDateMillis() + ",Creator=" + creator
                + ",Description=" + getDescription() + ",Owner=" + owner + ",P1=" + P1 + ",P2=" + P2 + ",TeleportPoint="
                + getTeleportPoint() + ",ZoneId=" + zone.getId() + "}";
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof SoundRegion)) return false;

        SoundRegion that = (SoundRegion) o;

        return getCreationDate().equals(that.getCreationDate()) &&
                getId().equals(that.getId()) &&
                getPosition1().equals(that.getPosition1()) &&
                getPosition2().equals(that.getPosition2()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getTeleportPoint(), that.getTeleportPoint()) &&
                zone.equals(that.zone) &&
                getCreatorName().equals(that.getCreatorName()) &&
                getOwnerName().equals(that.getOwnerName());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getId(), getPosition1(), getPosition2(), getCreationDate(), getDescription(), getTeleportPoint()
                , zone, getCreatorName(), getOwnerName());
    }
}
