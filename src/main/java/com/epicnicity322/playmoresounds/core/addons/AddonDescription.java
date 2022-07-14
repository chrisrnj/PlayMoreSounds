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

package com.epicnicity322.playmoresounds.core.addons;

import com.epicnicity322.epicpluginlib.core.EpicPluginLib;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsVersion;
import com.epicnicity322.playmoresounds.core.addons.exceptions.InvalidAddonException;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.YamlConfigurationLoader;
import com.epicnicity322.yamlhandler.exceptions.InvalidConfigurationException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

public class AddonDescription {
    private static final @NotNull Pattern duplicatedSpaces = Pattern.compile(" +");
    private static final @NotNull Pattern notAlphaNumericAndSpace = Pattern.compile("[^A-Za-z\\d\\s]");
    private static final @NotNull Pattern notLetters = Pattern.compile("[^A-Za-z]");
    private static final @NotNull YamlConfigurationLoader loader = new YamlConfigurationLoader();
    final @NotNull Path jar;
    private final @NotNull Collection<String> addonHooks;
    private final @NotNull Collection<String> authors;
    private final @NotNull Collection<String> pluginHooks;
    private final @NotNull Collection<String> requiredAddons;
    private final @NotNull Collection<String> requiredPlugins;
    private final @NotNull StartTime startTime;
    private final @NotNull String description;
    private final @NotNull String mainClass;
    private final @NotNull String name;
    private final @NotNull Version apiVersion;
    private final @NotNull Version version;

    protected AddonDescription(@NotNull Path jar) throws IOException, InvalidAddonException {
        this.jar = jar;

        // Getting the description file.
        try (var jarFile = new JarFile(jar.toFile())) {
            var entry = jarFile.getJarEntry("pmsaddon.yml");
            var fileName = jar.getFileName().toString();

            if (entry == null)
                throw new InvalidAddonException(new FileNotFoundException("The jar '" + fileName + "' in addons folder does not contain a description file."));

            try {
                Configuration description = loader.load(new InputStreamReader(jarFile.getInputStream(entry)));

                try {
                    name = parseName(description.getString("Name").orElseThrow(() -> new InvalidAddonException("The addon '" + fileName + "' does not contain 'Name' property in description.")));
                } catch (IllegalArgumentException ex) {
                    throw new InvalidAddonException("The addon '" + fileName + "' has an invalid name: " + ex.getMessage(), ex);
                }

                Optional<String> mainClass = description.getString("Main Class");

                if (mainClass.isPresent()) {
                    this.mainClass = mainClass.get();
                } else {
                    Supplier<InvalidAddonException> noMainClass = () -> new InvalidAddonException("The addon '" + name + "' does not contain 'Main Class' property in description.");

                    switch (EpicPluginLib.Platform.getPlatform()) {
                        case BUKKIT ->
                                this.mainClass = description.getString("Bukkit Main Class").orElseThrow(noMainClass);
                        case SPONGE ->
                                this.mainClass = description.getString("Sponge Main Class").orElseThrow(noMainClass);
                        default ->
                                throw new UnsupportedOperationException("PlayMoreSounds is running on an unsupported platform and the addon '" + name + "' has not specified a general 'Main Class' property in description.");
                    }
                }

                Optional<String> startTime = description.getString("Start Time");

                if (startTime.isPresent())
                    try {
                        this.startTime = StartTime.valueOf(startTime.get());
                    } catch (IllegalArgumentException ex) {
                        throw new InvalidAddonException("The addon '" + name + "' has an invalid start time.", ex);
                    }
                else this.startTime = StartTime.SERVER_LOAD_COMPLETE;

                try {
                    version = new Version(description.getString("Version").orElse("1.0"));
                } catch (IllegalArgumentException ex) {
                    throw new InvalidAddonException("The addon '" + name + "' has an invalid version defined for Version key.", ex);
                }

                try {
                    apiVersion = new Version(description.getString("Api Version").orElse(PlayMoreSoundsVersion.version));
                } catch (IllegalArgumentException ex) {
                    throw new InvalidAddonException("The addon '" + name + "' has an invalid version defined for Api Version key.", ex);
                }

                this.description = description.getString("Description").orElse("I'm a PMSAddon.");
                authors = Collections.unmodifiableCollection(description.getCollection("Authors", Object::toString));
                pluginHooks = Collections.unmodifiableCollection(description.getCollection("Plugin Hooks", Object::toString));
                addonHooks = Collections.unmodifiableCollection(description.getCollection("Addon Hooks", Object::toString));
                requiredPlugins = Collections.unmodifiableCollection(description.getCollection("Required Plugins", Object::toString));
                requiredAddons = Collections.unmodifiableCollection(description.getCollection("Required Addons", Object::toString));
            } catch (InvalidConfigurationException ex) {
                throw new InvalidAddonException("The addon '" + fileName + "' has a misconfigured description file.", ex);
            }
        }
    }

    /**
     * For better organization addons names must:
     * <ul>
     *     <li>Contain only letters, numbers and spaces.</li>
     *     <li>Have the maximum of 26 characters.</li>
     *     <li>Have at least 3 characters.</li>
     *     <li>Have at least 3 letters.</li>
     * </ul>
     * <p>
     * All double spaces will be replaced by one.
     * <p>
     * The name extra spaces at the tail of the string will trimmed.
     *
     * @param name The string to parse as a PMSAddon name.
     * @return The formatted PMSAddon name.
     * @throws IllegalArgumentException If {@param name} does not meet any of the requirements listed above.
     */
    @Contract("null -> null")
    public static String parseName(String name) {
        if (name == null) return null;

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

    public @NotNull Collection<String> getAddonHooks() {
        return addonHooks;
    }

    public @NotNull Collection<String> getAuthors() {
        return authors;
    }

    public @NotNull Collection<String> getPluginHooks() {
        return pluginHooks;
    }

    public @NotNull Collection<String> getRequiredAddons() {
        return requiredAddons;
    }

    public @NotNull Collection<String> getRequiredPlugins() {
        return requiredPlugins;
    }

    public @NotNull StartTime getStartTime() {
        return startTime;
    }

    public @NotNull String getDescription() {
        return description;
    }

    public @NotNull String getMainClass() {
        return mainClass;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull Version getApiVersion() {
        return apiVersion;
    }

    public @NotNull Version getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "AddonDescription{" +
                "addonHooks=" + addonHooks +
                ", apiVersion=" + apiVersion +
                ", authors=" + authors +
                ", description='" + description + '\'' +
                ", mainClass='" + mainClass + '\'' +
                ", name='" + name + '\'' +
                ", pluginHooks=" + pluginHooks +
                ", requiredAddons=" + requiredAddons +
                ", requiredPlugins=" + requiredPlugins +
                ", startTime=" + startTime +
                ", version=" + version +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AddonDescription that)) return false;

        return addonHooks.equals(that.addonHooks) &&
                apiVersion.equals(that.apiVersion) &&
                authors.equals(that.authors) &&
                description.equals(that.description) &&
                mainClass.equals(that.mainClass) &&
                name.equals(that.name) &&
                pluginHooks.equals(that.pluginHooks) &&
                requiredAddons.equals(that.requiredAddons) &&
                requiredPlugins.equals(that.requiredPlugins) &&
                startTime == that.startTime &&
                version.equals(that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addonHooks, apiVersion, authors, description, mainClass, name, pluginHooks, requiredAddons, requiredPlugins, startTime, version);
    }
}
