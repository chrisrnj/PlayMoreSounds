package com.epicnicity322.playmoresounds.bukkit.sound;

import com.epicnicity322.playmoresounds.bukkit.sound.events.PlayRichSoundEvent;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;

public class RichSound implements Playable
{
    private String name;
    private ConfigurationSection section;
    private boolean enabled;
    private boolean cancellable;
    private Collection<Sound> childSounds;

    public RichSound(@NotNull ConfigurationSection section)
    {
        Validate.notNull(section, "section is null");

        this.section = section;
        this.name = section.getCurrentPath();
        enabled = section.getBoolean("Enabled");
        cancellable = section.getBoolean("Cancellable");
        childSounds = new LinkedHashSet<>();

        if (section.contains("Sounds")) {
            ConfigurationSection soundsSection = section.getConfigurationSection("Sounds");

            for (String childSound : soundsSection.getKeys(false)) {
                childSounds.add(new Sound(soundsSection.getConfigurationSection(childSound)));
            }
        }
    }

    public RichSound(@NotNull String name, boolean enabled, boolean cancellable, @Nullable Collection<Sound> childSounds)
    {
        Validate.notNull(name, "name is null");

        this.name = name;
        this.enabled = enabled;
        this.cancellable = cancellable;
        setChildSounds(childSounds);
    }

    public @NotNull String getName()
    {
        return name;
    }

    public @Nullable ConfigurationSection getSection()
    {
        return section;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean isCancellable()
    {
        return cancellable;
    }

    public void setCancellable(boolean cancellable)
    {
        this.cancellable = cancellable;
    }

    public @NotNull Collection<Sound> getChildSounds()
    {
        return childSounds;
    }

    public void setChildSounds(@Nullable Collection<Sound> sounds)
    {
        if (sounds == null) {
            this.childSounds = new HashSet<>();
        } else {
            this.childSounds = sounds;
        }
    }

    public void save(@NotNull Path path) throws IOException
    {
        Validate.notNull(path);

        if (Files.isDirectory(path)) {
            throw new IllegalArgumentException("path is pointing to a directory");
        }
        if (!Files.exists(path)) {
            Files.createFile(path);
        }


    }

    @Override
    public void play(@NotNull Location sourceLocation)
    {
        if (enabled) {
            if (!getChildSounds().isEmpty()) {
                PlayRichSoundEvent event = new PlayRichSoundEvent(null, sourceLocation, this);

                Bukkit.getPluginManager().callEvent(event);

                if (!event.isCancelled())
                    for (Sound s : getChildSounds())
                        s.play(event.getLocation());
            }
        }
    }

    @Override
    public void play(@NotNull Player player)
    {
        if (enabled) {
            if (!getChildSounds().isEmpty()) {
                PlayRichSoundEvent event = new PlayRichSoundEvent(player, player.getLocation(), this);

                Bukkit.getPluginManager().callEvent(event);

                if (!event.isCancelled())
                    for (Sound s : getChildSounds())
                        s.play(player);
            }
        }
    }

    @Override
    public void play(@Nullable Player player, @NotNull Location sourceLocation)
    {
        if (enabled) {
            if (!getChildSounds().isEmpty()) {
                PlayRichSoundEvent event = new PlayRichSoundEvent(player, sourceLocation, this);

                Bukkit.getPluginManager().callEvent(event);

                if (!event.isCancelled())
                    for (Sound s : getChildSounds())
                        s.play(player, event.getLocation());
            }
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RichSound richSound = (RichSound) o;

        return isEnabled() == richSound.isEnabled() &&
                isCancellable() == richSound.isCancellable() &&
                Objects.equals(getSection(), richSound.getSection()) &&
                getChildSounds().equals(richSound.getChildSounds());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getSection(), isEnabled(), isCancellable(), getChildSounds());
    }

    @Override
    public String toString()
    {
        return "RichSound{" +
                "section=" + section +
                ", enabled=" + enabled +
                ", cancellable=" + cancellable +
                ", sounds=" + childSounds +
                '}';
    }
}
