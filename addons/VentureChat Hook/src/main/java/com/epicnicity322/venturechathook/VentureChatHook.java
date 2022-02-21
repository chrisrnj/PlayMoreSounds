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

package com.epicnicity322.venturechathook;

import com.epicnicity322.channelshandler.ChannelsHandler;
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import mineverse.Aust1n46.chat.api.events.VentureChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public final class VentureChatHook extends PMSAddon implements Listener {
    private ChannelsHandler handler;

    @Override
    protected void onStart() {
        if (!Bukkit.getPluginManager().isPluginEnabled("VentureChat")) {
            PlayMoreSounds.getConsoleLogger().log("[VentureChat Hook] Addon could not be started because VentureChat plugin failed to enable.", ConsoleLogger.Level.ERROR);
            PlayMoreSounds.getAddonManager().stopAddon(this);
            return;
        }

        handler = new ChannelsHandler("VentureChat", this, new ChannelsHandler.ChannelSoundPreventer() {
            @Override
            protected boolean preventReceivingSound(@NotNull Player receiver, @NotNull Player chatter, @NotNull String channel) {
                MineverseChatPlayer mcp = MineverseChatAPI.getMineverseChatPlayer(receiver);
                return !mcp.isListening(channel) || mcp.getIgnores().contains(chatter.getUniqueId());
            }
        });
    }

    @EventHandler
    public void onVentureChat(VentureChatEvent event) {
        handler.onChat(event.getMineverseChatPlayer().getPlayer(), event.getChannel().getName(), event.getChat());
    }
}
