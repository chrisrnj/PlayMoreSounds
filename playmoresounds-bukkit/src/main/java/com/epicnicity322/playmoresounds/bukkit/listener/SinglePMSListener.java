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

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSoundsPlugin;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.sound.Sound;
import com.epicnicity322.yamlhandler.Configuration;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class SinglePMSListener implements PMSListener {
    protected final @NotNull PlayMoreSoundsPlugin plugin;
    private final @NotNull String name;
    protected Sound sound;
    private boolean registered = false;

    public SinglePMSListener(@NotNull PlayMoreSoundsPlugin plugin, @NotNull String name) {
        this.plugin = plugin;
        this.name = name;
    }

    @Override
    public boolean isRegistered() {
        return registered;
    }

    @Override
    public void register() {
        Configuration config = Configurations.SOUNDS.getConfiguration();

        if (config.getBoolean(name + ".Enabled").orElse(false)) {
            sound = new Sound(Objects.requireNonNull(config.getConfigurationSection(name)));
            if (!registered) {
                plugin.getServer().getPluginManager().registerEvents(this, plugin);
                registered = true;
            }
        } else {
            sound = null;
            if (registered) {
                HandlerList.unregisterAll(this);
                registered = false;
            }
        }
    }
}
