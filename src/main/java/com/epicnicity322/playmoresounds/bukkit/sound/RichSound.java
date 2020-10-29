package com.epicnicity322.playmoresounds.bukkit.sound;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.events.PlayRichSoundEvent;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.function.Supplier;

public class RichSound implements Playable
{
    private final @NotNull String name;
    private @Nullable ConfigurationSection section;
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
     * The {@link BukkitRunnable} will not run if this sound is disabled or has no child sounds.
     * {@link PlayRichSoundEvent} will be called for every time the sound is played by this loop.
     *
     * @param player         The player to play the sound.
     * @param sourceLocation The location where the sound will play. May change depending on {@link SoundOptions#getRelativeLocation()}.
     * @param delay          The time in ticks to wait before playing the first sound.
     * @param period         The time in ticks to wait before playing the sound again.
     * @param breaker        A boolean that will run in the loop, if the boolean is true the loop will be cancelled.
     * @return The {@link BukkitRunnable} of the loop that can be used to cancel later.
     * @throws IllegalStateException If PlayMoreSounds was not instantiated by bukkit yet.
     */
    public @NotNull BukkitRunnable playInLoop(@Nullable Player player, @NotNull Location sourceLocation, long delay, long period, @Nullable Supplier<Boolean> breaker)
    {
        PlayMoreSounds main = PlayMoreSounds.getInstance();

        if (main == null)
            throw new IllegalStateException("PlayMoreSounds is not loaded.");

        BukkitRunnable runnable;

        // The runnable will not check if the breaker is null on each loop.
        if (breaker == null)
            runnable = new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    play(player, sourceLocation);
                }
            };
        else
            runnable = new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if (breaker.get()) {
                        cancel();
                        return;
                    }

                    play(player, sourceLocation);
                }
            };

        if (isEnabled() && !getChildSounds().isEmpty())
            runnable.runTaskTimer(main, delay, period);

        return runnable;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RichSound richSound = (RichSound) o;

        return enabled == richSound.enabled &&
                cancellable == richSound.cancellable &&
                Objects.equals(section, richSound.section) &&
                childSounds.equals(richSound.childSounds);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(section, enabled, cancellable, childSounds);
    }

    @Override
    public String toString()
    {
        return "RichSound{" +
                "name='" + name + '\'' +
                ", section=" + (section == null ? "null" : "'" + section.getPath() + '\'') +
                ", enabled=" + enabled +
                ", cancellable=" + cancellable +
                ", childSounds=" + childSounds +
                '}';
    }
}
