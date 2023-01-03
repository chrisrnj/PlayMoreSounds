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

package com.epicnicity322.playmoresounds.sponge.listeners;

import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.sound.Sound;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.plugin.PluginContainer;

import java.util.Objects;

public abstract class PMSListener {
    private final @NotNull PluginContainer plugin;
    protected Sound sound;

    public PMSListener(@NotNull PluginContainer plugin) {
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
            Sponge.eventManager().registerListeners(plugin, this);
        } else {
            Sponge.eventManager().unregisterListeners(this);
            sound = null;
        }
    }
}
