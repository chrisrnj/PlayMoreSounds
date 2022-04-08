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

package com.epicnicity322.essentialschathook;

import com.earth2me.essentials.chat.EssentialsChatPlayerListenerLowest;
import com.epicnicity322.channelshandler.ChannelsHandler;
import com.epicnicity322.channelshandler.ChannelsHandlerAddon;
import com.epicnicity322.epicpluginlib.bukkit.logger.Logger;
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.core.util.PathUtils;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import net.ess3.api.IEssentials;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class EssentialsChatHook extends PMSAddon implements Listener
{
    private final Map<AsyncPlayerChatEvent, String> channels = Collections.synchronizedMap(new HashMap<>());
    private ChannelsHandler handler;
    private IEssentials ess;
    private Map<?, ?> chatStore;

    @Override
    protected void onStart()
    {
        Logger logger = PlayMoreSounds.getConsoleLogger();
        PluginManager pm = Bukkit.getPluginManager();
        ess = (IEssentials) pm.getPlugin("Essentials");

        if (ess == null || !pm.isPluginEnabled("EssentialsChat")) {
            logger.log("[EssentialsChat Hook] Addon could not be started because EssentialsChat plugin failed to enable.", ConsoleLogger.Level.ERROR);
            PlayMoreSounds.getAddonManager().stopAddon(this);
            return;
        }

        for (RegisteredListener registeredListener : HandlerList.getRegisteredListeners(pm.getPlugin("EssentialsChat"))) {
            Listener listener = registeredListener.getListener();

            if (listener instanceof EssentialsChatPlayerListenerLowest) {
                try {
                    Field chatStoreField = listener.getClass().getSuperclass().getDeclaredField("chatStorage");
                    chatStoreField.setAccessible(true);
                    chatStore = (Map<?, ?>) chatStoreField.get(listener);
                } catch (Exception e) {
                    PlayMoreSoundsCore.getErrorHandler().report(e, "EssentialsChat Hook Addon Error - Could not get field using reflection:");
                }
                break;
            }
        }

        if (chatStore == null) {
            logger.log("[EssentialsChat Hook] Failed to get EssentialsChat's chatStorage map.", ConsoleLogger.Level.ERROR);
            PlayMoreSounds.getAddonManager().stopAddon(this);
            return;
        }

        handler = new ChannelsHandler("EssentialsChat", this, new ChannelsHandler.ChannelSoundPreventer()
        {
            @Override
            protected boolean preventReceivingSound(@NotNull Player receiver, @NotNull Player chatter, @NotNull String channel)
            {
                return ess.getUser(receiver).isIgnoredPlayer(ess.getUser(chatter));
            }
        });

        // Appending defaults to channels.yml
        if (!ChannelsHandlerAddon.CHANNELS_CONFIG.getConfiguration().contains("EssentialsChat")) {
            int defaultRadius = ess.getSettings().getChatRadius();
            String data = "\n\nEssentialsChat:\n" +
                    "  local: # This is the default channel everyone talks.\n" +
                    "    Enabled: true\n" +
                    "    Sounds:\n" +
                    "      '1':\n" +
                    "        Options:\n" +
                    "          # If you have assigned a radius to local channel in essentials config, make sure to put\n" +
                    "          #the right radius here.\n" +
                    "          Radius: " + (defaultRadius <= 0 ? "-1" : defaultRadius) + "\n" + // Essentials use radius 0 for global.
                    "          # Essentials makes so only players with this permission receives the message.\n" +
                    "          Permission To Listen: 'essentials.chat.receive.local'\n" +
                    "        Sound: ENTITY_ITEM_PICKUP\n" +
                    "    Chat Words: # Like for VentureChat, you can assign a sound to a word aswell.\n" +
                    "      pling:\n" +
                    "        Enabled: true\n" +
                    "        Sounds:\n" +
                    "          '1':\n" +
                    "            Sound: BLOCK_NOTE_BLOCK_PLING\n";
            if (defaultRadius > 0) {
                data = data +
                        "      can you hear me: # A useful sound so players can know if they are near each other.\n" +
                        "        Enabled: true\n" +
                        "        Prevent Other Sounds: # Like in VentureChat you can prevent Chat Sound and Other Chat Words sounds.\n" +
                        "          Chat Sound: true\n" +
                        "        Sounds:\n" +
                        "          '1':\n" +
                        "            Options:\n" +
                        "              Radius: " + (defaultRadius / 2.0) + "\n" +
                        "            Pitch: 1.3\n" +
                        "            Sound: ENTITY_VILLAGER_TRADE\n";
            }
            data = data + "\n" +
                    "  shout: # When players send messages starting with !, this channel is heard by everyone.\n" +
                    "    Enabled: true\n" +
                    "    Sounds:\n" +
                    "      '1':\n" +
                    "        Options:\n" +
                    "          Radius: -1\n" +
                    "          Permission To Listen: 'essentials.chat.receive.shout'\n" +
                    "        Sound: ENTITY_ITEM_PICKUP\n" +
                    "\n" +
                    "  question: # When players send messages starting with ?, this channel is heard by everyone.\n" +
                    "    Enabled: true\n" +
                    "    Sounds:\n" +
                    "      '1':\n" +
                    "        Options:\n" +
                    "          Radius: -1\n" +
                    "          Permission To Listen: 'essentials.chat.receive.question'\n" +
                    "        Sound: ENTITY_VILLAGER_TRADE\n";
            try {
                PathUtils.write(data, ChannelsHandlerAddon.CHANNELS_CONFIG.getPath());
                logger.log("&eAdded default EssentialsChat sound to channels.yml");
                Configurations.getConfigurationLoader().loadConfigurations();
            } catch (IOException e) {
                logger.log("[EssentialsChat Hook] Failed to add defaults to channels.yml configuration. You can find a tutorial on how to set a channel sound in PlayMoreSounds's forums: https://playmoresounds.freeforums.net/thread/27/essentialschat-hook", ConsoleLogger.Level.WARN);
            }
        }
    }

    // Essentials removes the ChatStore on HIGHEST and charges the player for talking in the channel, so we're adding
    //the channel name to #channels map in HIGH and playing the sound on MONITOR
    @EventHandler(priority = EventPriority.HIGH)
    public void onAsyncPlayerChatHigh(AsyncPlayerChatEvent event)
    {
        Object thisChatStore = chatStore.get(event);
        if (thisChatStore == null) return;

        try {
            Method getTypeMethod = thisChatStore.getClass().getDeclaredMethod("getType");
            getTypeMethod.setAccessible(true);
            String type = getTypeMethod.invoke(thisChatStore).toString();
            channels.put(event, type.isEmpty() ? "local" : type);
        } catch (Exception e) {
            PlayMoreSoundsCore.getErrorHandler().report(e, "EssentialsChat Hook Addon Error - Could not get 'getType' method using reflection:");
            PlayMoreSounds.getConsoleLogger().log("[EssentialsChat Hook] Something went wrong while getting EssentialsChat channel name using reflection.", ConsoleLogger.Level.ERROR);
            HandlerList.unregisterAll(this);
            PlayMoreSounds.getConsoleLogger().log("[EssentialsChat Hook] Chat event was unregistered.", ConsoleLogger.Level.ERROR);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPlayerChatMonitor(AsyncPlayerChatEvent event)
    {
        String channel = channels.remove(event);

        if (!event.isCancelled() && channel != null) {
            handler.onChat(event.getPlayer(), channel, event.getMessage());
        }
    }
}
