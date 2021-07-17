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

import com.epicnicity322.epicpluginlib.bukkit.reflection.ReflectionUtil;
import com.epicnicity322.epicpluginlib.bukkit.reflection.type.PackageType;
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.epicpluginlib.core.util.ObjectUtils;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.events.PlaySoundEvent;
import com.epicnicity322.playmoresounds.bukkit.sound.events.PrePlaySoundEvent;
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.sound.Sound;
import com.epicnicity322.playmoresounds.core.sound.SoundCategory;
import com.epicnicity322.playmoresounds.core.sound.SoundOptions;
import com.epicnicity322.playmoresounds.core.sound.SoundType;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

public class PlayableSound extends Sound implements Playable
{
    private static final @NotNull HashSet<String> blackListedWorlds = new HashSet<>();
    private static final @NotNull Validator validator;
    private static final @NotNull SoundPlayer soundPlayer;
    private static Object[] soundCategory_enumConstants;
    private static Method soundCategory_a_method;
    private static Constructor<?> minecraftKey_constructor;

    static {
        // PlayMoreSounds might be used on versions where GameMode.SPECTATOR is not a thing.
        Validator noSpectator = (player, permissionRequired) -> (permissionRequired != null && !player.hasPermission(permissionRequired)) || (player.hasPotionEffect(PotionEffectType.INVISIBILITY) && player.hasPermission("playmoresounds.bypass.invisibility"));

        if (GameMode.values().length >= 4) {
            validator = (player, permissionRequired) -> noSpectator.isInvalidPlayer(player, permissionRequired) || player.getGameMode() == GameMode.SPECTATOR;
        } else {
            validator = noSpectator;
        }

        // Checking if SoundCategory is present.
        boolean hasSoundCategory = ReflectionUtil.getClass("org.bukkit.SoundCategory") != null;
        // Checking if custom sounds are present.
        boolean hasCustomSounds = false;

        try {
            Player.class.getMethod("playSound", Location.class, String.class, float.class, float.class);
            hasCustomSounds = true;
        } catch (NoSuchMethodException ignored) {
        }

        // Setting the sound player
        if (hasSoundCategory) {
            // Custom sounds were already a thing when sound categories were added, so no need to check again.
            soundPlayer = (player, location, sound) -> player.playSound(location, sound.getSound(), sound.getCategory().asBukkit(), sound.getVolume(), sound.getPitch());
        } else {
            if (hasCustomSounds) {
                if (Configurations.CONFIG.getConfigurationHolder().getConfiguration().getBoolean("Send Packets Directly").orElse(false) && VersionUtils.getBukkitVersion().compareTo(new Version("1.13")) >= 0) {
                    soundPlayer = getPacketSoundPlayer();
                } else {
                    soundPlayer = (player, location, sound) -> player.playSound(location, sound.getSound(), sound.getVolume(), sound.getPitch());
                }
            } else {
                // Versions before Player#playSound(Location,String,float,float) used to have Packet62NamedSoundEffect, sending packet manually.
                Class<?> packet62NamedSoundEffect_class = ReflectionUtil.getClass("Packet62NamedSoundEffect", PackageType.MINECRAFT_SERVER);
                Constructor<?> packet62NamedSoundEffect_constructor = null;

                try {
                    if (packet62NamedSoundEffect_class != null) {
                        packet62NamedSoundEffect_constructor = packet62NamedSoundEffect_class.getConstructor(String.class, double.class, double.class, double.class, float.class, float.class);
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }

                Constructor<?> finalPacket62NamedSoundEffect_constructor = packet62NamedSoundEffect_constructor;

                soundPlayer = (player, location, sound) -> {
                    try {
                        ReflectionUtil.sendPacket(player, finalPacket62NamedSoundEffect_constructor.newInstance(sound.getSound(), location.getX() + 0.5, location.getY() + 0.5, location.getZ() + 0.5, sound.getVolume(), sound.getPitch()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                };
            }
        }

        // Updating blackListedWorlds, this way it doesn't need to read from config when playing a sound.
        Runnable blackListedWorldsUpdater = () -> {
            synchronized (blackListedWorlds) {
                blackListedWorlds.clear();
                blackListedWorlds.addAll(Configurations.CONFIG.getConfigurationHolder().getConfiguration().getCollection("World Black List", Object::toString));
            }
        };

        blackListedWorldsUpdater.run();
        PlayMoreSounds.onReload(blackListedWorldsUpdater);
    }

    private Object minecraftKeySound;
    private Object soundCategory;
    private PlayMoreSounds plugin = PlayMoreSounds.getInstance();

    public PlayableSound(@NotNull String sound, @Nullable SoundCategory category, float volume, float pitch, long delay, @Nullable SoundOptions options)
    {
        super(sound, category, volume, pitch, delay, options);

        if (delay > 0 && plugin == null) {
            throw new UnsupportedOperationException("PlayMoreSounds must be enabled to play delayed sounds.");
        }
    }

    public PlayableSound(@NotNull ConfigurationSection section)
    {
        super(section);

        if (getDelay() > 0 && plugin == null) {
            throw new UnsupportedOperationException("PlayMoreSounds must be enabled to play delayed sounds.");
        }
    }

    private static @NotNull SoundPlayer getPacketSoundPlayer()
    {
        SoundPlayer defaultSoundPlayer = (player, location, sound) -> player.playSound(location, sound.getSound(), sound.getVolume(), sound.getPitch());

        try {
            Class<?> packetPlayOutCustomSoundEffect_class = ObjectUtils.getOrDefault(ReflectionUtil.getClass("PacketPlayOutCustomSoundEffect", PackageType.MINECRAFT_SERVER), ReflectionUtil.getClass("net.minecraft.network.protocol.game.PacketPlayOutCustomSoundEffect"));
            Class<?> minecraftKey_class = ObjectUtils.getOrDefault(ReflectionUtil.getClass("MinecraftKey", PackageType.MINECRAFT_SERVER), ReflectionUtil.getClass("net.minecraft.resources.MinecraftKey"));
            Class<?> soundCategory_class = ObjectUtils.getOrDefault(ReflectionUtil.getClass("SoundCategory", PackageType.MINECRAFT_SERVER), ReflectionUtil.getClass("net.minecraft.sounds.SoundCategory"));
            Class<?> vec3D_class = ObjectUtils.getOrDefault(ReflectionUtil.getClass("Vec3D", PackageType.MINECRAFT_SERVER), ReflectionUtil.getClass("net.minecraft.world.phys.Vec3D"));

            if (packetPlayOutCustomSoundEffect_class == null || minecraftKey_class == null || soundCategory_class == null || vec3D_class == null)
                return defaultSoundPlayer;

            soundCategory_a_method = soundCategory_class.getMethod("a");
            soundCategory_enumConstants = soundCategory_class.getEnumConstants();

            if (soundCategory_a_method == null || soundCategory_enumConstants == null) return defaultSoundPlayer;

            // Removing checks for security, making instantiation faster.
            soundCategory_a_method.setAccessible(true);

            Constructor<?> packetPlayOutCustomSoundEffect_constructor = ReflectionUtil.getConstructor(packetPlayOutCustomSoundEffect_class, minecraftKey_class, soundCategory_class, vec3D_class, float.class, float.class);
            minecraftKey_constructor = ReflectionUtil.getConstructor(minecraftKey_class, String.class);
            Constructor<?> vec3D_constructor = ReflectionUtil.getConstructor(vec3D_class, double.class, double.class, double.class);

            if (packetPlayOutCustomSoundEffect_constructor == null || minecraftKey_constructor == null || vec3D_constructor == null)
                return defaultSoundPlayer;

            // Removing checks for security, making instantiation faster.
            packetPlayOutCustomSoundEffect_constructor.setAccessible(true);
            vec3D_constructor.setAccessible(true);

            return (player, location, sound) -> {
                try {
                    ReflectionUtil.sendPacket(player, packetPlayOutCustomSoundEffect_constructor.newInstance(sound.minecraftKeySound, sound.soundCategory, vec3D_constructor.newInstance(location.getX(), location.getY(), location.getZ()), sound.getVolume(), sound.getPitch()));
                } catch (Exception e) {
                    PlayMoreSounds.getConsoleLogger().log("Could not play '" + sound.getSound() + "' using reflection. Please disable 'Send Packets Directly' in config.", ConsoleLogger.Level.WARN);
                    PlayMoreSoundsCore.getErrorHandler().report(e, "Send Sound Packet Exception:");
                }
            };
        } catch (Exception e) {
            return defaultSoundPlayer;
        }
    }

    @Override
    public void setSound(@NotNull String sound)
    {
        super.setSound(sound);

        if (minecraftKey_constructor == null) return;

        try {
            minecraftKeySound = minecraftKey_constructor.newInstance(getSound());
        } catch (Exception e) {
            PlayMoreSounds.getConsoleLogger().log("Failed to create a MinecraftKey to the sound '" + sound + "'. Please disable 'Send Packets Directly' in config.", ConsoleLogger.Level.WARN);
            PlayMoreSoundsCore.getErrorHandler().report(e, "MinecraftKey instantiation exception:");
        }
    }

    @Override
    public void setSoundType(@NotNull SoundType soundType)
    {
        super.setSoundType(soundType);

        if (minecraftKey_constructor == null) return;

        try {
            minecraftKeySound = minecraftKey_constructor.newInstance(getSound());
        } catch (Exception e) {
            PlayMoreSounds.getConsoleLogger().log("Failed to create a MinecraftKey to the sound '" + getSound() + "'. Please disable 'Send Packets Directly' in config.", ConsoleLogger.Level.WARN);
            PlayMoreSoundsCore.getErrorHandler().report(e, "MinecraftKey instantiation exception:");
        }
    }

    @Override
    public void setCategory(@Nullable SoundCategory category)
    {
        super.setCategory(category);

        if (soundCategory_a_method == null || soundCategory_enumConstants == null) return;

        for (Object soundCategory : soundCategory_enumConstants) {
            try {
                String name = soundCategory_a_method.invoke(soundCategory).toString();

                if (getCategory().name().toLowerCase(Locale.ROOT).equals(name)) {
                    this.soundCategory = soundCategory;
                    break;
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void setDelay(long delay)
    {
        plugin = PlayMoreSounds.getInstance();

        if (delay > 0 && plugin == null) {
            throw new UnsupportedOperationException("PlayMoreSounds must be enabled to play delayed sounds.");
        }

        super.setDelay(delay);
    }

    @Override
    public void play(@Nullable Player player, @NotNull Location sourceLocation)
    {
        PrePlaySoundEvent preEvent = new PrePlaySoundEvent(player, sourceLocation, this);

        Bukkit.getPluginManager().callEvent(preEvent);

        // Validating if the sound should be played for this player.
        if (preEvent.isCancelled() || (player != null && validator.isInvalidPlayer(player, getOptions().getPermissionRequired())))
            return;

        Location soundLocation = SoundManager.addRelativeLocation(preEvent.getLocation(), getOptions().getRelativeLocation());
        Collection<Player> players = SoundManager.getInRange(getOptions().getRadiusSquared(), soundLocation);

        if (player != null && getOptions().getRadiusSquared() == 0) players.add(player);

        if (getDelay() == 0) {
            play(player, players, soundLocation);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, () -> play(player, players, soundLocation), getDelay());
        }
    }

    private void play(@Nullable Player sourcePlayer, @NotNull Collection<Player> players, @NotNull Location soundLocation)
    {
        SoundOptions options = getOptions();

        for (Player inRange : players) {
            if (!blackListedWorlds.contains(inRange.getWorld().getName())
                    && (options.ignoresDisabled() || SoundManager.getSoundsState(inRange))
                    && (options.getPermissionToListen() == null || inRange.hasPermission(options.getPermissionToListen()))
                    && (sourcePlayer == null || inRange.canSee(sourcePlayer))) {
                Location fixedLocation = soundLocation;

                if (options.getRadius() < 0)
                    fixedLocation = SoundManager.addRelativeLocation(inRange.getLocation(), options.getRelativeLocation());

                PlaySoundEvent event = new PlaySoundEvent(this, inRange, fixedLocation, players, sourcePlayer, soundLocation);

                Bukkit.getPluginManager().callEvent(event);

                if (!event.isCancelled())
                    soundPlayer.play(inRange, event.getLocation(), this);
            }
        }
    }

    private interface SoundPlayer
    {
        /**
         * Plays the sound to the player.
         *
         * @param player   The player to play the sound.
         * @param location The location the sound should be played at.
         * @param pmsSound The sound to play.
         */
        void play(@NotNull Player player, @NotNull Location location, @NotNull PlayableSound pmsSound);
    }

    private interface Validator
    {
        /**
         * Validates if the sound should be played to the {@param player}.
         *
         * @param player             The player to validate.
         * @param permissionRequired The permission required to check if the player has it.
         * @return If the sound should be played.
         */
        boolean isInvalidPlayer(@NotNull Player player, @Nullable String permissionRequired);
    }
}