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

import com.epicnicity322.playmoresounds.core.sound.Sound;
import com.epicnicity322.playmoresounds.core.sound.SoundCategory;
import com.epicnicity322.playmoresounds.core.sound.SoundOptions;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.Ticks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.server.ServerLocation;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;

public class PlayableSound extends Sound implements Delayable
{
    private @NotNull net.kyori.adventure.sound.Sound kyoriSound;
    private @NotNull Duration delay;

    public PlayableSound(@Nullable String id, @NotNull String sound, @Nullable SoundCategory category, float volume, float pitch, long delay, @Nullable SoundOptions options)
    {
        super(id, sound, category, volume, pitch, delay, options);
        this.delay = Ticks.duration(getDelay());
        this.kyoriSound = net.kyori.adventure.sound.Sound.sound(Key.key(getSound()), category.asSponge(), volume, pitch);
    }

    public PlayableSound(@NotNull ConfigurationSection section)
    {
        super(section);
        this.delay = Ticks.duration(getDelay());
        this.kyoriSound = net.kyori.adventure.sound.Sound.sound(Key.key(getSound()), getCategory().asSponge(), getVolume(), getPitch());
    }

    @Override
    public void setDelay(long delay)
    {
        super.setDelay(delay);
        this.delay = Ticks.duration(delay);
    }

    @Override
    public void setSound(@NotNull String sound)
    {
        super.setSound(sound);
        updateSound();
    }

    @Override
    public void setCategory(@Nullable SoundCategory category)
    {
        super.setCategory(category);
        updateSound();
    }

    @Override
    public void setVolume(float volume)
    {
        super.setVolume(volume);
        updateSound();
    }

    @Override
    public void setPitch(float pitch)
    {
        super.setPitch(pitch);
        updateSound();
    }

    private void updateSound()
    {
        this.kyoriSound = net.kyori.adventure.sound.Sound.sound(Key.key(getSound()), getCategory().asSponge(), getVolume(), getPitch());
    }

    @Override
    public @NotNull ChildPlayResult playDelayable(@Nullable ServerPlayer player, @NotNull ServerLocation location)
    {
        SoundOptions options = getOptions();
        final Collection<ServerPlayer> listeners;

        if (player != null) {
            String permission = options.getPermissionRequired();

            if (permission != null && !player.hasPermission(permission)) {
                return new ChildPlayResult(Collections.emptySet(), null);
            }

            // Sound should only be played to the source player if radius is 0, the game mode is spectator, or if they are valid to be in invisibility mode.
            if (options.getRadius() == 0.0 || player.gameMode().get().equals(GameModes.SPECTATOR.get()) || ignoreInvisible(player)) {
                listeners = Collections.singleton(player);
            } else {
                listeners = SoundManager.getInRange(options.getRadius(), location);
            }
        } else {
            listeners = SoundManager.getInRange(options.getRadius(), location);
        }

        if (getDelay() == 0) {
            play(player, listeners, location);
            return new ChildPlayResult(listeners, null);
        } else {
            return new ChildPlayResult(listeners,
                    Sponge.server().scheduler().submit(
                            Task.builder()
                                    .delay(delay)
                                    .execute(() -> play(player, listeners, location))
                                    .build()));
        }
    }

    private void play(@Nullable ServerPlayer sourcePlayer, @NotNull Collection<ServerPlayer> listeners, @NotNull ServerLocation soundLocation)
    {
        SoundOptions options = getOptions();
        boolean global = options.getRadius() == -1.0 || options.getRadius() == -2.0;

        for (ServerPlayer listener : listeners) {
            // Validating if listener is allowed to hear this sound.
            if ((options.ignoresDisabled() || SoundManager.getSoundsState(listener))
                    && (options.getPermissionToListen() == null || listener.hasPermission(options.getPermissionToListen()))
                    && (sourcePlayer == null || listener.canSee(sourcePlayer))) {
                final ServerLocation finalLocation;

                if (global) {
                    finalLocation = listener.serverLocation();
                } else {
                    finalLocation = soundLocation;
                }

                listener.playSound(kyoriSound, finalLocation.x(), finalLocation.y(), finalLocation.z());
            }
        }
    }

    private boolean ignoreInvisible(@NotNull ServerPlayer player)
    {
        if (!player.hasPermission("playmoresounds.bypass.invisibility")) return false;

        for (PotionEffect potionEffect : player.potionEffects()) {
            if (potionEffect.type().equals(PotionEffectTypes.INVISIBILITY.get())) {
                return true;
            }
        }
        return false;
    }
}
