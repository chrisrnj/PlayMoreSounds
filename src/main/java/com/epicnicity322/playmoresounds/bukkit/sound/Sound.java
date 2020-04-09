package com.epicnicity322.playmoresounds.bukkit.sound;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.events.PrePlaySoundEvent;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public class Sound implements Playable
{
    private String id;
    private String name;
    private String sound;
    private SoundType soundType;
    private float volume;
    private float pitch;
    private long delay;
    private SoundOptions options;

    public Sound(@Nullable String id, @NotNull String sound, float volume, float pitch, long delay,
                 @Nullable SoundOptions options)
    {
        setSound(sound);
        setOptions(options);

        if (id == null) {
            id = PMSHelper.getRandomString(5);
        } else {
            this.id = id;
        }

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
        Validate.notNull(section, "section is null");

        setSound(section.getString("Sound", "BLOCK_NOTE_BLOCK_PLING"));
        id = section.getName();
        name = section.getCurrentPath();
        delay = section.getLong("Delay", 0);

        // Spigot team please add a way of getting floats on configurations.
        volume = (float) section.getDouble("Volume", 10);
        pitch = (float) section.getDouble("Pitch", 1);

        if (section.contains("Options")) {
            options = new SoundOptions(section.getConfigurationSection("Options"));
        } else {
            setOptions(null);
        }
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    /**
     * This is not a {@link SoundType}, this is a minecraft sound name.
     *
     * @return The sound that will be played.
     * @see #getSoundType()
     */
    @NotNull
    public String getSound()
    {
        return sound;
    }

    public void setSound(@NotNull String sound)
    {
        Validate.notNull(sound, "sound is null");

        if (PlayMoreSounds.SOUND_LIST.contains(sound.toUpperCase())) {
            SoundType type = SoundType.valueOf(sound.toUpperCase());

            this.sound = type.getSoundOnVersion();
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

    @Nullable
    public SoundOptions getOptions()
    {
        return options;
    }

    public void setOptions(@Nullable SoundOptions options)
    {
        if (options == null) {
            this.options = new SoundOptions(false, false, null,
                    null, 0.0, null);
        } else {
            this.options = options;
        }
    }

    @Override
    public void play(@NotNull Location sourceLocation) throws NullPointerException
    {
        play(null, sourceLocation);
    }

    @Override
    public void play(@NotNull Player player)
    {
        Validate.notNull(player, "player is null");

        if (options.isEyeLocation()) {
            play(player, player.getEyeLocation());
        } else {
            play(player, player.getLocation());
        }
    }

    @Override
    public void play(@Nullable Player player, @NotNull Location sourceLocation)
    {
        Validate.notNull(sourceLocation, "sourceLocation is null");

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

            if (delay == 0) {
                play(player, players, soundLocation, instance);
            } else {
                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        play(player, players, soundLocation, instance);
                    }
                }.runTaskLater(PlayMoreSounds.getPlugin(), delay);
            }
        }
    }

    private void play(@Nullable Player sourcePlayer, @NotNull HashSet<Player> players, @NotNull Location soundLocation,
                      @NotNull Sound instance)
    {
        for (Player inRange : players) {
            if (!PMSHelper.getConfig("config").getStringList("World-BlackList").contains(inRange.getWorld().getName())
                    && (options.ignoresToggle() || !PlayMoreSounds.IGNORED_PLAYERS.contains(inRange.getName()))
                    && (options.getPermissionToListen() == null || inRange.hasPermission(options.getPermissionToListen()))
                    && (sourcePlayer == null || inRange.canSee(sourcePlayer))) {
                // If the radius is -2 or -1 the sound will be played to multiple players so the location needs to be fixed.
                Location fixedLocation = soundLocation;

                if (options.getRadius() < 0) {
                    if (options.isEyeLocation())
                        fixedLocation = inRange.getEyeLocation();
                    else
                        fixedLocation = inRange.getLocation();

                    fixedLocation = SoundManager.addRelativeLocation(fixedLocation, options.getRelativeLocation());
                }

                //TODO update this
                //PlaySoundEvent event = new PlaySoundEvent(sourcePlayer, players, soundLocation, instance);

                //Bukkit.getPluginManager().callEvent(event);

                //if (!event.isCancelled()) {
                inRange.playSound(fixedLocation, sound, volume, pitch);
                //}
            }
        }
    }
}
