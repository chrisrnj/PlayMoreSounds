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

package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PMSListener implements Listener
{
    protected final @NotNull PlayMoreSounds plugin;
    private @Nullable PlayableRichSound richSound;
    private boolean loaded = false;

    public PMSListener(@NotNull PlayMoreSounds plugin)
    {
        this.plugin = plugin;
    }

    public abstract @NotNull String getName();

    public final boolean isLoaded()
    {
        return loaded;
    }

    protected final void setLoaded(boolean loaded)
    {
        this.loaded = loaded;
    }

    public @Nullable PlayableRichSound getRichSound()
    {
        return richSound;
    }

    protected final void setRichSound(@Nullable PlayableRichSound richSound)
    {
        this.richSound = richSound;
    }

    /**
     * Register the events of this listener if they were not registered before and if the listener is enabled on sounds.yml
     * or unregister the listener if it is disabled on sounds.yml.
     */
    public void load()
    {
        Configuration sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();
        ConfigurationSection section = sounds.getConfigurationSection(getName());

        if (section == null || !section.getBoolean("Enabled").orElse(false)) {
            if (loaded) {
                HandlerList.unregisterAll(this);
                loaded = false;
            }
        } else {
            richSound = new PlayableRichSound(section);

            if (!loaded) {
                Bukkit.getPluginManager().registerEvents(this, plugin);
                loaded = true;
            }
        }
    }
}
