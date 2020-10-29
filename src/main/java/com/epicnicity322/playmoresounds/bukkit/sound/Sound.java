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

package com.epicnicity322.playmoresounds.bukkit.sound;

import com.epicnicity322.epicpluginlib.core.config.PluginConfig;
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

import java.util.HashSet;
import java.util.Objects;

public class Sound implements Playable
{
    private static final @NotNull PluginConfig config = Configurations.CONFIG.getPluginConfig();
    private static final @NotNull BukkitScheduler scheduler = Bukkit.getScheduler();
    private final @Nullable ConfigurationSection section;
    private String sound;
    private @Nullable SoundType soundType;
    private float volume;
    private float pitch;
    private long delay;
    private SoundOptions options;

    public Sound(@NotNull String sound, float volume, float pitch, long delay, @Nullable SoundOptions options)
    {
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
        setSound(section.getString("Sound").orElse("BLOCK_NOTE_BLOCK_PLING"));
        volume = section.getNumber("Volume").orElse(10).floatValue();
        pitch = section.getNumber("Pitch").orElse(1).floatValue();
        delay = section.getNumber("Delay").orElse(0).longValue();
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
            HashSet<Player> players = SoundManager.getInRange(options.getRadius(), preEvent.getLocation());

            if (player != null)
                players.add(player);

            Sound instance = this;

            if (delay == 0)
                play(player, players, soundLocation, instance);
            else {
                PlayMoreSounds main = PlayMoreSounds.getInstance();

                if (main == null)
                    throw new IllegalStateException("PlayMoreSounds is not loaded.");

                scheduler.runTaskLater(main, () -> play(player, players, soundLocation, instance), delay);
            }
        }
    }

    private void play(@Nullable Player sourcePlayer, @NotNull HashSet<Player> players, @NotNull Location soundLocation,
                      @NotNull Sound instance)
    {
        for (Player inRange : players) {
            if (!config.getConfiguration().getCollection("World Black List").contains(inRange.getWorld().getName())
                    && (options.ignoresToggle() || !SoundManager.getIgnoredPlayers().contains(inRange.getUniqueId()))
                    && (options.getPermissionToListen() == null || inRange.hasPermission(options.getPermissionToListen()))
                    && (sourcePlayer == null || inRange.canSee(sourcePlayer))) {
                Location fixedLocation = soundLocation;

                if (options.getRadius() < 0)
                    fixedLocation = SoundManager.addRelativeLocation(fixedLocation, options.getRelativeLocation());

                PlaySoundEvent event = new PlaySoundEvent(instance, inRange, fixedLocation, players, sourcePlayer,
                        soundLocation);

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
