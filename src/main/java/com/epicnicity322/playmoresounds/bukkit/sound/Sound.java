/*
 * PlayMoreSounds - A bukkit plugin that manages and plays sounds.
 * Copyright (C) 2021 Christiano Rangel
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

package com.epicnicity322.playmoresounds.bukkit.sound;

import com.epicnicity322.epicpluginlib.core.config.ConfigurationHolder;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.events.PlaySoundEvent;
import com.epicnicity322.playmoresounds.bukkit.sound.events.PrePlaySoundEvent;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;

public class Sound implements Playable
{
    private static final @NotNull ConfigurationHolder config = Configurations.CONFIG.getConfigurationHolder();
    private static final @NotNull BukkitScheduler scheduler = Bukkit.getScheduler();

    private final PlayMoreSounds plugin = PlayMoreSounds.getInstance();
    private final @Nullable ConfigurationSection section;
    private String sound;
    private @Nullable SoundType soundType;
    private float volume;
    private float pitch;
    private long delay;
    private SoundOptions options;

    public Sound(@NotNull String sound, float volume, float pitch, long delay, @Nullable SoundOptions options)
    {
        if (delay > 0 && plugin == null) {
            throw new UnsupportedOperationException("PlayMoreSounds must be enabled to play delayed sounds.");
        }

        setSound(sound);
        setOptions(options);

        this.volume = volume;
        this.pitch = pitch;
        this.delay = delay;
        this.section = null;
    }

    /**
     * Creates a {@link Sound} instance based on a section. This section is where Delay, Options, Pitch, Sound and
     * Volume nodes stands. Options are automatically constructed by this, but they must follow {@link SoundOptions}
     * {@link ConfigurationSection} constructor rules.
     *
     * @param section The section where the sound is.
     * @see SoundOptions
     * @see RichSound
     */
    public Sound(@NotNull ConfigurationSection section)
    {
        delay = section.getNumber("Delay").orElse(0).longValue();

        if (delay > 0 && plugin == null) {
            throw new UnsupportedOperationException("PlayMoreSounds must be enabled to play delayed sounds.");
        }

        setSound(section.getString("Sound").orElse("BLOCK_NOTE_BLOCK_PLING"));
        volume = section.getNumber("Volume").orElse(10).floatValue();
        pitch = section.getNumber("Pitch").orElse(1).floatValue();
        this.section = section;

        ConfigurationSection options = section.getConfigurationSection("Options");

        if (options == null)
            setOptions(null);
        else
            this.options = new SoundOptions(options);
    }

    /**
     * @return The {@link ConfigurationSection} of this sound or null if this sound was not constructed by a section.
     */
    public @Nullable ConfigurationSection getSection()
    {
        return section;
    }

    /**
     * This is not a {@link SoundType}, this is a minecraft sound name.
     *
     * @return The sound that will be played.
     * @see #getSoundType()
     */
    public @NotNull String getSound()
    {
        return sound;
    }

    public void setSound(@NotNull String sound)
    {
        if (SoundManager.getSoundList().contains(sound.toUpperCase())) {
            SoundType type = SoundType.valueOf(sound.toUpperCase());

            this.sound = type.getSound().orElse("BLOCK_NOTE_BLOCK_PLING");
            soundType = type;
        } else {
            this.sound = sound;
        }
    }

    /**
     * @return The sound type that will be played.
     */
    public @Nullable SoundType getSoundType()
    {
        return soundType;
    }

    /**
     * @return The volume of this sound.
     */
    public float getVolume()
    {
        return volume;
    }

    public void setVolume(float volume)
    {
        this.volume = volume;
    }

    /**
     * @return The pitch of this sound.
     */
    public float getPitch()
    {
        return pitch;
    }

    public void setPitch(float pitch)
    {
        this.pitch = pitch;
    }

    /**
     * @return The time in ticks to wait before playing this sound.
     */
    public long getDelay()
    {
        return delay;
    }

    /**
     * Sets the time in ticks to wait before playing this sound. 1 second = 20 ticks
     *
     * @param delay The time in ticks.
     */
    public void setDelay(long delay)
    {
        this.delay = delay;
    }

    public @NotNull SoundOptions getOptions()
    {
        return options;
    }

    public void setOptions(@Nullable SoundOptions options)
    {
        if (options == null)
            this.options = new SoundOptions(false, null, null, 0.0, null);
        else
            this.options = options;
    }

    @Override
    public void play(@Nullable Player player, @NotNull Location sourceLocation)
    {
        PrePlaySoundEvent preEvent = new PrePlaySoundEvent(player, sourceLocation, this);

        Bukkit.getPluginManager().callEvent(preEvent);

        if (!preEvent.isCancelled() &&
                (options.getPermissionRequired() == null || (player == null || player.hasPermission(options.getPermissionRequired()))) &&
                (player == null || (!player.hasPotionEffect(PotionEffectType.INVISIBILITY) || !player.hasPermission("playmoresounds.bypass.invisibility")))) {
            Location soundLocation = SoundManager.addRelativeLocation(preEvent.getLocation(), options.getRelativeLocation());
            Collection<Player> players = SoundManager.getInRange(options.getRadius(), preEvent.getLocation());

            if (player != null)
                players.add(player);

            Sound instance = this;

            if (delay == 0)
                play(player, players, soundLocation, instance);
            else
                scheduler.runTaskLater(plugin, () -> play(player, players, soundLocation, instance), delay);
        }
    }

    private void play(@Nullable Player sourcePlayer, @NotNull Collection<Player> players, @NotNull Location soundLocation,
                      @NotNull Sound instance)
    {
        for (Player inRange : players) {
            if (!config.getConfiguration().getCollection("World Black List").contains(inRange.getWorld().getName())
                    && (options.ignoresDisabled() || SoundManager.getSoundsState(inRange))
                    && (options.getPermissionToListen() == null || inRange.hasPermission(options.getPermissionToListen()))
                    && (sourcePlayer == null || inRange.canSee(sourcePlayer))) {
                Location fixedLocation = soundLocation;

                if (options.getRadius() < 0)
                    fixedLocation = SoundManager.addRelativeLocation(inRange.getLocation(), options.getRelativeLocation());

                PlaySoundEvent event = new PlaySoundEvent(instance, inRange, fixedLocation, players, sourcePlayer, soundLocation);

                Bukkit.getPluginManager().callEvent(event);

                if (!event.isCancelled())
                    inRange.playSound(event.getLocation(), sound, volume, pitch);
            }
        }
    }

    @Override
    public String toString()
    {
        return "Sound{" +
                "sound='" + sound + '\'' +
                ", volume=" + volume +
                ", pitch=" + pitch +
                ", delay=" + delay +
                ", section=" + (section == null ? "null" : "'" + section.getPath() + '\'') +
                ", options=" + options +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Sound)) return false;

        Sound sound1 = (Sound) o;

        return Float.compare(sound1.getVolume(), getVolume()) == 0 &&
                Float.compare(sound1.getPitch(), getPitch()) == 0 &&
                getDelay() == sound1.getDelay() &&
                getSound().equals(sound1.getSound()) &&
                getOptions().equals(sound1.getOptions());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getSound(), getVolume(), getPitch(), getDelay(), getOptions(), getSection());
    }
}
