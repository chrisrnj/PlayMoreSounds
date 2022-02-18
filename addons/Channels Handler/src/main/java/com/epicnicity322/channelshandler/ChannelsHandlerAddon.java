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
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsVersion;
import com.epicnicity322.playmoresounds.core.addons.AddonDescription;
import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import com.epicnicity322.playmoresounds.core.config.Configurations;

public final class ChannelsHandlerAddon extends PMSAddon {
    public static final ConfigurationHolder CHANNELS_CONFIG = new ConfigurationHolder(Configurations.BIOMES.getConfigurationHolder().getPath().getParent().resolve("channels.yml"),
            "# Play sounds when players talk in specific channels.\n" +
                    "\n" +
                    "# This file is generated and managed by the addon 'Channels Handler'. Other addons may hook to add\n" +
                    "#compatibility to channel based chat plugins.\n" +
                    "# You can install official hooking addons through the command '/pms addons'\n" +
                    "\n" +
                    "# >> Sound Example:\n" +
                    "\n" +
                    "VentureChat: # The name of the plugin you are hooking to, in this example VentureChat. Make sure you\n" +
                    "#have the addon to add compatibility to it installed, or this won't work.\n" +
                    "  Global: # The channel name you want to play a sound when talking. This is case sensitive.\n" +
                    "    Enabled: true\n" +
                    "    Prevent For Muted: true # Prevents this channel's sound for playing for muted players.\n" +
                    "    Cancellable: true\n" +
                    "    Sounds: # The sounds that will play when people talk on this channel.\n" +
                    "      '1':\n" +
                    "        Options:\n" +
                    "          # Since this is global channel, it's best to leave radius at -1.\n" +
                    "          Radius: -1.0\n" +
                    "        Sound: ENTITY_CHICKEN_EGG\n" +
                    "    # You can set a word that when said in this channel, a sound will play.\n" +
                    "    Chat Words:\n" +
                    "      pling: # The word to play a sound.\n" +
                    "        Enabled: true\n" +
                    "        Prevent For Muted: true\n" +
                    "        Cancellable: true\n" +
                    "        Prevent Other Sounds:\n" +
                    "          # Prevents the sound above from playing. This way only the chat word sound plays.\n" +
                    "          Chat Sound: true\n" +
                    "          # If you add more than one chat word, like I did below, this makes so when the player says\n" +
                    "          #both words 'pling' and 'bass', only this pling sound plays instead of both sounds.\n" +
                    "          Other Chat Words: true\n" +
                    "        Sounds:\n" +
                    "          '1':\n" +
                    "            Options:\n" +
                    "              Radius: -1.0\n" +
                    "            Sound: BLOCK_NOTE_BLOCK_PLING\n" +
                    "      bass: # Have as many words as you want, just create a section for each word.\n" +
                    "        Enabled: true\n" +
                    "        Prevent For Muted: true\n" +
                    "        Cancellable: true\n" +
                    "        Prevent Other Sounds:\n" +
                    "          Chat Sound: true\n" +
                    "        Sounds:\n" +
                    "          '1':\n" +
                    "            Options:\n" +
                    "              Radius: -1.0\n" +
                    "            Sound: BLOCK_NOTE_BLOCK_BASS\n" +
                    "\n" +
                    "  Local: # Local channel has a distance where messages can be seen.\n" +
                    "    Enabled: true\n" +
                    "    Prevent For Muted: true\n" +
                    "    Cancellable: true\n" +
                    "    Sounds:\n" +
                    "      '1':\n" +
                    "        Options:\n" +
                    "          # Make sure to put the right radius in blocks of the channel here.\n" +
                    "          Radius: 230.0\n" +
                    "        Sound: ENTITY_CHICKEN_EGG\n" +
                    "    Chat Words:\n" +
                    "      boo:\n" +
                    "        Enabled: true\n" +
                    "        Prevent For Muted: true\n" +
                    "        Cancellable: true\n" +
                    "        Sounds:\n" +
                    "          '1':\n" +
                    "            Options:\n" +
                    "              Radius: 40.0\n" +
                    "            Sound: ENTITY_GHAST_SCREAM\n" +
                    "\n" +
                    "  Staff: # Staff channel can only be seen by the staff online.\n" +
                    "    Enabled: true\n" +
                    "    Prevent For Muted: true\n" +
                    "    Cancellable: true\n" +
                    "    Sounds:\n" +
                    "      '1':\n" +
                    "        Options:\n" +
                    "          # This sound will only be heard by staff.\n" +
                    "          Permission To Listen: 'venturechat.staff'\n" +
                    "          Radius: -1.0\n" +
                    "        Sound: BLOCK_NOTE_BLOCK_BASS\n" +
                    "        Pitch: 0.0\n" +
                    "    Chat Words:\n" +
                    "      keep your eyes on:\n" +
                    "        Enabled: true\n" +
                    "        Prevent For Muted: true\n" +
                    "        Cancellable: true\n" +
                    "        Sounds:\n" +
                    "          '1':\n" +
                    "            Options:\n" +
                    "              Permission To Listen: 'venturechat.staff'\n" +
                    "              Radius: -1.0\n" +
                    "            Sound: ENTITY_GOAT_SCREAMING_AMBIENT\n" +
                    "\n" +
                    "# You can find more information about how to configure sounds in sounds.yml.\n" +
                    "\n" +
                    "Version: '" + PlayMoreSoundsVersion.version + "'");

    @Override
    protected void onStart() {
        Configurations.getConfigurationLoader().registerConfiguration(CHANNELS_CONFIG, null, PlayMoreSoundsVersion.getVersion());
        int dependingAddons = 0;

        for (PMSAddon addon : PlayMoreSounds.getAddonManager().getAddons()) {
            AddonDescription description = addon.getDescription();

            if (description.getAddonHooks().contains("Channels Handler") || description.getRequiredAddons().contains("Channels Handler"))
                ++dependingAddons;
        }

        if (dependingAddons == 0) {
            PlayMoreSounds.getConsoleLogger().log("[Channels Handler] &4Channels configuration was registered, but no depending addons were found.", ConsoleLogger.Level.WARN);
        } else {
            PlayMoreSounds.getConsoleLogger().log("[Channels Handler] &eChannels configuration was registered. " + dependingAddons + " depending addon" + (dependingAddons == 1 ? " was" : "s were") + " found.");
        }
    }
}
