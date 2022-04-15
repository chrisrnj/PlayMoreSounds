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

public class PlayableSound extends Sound implements Delayable
{
    public PlayableSound(@Nullable String id, @NotNull String sound, @Nullable SoundCategory category, float volume, float pitch, long delay, @Nullable SoundOptions options)
    {
        super(id, sound, category, volume, pitch, delay, options);

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
    public @NotNull PlayResult playDelayable(@Nullable Player player, @NotNull Location sourceLocation)
    {
        SoundOptions options = getOptions();
        final Collection<Player> listeners;

        if (player != null) {
            String permission = options.getPermissionRequired();

            if (permission != null && !player.hasPermission(permission)) {
                return new PlayResult(Collections.emptySet(), null);
            }

            // Sound should only be played to the source player if radius is 0, the game mode is spectator, or if they are valid to be in invisibility mode.
            if (options.getRadius() == 0.0 || player.getGameMode() == GameMode.SPECTATOR || (player.hasPotionEffect(PotionEffectType.INVISIBILITY) && player.hasPermission("playmoresounds.bypass.invisibility"))) {
                listeners = Collections.singleton(player);
            } else {
                listeners = SoundManager.getInRange(options.getRadius(), sourceLocation);
            }
        } else {
            listeners = SoundManager.getInRange(options.getRadius(), sourceLocation);
        }

        if (getDelay() == 0) {
            play(player, listeners, sourceLocation);
            return new PlayResult(listeners, null);
        } else {
            return new PlayResult(listeners, Bukkit.getScheduler().runTaskLater(PlayMoreSounds.getInstance(), () -> play(player, listeners, sourceLocation), getDelay()));
        }
    }

    private void play(@Nullable Player sourcePlayer, @NotNull Collection<Player> listeners, @NotNull Location soundLocation)
    {
        // Calling PlaySoundEvent.
        var event = new PlaySoundEvent(this, sourcePlayer, soundLocation, listeners, getOptions().getRadius() == -1.0 || getOptions().getRadius() == -2.0);

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        // Playing the sound to the valid listeners.
        for (Player listener : listeners) {
            if (!event.validateListener(listener)) continue;

            if (event.playingGlobally()) {
                listener.playSound(listener.getLocation(), getSound(), getCategory().asBukkit(), getVolume(), getPitch());
            } else {
                listener.playSound(event.location, getSound(), getCategory().asBukkit(), getVolume(), getPitch());
            }
        }
    }
}