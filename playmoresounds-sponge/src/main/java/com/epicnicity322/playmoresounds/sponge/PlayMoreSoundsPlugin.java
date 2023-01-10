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

package com.epicnicity322.playmoresounds.sponge;

import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.sponge.listeners.JoinServerListener;
import com.epicnicity322.playmoresounds.sponge.sound.SoundManager;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Server;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

@Plugin("playmoresounds")
public final class PlayMoreSoundsPlugin {
    private static PlayMoreSoundsPlugin instance;
    @NotNull
    private final PluginContainer plugin;
    @NotNull
    private final SoundManager soundManager;

    @Inject
    public PlayMoreSoundsPlugin(@NotNull PluginContainer plugin) {
        instance = this;
        this.plugin = plugin;
        soundManager = new SoundManager(plugin);
    }

    @NotNull
    public static SoundManager soundManager() {
        return instance.soundManager;
    }

    @Listener
    public void onServerStarting(final StartingEngineEvent<Server> event) {
        System.out.println("Hello World!");
        Configurations.loader().loadConfigurations();
        new JoinServerListener(plugin).register();
    }

    @Listener
    public void onRefreshGame(final RefreshGameEvent event) {
        System.out.println("Refresh.");
    }
}
