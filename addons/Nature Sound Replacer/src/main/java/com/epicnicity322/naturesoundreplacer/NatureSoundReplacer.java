/*
 * Copyright (C) 2022 Christiano Rangel
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

package com.epicnicity322.naturesoundreplacer;

import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsVersion;
import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public final class NatureSoundReplacer extends PMSAddon implements Listener {
    @Override
    protected void onStart() {
        Configurations.getConfigurationLoader().registerConfiguration(NatureSoundReplacerPacketAdapter.natureSoundReplacerConfig, new Version("4.0.0"), PlayMoreSoundsVersion.getVersion());
        PlayMoreSounds.getConsoleLogger().log("&eNature Sound Replacer configuration was registered.");

        // Running when server has fully started.
        Bukkit.getScheduler().runTask(PlayMoreSounds.getInstance(), () -> {
            Runnable loader = () -> NatureSoundReplacerPacketAdapter.loadNatureSoundReplacer(PlayMoreSounds.getInstance());

            loader.run();
            PlayMoreSounds.onReload(loader);

            PlayMoreSounds.getConsoleLogger().log("&aNature Sound Replacer was enabled successfully.");
        });
    }
}
