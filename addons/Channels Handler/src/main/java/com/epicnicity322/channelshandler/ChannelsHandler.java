/*
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

package com.epicnicity322.channelshandler;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A class that other addons can hook to, so they can make use of channels.yml configuration.
 */
@ThreadSafe
public class ChannelsHandler
{
    private final @NotNull String pluginName;
    private final @NotNull Listener listener;
    private final @NotNull AtomicBoolean listenerRegistered = new AtomicBoolean(false);

    /**
     * Creates a channels handler that you can use to play sounds when a player says something in a specific channel.
     * <p>
     * Make sure to call the method {@link #onChat(Player, String, String)} when chat event for the plugin you are adding
     * compatibility to is called. You don't need to call the method using bukkit runnable on main thread (in case the
     * event is async), because this class is thread safe.
     * <p>
     * {@link #reloadListener()} is automatically called when you instance this, allowing the listener to be registered
     * if any sound for this plugin is enabled in channels.yml. A runnable that calls {@link #reloadListener()} is added
     * for {@link PlayMoreSounds#onReload(Runnable)} when you instance this as well.
     *
     * @param pluginName The name of the channel based chat plugin you are trying to add compatibility to.
     * @param listener   The listener that should be registered when sounds for this plugin are enabled.
     */
    public ChannelsHandler(@NotNull String pluginName, @NotNull Listener listener)
    {
        this.pluginName = pluginName;
        this.listener = listener;

        reloadListener();
        PlayMoreSounds.onReload(this::reloadListener);
    }

    /**
     * Checks if any sound for the plugin is enabled in channels.yml configuration, if so, the specified listener is
     * registered. If no sound is enabled then the listener is unregistered.
     */
    public void reloadListener()
    {
        Configuration channels = ChannelsHandlerAddon.CHANNELS_CONFIG.getConfiguration();
        Configuration sounds = Configurations.SOUNDS.getConfigurationHolder().getConfiguration();
        boolean shouldRegister = sounds.getBoolean("Player Chat.Hook Mode").orElse(false) && sounds.getBoolean("Player Chat.Enabled").orElse(false);

        if (!shouldRegister) {
            ConfigurationSection section = channels.getConfigurationSection(pluginName);

            if (section != null) {
                for (Map.Entry<String, Object> node : section.getNodes().entrySet()) {
                    if (!(node.getValue() instanceof ConfigurationSection)) continue;
                    if (((ConfigurationSection) node.getValue()).getBoolean("Enabled").orElse(false)) {
                        shouldRegister = true;
                        break;
                    }
                }
            }
        }

        // Checking if listener is currently not registered and should register.
        if (shouldRegister) {
            if (!listenerRegistered.getAndSet(true)) {
                Bukkit.getPluginManager().registerEvents(listener, PlayMoreSounds.getInstance());
            }
        } else {
            if (listenerRegistered.getAndSet(false)) {
                HandlerList.unregisterAll(listener);
            }
        }
    }

    /**
     * Call this method on the event of the plugin you're trying to add compatibility to.
     * <p>
     * In case this plugin uses an async chat event, you don't need to call this on bukkit's main thread using
     * {@link org.bukkit.scheduler.BukkitScheduler#runTask(Plugin, Runnable)}, this is automatically handled by this method.
     *
     * @param chatter The player who talked in chat.
     * @param channel The name of the channel the player send the message on.
     * @param message The message the player sent, used for checking if a sound should be played in 'chat words.yml'
     */
    public void onChat(@NotNull Player chatter, @NotNull String channel, @NotNull String message)
    {
        //TODO: Play sound for the channel in channels.yml.
        //TODO: Play the sound for the words that were said in chat that are in 'chat words.yml'
    }
}
