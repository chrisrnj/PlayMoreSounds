package com.epicnicity322.playmoresounds.bukkit.region;

import org.bukkit.Location;

import java.nio.file.Path;

public class DenyPoint
{
    private Path file;
    private Location p1;
    private Location p2;

    public DenyPoint(Path savedFile, Location p1, Location p2)
    {
        file = savedFile;
        this.p1 = p1;
        this.p2 = p2;
    }

    public Path getSavedFile()
    {
        return file;
    }

    public String getName()
    {
        String name = file.getFileName().toString();
        return name.substring(0, name.lastIndexOf("."));
    }

    public Location getPosition1()
    {
        return p1;
    }

    public Location getPosition2()
    {
        return p2;
    }
}
