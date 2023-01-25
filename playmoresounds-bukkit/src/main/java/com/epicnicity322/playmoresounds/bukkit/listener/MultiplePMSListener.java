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

import com.epicnicity322.epicpluginlib.core.config.ConfigurationHolder;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSoundsPlugin;
import com.epicnicity322.playmoresounds.core.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.sound.Sound;
import com.epicnicity322.yamlhandler.Configuration;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class MultiplePMSListener implements PMSListener {
    protected final @NotNull PlayMoreSoundsPlugin plugin;
    protected final Sound @NotNull [] sounds;
    private final @NotNull String[] names;
    private final @NotNull ConfigurationHolder config;
    private boolean registered;

    public MultiplePMSListener(@NotNull PlayMoreSoundsPlugin plugin, @NotNull String @NotNull [] names, @NotNull ConfigurationHolder config) {
        this.plugin = plugin;
        this.names = names;
        this.sounds = new Sound[names.length];
        this.config = config;
    }

    @Override
    public boolean isRegistered() {
        return registered;
    }

    @Override
    public void register() {
        Configuration config = this.config.getConfiguration();
        boolean shouldRegister = false;

        int count = 0;
        for (String name : names) {
            Sound sound2 = null;
            if (config.getBoolean(name + ".Enabled").orElse(false)) {
                try {
                    sound2 = new Sound(Objects.requireNonNull(config.getConfigurationSection(name)));
                    shouldRegister = true;
                } catch (Exception e) {
                    PlayMoreSounds.logger().log("Unable to register sound '" + name + "' of config '" + config.getName() + "' because the sound has an invalid namespaced key!");
                }
            }
            sounds[count++] = sound2;
        }

        if (shouldRegister) {
            if (!registered) {
                plugin.getServer().getPluginManager().registerEvents(this, plugin);
                registered = true;
            }
        } else {
            if (registered) {
                HandlerList.unregisterAll(this);
                registered = false;
            }
        }
    }
}
