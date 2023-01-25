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

package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.epicpluginlib.bukkit.reflection.ReflectionUtil;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSoundsPlugin;
import com.epicnicity322.playmoresounds.bukkit.listener.listeners.*;
import com.epicnicity322.playmoresounds.core.PlayMoreSounds;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public final class ListenerRegister {
    private final @NotNull HashSet<PMSListener> listeners = new HashSet<>(28);

    public ListenerRegister(@NotNull PlayMoreSoundsPlugin plugin) {
        listeners.add(new BedEnterListener(plugin));
        listeners.add(new BedLeaveListener(plugin));
        listeners.add(new ChangeHeldItemListener(plugin));
        listeners.add(new ChangeLevelListener(plugin));
        listeners.add(new CraftItemListener(plugin));
        listeners.add(new DropItemListener(plugin));
        listeners.add(new EditBookListener(plugin));
        listeners.add(new EntityHitListener(plugin));
        if (ReflectionUtil.getClass("com.destroystokyo.paper.event.entity.EntityJumpEvent") != null) {
            listeners.add(new EntityJumpListener(plugin));
        }
        listeners.add(new FurnaceExtractListener(plugin));
        listeners.add(new GameModeChangeListener(plugin));
        listeners.add(new InventoryClickListener(plugin));
        listeners.add(new InventoryCloseListener(plugin));
        listeners.add(new JoinServerListener(plugin));
        listeners.add(new LeaveServerListener(plugin));
        listeners.add(new PlayerChatListener(plugin));
        // Only paper has PlayerDeathEvent implementing cancellable.
        if (Cancellable.class.isAssignableFrom(PlayerDeathEvent.class)) {
            listeners.add(new PlayerDeathListener(plugin));
        }
        if (ReflectionUtil.getClass("com.destroystokyo.paper.event.player.PlayerJumpEvent") != null) {
            listeners.add(new PlayerJumpListener(plugin));
        }
        listeners.add(new PlayerKickListener(plugin));
        listeners.add(new PlayerSwingListener(plugin));
        listeners.add(new PortalCreateListener(plugin));
        listeners.add(new RespawnListener(plugin));
        listeners.add(new SendCommandListener(plugin));
        listeners.add(new StartFlyingListener(plugin));
        listeners.add(new SwapHandsListener(plugin));
        listeners.add(new TeleportListener(plugin));
        listeners.add(new ToggleSneakListener(plugin));
        listeners.add(new WeatherRainListener(plugin));
    }

    public void registerAll() {
        int count = 0;

        for (PMSListener listener : listeners) {
            try {
                listener.register();
                if (listener.isRegistered()) count++;
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        PlayMoreSounds.logger().log(count + " listeners registered.");
    }

    @NotNull
    public HashSet<PMSListener> listeners() {
        return listeners;
    }
}
