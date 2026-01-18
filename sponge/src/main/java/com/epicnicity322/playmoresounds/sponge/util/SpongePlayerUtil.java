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

import com.epicnicity322.playmoresounds.core.location.Pos;
import com.epicnicity322.playmoresounds.core.location.WorldPos;
import com.epicnicity322.playmoresounds.core.util.PlayerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.spongepowered.api.Game;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.plugin.PluginContainer;

import java.util.*;

public final class SpongePlayerUtil implements PlayerUtil<Player, Living> {
    private final @NotNull Game game;
    private final @NotNull Key<Value<Boolean>> stateKey;

    public SpongePlayerUtil(@NotNull PluginContainer plugin, @NotNull Game game) {
        stateKey = Key.from(plugin, "sound_state", Boolean.class);
        this.game = game;
    }

    @Override
    public @NotNull Collection<? extends Player> worldPlayers(@NotNull String world) {
        if (!game.isServerAvailable()) return Collections.emptyList();
        Optional<ServerWorld> w = game.server().worldManager().world(ResourceKey.resolve(world));
        if (w.isEmpty()) return Collections.emptyList();
        return w.get().players();
    }

    @Override
    public @NotNull Collection<? extends Player> onlinePlayers() {
        if (!game.isServerAvailable()) return Collections.emptyList();
        return game.server().onlinePlayers();
    }

    @Override
    public @NotNull Collection<? extends Player> playersInRange(double radius, @NotNull WorldPos pos) {
        if (radius <= 0) return Collections.emptyList();
        double radiusSquared = radius * radius;
        List<Player> inRange = new ArrayList<>();

        for (Player player : worldPlayers(pos.world())) {
            Location<?, ?> loc = player.location();
            if (pos.pos().distanceSquared(loc.x(), loc.y(), loc.z()) <= radiusSquared) {
                inRange.add(player);
            }
        }

        return inRange;
    }

    @Override
    public boolean hasPermission(@NonNull Player player, @NotNull String permission) {
        return ((ServerPlayer) player).hasPermission(permission);
    }

    @Override
    public boolean isSpectator(@NonNull Player player) {
        return ((ServerPlayer) player).gameMode().get().equals(GameModes.SPECTATOR.get());
    }

    @Override
    public boolean hasInvisibility(@NonNull Player player) {
        return player.potionEffects().get().stream().anyMatch(potionEffect -> potionEffect.type().equals(PotionEffectTypes.INVISIBILITY.get()));
    }

    @Override
    public boolean canSee(@NonNull Player player1, @NonNull Player player2) {
        return player1.canSee(player2);
    }

    @Override
    public @NotNull WorldPos wPos(@NonNull Living entity) {
        Location<?, ?> loc = entity.location();
        return new WorldPos(entity.world().toString(), new Pos(loc.x(), loc.y(), loc.z()));
    }

    @Override
    public @NotNull Pos pos(@NonNull Player player) {
        Location<?, ?> loc = player.location();
        return new Pos(loc.x(), loc.y(), loc.z());
    }

    @Override
    public float yaw(@NonNull Living entity) {
        return (float) entity.headRotation().get().y();
    }

    @Override
    public @NotNull UUID uuid(@NonNull Player player) {
        return player.uniqueId();
    }

    @Override
    public @NonNull Living asEntity(@NonNull Player player) {
        return player;
    }

    @Override
    public void saveStateOnPersistentData(@NonNull Player dataHolder, boolean value) {
        dataHolder.offer(stateKey, value);
    }

    @Override
    public @Nullable Boolean getStateOnPersistentData(@NonNull Player dataHolder) {
        return dataHolder.get(stateKey).orElse(null);
    }
}
