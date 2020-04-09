package com.epicnicity322.playmoresounds.bukkit.util;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.nio.file.Path;

public class Finder
{
    private String value;

    public Finder(String value)
    {
        value = value.replace("\\", "/");

        if (!value.startsWith("/"))
            throw new IllegalArgumentException("Finder values must start with /");

        if (value.length() < 4 || !value.substring(1).contains("/"))
            throw new IllegalArgumentException("You must specify a configuration section");

        this.value = value;
    }

    public String getValue()
    {
        return value;
    }

    public String getPathValue()
    {
        return value.substring(1, value.lastIndexOf("/"));
    }

    public String getSectionValue()
    {
        return value.substring(value.lastIndexOf("/") + 1);
    }

    public Path getPath()
    {
        return PlayMoreSounds.DATA_FOLDER.resolve(getPathValue());
    }

    public ConfigurationSection getSection()
    {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(getPath().toFile());

        return config.getConfigurationSection(getSectionValue());
    }
}
