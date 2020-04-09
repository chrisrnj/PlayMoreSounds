package com.epicnicity322.playmoresounds.core.addons;

import com.epicnicity322.playmoresounds.core.addons.exceptions.InvalidAddonException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AddonDescription
{
    private String main;
    private String name;
    private String version;
    private String apiVersion;
    private HashSet<String> authors = new HashSet<>();
    private StartTime startTime = StartTime.SERVER_LOAD_COMPLETE;
    private HashSet<String> hookPlugins = new HashSet<>();
    private HashSet<String> requiredPlugins = new HashSet<>();
    private HashSet<String> hookAddons = new HashSet<>();
    private HashSet<String> requiredAddons = new HashSet<>();

    public AddonDescription(@NotNull File jar) throws IOException, InvalidAddonException
    {
        Objects.requireNonNull(jar, "Jar can not be null.");

        InputStream description = null;
        JarFile jarFile = null;

        try {
            try {
                jarFile = new JarFile(jar);
                JarEntry entry = jarFile.getJarEntry("pmsaddon.properties");

                if (entry == null) {
                    entry = jarFile.getJarEntry("addon.properties");

                    if (entry == null) {
                        throw new InvalidAddonException(new FileNotFoundException("The jar '" + jar.getName() +
                                "' does not contain a description file."));
                    }
                }

                description = jarFile.getInputStream(entry);
            } catch (IOException ex) {
                throw new InvalidAddonException(ex);
            }

            Properties properties = new Properties();
            properties.load(description);

            if (properties.containsKey("name")) {
                try {
                    setName(properties.getProperty("name"));
                } catch (IllegalArgumentException ex) {
                    throw new InvalidAddonException("The addon '" + jar.getName() + "' has an invalid name. " + ex.getMessage(), ex);
                }
            } else {
                throw new InvalidAddonException("The addon '" + jar.getName() + "' does not contain the property 'name' in description.");
            }

            if (properties.containsKey("main")) {
                main = properties.getProperty("main");
            } else {
                throw new InvalidAddonException("The addon '" + name + "' does not contain the property 'main' in description.");
            }

            if (properties.containsKey("author")) {
                authors.add(properties.getProperty("author"));
            } else {
                if (!properties.containsKey("authors")) {
                    throw new InvalidAddonException("The addon '" + name + "' does not contain the property 'author' in description.");
                }
            }

            if (properties.containsKey("authors")) {
                authors.addAll(Arrays.asList(properties.getProperty("authors").split(", ")));
            } else {
                if (!properties.containsKey("author")) {
                    throw new InvalidAddonException("The addon '" + name + "' does not contain the property 'author' in description.");
                }
            }

            authors = parseSet(authors);

            if (authors.isEmpty()) {
                throw new InvalidAddonException("The addon '" + name + "' does not contain a valid author in description.");
            }

            if (properties.containsKey("version")) {
                version = properties.getProperty("version");
            } else {
                if (properties.containsKey("api-version")) {
                    throw new InvalidAddonException("The addon '" + name + "' does not contain the property 'version' in description.");
                }

                version = "1.0";
            }

            if (properties.containsKey("api-version")) {
                apiVersion = properties.getProperty("api-version");
            }

            if (properties.containsKey("start-time")) {
                try {
                    startTime = StartTime.valueOf(properties.getProperty("start-time").toUpperCase());

                    if (startTime == StartTime.HOOK_ADDONS || startTime == StartTime.HOOK_PLUGINS) {
                        String property;

                        if (properties.containsKey("hook-addons")) {
                            property = "hook-addons";
                        } else if (properties.containsKey("hook-plugins")) {
                            property = "hook-plugins";
                        } else {
                            throw new InvalidAddonException("The addon '" + name + "' has the start-time set to " +
                                    startTime.name() + ", but does not contain the property '" + startTime.name()
                                    .toLowerCase().replace("_", "-") + "' in description.");
                        }

                        HashSet<String> set = parseSet(new HashSet<>(Arrays.asList(properties.getProperty(property).split(", "))));

                        if (set.isEmpty()) {
                            throw new InvalidAddonException("The addon '" + name + "' has the start-time set to " +
                                    startTime.name() + ", but '" + property + "' property in description is empty.");
                        }

                        if (startTime == StartTime.HOOK_ADDONS) {
                            hookAddons.addAll(set);
                        } else {
                            hookPlugins.addAll(set);
                        }
                    }
                } catch (IllegalArgumentException ex) {
                    throw new InvalidAddonException("The addon '" + name + "' has an invalid start-time in description.", ex);
                }
            }

            if (properties.containsKey("required-plugins")) {
                requiredPlugins.addAll(parseSet(new HashSet<>(Arrays.asList(properties.getProperty("required-plugins")
                        .split(", ")))));
            }

            if (properties.containsKey("required-addons")) {
                requiredAddons.addAll(parseSet(new HashSet<>(Arrays.asList(properties.getProperty("required-addons")
                        .split(", ")))));
            }
        } finally {
            if (description != null) {
                try {
                    description.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private HashSet<String> parseSet(HashSet<String> set)
    {
        HashSet<String> newSet = new HashSet<>();

        for (String string : set) {
            if (!string.trim().equals("")) {
                newSet.add(string.trim());
            }
        }

        return newSet;
    }

    public String getMain()
    {
        return main;
    }

    public String getName()
    {
        return name;
    }

    private void setName(@NotNull String name)
    {
        Objects.requireNonNull(name, "name may not be null");
        name = name.trim().replaceAll(" +", " ");

        if (name.length() < 3) {
            throw new IllegalArgumentException("Addons names length can not be lower than 3.");
        }

        if (name.replaceAll("[^A-Za-z]", "").length() < 3) {
            throw new IllegalArgumentException("Addons names may contain at least 3 letters.");
        }

        if (name.length() > 26) {
            throw new IllegalArgumentException("Addons names length can not be greater than 26.");
        }

        if (!name.replaceAll("[A-Za-z0-9\\s]", "").equals("")) {
            throw new IllegalArgumentException("Addons names may only contain letters, numbers and spaces.");
        }

        this.name = name;
    }

    public String getVersion()
    {
        return version;
    }

    public String getApiVersion()
    {
        return apiVersion;
    }

    public HashSet<String> getAuthors()
    {
        return authors;
    }

    public StartTime getStartTime()
    {
        return startTime;
    }

    public HashSet<String> getHookPlugins()
    {
        return hookPlugins;
    }

    public HashSet<String> getRequiredPlugins()
    {
        return requiredPlugins;
    }

    public HashSet<String> getHookAddons()
    {
        return hookAddons;
    }

    public HashSet<String> getRequiredAddons()
    {
        return requiredAddons;
    }
}
