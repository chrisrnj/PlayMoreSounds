/*
 * PlayMoreSounds - A Minecraft plugin that manages and plays sounds.
 * Copyright (C) 2026 Christiano Rangel
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

package com.epicnicity322.playmoresounds.sponge.util;

import com.epicnicity322.playmoresounds.core.location.WorldPos;
import com.epicnicity322.playmoresounds.core.util.TaskFactory;
import net.kyori.adventure.util.Ticks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.spongepowered.api.Game;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.plugin.PluginContainer;

public final class SpongeTaskFactory implements TaskFactory<Living> {
    private final @NotNull PluginContainer plugin;
    private final @NotNull Game game;

    public SpongeTaskFactory(@NotNull PluginContainer plugin, @NotNull Game game) {
        this.plugin = plugin;
        this.game = game;
    }

    @Override
    public @NotNull Task delayedGlobal(@NotNull Runnable runnable, long delay) {
        Scheduler scheduler = game.isServerAvailable() ? game.server().scheduler() : game.isClientAvailable() ? game.client().scheduler() : game.asyncScheduler();
        ScheduledTask task = scheduler.submit(org.spongepowered.api.scheduler.Task.builder().delay(Ticks.duration(delay)).plugin(plugin).execute(runnable).build());

        return task::cancel;
    }

    @Override
    public @NotNull Task delayedLocal(@NotNull WorldPos worldPos, @NotNull Runnable runnable, long delay) {
        if (!game.isServerAvailable()) return () -> {
        };

        ServerWorld w = game.server().worldManager().world(ResourceKey.resolve(worldPos.world())).orElse(null);
        if (w == null) return () -> {
        };

        ScheduledTask task = w.scheduler().submit(org.spongepowered.api.scheduler.Task.builder().delay(Ticks.duration(delay)).plugin(plugin).execute(runnable).build());
        return task::cancel;
    }

    @Override
    public @NotNull Task delayedFor(@NonNull Living entity, @NotNull Runnable runnable, @Nullable Runnable retired, long delay) {
        World<?, ?> w = entity.world();
        ScheduledTask task = w.scheduler().submit(org.spongepowered.api.scheduler.Task.builder().delay(Ticks.duration(delay)).plugin(plugin).execute(runnable).build());

        return task::cancel;
    }
}
