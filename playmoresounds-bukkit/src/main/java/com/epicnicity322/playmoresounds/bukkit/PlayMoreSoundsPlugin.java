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

package com.epicnicity322.playmoresounds.bukkit;

import com.epicnicity322.playmoresounds.bukkit.listeners.JoinServerListener;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundManager;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class PlayMoreSoundsPlugin extends JavaPlugin {
    private static PlayMoreSoundsPlugin instance;
    @NotNull
    private final SoundManager soundManager = new SoundManager(this);

    public PlayMoreSoundsPlugin() {
        instance = this;
    }

    @NotNull
    public static SoundManager soundManager() {
        return instance.soundManager;
    }

    public void reload() {
        Configurations.loader().loadConfigurations();
        new JoinServerListener(this).register();
    }

    @Override
    public void onEnable() {
        reload();
    }
}
