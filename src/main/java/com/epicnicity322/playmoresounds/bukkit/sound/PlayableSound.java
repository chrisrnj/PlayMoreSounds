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
import com.epicnicity322.playmoresounds.core.sound.Sound;
import com.epicnicity322.playmoresounds.core.sound.SoundOptions;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class PlayableSound extends Sound implements Playable
{
    private static final @NotNull ConfigurationHolder config = Configurations.CONFIG.getConfigurationHolder();
    private static final @NotNull BukkitScheduler scheduler = Bukkit.getScheduler();

    private PlayMoreSounds plugin = PlayMoreSounds.getInstance();

    public PlayableSound(@NotNull String sound, float volume, float pitch, long delay, @Nullable SoundOptions options)
    {
        super(sound, volume, pitch, delay, options);

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

        if (!preEvent.isCancelled() &&
                (getOptions().getPermissionRequired() == null || (player == null || player.hasPermission(getOptions().getPermissionRequired()))) &&
                (player == null || (!player.hasPotionEffect(PotionEffectType.INVISIBILITY) || !player.hasPermission("playmoresounds.bypass.invisibility")))) {
            Location soundLocation = SoundManager.addRelativeLocation(preEvent.getLocation(), getOptions().getRelativeLocation());
            Collection<Player> players = SoundManager.getInRange(getOptions().getRadius(), preEvent.getLocation());

            if (player != null)
                players.add(player);

            PlayableSound instance = this;

            if (getDelay() == 0)
                play(player, players, soundLocation, instance);
            else
                scheduler.runTaskLater(plugin, () -> play(player, players, soundLocation, instance), getDelay());
        }
    }

    private void play(@Nullable Player sourcePlayer, @NotNull Collection<Player> players, @NotNull Location soundLocation,
                      @NotNull PlayableSound instance)
    {
        for (Player inRange : players) {
            if (!config.getConfiguration().getCollection("World Black List").contains(inRange.getWorld().getName())
                    && (getOptions().ignoresDisabled() || SoundManager.getSoundsState(inRange))
                    && (getOptions().getPermissionToListen() == null || inRange.hasPermission(getOptions().getPermissionToListen()))
                    && (sourcePlayer == null || inRange.canSee(sourcePlayer))) {
                Location fixedLocation = soundLocation;

                if (getOptions().getRadius() < 0)
                    fixedLocation = SoundManager.addRelativeLocation(inRange.getLocation(), getOptions().getRelativeLocation());

                PlaySoundEvent event = new PlaySoundEvent(instance, inRange, fixedLocation, players, sourcePlayer, soundLocation);

                Bukkit.getPluginManager().callEvent(event);

                if (!event.isCancelled())
                    inRange.playSound(event.getLocation(), getSound(), getVolume(), getPitch());
            }
        }
    }
}
