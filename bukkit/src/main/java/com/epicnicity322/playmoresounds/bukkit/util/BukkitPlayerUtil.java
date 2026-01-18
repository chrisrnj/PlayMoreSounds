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
import com.epicnicity322.playmoresounds.core.location.Pos;
import com.epicnicity322.playmoresounds.core.location.WorldPos;
import com.epicnicity322.playmoresounds.core.util.PlayerUtil;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;

public final class BukkitPlayerUtil implements PlayerUtil<Player, Entity> {
    private final @NotNull PlayMoreSoundsPlugin plugin;
    private final @NotNull NamespacedKey stateKey;

    public BukkitPlayerUtil(@NotNull PlayMoreSoundsPlugin plugin) {
        this.plugin = plugin;
        this.stateKey = new NamespacedKey(plugin, "sound_state");
    }

    @Override
    public @NotNull Collection<? extends Player> worldPlayers(@NotNull String world) {
        World w = plugin.getServer().getWorld(world);
        if (w == null) return Collections.emptyList();
        return w.getPlayers();
    }

    @Override
    public @NotNull Collection<? extends Player> onlinePlayers() {
        return plugin.getServer().getOnlinePlayers();
    }

    @Override
    public @NotNull Collection<? extends Player> playersInRange(double radius, @NotNull WorldPos pos) {
        if (radius <= 0) return Collections.emptyList();
        World w = plugin.getServer().getWorld(pos.world());
        if (w == null) return Collections.emptyList();

        List<Player> inRange = new ArrayList<>();
        double radiusSquared = radius * radius;

        for (Player player : w.getPlayers()) {
            if (pos.pos().distanceSquared(player.getX(), player.getY(), player.getZ()) <= radiusSquared) {
                inRange.add(player);
            }
        }

        return inRange;
    }

    @Override
    public boolean hasPermission(@NonNull Player player, @NotNull String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public boolean isSpectator(@NonNull Player player) {
        return player.getGameMode() == GameMode.SPECTATOR;
    }

    @Override
    public boolean hasInvisibility(@NonNull Player player) {
        return player.isInvisible();
    }

    @Override
    public boolean canSee(@NonNull Player player1, @NonNull Player player2) {
        return player1.canSee(player2);
    }

    @Override
    public @NotNull WorldPos wPos(@NonNull Entity entity) {
        return new WorldPos(entity.getWorld().getName(), new Pos(entity.getX(), entity.getY(), entity.getZ()));
    }

    @Override
    public @NotNull Pos pos(@NonNull Player player) {
        return new Pos(player.getX(), player.getY(), player.getZ());
    }

    @Override
    public float yaw(@NonNull Entity entity) {
        return entity.getYaw();
    }

    @Override
    public @NotNull UUID uuid(@NonNull Player player) {
        return player.getUniqueId();
    }

    @Override
    public @NonNull Entity asEntity(@NonNull Player player) {
        return player;
    }

    @Override
    public void saveStateOnPersistentData(@NonNull Player dataHolder, boolean value) {
        dataHolder.getPersistentDataContainer().set(stateKey, PersistentDataType.BOOLEAN, value);
    }

    @Override
    public @Nullable Boolean getStateOnPersistentData(@NonNull Player dataHolder) {
        return dataHolder.getPersistentDataContainer().get(stateKey, PersistentDataType.BOOLEAN);
    }
}
