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

import com.epicnicity322.playmoresounds.core.sound.RichSound;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import net.kyori.adventure.util.Ticks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.server.ServerLocation;

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
    public void play(@Nullable ServerPlayer player, @NotNull ServerLocation sourceLocation)
    {
        if (isEnabled() && !getChildSounds().isEmpty()) {
            for (PlayableSound s : getChildSounds()) s.play(player, sourceLocation);
        }
    }

    /**
     * Plays the sound repeatedly after the time set on period.
     * The {@link ScheduledTask} will be cancelled if this sound is disabled or has no child sounds.
     *
     * @param player         The player to play the sound.
     * @param sourceLocation The location where the sound will play.
     * @param delay          The time in ticks to wait before playing the first sound.
     * @param period         The time in ticks to wait before playing the sound again.
     * @param breaker        A boolean that will run in the loop, if the boolean is true the loop will be cancelled.
     * @return The {@link ScheduledTask} of the loop that can be used to cancel later.
     * @throws IllegalStateException If the {@link org.spongepowered.api.Server} engine is not available.
     */
    public @NotNull ScheduledTask playInLoop(@Nullable ServerPlayer player, @NotNull Supplier<ServerLocation> sourceLocation, long delay, long period, @Nullable Supplier<Boolean> breaker)
    {
        if (!Sponge.isServerAvailable()) throw new IllegalStateException("Server is not available.");

        return Sponge.server().scheduler().submit(
                Task.builder()
                        .execute(task -> {
                            if (!isEnabled() || getChildSounds().isEmpty() || (breaker != null && breaker.get())) {
                                task.cancel();
                                return;
                            }

                            play(player, sourceLocation.get());
                        })
                        .interval(Ticks.duration(period))
                        .delay(Ticks.duration(delay))
                        .build());
    }
}
