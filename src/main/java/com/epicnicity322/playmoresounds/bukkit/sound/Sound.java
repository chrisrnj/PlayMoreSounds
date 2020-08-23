package com.epicnicity322.playmoresounds.bukkit.sound;

import com.epicnicity322.epicpluginlib.core.config.PluginConfig;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.events.PlaySoundEvent;
import com.epicnicity322.playmoresounds.bukkit.sound.events.PrePlaySoundEvent;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public class Sound implements Playable
{
    private static final @NotNull PluginConfig config = Configurations.CONFIG.getPluginConfig();
    private static final @NotNull BukkitScheduler scheduler = Bukkit.getScheduler();
    private final @NotNull String name;
    private final @NotNull String id;
    private String sound;
    private @Nullable SoundType soundType;
    private float volume;
    private float pitch;
    private long delay;
    private SoundOptions options;

    public Sound(@Nullable String id, @NotNull String sound, float volume, float pitch, long delay,
                 @Nullable SoundOptions options)
    {
        setSound(sound);
        setOptions(options);

        if (id == null)
            this.id = PMSHelper.getRandomString(5);
        else
            this.id = id;

        name = id;
        this.volume = volume;
        this.pitch = pitch;
        this.delay = delay;
    }

    /**
     * Creates a Sound instance based on a section. This section is where Delay, Options, Pitch, Sound and Volume
     * stands. Options are automatically constructed by this, but they must follow {@link SoundOptions} ConfigurationSection
     * constructor rules.
     *
     * @param section The section where the sound is.
     * @see SoundOptions
     * @see RichSound
     */
    public Sound(@NotNull ConfigurationSection section)
    {
        if (section instanceof Configuration)
            throw new UnsupportedOperationException("Section can not be Configuration.");

        setSound(section.getString("Sound").orElse("BLOCK_NOTE_BLOCK_PLING"));
        id = section.getName();
        name = section.getPath();
        delay = section.getNumber("Delay").orElse(0).longValue();
        volume = section.getNumber("Volume").orElse(10).floatValue();
        pitch = section.getNumber("Pitch").orElse(1).floatValue();

        ConfigurationSection options = section.getConfigurationSection("Options");

        if (options == null)
            setOptions(null);
        else
            this.options = new SoundOptions(options);
    }

    public @NotNull String getId()
    {
        return id;
    }

    public @NotNull String getName()
    {
        return name;
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

            this.sound = type.getSound().orElse(null);
            soundType = type;
        } else {
            this.sound = sound;
        }
    }

    @Nullable
    public SoundType getSoundType()
    {
        return soundType;
    }

    public float getVolume()
    {
        return volume;
    }

    public void setVolume(float volume)
    {
        this.volume = volume;
    }

    public float getPitch()
    {
        return pitch;
    }

    public void setPitch(float pitch)
    {
        this.pitch = pitch;
    }

    public long getDelay()
    {
        return delay;
    }

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
            this.options = new SoundOptions(false, false, null,
                    null, 0.0, null);
        else
            this.options = options;
    }

    @Override
    public void play(@NotNull Player player)
    {
        if (options.isEyeLocation())
            play(player, player.getEyeLocation());
        else
            play(player, player.getLocation());
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
            if (!config.getConfiguration().getCollection("World-BlackList").contains(inRange.getWorld().getName())
                    && (options.ignoresToggle() || !SoundManager.getIgnoredPlayers().contains(inRange.getUniqueId()))
                    && (options.getPermissionToListen() == null || inRange.hasPermission(options.getPermissionToListen()))
                    && (sourcePlayer == null || inRange.canSee(sourcePlayer))) {
                Location fixedLocation = soundLocation;

                if (options.getRadius() < 0) {
                    if (options.isEyeLocation())
                        fixedLocation = inRange.getEyeLocation();
                    else
                        fixedLocation = inRange.getLocation();

                    fixedLocation = SoundManager.addRelativeLocation(fixedLocation, options.getRelativeLocation());
                }

                PlaySoundEvent event = new PlaySoundEvent(instance, inRange, fixedLocation, players, sourcePlayer,
                        soundLocation);

                Bukkit.getPluginManager().callEvent(event);

                if (!event.isCancelled())
                    inRange.playSound(event.getLocation(), sound, volume, pitch);
            }
        }
    }
}
