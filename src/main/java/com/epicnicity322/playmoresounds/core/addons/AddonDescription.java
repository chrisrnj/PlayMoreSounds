package com.epicnicity322.playmoresounds.core.addons;

import com.epicnicity322.playmoresounds.core.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.addons.exceptions.InvalidAddonException;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.YamlConfigurationLoader;
import com.epicnicity322.yamlhandler.exceptions.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AddonDescription
{
    private static final @NotNull YamlConfigurationLoader loader = YamlConfigurationLoader.build();
    private final @NotNull String main;
    private final @NotNull String name;
    private final @NotNull String version;
    private final @NotNull String apiVersion;
    private final @NotNull StartTime startTime;
    private final @NotNull Collection<String> authors;
    private final @NotNull Collection<String> hookPlugins;
    private final @NotNull Collection<String> requiredPlugins;
    private final @NotNull Collection<String> hookAddons;
    private final @NotNull Collection<String> requiredAddons;

    protected AddonDescription(@NotNull Path jar) throws IOException, InvalidAddonException
    {
        // Getting the description file.
        JarFile jarFile = new JarFile(jar.toFile());
        JarEntry entry = jarFile.getJarEntry("pmsaddon.yml");

        String fileName = jar.getFileName().toString();

        if (entry == null)
            throw new InvalidAddonException(new FileNotFoundException("The jar '" + fileName + "' in addons folder does not contain a description file."));

        try {
            Configuration description = loader.load(new InputStreamReader(jarFile.getInputStream(entry)));

            name = parseName(description.getString("Name").orElseThrow(() -> new InvalidAddonException("The addon '" + fileName + "' does not contain name property in description.")));
            main = description.getString("Main").orElseThrow(() -> new InvalidAddonException("The addon '" + name + "' does not contain main property in description."));

            Optional<String> startTime = description.getString("Start Time");

            if (startTime.isPresent())
                try {
                    this.startTime = StartTime.valueOf(startTime.get());
                } catch (IllegalArgumentException ex) {
                    throw new InvalidAddonException("The addon '" + name + "' has an invalid Start Time.");
                }
            else
                this.startTime = StartTime.SERVER_LOAD_COMPLETE;

            version = description.getString("Version").orElse("1.0");
            apiVersion = description.getString("Api Version").orElse(PlayMoreSounds.version);
            authors = Collections.unmodifiableCollection(description.getCollection("Authors", Object::toString));
            hookPlugins = Collections.unmodifiableCollection(description.getCollection("Hook Plugins", Object::toString));
            requiredPlugins = Collections.unmodifiableCollection(description.getCollection("Required Plugins", Object::toString));
            hookAddons = Collections.unmodifiableCollection(description.getCollection("Hook Addons", Object::toString));
            requiredAddons = Collections.unmodifiableCollection(description.getCollection("Required Addons", Object::toString));
        } catch (InvalidConfigurationException ex) {
            throw new InvalidAddonException("The addon '" + fileName + "' has a misconfigured description file.", ex);
        }
    }

    private String parseName(String name)
    {
        name = name.trim().replaceAll(" +", " ");

        if (name.length() < 3)
            throw new IllegalArgumentException("Addons names length can not be lower than 3.");

        if (name.replaceAll("[^A-Za-z]", "").length() < 3)
            throw new IllegalArgumentException("Addons names may contain at least 3 letters.");

        if (name.length() > 26)
            throw new IllegalArgumentException("Addons names length can not be greater than 26.");

        if (!name.replaceAll("[A-Za-z0-9\\s]", "").equals(""))
            throw new IllegalArgumentException("Addons names may only contain letters, numbers and spaces.");

        return name;
    }

    public @NotNull String getMain()
    {
        return main;
    }

    public @NotNull String getName()
    {
        return name;
    }

    public @NotNull String getVersion()
    {
        return version;
    }

    public @NotNull String getApiVersion()
    {
        return apiVersion;
    }

    public @NotNull StartTime getStartTime()
    {
        return startTime;
    }

    public @NotNull Collection<String> getAuthors()
    {
        return authors;
    }

    public @NotNull Collection<String> getHookPlugins()
    {
        return hookPlugins;
    }

    public @NotNull Collection<String> getRequiredPlugins()
    {
        return requiredPlugins;
    }

    public @NotNull Collection<String> getHookAddons()
    {
        return hookAddons;
    }

    public @NotNull Collection<String> getRequiredAddons()
    {
        return requiredAddons;
    }
}
