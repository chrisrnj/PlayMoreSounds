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

package com.epicnicity322.ultimatechathook;

import br.net.fabiozumbi12.UltimateChat.Bukkit.API.PostFormatChatMessageEvent;
import com.epicnicity322.channelshandler.ChannelsHandler;
import com.epicnicity322.channelshandler.ChannelsHandlerAddon;
import com.epicnicity322.epicpluginlib.bukkit.logger.Logger;
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.core.util.PathUtils;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class UltimateChatHook extends PMSAddon implements Listener {
    private final Map<CommandSender, Set<CommandSender>> receivers = Collections.synchronizedMap(new HashMap<>());
    private ChannelsHandler handler;

    @Override
    protected void onStart() {
        Logger logger = PlayMoreSounds.getConsoleLogger();
        if (!Bukkit.getPluginManager().isPluginEnabled("UltimateChat")) {
            logger.log("[UltimateChat Hook] Addon could not be started because UltimateChat plugin failed to enable.", ConsoleLogger.Level.ERROR);
            PlayMoreSounds.getAddonManager().stopAddon(this);
            return;
        }

        handler = new ChannelsHandler("UltimateChat", this, new ChannelsHandler.ChannelSoundPreventer() {
            @Override
            protected boolean preventReceivingSound(@NotNull Player receiver, @NotNull Player chatter, @NotNull String channel) {
                Set<CommandSender> receiverSet = receivers.get(chatter);
                if (receiverSet == null) return false;
                return !receiverSet.contains(receiver);
            }
        });

        // Appending defaults to channels.yml
        if (!ChannelsHandlerAddon.CHANNELS_CONFIG.getConfiguration().contains("UltimateChat")) {
            String data = "\n\nUltimateChat:\n" +
                    "  Local: # This is the local channel, only people nearby can hear it.\n" +
                    "    Enabled: true\n" +
                    "    Cancellable: true # Other plugins might cancel the chat event.\n" +
                    "    Sounds:\n" +
                    "      '1':\n" +
                    "        Options:\n" +
                    "          Radius: 40\n" +
                    "        Sound: ENTITY_ITEM_PICKUP\n" +
                    "    Chat Words: # Like for VentureChat, you can assign a sound to a word as well.\n" +
                    "      pling:\n" +
                    "        Enabled: true\n" +
                    "        Cancellable: true\n" +
                    "        Sounds:\n" +
                    "          '1':\n" +
                    "            Options:\n" +
                    "              Radius: 40\n" +
                    "            Sound: BLOCK_NOTE_BLOCK_PLING\n" +
                    "      can you hear me: # A useful sound so players can know if they are near each other.\n" +
                    "        Enabled: true\n" +
                    "        Cancellable: true\n" +
                    "        Prevent Other Sounds: # Like in VentureChat you can prevent Chat Sound and Other Chat Words sounds.\n" +
                    "          Chat Sound: true\n" +
                    "        Sounds:\n" +
                    "          '1':\n" +
                    "            Options:\n" +
                    "              Radius: 20\n" +
                    "            Sound: ENTITY_VILLAGER_TRADE\n" +
                    "\n" +
                    "  Global:\n" +
                    "    Enabled: true\n" +
                    "    Cancellable: true\n" +
                    "    Sounds:\n" +
                    "      '1':\n" +
                    "        Options:\n" +
                    "          Radius: -1\n" +
                    "        Sound: ENTITY_ITEM_PICKUP";
            try {
                PathUtils.write(data, ChannelsHandlerAddon.CHANNELS_CONFIG.getPath());
                logger.log("&eAdded default UltimateChat sound to channels.yml");
                Configurations.getConfigurationLoader().loadConfigurations();
            } catch (IOException e) {
                logger.log("[UltimateChat Hook] Failed to add defaults to channels.yml configuration. You can find a tutorial on how to set a channel sound in PlayMoreSounds's forums: https://playmoresounds.freeforums.net/thread/30/ultimatechat-hook", ConsoleLogger.Level.WARN);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPostFormatChatMessageHighest(PostFormatChatMessageEvent event) {
        CommandSender sender = event.getSender();
        if (!(sender instanceof Player)) return;
        receivers.put(sender, event.getMessages().keySet());
        handler.onChat((Player) event.getSender(), event.getChannel().getName(), event.getRawMessage(), event.isCancelled());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPostFormatChatMessageMonitor(PostFormatChatMessageEvent event) {
        receivers.remove(event.getSender());
    }
}
