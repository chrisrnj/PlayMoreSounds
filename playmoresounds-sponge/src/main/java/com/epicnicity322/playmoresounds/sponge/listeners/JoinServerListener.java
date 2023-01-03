/*
 * PlayMoreSounds - A minecraft plugin that manages and plays sounds.
 * Copyright (C) 2023 Christiano Rangel
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

package com.epicnicity322.playmoresounds.sponge.listeners;

import com.epicnicity322.playmoresounds.sponge.PlayMoreSoundsPlugin;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.plugin.PluginContainer;

public final class JoinServerListener extends PMSListener {
    public JoinServerListener(@NotNull PluginContainer plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String name() {
        return "Join Server";
    }

    @Listener
    public void onJoin(ServerSideConnectionEvent.Join event) {
        PlayMoreSoundsPlugin.soundManager().play(sound, event.player());
    }
}
