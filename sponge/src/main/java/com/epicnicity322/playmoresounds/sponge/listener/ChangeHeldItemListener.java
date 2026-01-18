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

package com.epicnicity322.playmoresounds.sponge.listener;

import com.epicnicity322.playmoresounds.core.listener.SinglePMSListener;
import com.epicnicity322.playmoresounds.core.sound.SoundManager;
import com.epicnicity322.playmoresounds.core.util.PlatformUtil;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;

import java.util.Optional;

public final class ChangeHeldItemListener extends SinglePMSListener {
    private final @NotNull SoundManager<Player, Living> soundManager;

    public ChangeHeldItemListener(@NotNull PlatformUtil platformUtil, @NotNull SoundManager<Player, Living> soundManager) {
        super(platformUtil, "Change Held Item");
        this.soundManager = soundManager;
    }

    @Listener(order = Order.POST)
    public void onChangeHeldItem(ChangeInventoryEvent.Held event) {
        Optional<Player> player = event.cause().first(Player.class);
        if (player.isEmpty()) return;
        soundManager.play(sound, player.get());
    }
}
