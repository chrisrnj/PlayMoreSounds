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

package com.epicnicity322.playmoresounds.bukkit.sound;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.events.PlaySoundEvent;
import com.epicnicity322.playmoresounds.core.sound.Sound;
import com.epicnicity322.playmoresounds.core.sound.SoundCategory;
import com.epicnicity322.playmoresounds.core.sound.SoundOptions;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class PlayableSound extends Sound implements Playable
{
    public PlayableSound(@NotNull String sound, @Nullable SoundCategory category, float volume, float pitch, long delay, @Nullable SoundOptions options)
    {
        super(sound, category, volume, pitch, delay, options);

        if (delay > 0 && PlayMoreSounds.getInstance() == null) {
            throw new UnsupportedOperationException("PlayMoreSounds must be enabled to play delayed sounds.");
        }
    }

    public PlayableSound(@NotNull ConfigurationSection section)
    {
        super(section);

        if (getDelay() > 0 && PlayMoreSounds.getInstance() == null) {
            throw new UnsupportedOperationException("PlayMoreSounds must be enabled to play delayed sounds.");
        }
    }

    @Override
    public void setDelay(long delay)
    {
        if (delay > 0 && PlayMoreSounds.getInstance() == null) {
            throw new UnsupportedOperationException("PlayMoreSounds must be enabled to play delayed sounds.");
        }

        super.setDelay(delay);
    }

    @Override
    public void play(@Nullable Player player, @NotNull Location location)
    {
        var options = getOptions();
        double radius = options.getRadius();

        if (player != null) {
            var permission = options.getPermissionRequired();

            if (permission != null && !player.hasPermission(permission)) {
                return;
            }
            if (player.getGameMode() == GameMode.SPECTATOR || (player.hasPotionEffect(PotionEffectType.INVISIBILITY) && player.hasPermission("playmoresounds.bypass.invisibility"))) {
                radius = 0;
            }
        }

        Collection<Player> listeners;

        if (player != null && radius == 0) listeners = Collections.singleton(player);
        else listeners = SoundManager.getInRange(radius, location);

        if (getDelay() == 0) {
            play(player, listeners, location);
        } else {
            final Location finalLocation = location;
            Bukkit.getScheduler().runTaskLater(PlayMoreSounds.getInstance(), () -> play(player, listeners, finalLocation), getDelay());
        }
    }

    private void play(@Nullable Player sourcePlayer, @NotNull Collection<Player> listeners, @NotNull Location soundLocation)
    {
        var options = getOptions();

        for (Player listener : listeners) {
            // Validating if listener is allowed to hear this sound.
            if ((options.ignoresDisabled() || SoundManager.getSoundsState(listener))
                    && (options.getPermissionToListen() == null || listener.hasPermission(options.getPermissionToListen()))
                    && (sourcePlayer == null || listener.canSee(sourcePlayer))) {
                Location finalLocation = soundLocation;

                // Radius lower than 0 is global and must be set to the listener's location.
                if (options.getRadius() < 0)
                    finalLocation = listener.getLocation();

                var event = new PlaySoundEvent(this, listener, finalLocation, listeners, sourcePlayer, soundLocation);

                Bukkit.getPluginManager().callEvent(event);

                if (!event.isCancelled())
                    listener.playSound(event.getLocation(), getSound(), getCategory().asBukkit(), getVolume(), getPitch());
            }
        }
    }
}