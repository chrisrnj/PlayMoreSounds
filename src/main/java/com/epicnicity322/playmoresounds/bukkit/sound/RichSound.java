package com.epicnicity322.playmoresounds.bukkit.sound;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.events.PlayRichSoundEvent;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;

public class RichSound implements Playable
{
    private static final @NotNull BukkitScheduler scheduler = Bukkit.getScheduler();
    private final String name;
    private ConfigurationSection section;
    private boolean enabled;
    private boolean cancellable;
    private Collection<Sound> childSounds;

    public RichSound(@NotNull ConfigurationSection section)
    {
        if (section instanceof Configuration)
            throw new UnsupportedOperationException("Section can not be Configuration.");

        this.section = section;
        this.name = section.getPath();
        enabled = section.getBoolean("Enabled").orElse(false);
        cancellable = section.getBoolean("Cancellable").orElse(false);
        childSounds = new LinkedHashSet<>();

        ConfigurationSection sounds = section.getConfigurationSection("Sounds");

        if (sounds != null) {
            for (String childSound : sounds.getNodes().keySet())
                childSounds.add(new Sound(sounds.getConfigurationSection(childSound)));
        }
    }

    public RichSound(@NotNull String name, boolean enabled, boolean cancellable, @Nullable Collection<Sound> childSounds)
    {
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
        if (sounds == null)
            this.childSounds = new HashSet<>();
        else
            this.childSounds = sounds;
    }

    public void save(@NotNull Path path)
    {
    }

    @Override
    public void play(@Nullable Player player, @NotNull Location sourceLocation)
    {
        if (isEnabled() && !getChildSounds().isEmpty()) {
            PlayRichSoundEvent event = new PlayRichSoundEvent(player, sourceLocation, this);

            Bukkit.getPluginManager().callEvent(event);

            if (!event.isCancelled())
                for (Sound s : getChildSounds())
                    s.play(player, event.getLocation());
        }
    }

    /**
     * Plays the sound repeatedly after the time set on period.
     * The task will be cancelled if the sound is disabled or has no child sounds.
     * {@link PlayRichSoundEvent} will be called for every time the sound is played by this loop.
     *
     * @param player         The player to play the sound.
     * @param sourceLocation The location where the sound will play. May change depending on
     *                       {@link SoundOptions#getRelativeLocation()} and {@link SoundOptions#isEyeLocation()}.
     * @param delay          The time in ticks to wait before playing the first sound.
     * @param period         The time in ticks to wait before playing the sound again.
     * @return The {@link BukkitTask} of the loop that can be used to cancel later.
     * @throws IllegalStateException If PlayMoreSounds was not instantiated by bukkit yet.
     */
    public @NotNull BukkitTask playInLoop(@Nullable Player player, @NotNull Location sourceLocation, long delay, long period)
    {
        PlayMoreSounds main = PlayMoreSounds.getInstance();

        if (main == null)
            throw new IllegalStateException("PlayMoreSounds is not loaded.");

        BukkitTask task = scheduler.runTaskTimer(main, () -> play(player, sourceLocation), delay, period);

        if (!isEnabled() || getChildSounds().isEmpty())
            task.cancel();

        return task;
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
