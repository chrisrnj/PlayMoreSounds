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
import com.epicnicity322.playmoresounds.bukkit.sound.events.PlayRichSoundEvent;
import com.epicnicity322.playmoresounds.core.sound.RichSound;
import com.epicnicity322.playmoresounds.core.sound.SoundOptions;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Supplier;

public class PlayableRichSound extends RichSound<PlayableSound> implements Playable
{
    public PlayableRichSound(@NotNull String name, boolean enabled, boolean cancellable, @Nullable Collection<PlayableSound> childSounds)
    {
        super(name, enabled, cancellable, childSounds);
    }

    public PlayableRichSound(@NotNull ConfigurationSection section)
    {
        super(section);
    }

    @Override
    protected @NotNull PlayableSound newCoreSound(@NotNull ConfigurationSection section)
    {
        return new PlayableSound(section);
    }

    @Override
    public void play(@Nullable Player player, @NotNull Location sourceLocation)
    {
        if (isEnabled() && !getChildSounds().isEmpty()) {
            PlayRichSoundEvent event = new PlayRichSoundEvent(player, sourceLocation, this);

            Bukkit.getPluginManager().callEvent(event);

            if (!event.isCancelled())
                for (PlayableSound s : getChildSounds())
                    s.play(player, event.getLocation());
        }
    }

    /**
     * Plays the sound repeatedly after the time set on period.
     * The {@link BukkitRunnable} will not run if this sound is disabled or has no child sounds.
     * {@link PlayRichSoundEvent} will be called for every time the sound is played by this loop.
     *
     * @param player         The player to play the sound.
     * @param sourceLocation The location where the sound will play. May change depending on {@link SoundOptions#getRelativeLocation()}.
     * @param delay          The time in ticks to wait before playing the first sound.
     * @param period         The time in ticks to wait before playing the sound again.
     * @param breaker        A boolean that will run in the loop, if the boolean is true the loop will be cancelled.
     * @return The {@link BukkitRunnable} of the loop that can be used to cancel later.
     * @throws IllegalStateException If PlayMoreSounds was not instantiated by bukkit yet.
     */
    public @NotNull BukkitRunnable playInLoop(@Nullable Player player, @NotNull Supplier<Location> sourceLocation, long delay, long period, @Nullable Supplier<Boolean> breaker)
    {
        PlayMoreSounds main = PlayMoreSounds.getInstance();

        if (main == null)
            throw new IllegalStateException("PlayMoreSounds is not loaded.");

        BukkitRunnable runnable;

        // The runnable will not check if the breaker is null on each loop.
        if (breaker == null)
            runnable = new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    play(player, sourceLocation.get());
                }
            };
        else
            runnable = new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if (breaker.get()) {
                        cancel();
                        return;
                    }

                    play(player, sourceLocation.get());
                }
            };

        if (isEnabled() && !getChildSounds().isEmpty())
            runnable.runTaskTimer(main, delay, period);

        return runnable;
    }
}
