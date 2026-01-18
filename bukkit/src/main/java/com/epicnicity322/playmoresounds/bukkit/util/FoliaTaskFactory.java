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

package com.epicnicity322.playmoresounds.bukkit.util;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSoundsPlugin;
import com.epicnicity322.playmoresounds.core.location.WorldPos;
import com.epicnicity322.playmoresounds.core.util.TaskFactory;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public final class FoliaTaskFactory implements TaskFactory<Entity> {
    private final @NotNull PlayMoreSoundsPlugin plugin;

    public FoliaTaskFactory(@NotNull PlayMoreSoundsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull Task delayedGlobal(@NotNull Runnable runnable, long delay) {
        ScheduledTask task = plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, t -> runnable.run(), delay);
        return task::cancel;
    }

    @Override
    public @NotNull Task delayedLocal(@NotNull WorldPos worldPos, @NotNull Runnable runnable, long delay) {
        World world = plugin.getServer().getWorld(worldPos.world());
        if (world == null) return () -> {
        };
        ScheduledTask task = plugin.getServer().getRegionScheduler().runDelayed(plugin, world, worldPos.pos().chunkX(), worldPos.pos().chunkZ(), t -> runnable.run(), delay);
        return task::cancel;
    }

    @Override
    public @NotNull Task delayedFor(@NonNull Entity entity, @NotNull Runnable runnable, @Nullable Runnable retired, long delay) {
        ScheduledTask task = entity.getScheduler().runDelayed(plugin, t -> runnable.run(), retired, delay);
        if (task == null) return () -> {
        };
        return task::cancel;
    }
}
