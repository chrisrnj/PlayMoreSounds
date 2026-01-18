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

package com.epicnicity322.playmoresounds.core.listener;

import com.epicnicity322.playmoresounds.core.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.sound.PMSSoundList;
import com.epicnicity322.playmoresounds.core.util.PlatformUtil;
import com.epicnicity322.yamlhandler.Configuration;
import net.kyori.adventure.key.InvalidKeyException;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class SinglePMSListener implements PMSListener {
    private final @NotNull PlatformUtil platformUtil;
    private final @NotNull String name;
    protected PMSSoundList sound;
    private boolean registered = false;

    public SinglePMSListener(@NotNull PlatformUtil platformUtil, @NotNull String name) {
        this.platformUtil = platformUtil;
        this.name = name;
    }

    @Override
    public synchronized boolean isRegistered() {
        return registered;
    }

    @Override
    public synchronized void register() {
        Configuration config = Configurations.SOUNDS.config();

        if (config.getBoolean(name + ".Enabled").orElse(false)) {
            try {
                sound = new PMSSoundList(Objects.requireNonNull(config.getConfigurationSection(name)));
            } catch (InvalidKeyException e) {
                PlayMoreSounds.logger().log("Unable to register '" + name + "' in config '" + config.getName() + "' because one of the sounds has an invalid namespaced key!");
                return;
            }

            if (!registered) {
                platformUtil.registerEvents(this);
                registered = true;
            }
        } else {
            sound = null;
            if (registered) {
                platformUtil.unregisterEvents(this);
                registered = false;
            }
        }
    }
}
