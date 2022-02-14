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

package com.epicnicity322.channelshandler;

import com.epicnicity322.epicpluginlib.core.config.ConfigurationHolder;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsVersion;
import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import com.epicnicity322.playmoresounds.core.config.Configurations;

public final class ChannelsHandlerAddon extends PMSAddon
{
    //TODO: Create channels.yml with documentation of how to set sounds for specific channels, also a notice for 'Hook Mode' and how it replaces Player Chat sound and adds compatibility to 'chat words' feature.
    public static final ConfigurationHolder CHANNELS_CONFIG = new ConfigurationHolder(Configurations.BIOMES.getConfigurationHolder().getPath().getParent().resolve("channels.yml"),
            "");

    @Override
    protected void onStart()
    {
        Configurations.getConfigurationLoader().registerConfiguration(CHANNELS_CONFIG, new Version("4.1.2"), PlayMoreSoundsVersion.getVersion());
        PlayMoreSounds.getConsoleLogger().log("&eChannels configuration was registered.");
    }
}
