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

package com.epicnicity322.playmoresounds.sponge.listeners;

import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.sponge.sound.PlayableRichSound;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

public class OnServerSideConnection {
    @Listener
    public void onJoin(ServerSideConnectionEvent.Join event) {
        new PlayableRichSound(Configurations.SOUNDS.getConfigurationHolder().getConfiguration().getConfigurationSection("Join Server")).play(event.player());
    }
}
