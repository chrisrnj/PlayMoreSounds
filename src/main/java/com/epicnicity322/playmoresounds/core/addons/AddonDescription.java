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

package com.epicnicity322.playmoresounds.core.addons;

import com.epicnicity322.epicpluginlib.core.tools.Version;
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
import java.util.Objects;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

public class AddonDescription
{
    private static final @NotNull YamlConfigurationLoader loader = new YamlConfigurationLoader();
    private static final @NotNull Pattern duplicatedSpaces = Pattern.compile(" +");
    private static final @NotNull Pattern notLetters = Pattern.compile("[^A-Za-z]");
    private static final @NotNull Pattern notAlphaNumericAndSpace = Pattern.compile("[^A-Za-z0-9\\s]");
    private final @NotNull String mainClass;
    private final @NotNull String name;
    private final @NotNull Version version;
    private final @NotNull Version apiVersion;
    private final @NotNull StartTime startTime;
    private final @NotNull Collection<String> authors;
    private final @NotNull Collection<String> pluginHooks;
    private final @NotNull Collection<String> addonHooks;
    private final @NotNull Collection<String> requiredPlugins;
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

            try {
                name = parseName(description.getString("Name").orElseThrow(() -> new InvalidAddonException("The addon '" + fileName + "' does not contain 'Name' property in description.")));
            } catch (IllegalArgumentException ex) {
                throw new InvalidAddonException("The addon '" + fileName + "' has an invalid name: " + ex.getMessage(), ex);
            }

            mainClass = description.getString("Main Class").orElseThrow(() -> new InvalidAddonException("The addon '" + name + "' does not contain 'Main Class' property in description."));

            Optional<String> startTime = description.getString("Start Time");

            if (startTime.isPresent())
                try {
                    this.startTime = StartTime.valueOf(startTime.get());
                } catch (IllegalArgumentException ex) {
                    throw new InvalidAddonException("The addon '" + name + "' has an invalid start time.", ex);
                }
            else
                this.startTime = StartTime.SERVER_LOAD_COMPLETE;

            try {
                version = new Version(description.getString("Version").orElse("1.0"));
            } catch (IllegalArgumentException ex) {
                throw new InvalidAddonException("The addon '" + name + "' has an invalid version defined for Version key.", ex);
            }

            try {
                apiVersion = new Version(description.getString("Api Version").orElse(PlayMoreSounds.version.getVersion()));
            } catch (IllegalArgumentException ex) {
                throw new InvalidAddonException("The addon '" + name + "' has an invalid version defined for Api Version key.", ex);
            }

            authors = Collections.unmodifiableCollection(description.getCollection("Authors", Object::toString));
            pluginHooks = Collections.unmodifiableCollection(description.getCollection("Plugin Hooks", Object::toString));
            addonHooks = Collections.unmodifiableCollection(description.getCollection("Addon Hooks", Object::toString));
            requiredPlugins = Collections.unmodifiableCollection(description.getCollection("Required Plugins", Object::toString));
            requiredAddons = Collections.unmodifiableCollection(description.getCollection("Required Addons", Object::toString));
        } catch (InvalidConfigurationException ex) {
            throw new InvalidAddonException("The addon '" + fileName + "' has a misconfigured description file.", ex);
        }
    }

    /**
     * For better organization addons names must:
     * - Be not greater than 26 characters.
     * - Have at least 3 characters.
     * - Have at least 3 letters.
     * - Contain only letters, numbers and spaces.
     */
    private String parseName(String name)
    {
        // Trimming and removing duplicated spaces.
        name = duplicatedSpaces.matcher(name.trim()).replaceAll(" ");

        if (name.length() > 26)
            throw new IllegalArgumentException("Addon names length can not be greater than 26.");

        if (notLetters.matcher(name).replaceAll("").length() < 3)
            throw new IllegalArgumentException("Addon names may contain at least 3 letters.");

        if (name.length() < 3)
            throw new IllegalArgumentException("Addon names length can not be lower than 3.");

        if (notAlphaNumericAndSpace.matcher(name).find())
            throw new IllegalArgumentException("Addon names may only contain alphanumeric and space characters.");

        return name;
    }

    public @NotNull String getMainClass()
    {
        return mainClass;
    }

    public @NotNull String getName()
    {
        return name;
    }

    public @NotNull Version getVersion()
    {
        return version;
    }

    public @NotNull Version getApiVersion()
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

    public @NotNull Collection<String> getPluginHooks()
    {
        return pluginHooks;
    }

    public @NotNull Collection<String> getAddonHooks()
    {
        return addonHooks;
    }

    public @NotNull Collection<String> getRequiredPlugins()
    {
        return requiredPlugins;
    }

    public @NotNull Collection<String> getRequiredAddons()
    {
        return requiredAddons;
    }

    @Override
    public String toString()
    {
        return "AddonDescription{" +
                "mainClass='" + mainClass + '\'' +
                ", name='" + name + '\'' +
                ", version=" + version +
                ", apiVersion=" + apiVersion +
                ", startTime=" + startTime +
                ", authors=" + authors +
                ", pluginHooks=" + pluginHooks +
                ", addonHooks=" + addonHooks +
                ", requiredPlugins=" + requiredPlugins +
                ", requiredAddons=" + requiredAddons +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddonDescription that = (AddonDescription) o;
        return mainClass.equals(that.mainClass) &&
                name.equals(that.name) &&
                version.equals(that.version) &&
                apiVersion.equals(that.apiVersion) &&
                startTime == that.startTime &&
                authors.equals(that.authors) &&
                pluginHooks.equals(that.pluginHooks) &&
                addonHooks.equals(that.addonHooks) &&
                requiredPlugins.equals(that.requiredPlugins) &&
                requiredAddons.equals(that.requiredAddons);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(mainClass, name, version, apiVersion, startTime, authors, pluginHooks, addonHooks, requiredPlugins, requiredAddons);
    }
}
