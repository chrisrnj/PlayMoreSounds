/*
 * PlayMoreSounds - A bukkit plugin that manages and plays sounds.
 * Copyright (C) 2022 Christiano Rangel
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

package com.epicnicity322.playmoresounds.sponge.sound;

import com.epicnicity322.epicpluginlib.core.config.ConfigurationHolder;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.sound.Sound;
import com.epicnicity322.playmoresounds.core.sound.SoundCategory;
import com.epicnicity322.playmoresounds.core.sound.SoundOptions;
import com.epicnicity322.playmoresounds.sponge.PlayMoreSounds;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import com.flowpowered.math.vector.Vector3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;

public class PlayableSound extends Sound implements Playable
{
    private static final @NotNull ConfigurationHolder config = Configurations.CONFIG.getConfigurationHolder();

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
    public void play(@Nullable Player player, @NotNull Location<World> sourceLocation)
    {
        //PrePlaySoundEvent preEvent = new PrePlaySoundEvent(player, sourceLocation, this);
        //Bukkit.getPluginManager().callEvent(preEvent);

        if (//!preEvent.isCancelled() &&
                getOptions().getPermissionRequired() == null || (player == null || player.hasPermission(getOptions().getPermissionRequired()))) {
            Vector3d soundLocation = SoundManager.addRelativeLocation(sourceLocation.getPosition(), player == null ? null : player.getRotation(), getOptions().getRelativeLocation());
            Collection<Player> players = SoundManager.getInRange(getOptions().getRadius(), sourceLocation);

            if (player != null)
                players.add(player);

            PlayableSound instance = this;

            if (getDelay() == 0)
                play(player, players, soundLocation, instance);
            else
                Task.builder().delayTicks(getDelay()).execute(() -> play(player, players, soundLocation, instance)).submit(plugin);
        }
    }

    private void play(@Nullable Player sourcePlayer, @NotNull Collection<Player> players, @NotNull Vector3d soundLocation,
                      @NotNull PlayableSound instance)
    {
        for (Player inRange : players) {
            if (!config.getConfiguration().getCollection("World Black List").contains(inRange.getWorld().getName())
                    //&& (getOptions().ignoresDisabled() || SoundManager.getSoundsState(inRange))
                    && (getOptions().getPermissionToListen() == null || inRange.hasPermission(getOptions().getPermissionToListen()))
                    && (sourcePlayer == null || inRange.canSee(sourcePlayer))) {
                Vector3d fixedLocation = soundLocation;

                if (getOptions().getRadius() < 0)
                    fixedLocation = SoundManager.addRelativeLocation(inRange.getPosition(), inRange.getRotation(), getOptions().getRelativeLocation());

//                PlaySoundEvent event = new PlaySoundEvent(instance, inRange, fixedLocation, players, sourcePlayer, soundLocation);
//
//                Bukkit.getPluginManager().callEvent(event);
//
//                if (!event.isCancelled())
                inRange.playSound(SoundType.of(getSound()), fixedLocation, getVolume(), getPitch());
            }
        }
    }
}
