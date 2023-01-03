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
