/*
 * PlayMoreSounds - A bukkit plugin that manages and plays sounds.
 * Copyright (C) 2021 Christiano Rangel
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
import com.epicnicity322.playmoresounds.sponge.sound.RichSound;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;

public final class OnChangeInventory
{
    @Listener
    public void onChangeInventoryHeld(ChangeInventoryEvent.Held event, @First Player player)
    {
        RichSound sound = new RichSound(Configurations.SOUNDS.getConfigurationHolder().getConfiguration().getConfigurationSection("Change Held Item"));

        sound.play(player);
    }
}
