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
