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

package com.epicnicity322.playmoresounds.bukkit.util;

import com.epicnicity322.epicpluginlib.bukkit.reflection.ReflectionUtil;
import com.epicnicity322.playmoresounds.bukkit.listener.*;
import com.epicnicity322.playmoresounds.core.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.listener.PMSListener;
import com.epicnicity322.playmoresounds.core.sound.SoundManager;
import com.epicnicity322.playmoresounds.core.util.PlatformUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public final class ListenerRegister {
    private final @NotNull HashSet<PMSListener> listeners = new HashSet<>(28);

    public ListenerRegister(@NotNull PlatformUtil platformUtil, @NotNull SoundManager<Player, Entity> soundManager) {
        listeners.add(new BedEnterListener(platformUtil, soundManager));
        listeners.add(new BedLeaveListener(platformUtil, soundManager));
        listeners.add(new ChangeHeldItemListener(platformUtil, soundManager));
        listeners.add(new ChangeLevelListener(platformUtil, soundManager));
        listeners.add(new CraftItemListener(platformUtil, soundManager));
        listeners.add(new DropItemListener(platformUtil, soundManager));
        listeners.add(new EditBookListener(platformUtil, soundManager));
        listeners.add(new EntityHitListener(platformUtil, soundManager));
        if (ReflectionUtil.getClass("com.destroystokyo.paper.event.entity.EntityJumpEvent") != null) {
            listeners.add(new EntityJumpListener(platformUtil, soundManager));
        }
        listeners.add(new FurnaceExtractListener(platformUtil, soundManager));
        listeners.add(new GameModeChangeListener(platformUtil, soundManager));
        listeners.add(new InventoryClickListener(platformUtil, soundManager));
        listeners.add(new InventoryCloseListener(platformUtil, soundManager));
        listeners.add(new JoinServerListener(platformUtil, soundManager));
        listeners.add(new LeaveServerListener(platformUtil, soundManager));
        listeners.add(new PlayerChatListener(platformUtil, soundManager));
        //noinspection ConstantValue - Only paper has PlayerDeathEvent implementing cancellable.
        if (Cancellable.class.isAssignableFrom(PlayerDeathEvent.class)) {
            listeners.add(new PlayerDeathListener(platformUtil, soundManager));
        }
        if (ReflectionUtil.getClass("com.destroystokyo.paper.event.player.PlayerJumpEvent") != null) {
            listeners.add(new PlayerJumpListener(platformUtil, soundManager));
        }
        listeners.add(new PlayerKickListener(platformUtil, soundManager));
        listeners.add(new PlayerSwingListener(platformUtil, soundManager));
        listeners.add(new PortalCreateListener(platformUtil, soundManager));
        listeners.add(new RespawnListener(platformUtil, soundManager));
        listeners.add(new SendCommandListener(platformUtil, soundManager));
        listeners.add(new StartFlyingListener(platformUtil, soundManager));
        listeners.add(new SwapHandsListener(platformUtil, soundManager));
        listeners.add(new TeleportListener(platformUtil, soundManager));
        listeners.add(new ToggleSneakListener(platformUtil, soundManager));
        listeners.add(new WeatherRainListener(platformUtil, soundManager));
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
