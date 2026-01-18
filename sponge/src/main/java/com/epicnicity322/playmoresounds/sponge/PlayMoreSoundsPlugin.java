/*
 * PlayMoreSounds - A Minecraft plugin that manages and plays sounds.
 * Copyright (C) 2023-2026 Christiano Rangel
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
import com.epicnicity322.playmoresounds.core.sound.SoundManager;
import com.epicnicity322.playmoresounds.core.util.PlatformUtil;
import com.epicnicity322.playmoresounds.core.util.PlayerUtil;
import com.epicnicity322.playmoresounds.core.util.SoundsToggleState;
import com.epicnicity322.playmoresounds.core.util.TaskFactory;
import com.epicnicity322.playmoresounds.sponge.listener.ChangeHeldItemListener;
import com.epicnicity322.playmoresounds.sponge.util.SpongePlatformUtil;
import com.epicnicity322.playmoresounds.sponge.util.SpongePlayerUtil;
import com.epicnicity322.playmoresounds.sponge.util.SpongeTaskFactory;
import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.bstats.sponge.Metrics;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

@Plugin("playmoresounds")
public final class PlayMoreSoundsPlugin {
    private final @NotNull Logger logger;
    private final @NotNull PlatformUtil platformUtil;
    private final @NotNull PlayerUtil<Player, Living> playerUtil;
    private final @NotNull TaskFactory<Living> taskFactory;
    private final @NotNull SoundsToggleState<Player> toggleStates;
    private final @NotNull SoundManager<Player, Living> soundManager;

    @Inject
    public PlayMoreSoundsPlugin(@NotNull PluginContainer plugin, @NotNull Logger logger, @NotNull Game game, @NotNull Metrics.Factory metricsFactory) {
        this.logger = logger;
        platformUtil = new SpongePlatformUtil(plugin, game);
        playerUtil = new SpongePlayerUtil(plugin, game);
        taskFactory = new SpongeTaskFactory(plugin, game);
        toggleStates = new SoundsToggleState<>(playerUtil);
        soundManager = new SoundManager<>(playerUtil, toggleStates, taskFactory);
        metricsFactory.make(8393);
    }

    @Listener
    public void onServerStarting(final StartingEngineEvent<Server> event) {
        Configurations.manager().loadConfigurations();
        new ChangeHeldItemListener(platformUtil, soundManager).register();
        logger.info("Loaded change held item listener");
    }

    @Listener
    public void onRefreshGame(final RefreshGameEvent event) {
        logger.info("refresh.");
    }
}
