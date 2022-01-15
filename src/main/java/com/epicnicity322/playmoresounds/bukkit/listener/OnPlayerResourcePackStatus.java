/*
 * PlayMoreSounds - A bukkit plugin that manages and plays sounds.
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

package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionEnterEvent;
import com.epicnicity322.playmoresounds.bukkit.util.ListenerRegister;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import java.util.HashSet;

public final class OnPlayerResourcePackStatus implements Listener
{
    @EventHandler
    public void onPlayerResourcePackStatus(PlayerResourcePackStatusEvent event)
    {
        Configuration config = Configurations.CONFIG.getConfigurationHolder().getConfiguration();

        if (config.getBoolean("Resource Packs.Request").orElse(false)) {
            PlayerResourcePackStatusEvent.Status status = event.getStatus();
            Player player = event.getPlayer();

            if (status == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
                OnRegionEnterLeave regionListener = (OnRegionEnterLeave) ListenerRegister.getListeners().stream().filter(listener -> listener.getName().equals("Region Enter|Region Leave")).findFirst().orElseThrow(NullPointerException::new);
                HashSet<RegionEnterEvent> removedRegionEvents = new HashSet<>();

                OnPlayerJoin.playersInRegionWaitingToLoadResourcePack.removeIf(regionEvent -> {
                    if (player.equals(regionEvent.getPlayer())) {
                        removedRegionEvents.add(regionEvent);
                        return true;
                    }

                    return false;
                });

                for (RegionEnterEvent regionEvent : removedRegionEvents) {
                    regionListener.onRegionEnter(regionEvent);
                }
            } else if (status != PlayerResourcePackStatusEvent.Status.ACCEPTED && config.getBoolean("Resource Packs.Force.Enabled").orElse(false)) {
                if (status == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD && !config.getBoolean("Resource Packs.Force.Even If Download Fail").orElse(false))
                    return;

                player.kickPlayer(PlayMoreSounds.getLanguage().getColored("Resource Packs.Kick Message"));
            }
        }
    }
}
