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

package com.epicnicity322.playmoresounds.bukkit.listeners;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSoundsPlugin;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.sound.Sound;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class PMSListener implements Listener {
    private final @NotNull PlayMoreSoundsPlugin plugin;
    protected Sound sound;

    public PMSListener(@NotNull PlayMoreSoundsPlugin plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public abstract String name();

    public boolean shouldRegister() {
        return Configurations.SOUNDS.getConfiguration().getBoolean(name() + ".Enabled").orElse(false);
    }

    public void register() {
        if (shouldRegister()) {
            sound = new Sound(Objects.requireNonNull(Configurations.SOUNDS.getConfiguration().getConfigurationSection(name())));
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        } else {
            HandlerList.unregisterAll(this);
            sound = null;
        }
    }
}
