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

package com.epicnicity322.playmoresounds.bukkit.listeners;

import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.bukkit.util.ListenerRegister;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.ThreadSafe;
import java.nio.file.Path;
import java.util.Optional;

/**
 * A class that automatically registers the listener in case a sound with name {@link #getName()} is enabled in sounds.yml.
 * <p>
 * When PlayMoreSounds is loaded or reloaded and this class is in {@link ListenerRegister#getListeners()}, the method
 * {@link #load()} is called.
 */
@ThreadSafe
public abstract class PMSListener implements Listener
{
    protected final @NotNull PlayMoreSounds plugin;
    private @Nullable PlayableRichSound richSound;
    private boolean loaded = false;

    public PMSListener(@NotNull PlayMoreSounds plugin)
    {
        this.plugin = plugin;
    }

    /**
     * Gets the {@link PlayableRichSound} in the section.
     * <p>
     * If any of the child sounds is invalid, a warning message is sent to console and null is returned.
     *
     * @param section The section to create the sound
     * @return The sound or null if it's disabled or has invalid sound.
     */
    @Contract("null -> null")
    protected static @Nullable PlayableRichSound getRichSound(@Nullable ConfigurationSection section)
    {
        if (section == null) return null;
        if (!section.getBoolean("Enabled").orElse(false)) return null;

        try {
            return new PlayableRichSound(section);
        } catch (IllegalArgumentException e) {
            Optional<Path> optionalPath = section.getRoot().getFilePath();
            if (optionalPath.isEmpty()) return null;
            PlayMoreSounds.getConsoleLogger().log("The sound " + section.getPath() + " in config " + optionalPath.get().getFileName().toString() + " has a child sound with invalid namespaced key characters, so it was ignored.", ConsoleLogger.Level.WARN);
            return null;
        }
    }

    public abstract @NotNull String getName();

    public final synchronized boolean isLoaded()
    {
        return loaded;
    }

    protected final synchronized void setLoaded(boolean loaded)
    {
        this.loaded = loaded;
    }

    public final synchronized @Nullable PlayableRichSound getRichSound()
    {
        return richSound;
    }

    protected final synchronized void setRichSound(@Nullable PlayableRichSound richSound)
    {
        this.richSound = richSound;
    }

    /**
     * Checks if a sound with name {@link #getName()} is enabled in sounds.yml configuration. If so, then the listener
     * is registered in case it was not registered before. The listener is unregistered if the sound is disabled as well.
     * <p>
     * This also updates the {@link #getRichSound()} by setting it to null if it's disabled, or create a new instance if
     * it's enabled.
     * <p>
     * You may inherit this method if you want your listener registered/unregistered in special occasions.
     *
     * @see #setLoaded(boolean)
     * @see #setRichSound(PlayableRichSound)
     */
    public void load()
    {
        var sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();

        synchronized (this) {
            richSound = getRichSound(sounds.getConfigurationSection(getName()));

            if (richSound == null) {
                if (loaded) {
                    HandlerList.unregisterAll(this);
                    loaded = false;
                }
            } else {
                if (!loaded) {
                    Bukkit.getPluginManager().registerEvents(this, plugin);
                    loaded = true;
                }
            }
        }
    }
}
