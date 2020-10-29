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

package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
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
    private final @NotNull PlayMoreSounds plugin;
    private @Nullable RichSound richSound;
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

    public @Nullable RichSound getRichSound()
    {
        return richSound;
    }

    protected final void setRichSound(@Nullable RichSound richSound)
    {
        this.richSound = richSound;
    }

    /**
     * Register the events of this listener if they were not registered before and if the listener is enabled on sounds.yml
     * or unregister the listener if it is disabled on sounds.yml.
     */
    public void load()
    {
        Configuration sounds = Configurations.SOUNDS.getPluginConfig().getConfiguration();
        ConfigurationSection section = sounds.getConfigurationSection(getName());

        if (section == null || !section.getBoolean("Enabled").orElse(false)) {
            if (loaded) {
                HandlerList.unregisterAll(this);
                loaded = false;
            }
        } else {
            richSound = new RichSound(section);

            if (!loaded) {
                Bukkit.getPluginManager().registerEvents(this, plugin);
                loaded = true;
            }
        }
    }
}
