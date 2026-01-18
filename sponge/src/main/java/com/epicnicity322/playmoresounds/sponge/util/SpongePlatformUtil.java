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

import com.epicnicity322.playmoresounds.core.listener.PMSListener;
import com.epicnicity322.playmoresounds.core.util.PlatformUtil;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Game;
import org.spongepowered.plugin.PluginContainer;

import java.lang.invoke.MethodHandles;

public final class SpongePlatformUtil implements PlatformUtil {
    private final @NotNull PluginContainer plugin;
    private final @NotNull Game game;

    public SpongePlatformUtil(@NotNull PluginContainer plugin, @NotNull Game game) {
        this.plugin = plugin;
        this.game = game;
    }

    @Override
    public <L extends PMSListener> void registerEvents(L listener) {
        game.eventManager().registerListeners(plugin, listener, MethodHandles.publicLookup());
    }

    @Override
    public <L extends PMSListener> void unregisterEvents(L listener) {
        game.eventManager().unregisterListeners(listener);
    }
}
