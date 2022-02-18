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

import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableSound;
import com.epicnicity322.playmoresounds.bukkit.sound.events.PlaySoundEvent;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.ThreadSafe;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * A class that other addons can hook to, so they can make use of channels.yml configuration.
 */
@ThreadSafe
public class ChannelsHandler {
    protected final @NotNull HashMap<String, ChannelSound> channelSounds = new HashMap<>();
    protected final @NotNull AtomicBoolean listenerRegistered = new AtomicBoolean(false);
    private final @NotNull String pluginName;
    private final @NotNull Listener listener;
    private final @Nullable Listener mutedCheckerListener;

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
     * @see #ChannelsHandler(String, Listener, MutedChecker)
     */
    public ChannelsHandler(@NotNull String pluginName, @NotNull Listener listener) {
        this(pluginName, listener, null);
    }

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
     * <p>
     * You can provide a {@link MutedChecker} that has a boolean that will be used to determine if the channel sound
     * should be played or not. Use it to return if a player has the channel muted or not. This will only work if the
     * user has 'Prevent for muted' boolean true in channels configuration.
     *
     * @param pluginName   The name of the channel based chat plugin you are trying to add compatibility to.
     * @param listener     The listener that should be registered when sounds for this plugin are enabled.
     * @param mutedChecker The checker to see if the sound should not play in case the player has the channel muted.
     */
    public ChannelsHandler(@NotNull String pluginName, @NotNull Listener listener, @Nullable MutedChecker mutedChecker) {
        this.pluginName = pluginName;
        this.listener = listener;
        if (mutedChecker == null)
            mutedCheckerListener = null;
        else
            mutedCheckerListener = new Listener() {
                @EventHandler(priority = EventPriority.LOWEST)
                public void onPlaySound(PlaySoundEvent event) {
                    PlayableSound sound = event.getSound();
                    ConfigurationSection section = sound.getSection();

                    if (section == null || section.getParent() == null) return;

                    Optional<Path> configPath = section.getRoot().getFilePath();

                    if (!configPath.isPresent() || !configPath.get().equals(ChannelsHandlerAddon.CHANNELS_CONFIG.getPath()))
                        return;

                    String sectionPath = section.getPath();

                    if (!sectionPath.substring(0, sectionPath.indexOf(section.getSectionSeparator())).equals(pluginName))
                        return;

                    String channel = sectionPath.substring(sectionPath.indexOf(section.getSectionSeparator()) + 1);
                    channel = channel.substring(0, channel.indexOf(section.getSectionSeparator()));

                    if (section.getParent().getParent().getBoolean("Prevent For Muted").orElse(false)
                            && mutedChecker.isMuted(channel, event.getPlayer())) {
                        event.setCancelled(true);
                    }
                }
            };

        reloadListener();
        PlayMoreSounds.onReload(this::reloadListener);
    }

    /**
     * Checks if any sound for the plugin is enabled in channels.yml configuration, if so, the specified listener is
     * registered. If no sound is enabled then the listener is unregistered. Also reloads the sounds and chat words set
     * to play on chat.
     */
    public void reloadListener() {
        channelSounds.clear();
        Configuration channels = ChannelsHandlerAddon.CHANNELS_CONFIG.getConfiguration();
        ConfigurationSection pluginSection = channels.getConfigurationSection(pluginName);

        if (pluginSection != null) {
            for (Map.Entry<String, Object> channel : pluginSection.getNodes().entrySet()) {
                if (!(channel.getValue() instanceof ConfigurationSection)) continue;
                ConfigurationSection channelSection = (ConfigurationSection) channel.getValue();
                ConfigurationSection chatWordsSection = channelSection.getConfigurationSection("Chat Words");
                HashMap<Pattern, PlayableRichSound> chatWordSounds = null;

                if (chatWordsSection != null) {
                    for (Map.Entry<String, Object> chatWord : chatWordsSection.getNodes().entrySet()) {
                        if (!(chatWord.getValue() instanceof ConfigurationSection)) continue;
                        ConfigurationSection chatWordSection = (ConfigurationSection) chatWord.getValue();

                        if (chatWordSection.getBoolean("Enabled").orElse(false)) {
                            try {
                                PlayableRichSound chatWordSound = new PlayableRichSound(chatWordSection);
                                if (chatWordSounds == null) chatWordSounds = new HashMap<>();
                                chatWordSounds.put(Pattern.compile(".*\\b" + Pattern.quote(chatWord.getKey().toLowerCase()) + "\\b.*"), chatWordSound);
                            } catch (IllegalArgumentException ignored) {
                                PlayMoreSounds.getConsoleLogger().log("[Channels Handler] The chat word sound '" + chatWord.getKey() + "' for the channel '" + channel.getKey() + "' is an invalid sound, so it was ignored.", ConsoleLogger.Level.WARN);
                            }
                        }
                    }
                }

                PlayableRichSound channelSound = null;

                try {
                    channelSound = channelSection.getBoolean("Enabled").orElse(false) ? new PlayableRichSound(channelSection) : null;
                } catch (IllegalArgumentException ignored) {
                    PlayMoreSounds.getConsoleLogger().log("[Channels Handler] The channel '" + channel.getKey() + "' has an invalid sound, so " + (chatWordSounds == null ? "it was ignored." : "only the chat words sounds were registered."), ConsoleLogger.Level.WARN);
                }

                if (channelSound != null || chatWordSounds != null)
                    channelSounds.put(channel.getKey(), new ChannelSound(channelSound, chatWordSounds));
            }
        }

        // Checking if listener is currently not registered and should register.
        if (channelSounds.isEmpty()) {
            if (listenerRegistered.getAndSet(false)) {
                HandlerList.unregisterAll(listener);
                if (mutedCheckerListener != null) {
                    HandlerList.unregisterAll(mutedCheckerListener);
                }
            }
        } else {
            if (!listenerRegistered.getAndSet(true)) {
                Bukkit.getPluginManager().registerEvents(listener, PlayMoreSounds.getInstance());
                if (mutedCheckerListener != null) {
                    Bukkit.getPluginManager().registerEvents(mutedCheckerListener, PlayMoreSounds.getInstance());
                }
            }
        }
    }

    /**
     * Call this method on the event of the plugin you're trying to add compatibility to.
     * <p>
     * In case this plugin uses an async chat event, you don't need to call this on bukkit's main thread using
     * {@link org.bukkit.scheduler.BukkitScheduler#runTask(Plugin, Runnable)}, this is automatically identified and
     * handled by this method.
     *
     * @param chatter The player who talked in chat.
     * @param channel The name of the channel the player send the message on.
     * @param message The message the player sent, used for checking if a sound should be played in 'chat words.yml'
     * @see #onChat(Player, String, String, boolean isCancelled)
     */
    public void onChat(@NotNull Player chatter, @NotNull String channel, @NotNull String message) {
        onChat(chatter, channel, message, false);
    }

    /**
     * Call this method on the event of the plugin you're trying to add compatibility to.
     * <p>
     * In case this plugin uses an async chat event, you don't need to call this on bukkit's main thread using
     * {@link org.bukkit.scheduler.BukkitScheduler#runTask(Plugin, Runnable)}, this is automatically identified and
     * handled by this method.
     *
     * @param chatter     The player who talked in chat.
     * @param channel     The name of the channel the player send the message on.
     * @param message     The message the player sent, used for checking if a sound should be played in 'chat words.yml'
     * @param isCancelled If the event is cancelled. Sometimes the user wants to play the sound even if the event is cancelled.
     */
    public void onChat(@NotNull Player chatter, @NotNull String channel, @NotNull String message, boolean isCancelled) {
        boolean mainThread = Bukkit.isPrimaryThread();
        ChannelSound channelSound = channelSounds.get(channel);
        if (channelSound == null) return;
        boolean playChannelSound = channelSound.channelSound != null && (!isCancelled || !channelSound.channelSound.isCancellable());

        if (channelSound.chatWords != null)
            for (Map.Entry<Pattern, PlayableRichSound> chatWord : channelSound.chatWords.entrySet()) {
                PlayableRichSound chatWordSound = chatWord.getValue();
                if (isCancelled && chatWordSound.isCancellable()) continue;
                if (!chatWord.getKey().matcher(message).matches()) continue;

                if (mainThread) {
                    chatWordSound.play(chatter);
                } else {
                    Bukkit.getScheduler().runTask(PlayMoreSounds.getInstance(), () -> chatWordSound.play(chatter));
                }

                if (chatWordSound.getSection().getBoolean("Prevent Other Sounds.Chat Sound").orElse(false))
                    playChannelSound = false;
                if (chatWordSound.getSection().getBoolean("Prevent Other Sounds.Other Chat Words").orElse(false)) break;
            }

        if (playChannelSound) {
            if (mainThread) {
                channelSound.channelSound.play(chatter);
            } else {
                Bukkit.getScheduler().runTask(PlayMoreSounds.getInstance(), () -> channelSound.channelSound.play(chatter));
            }
        }
    }

    private static final class ChannelSound {
        private final @Nullable PlayableRichSound channelSound;
        private final @Nullable HashMap<Pattern, PlayableRichSound> chatWords;

        private ChannelSound(@Nullable PlayableRichSound channelSound, @Nullable HashMap<Pattern, PlayableRichSound> chatWords) {
            this.channelSound = channelSound;
            this.chatWords = chatWords;
        }
    }

    public static abstract class MutedChecker {
        /**
         * Check if the player has a channel muted. This will be called before playing the sound for this channel. Use
         * it to check if the player has messages muted for this channel.
         *
         * @param channelName The name of the channel that will play a sound.
         * @param player      The player that the channel sound will be played to.
         * @return If the channel sound should be played or not.
         */
        protected abstract boolean isMuted(@NotNull String channelName, @NotNull Player player);
    }
}
