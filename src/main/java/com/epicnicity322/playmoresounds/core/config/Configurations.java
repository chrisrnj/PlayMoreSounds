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

package com.epicnicity322.playmoresounds.core.config;

import com.epicnicity322.epicpluginlib.core.config.ConfigurationHolder;
import com.epicnicity322.epicpluginlib.core.config.ConfigurationLoader;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsVersion;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public enum Configurations
{
    //100 chars per line for organization
    BIOMES(StaticFields.sounds.resolve("biomes.yml"), "# Set a sound to play when you enter, leave or stand on a specific biome.\n" +
            "#\n" +
            "# To set a sound, just create a configuration section with the name of the biome or just copy the\n" +
            "# sample below.\n" +
            "# Biome list: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/Biome.html\n" +
            "#\n" +
            "# Sample:\n" +
            "# (Take a note that this is a sample and the sounds and biomes may not be available on your MC\n" +
            "# version.)\n" +
            "#\n" +
            "#world: # The world name, replace 'world' with the name of the world you want to play the sound.\n" +
            "#  PLAINS: # The biome name.\n" +
            "#    Enter: # When a player enters this biome.\n" +
            "#      Cancellable: true\n" +
            "#      Enabled: true\n" +
            "#      Stop On Exit:\n" +
            "#        Enabled: true # If enabled, the sound will be stopped when the player leaves the biome.\n" +
            "#        Delay: 20 # The time to wait before stopping the sound.\n" +
            "#      Sounds:\n" +
            "#        '1':\n" +
            "#          Delay: 0\n" +
            "#          Options:\n" +
            "#            Ignores Disabled: false\n" +
            "#            Permission Required: ''\n" +
            "#            Permission To Listen: ''\n" +
            "#            Radius: 0.0\n" +
            "#            Relative Location:\n" +
            "#              FRONT_BACK: 0.0\n" +
            "#              LEFT_RIGHT: 0.0\n" +
            "#              UP_DOWN: 0.0\n" +
            "#          Pitch: 1.0\n" +
            "#          Sound: BLOCK_NOTE_BLOCK_PLING\n" +
            "#          Volume: 10.0\n" +
            "#\n" +
            "#    Leave: # When a player exits this biome.\n" +
            "#      Cancellable: true\n" +
            "#      Enabled: true\n" +
            "#      Sounds:\n" +
            "#        '1':\n" +
            "#          Delay: 0\n" +
            "#          Options:\n" +
            "#            Ignores Disabled: false\n" +
            "#            Permission Required: ''\n" +
            "#            Permission To Listen: ''\n" +
            "#            Radius: 0.0\n" +
            "#            Relative Location:\n" +
            "#              FRONT_BACK: 0.0\n" +
            "#              LEFT_RIGHT: 0.0\n" +
            "#              UP_DOWN: 0.0\n" +
            "#          Pitch: 1.0\n" +
            "#          Sound: BLOCK_NOTE_BLOCK_BASS\n" +
            "#          Volume: 10.0\n" +
            "#\n" +
            "#    Loop: # When a player enters the biome, a loop will be triggered and play.\n" +
            "#      Cancellable: true\n" +
            "#      Delay: 0 # Time in ticks to wait to start the loop once triggered.\n" +
            "#      Enabled: true\n" +
            "#      Period: 100 # Time in ticks to wait before playing these sounds again.\n" +
            "#      Stop On Exit:\n" +
            "#        Enabled: true\n" +
            "#        Delay: 20\n" +
            "#      Prevent Enter Sound: true # Makes so Enter sound is not played when Loop is enabled.\n" +
            "#      Sounds:\n" +
            "#        '1':\n" +
            "#          Delay: 0\n" +
            "#          Options:\n" +
            "#            Ignores Disabled: false\n" +
            "#            Permission Required: ''\n" +
            "#            Permission To Listen: ''\n" +
            "#            Radius: 0.0\n" +
            "#            Relative Location:\n" +
            "#              FRONT_BACK: 0.0\n" +
            "#              LEFT_RIGHT: 0.0\n" +
            "#              UP_DOWN: 0.0\n" +
            "#          Pitch: 1.0\n" +
            "#          Sound: BLOCK_NOTE_BLOCK_BASS\n" +
            "#          Volume: 10.0\n" +
            "#\n" +
            "# This is a small sample. You can add more biomes, worlds and more options to the sound options.\n" +
            "# More information about sounds on sounds.yml.\n" +
            "\n" +
            "Version: '" + PlayMoreSoundsVersion.version + "'", StaticFields.version3_2_0),
    CHAT_SOUNDS(StaticFields.sounds.resolve("chat sounds.yml"), "# Set a sound to play when a player type a sentence in chat.\n" +
            "#\n" +
            "#  There are five filters to choose:\n" +
            "#\n" +
            "#  -> Contains:\n" +
            "#  Use this section to play a sound to every message that contains the word you specify.\n" +
            "#  Sample:\n" +
            "#\n" +
            "#Contains:\n" +
            "#  hello:\n" +
            "#    Cancellable: true\n" +
            "#    Enabled: true\n" +
            "#    Prevent Other Sounds:\n" +
            "#      Default Sound: true # This will prevent the default sound set on sounds.yml from playing.\n" +
            "#      Other Filters: true # If the message match other filters, this will make so this is the only filter that will play a sound.\n" +
            "#\n" +
            "#  -> Contains SubString:\n" +
            "#  Use this section to play a sound to every message that contains the following string you specify.\n" +
            "#  This is different than Contains because Contains check for words, this checks for any part of the message.\n" +
            "#  Sample:\n" +
            "#\n" +
            "#Contains SubString:\n" +
            "#  pling:\n" +
            "#    Cancellable: true\n" +
            "#    Enabled: true\n" +
            "#    Prevent Other Sounds:\n" +
            "#      Default Sound: true # This will prevent the default sound set on sounds.yml from playing.\n" +
            "#      Other Filters: true # If the command match other filters, this will make so this is the only filter that will play a sound.\n" +
            "#\n" +
            "#  -> Ends With:\n" +
            "#  Self explanatory. If a message ends with the sentence specified, the sound will play.\n" +
            "#  Sample:\n" +
            "#\n" +
            "#Ends With:\n" +
            "#  something:\n" +
            "#    Cancellable: true\n" +
            "#    Enabled: true\n" +
            "#    Prevent Other Sounds:\n" +
            "#      Default Sound: true\n" +
            "#      Other Filters: true\n" +
            "#    Sounds:\n" +
            "#      '0':\n" +
            "#        Delay: 0\n" +
            "#        Options:\n" +
            "#          Radius: 0.0\n" +
            "#        Pitch: 1.0\n" +
            "#        Sound: 'ENTITY_CREEPER_PRIMED'\n" +
            "#        Volume: 10.0\n" +
            "#\n" +
            "#  -> Equals Exactly:\n" +
            "#  When a message equals exactly like the specified here. (Case sensitive)\n" +
            "#  Sample:\n" +
            "#\n" +
            "#Equals Exactly:\n" +
            "#  play BLOCK_PORTAL_TRAVEL sound:\n" +
            "#    Cancellable: true\n" +
            "#    Enabled: true\n" +
            "#    Prevent Other Sounds:\n" +
            "#      Default Sound: true\n" +
            "#      Other Filters: true\n" +
            "#    Sounds:\n" +
            "#      '0':\n" +
            "#        Delay: 1\n" +
            "#        Options:\n" +
            "#          Radius: 0.0\n" +
            "#        Pitch: 2.0\n" +
            "#        Sound: 'BLOCK_PORTAL_TRAVEL'\n" +
            "#        Volume: 0.4\n" +
            "#\n" +
            "#  -> Equals Ignore Case:\n" +
            "#  When a message is equals to the specified but, it doesn't matter if it's on lower case or\n" +
            "# upper case.\n" +
            "#  If a player accidentally toggled upper case on it's keyboard and typed SOMETHING and you want\n" +
            "# to set a sound for the message \"something\", put it in this section so the sound will be played\n" +
            "# even if its on upper case.\n" +
            "#  Sample:\n" +
            "#\n" +
            "#Equals Ignore Case:\n" +
            "#  something:\n" +
            "#    Cancellable: false\n" +
            "#    Enabled: true\n" +
            "#    Prevent Other Sounds:\n" +
            "#      Default Sound: true\n" +
            "#      Other Filters: true\n" +
            "#    Sounds:\n" +
            "#      '1':\n" +
            "#        Delay: 0\n" +
            "#        Options:\n" +
            "#          Radius: 0.0\n" +
            "#        Pitch: 2.0\n" +
            "#        Sound: 'BLOCK_PORTAL_TRAVEL'\n" +
            "#        Volume: 0.4\n" +
            "#\n" +
            "#  -> Starts With:\n" +
            "#  Plays a sound when a message starts with the sentence you specify.\n" +
            "#  Sample:\n" +
            "#\n" +
            "#Starts With:\n" +
            "#  hello:\n" +
            "#    Cancellable: true\n" +
            "#    Enabled: true\n" +
            "#    Prevent Other Sounds:\n" +
            "#      Default Sound: true\n" +
            "#      Other Filters: true\n" +
            "#    Sounds:\n" +
            "#      '1':\n" +
            "#        Delay: 0\n" +
            "#        Options:\n" +
            "#          Radius: 0.0\n" +
            "#        Pitch: 2.0\n" +
            "#        Sound: 'BLOCK_PORTAL_TRAVEL'\n" +
            "#        Volume: 0.4\n" +
            "#\n" +
            "# More information about sounds on sounds.yml\n" +
            "\n" +
            "Version: '" + PlayMoreSoundsVersion.version + "'", StaticFields.version3_2_0),
    COMMANDS(StaticFields.sounds.resolve("commands.yml"), "# Set a sound to play when a player type a specific command.\n" +
            "#\n" +
            "#  There are five filters to choose:\n" +
            "#\n" +
            "#  -> Contains:\n" +
            "#  Use this section to play a sound to every command that contains the word you specify.\n" +
            "#  Sample:\n" +
            "#\n" +
            "#Contains:\n" +
            "#  play:\n" +
            "#    Cancellable: true\n" +
            "#    Enabled: true\n" +
            "#    Prevent Other Sounds:\n" +
            "#      Default Sound: true # This will prevent the default sound set on sounds.yml from playing.\n" +
            "#      Other Filters: true # If the command match other filters, this will make so this is the only filter that will play a sound.\n" +
            "#\n" +
            "#  -> Contains SubString:\n" +
            "#  Use this section to play a sound to every command that contains the following string you specify.\n" +
            "#  This is different than Contains because Contains check for words, this checks for any part of the command.\n" +
            "#  Sample:\n" +
            "#\n" +
            "#Contains SubString:\n" +
            "#  set:\n" +
            "#    Cancellable: true\n" +
            "#    Enabled: true\n" +
            "#    Prevent Other Sounds:\n" +
            "#      Default Sound: true # This will prevent the default sound set on sounds.yml from playing.\n" +
            "#      Other Filters: true # If the command match other filters, this will make so this is the only filter that will play a sound.\n" +
            "#\n" +
            "#  -> Ends With:\n" +
            "#  Self explanatory. If a command ends with the sentence specified, the sound will play.\n" +
            "#  Sample:\n" +
            "#\n" +
            "#Ends With:\n" +
            "#  -force:\n" +
            "#    Cancellable: true\n" +
            "#    Enabled: true\n" +
            "#    Prevent Other Sounds:\n" +
            "#      Default Sound: true\n" +
            "#      Other Filters: true\n" +
            "#    Sounds:\n" +
            "#      '0':\n" +
            "#        Delay: 0\n" +
            "#        Options:\n" +
            "#          Radius: 0.0\n" +
            "#        Pitch: 1.0\n" +
            "#        Sound: 'ENTITY_CREEPER_PRIMED'\n" +
            "#        Volume: 10.0\n" +
            "#\n" +
            "#  -> Equals Exactly:\n" +
            "#  When a command equals exactly like the specified here. (Case sensitive)\n" +
            "#  Sample:\n" +
            "#\n" +
            "#Equals Exactly:\n" +
            "#  /warp MALL:\n" +
            "#    Cancellable: true\n" +
            "#    Enabled: true\n" +
            "#    Prevent Other Sounds:\n" +
            "#      Default Sound: true\n" +
            "#      Other Filters: true\n" +
            "#    Sounds:\n" +
            "#      '0':\n" +
            "#        Delay: 1\n" +
            "#        Options:\n" +
            "#          Radius: 0.0\n" +
            "#        Pitch: 2.0\n" +
            "#        Sound: 'BLOCK_PORTAL_TRAVEL'\n" +
            "#        Volume: 0.4\n" +
            "#\n" +
            "#  -> Equals Ignore Case:\n" +
            "#  When a command is equals to the specified but, it doesn't matter if it's on lower case or\n" +
            "# upper case.\n" +
            "#  If a player accidentally toggled upper case on it's keyboard and typed /SPAWN and you want\n" +
            "# to set a sound for the command \"/spawn\", put him in this section so the sound will be played\n" +
            "# even if is on upper case.\n" +
            "#  Sample:\n" +
            "#\n" +
            "#Equals Ignore Case:\n" +
            "#  /spawn:\n" +
            "#    Cancellable: false\n" +
            "#    Enabled: true\n" +
            "#    Prevent Other Sounds:\n" +
            "#      Default Sound: true\n" +
            "#      Other Filters: true\n" +
            "#    Sounds:\n" +
            "#      '1':\n" +
            "#        Delay: 0\n" +
            "#        Options:\n" +
            "#          Radius: 0.0\n" +
            "#        Pitch: 2.0\n" +
            "#        Sound: 'BLOCK_PORTAL_TRAVEL'\n" +
            "#        Volume: 0.4\n" +
            "#\n" +
            "#  -> Starts With:\n" +
            "#  This is the most used of them all. Plays a sound when a command starts with the sentence\n" +
            "# you specify.\n" +
            "#  Sample:\n" +
            "#\n" +
            "#Starts With:\n" +
            "#  /teleport:\n" +
            "#    Cancellable: true\n" +
            "#    Enabled: true\n" +
            "#    Prevent Other Sounds:\n" +
            "#      Default Sound: true\n" +
            "#      Other Filters: true\n" +
            "#    Sounds:\n" +
            "#      '1':\n" +
            "#        Delay: 0\n" +
            "#        Options:\n" +
            "#          Radius: 0.0\n" +
            "#        Pitch: 2.0\n" +
            "#        Sound: 'BLOCK_PORTAL_TRAVEL'\n" +
            "#        Volume: 0.4\n" +
            "#\n" +
            "# More information about sounds on sounds.yml\n" +
            "# The following sounds are here just to prevent the default sound on sounds.yml from playing.\n" +
            "\n" +
            "Version: '" + PlayMoreSoundsVersion.version + "'\n" +
            "\n" +
            "Starts With:\n" +
            "  /tp:\n" +
            "    Cancellable: false\n" +
            "    Enabled: true\n" +
            "    Prevent Other Sounds:\n" +
            "      Default Sound: true\n" +
            "      Other Filters: true\n" +
            "  /warp:\n" +
            "    Cancellable: false\n" +
            "    Enabled: true\n" +
            "    Prevent Other Sounds:\n" +
            "      Default Sound: true\n" +
            "      Other Filters: true\n" +
            "  /spawn:\n" +
            "    Cancellable: false\n" +
            "    Enabled: true\n" +
            "    Prevent Other Sounds:\n" +
            "      Default Sound: true\n" +
            "      Other Filters: true\n" +
            "  /gamemode:\n" +
            "    Cancellable: false\n" +
            "    Enabled: true\n" +
            "    Prevent Other Sounds:\n" +
            "      Default Sound: true\n" +
            "      Other Filters: true\n" +
            "\n" +
            "Contains SubString:\n" +
            "  play:\n" +
            "    Cancellable: false\n" +
            "    Enabled: true\n" +
            "    Prevent Other Sounds:\n" +
            "      Default Sound: true\n" +
            "      Other Filters: true", StaticFields.version3_2_0),
    CONFIG(PlayMoreSoundsCore.getFolder().resolve("config.yml"), "####################################################################################################\n" +
            "##  PlayMoreSounds Configuration v" + PlayMoreSoundsVersion.version + "\n" +
            "##\n" +
            "## PlayMoreSounds configuration about general stuff like commands and regions, if you are looking\n" +
            "##for event sounds, check sounds.yml. If you are looking for situational sounds, check the folder\n" +
            "##'Sounds'.\n" +
            "##\n" +
            "## If you have any questions about PlayMoreSounds join the discord: https://discord.gg/eAHPbc3\n" +
            "####################################################################################################\n" +
            "\n" +
            "# The version of this configuration, each configuration has one of this. If the version is too old\n" +
            "#the configuration will be renamed to \"outdated config.yml\" and a new one will be generated so you\n" +
            "#don't lose your properties and can update each property manually.\n" +
            "Version: '" + PlayMoreSoundsVersion.version + "'\n" +
            "\n" +
            "# Available languages: EN_US, ES_LA, PT_BR, ZH_CN\n" +
            "Language Locale: EN_US\n" +
            "\n" +
            "# All playing sounds will be logged to console with their location.\n" +
            "Debug: false\n" +
            "\n" +
            "# Should the sounds disabled by \"/pms toggle\" be re-enabled on login?\n" +
            "Enable Sounds On Login: false\n" +
            "\n" +
            "# A simple halloween event, disable if you find it annoying.\n" +
            "Halloween Event: true\n" +
            "\n" +
            "Resource Packs:\n" +
            "  # Request player to download a resource pack on join.\n" +
            "  Request: false\n" +
            "  # The URL of the resource pack. Must be a direct link.\n" +
            "  URL: ''\n" +
            "  # If a player denies the download, this player will be kicked immediately.\n" +
            "  Force:\n" +
            "    Enabled: false\n" +
            "    # Should the player be kicked even if the download of the resource pack is accepted but fails?\n" +
            "    Even If Download Fail: false\n" +
            "\n" +
            "# Sound Regions configuration:\n" +
            "Sound Regions:\n" +
            "  # The border particles of \"/pms region info\"\n" +
            "  Border:\n" +
            "    # A limit of how many players can see borders at once, for performance.\n" +
            "    Max Showing Borders: 30\n" +
            "    # The time in ticks of how long the border should be shown.\n" +
            "    Showing Time: 140\n" +
            "  # The max area in m³ a region can have.\n" +
            "  # Use permission 'playmoresounds.region.create.unlimited.area' to bypass.\n" +
            "  Max Area: 15625\n" +
            "  # The max characters a region name can have.\n" +
            "  Max Name Characters: 20\n" +
            "  # The max regions a single player can have.\n" +
            "  # Use permission 'playmoresounds.region.create.unlimited.regions' to bypass.\n" +
            "  Max Regions: 5\n" +
            "  # The region selection tool properties.\n" +
            "  Wand:\n" +
            "    Name: '&6&l&nRegion Selection Tool'\n" +
            "    Glowing: true\n" +
            "    Material: FEATHER\n" +
            "\n" +
            "# The worlds that sounds should not play.\n" +
            "#\n" +
            "# Example:\n" +
            "#World Black List:\n" +
            "#- 'world_nether'\n" +
            "#- 'world_the_end'\n" +
            "#\n" +
            "# All sounds played by PlayMoreSounds will respect this, even plugins that hook to PlayMoreSounds to\n" +
            "#play sounds.\n" +
            "World Black List: []\n" +
            "\n" +
            "# Update scheduler\n" +
            "Updater:\n" +
            "  Enabled: true\n" +
            "  # If false updater messages will only be logged when an update is available.\n" +
            "  Log: false\n" +
            "  # Updates will be checked every 144000 ticks or 1 hour.\n" +
            "  Period: 144000\n" +
            "\n" +
            "# Properties of \"/pms list\" command.\n" +
            "List:\n" +
            "  Default:\n" +
            "    Alternate Color: '&8'\n" +
            "    Color: '&e'\n" +
            "    Separator: '&f, '\n" +
            "    Max Per Page: 10\n" +
            "  # Properties of the sound list GUI.\n" +
            "  Inventory:\n" +
            "    Next Page Item:\n" +
            "      Material: SPECTRAL_ARROW\n" +
            "      Glowing: false\n" +
            "    Stop Sound Item:\n" +
            "      Material: BARRIER\n" +
            "      Glowing: true\n" +
            "    Previous Page Item:\n" +
            "      Material: SPECTRAL_ARROW\n" +
            "      Glowing: false\n" +
            "    Sound Item:\n" +
            "      # The item will be picked randomly from this list for each sound.\n" +
            "      Material:\n" +
            "      - MUSIC_DISC_13\n" +
            "      - MUSIC_DISC_CAT\n" +
            "      - MUSIC_DISC_CHIRP\n" +
            "      - MUSIC_DISC_BLOCKS\n" +
            "      - MUSIC_DISC_FAR\n" +
            "      - MUSIC_DISC_MALL\n" +
            "      - MUSIC_DISC_MELLOHI\n" +
            "      - MUSIC_DISC_STAL\n" +
            "      - MUSIC_DISC_STRAD\n" +
            "      - MUSIC_DISC_WARD\n" +
            "      - MUSIC_DISC_WAIT\n" +
            "      Glowing: false\n" +
            "    # How many rows of sounds should there be per page.\n" +
            "    Rows Per Page: 4", StaticFields.version4_0_0),
    CUSTOM_DISCS(StaticFields.sounds.resolve("custom discs.yml"), "# Set a sound to play when a player clicks at a jukebox with a specific item.\n" +
            "#\n" +
            "# Warnings: \n" +
            "#   >> You must be on version 1.14+!\n" +
            "#   >> Players must have the permission 'playmoresounds.disc.use'.\n" +
            "#   >> Delayed sounds will not stop when the disc is removed.\n" +
            "#   >> For performance reasons, the sound will only play if you have only 1 disc in your hand.\n" +
            "#   >> When the disc is removed the sound will only stop for the player who removed it,\n" +
            "#   meaning if the sound has a radius the sound will not be stopped to the players in\n" +
            "#   the radius.\n" +
            "#\n" +
            "# To set a sound, just create a configuration section with an id and set the item name,\n" +
            "# material and lore or just copy the sample.\n" +
            "# Item material list: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html\n" +
            "#\n" +
            "# Usage In-Game: \n" +
            "#   Get the disc with the command '/pms disc <id>'\n" +
            "#   Click on a jukebox with one of the discs that you set here to play the sound.\n" +
            "#\n" +
            "# Sample:\n" +
            "# (Take a note that this is a sample and the sounds and items may not be available on\n" +
            "# your MC version.)\n" +
            "#\n" +
            "#PLING_DISC: # This is the ID of the custom disc. Here I named this disc PLING_DISC. Disc IDs can not have spaces.\n" +
            "#  Enabled: true\n" +
            "#  Item:\n" +
            "#    Material: GOLDEN_APPLE # The material of the custom disc item.\n" +
            "#    Name: '&2&lPling Disc' # The name of the custom disc item.\n" +
            "#    Lore: 'Different pitched pling sounds!' # The lore of the custom disc item. Use <line> to break a line.\n" +
            "#    Glowing: true # If this disc should glow.\n" +
            "#  Sounds: # The sounds to play when a player uses this disc.\n" +
            "#    '0':\n" +
            "#      Delay: 0\n" +
            "#      Options:\n" +
            "#        Radius: 20.0\n" +
            "#      Pitch: 1.0\n" +
            "#      Sound: BLOCK_NOTE_BLOCK_PLING\n" +
            "#      Volume: 10.0\n" +
            "#    '1':\n" +
            "#      Delay: 20\n" +
            "#      Options:\n" +
            "#        Radius: 20.0\n" +
            "#      Pitch: 2.0\n" +
            "#      Sound: BLOCK_NOTE_BLOCK_PLING\n" +
            "#      Volume: 10.0\n" +
            "#    '2':\n" +
            "#      Delay: 40\n" +
            "#      Options:\n" +
            "#        Radius: 20.0\n" +
            "#      Pitch: 0.0\n" +
            "#      Sound: BLOCK_NOTE_BLOCK_PLING\n" +
            "#      Volume: 10.0\n" +
            "#\n" +
            "# More information about sounds on sounds.yml\n" +
            "\n" +
            "Version: '" + PlayMoreSoundsVersion.version + "'", StaticFields.version3_3_0),
    DEATH_TYPES(StaticFields.sounds.resolve("death types.yml"), "# Set a sound to play when a player die for a specific cause of death.\n" +
            "# Warning >> This setting only works for 1.14+!\n" +
            "#\n" +
            "# To set a sound, just create a configuration section with the name of the cause of\n" +
            "# death or just copy the sample below.\n" +
            "# Causes of death: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/entity/EntityDamageEvent.DamageCause.html\n" +
            "#\n" +
            "# Sample:\n" +
            "# (Take a note that this is a sample and the sounds and causes of death may not be\n" +
            "# available on your MC version.)\n" +
            "#\n" +
            "#MAGIC:\n" +
            "#  Enabled: true\n" +
            "#  #This should stop the sound set in sounds.yml\n" +
            "#  Prevent Default Sound: true # This will prevent the default sound set on sounds.yml from playing.\n" +
            "#  Sounds:\n" +
            "#    #This should play for players who has a specific vip perm.\n" +
            "#    '0':\n" +
            "#      Delay: 0\n" +
            "#      Options:\n" +
            "#        Permission Required: 'vip.customdeathsound.magic'\n" +
            "#        Radius: 5.5\n" +
            "#        Relative Location:\n" +
            "#          BACK: 2.0\n" +
            "#          UP: 1.0\n" +
            "#      Pitch: 1.0\n" +
            "#      Sound: ENTITY_WITHER_DEATH\n" +
            "#      Volume: 1.0\n" +
            "#    #Since this event should stop the regular death sound for whoever dies by magic,\n" +
            "#    #another sound need to be set so players that aren't vip can hear the regular.\n" +
            "#    '0':\n" +
            "#      Delay: 0\n" +
            "#      Options:\n" +
            "#        Permission Required: 'player.everyplayerexceptvipshavethispermission'\n" +
            "#        Radius: 0.0\n" +
            "#      Pitch: 1.0\n" +
            "#      Sound: ENTITY_WITHER_SPAWN\n" +
            "#      Volume: 1.0\n" +
            "#\n" +
            "# More information about sounds on sounds.yml\n" +
            "\n" +
            "Version: '" + PlayMoreSoundsVersion.version + "'", StaticFields.version3_2_0),
    GAME_MODES(StaticFields.sounds.resolve("game modes.yml"), "# Set a sound to play when you change your gamemode.\n" +
            "#\n" +
            "# To set a sound, just create a configuration section with the name of the game mode\n" +
            "# or just copy the sample below.\n" +
            "# Game mode list: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/GameMode.html\n" +
            "#\n" +
            "# Sample:\n" +
            "# (Take a note that this is a sample and the sounds and game modes may not be available\n" +
            "# on your MC version.)\n" +
            "#\n" +
            "#CREATIVE: # The gamemode that you changed to.\n" +
            "#  Cancellable: true\n" +
            "#  Enabled: true\n" +
            "#  Prevent Default Sound: true # This will prevent the default sound set on sounds.yml from playing.\n" +
            "#  Sounds:\n" +
            "#    '0':\n" +
            "#      Delay: 0\n" +
            "#      Options:\n" +
            "#        Radius: 0\n" +
            "#      Pitch: 1\n" +
            "#      Sound: BLOCK_NOTE_BLOCK_PLING\n" +
            "#      Volume: 10\n" +
            "#\n" +
            "# This is a small sample. You can add more gamemodes and more options\n" +
            "# to the sound options.\n" +
            "# More information about sounds on sounds.yml.\n" +
            "\n" +
            "Version: '" + PlayMoreSoundsVersion.version + "'", StaticFields.version3_2_0),
    HIT_SOUNDS(StaticFields.sounds.resolve("hit sounds.yml"), "# Set a sound to play when an entity hits another entity with a specific item on hand.\n" +
            "#\n" +
            "# Bukkit entity names: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html\n" +
            "# Bukkit item names: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html\n" +
            "#\n" +
            "# You need to write when the sound will be played. To do that you need to respect the following pattern:\n" +
            "# <damager> hit <victim> holding <item>\n" +
            "# The sound will be played when the damager hit the victim with the item.\n" +
            "#\n" +
            "# After you've chosen the entities and items and put them into the pattern, create a section with your\n" +
            "#condition like the one below.\n" +
            "#\n" +
            "#PLAYER hit ZOMBIE holding IRON_SWORD: # This sound will play when a player hits a zombie holding an iron sword.\n" +
            "#  Enabled: true\n" +
            "#  Cancellable: true\n" +
            "#  Prevent Other Sounds:\n" +
            "#    Default Sound: true # This will prevent the default sound set on sounds.yml from playing.\n" +
            "#    Other Conditions: true # If the hit event matches more than one condition, this will make so this is the only condition that will play a sound.\n" +
            "#  Sounds:\n" +
            "#    '0':\n" +
            "#      Delay: 10\n" +
            "#      Options:\n" +
            "#        Radius: 16.0\n" +
            "#      Pitch: 2.0\n" +
            "#      Sound: 'ENTITY_ZOMBIE_ATTACK_IRON_DOOR'\n" +
            "#      Volume: 1.0\n" +
            "#\n" +
            "# The pattern also supports criteria, like the ones found on commands.yml, chat sounds.yml, item clicked.yml, items held.yml and items swung.yml.\n" +
            "# You have the following criteria: Any, Contains[], EndsWith[], Equals[], and StartsWith[].\n" +
            "#\n" +
            "# Examples:\n" +
            "#\n" +
            "#   If I want to play a sound when any kind of zombie hits any entity with any item, I would use the condition:\n" +
            "#   Contains[ZOMBIE] hit Any holding Any\n" +
            "#\n" +
            "#   If I want to play a sound when a player hits any entity with any kind of sword, I would use the condition:\n" +
            "#   PLAYER hit Any holding EndsWith[SWORD]\n" +
            "#\n" +
            "#   If I want to play a sound when a player hits any entity with any diamond item, I would use the condition:\n" +
            "#   PLAYER hit Any holding StartsWith[DIAMOND]\n" +
            "#\n" +
            "# You can also use commas if you want to play the same sound for many criteria.\n" +
            "#\n" +
            "# Examples:\n" +
            "#\n" +
            "#   If I want to play the same sound when a player OR a zombie hits any entity with any item, I would use the condition:\n" +
            "#   Equals[PLAYER,ZOMBIE] hit Any holding Any\n" +
            "#\n" +
            "#   If I want to play the same sound when any entity hits any kind of cow (Mushroom or not) or any kind of pig (Zombie or not) with any item, I would use the condition:\n" +
            "#   Any hit Contains[COW,PIG] holding Any\n" +
            "#\n" +
            "#   If I want to play the same sound when a player hits any entity with any kind of sword, shovel or pickaxe, I would use the condition:\n" +
            "#   PLAYER hit Any EndsWith[SWORD,SHOVEL,PICKAXE]\n" +
            "#\n" +
            "# Hope everything is clear, if you have any doubts of a condition that you wanna use but can't find how, contact me on discord:\n" +
            "# https://discord.gg/eAHPbc3\n" +
            "\n" +
            "Version: '" + PlayMoreSoundsVersion.version + "'", StaticFields.version3_3_0),
    ITEMS_CLICKED(StaticFields.sounds.resolve("items clicked.yml"), "# Set a sound to play when a player clicks on a specific item in an inventory.\n" +
            "#\n" +
            "# To set a sound create a section with the name of the item.\n" +
            "# Bukkit item names: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html\n" +
            "#\n" +
            "#IRON_SWORD: # This sound will play when a player clicks on an iron sword.\n" +
            "#  Enabled: true\n" +
            "#  Cancellable: true\n" +
            "#  Prevent Other Sounds:\n" +
            "#    Default Sound: true # This will prevent the default Inventory Click sound set on sounds.yml from playing.\n" +
            "#    Other Criteria: true # If the click event matches more than one criteria, this will prevent the others from playing.\n" +
            "#  Sounds:\n" +
            "#    '0':\n" +
            "#      Delay: 0\n" +
            "#      Options:\n" +
            "#        Radius: 0.0\n" +
            "#      Pitch: 2.0\n" +
            "#      Sound: 'ENTITY_ZOMBIE_ATTACK_IRON_DOOR'\n" +
            "#      Volume: 1.0\n" +
            "#\n" +
            "# Items support criteria, like the ones found on commands.yml, chat sounds.yml, hit sounds.yml, items held.yml and items swung.yml.\n" +
            "# You have the following criteria: Contains[], EndsWith[], Equals[], and StartsWith[].\n" +
            "#\n" +
            "# Examples:\n" +
            "#\n" +
            "#   If I want to play a sound for any kind of coral:\n" +
            "#   Contains[CORAL]\n" +
            "#\n" +
            "#   If I want to play a sound for any kind of sword:\n" +
            "#   EndsWith[SWORD]\n" +
            "#\n" +
            "#   If I want to play a sound for any diamond item:\n" +
            "#   StartsWith[DIAMOND]\n" +
            "#\n" +
            "# You can also use commas if you want to play the same sound for many criteria.\n" +
            "#\n" +
            "# Examples:\n" +
            "#\n" +
            "#   If I want to play the same sound wools and carpets:\n" +
            "#   Contains[WOOL,CARPET]\n" +
            "#\n" +
            "#   If I want to play the same sound for glass and glass panes:\n" +
            "#   EndsWith[GLASS,GLASS_PANE]\n" +
            "#\n" +
            "#   If I want to play the same sound for furnaces and blast furnaces:\n" +
            "#   Equals[FURNACE,BLAST_FURNACE]\n" +
            "#\n" +
            "#   If I want to play the same sound for iron and diamond items:\n" +
            "#   StartsWith[IRON,DIAMOND]\n" +
            "#\n" +
            "# Hope everything is clear, if you have any doubts of a criteria that you wanna use but can't find how, contact me on discord:\n" +
            "# https://discord.gg/eAHPbc3\n" +
            "\n" +
            "Version: '" + PlayMoreSoundsVersion.version + "'", StaticFields.version3_3_0),
    ITEMS_HELD(StaticFields.sounds.resolve("items held.yml"), "# Set a sound to play when a player holds a specific item in their hand.\n" +
            "#\n" +
            "# To set a sound create a section with the name of the item.\n" +
            "# Bukkit item names: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html\n" +
            "#\n" +
            "#IRON_SWORD: # This sound will play when a player holds an iron sword.\n" +
            "#  Enabled: true\n" +
            "#  Cancellable: true\n" +
            "#  Prevent Other Sounds:\n" +
            "#    Default Sound: true # This will prevent the default Change Held Item sound set on sounds.yml from playing.\n" +
            "#    Other Criteria: true # If the item held event matches more than one criteria, this will prevent the others from playing.\n" +
            "#  Sounds:\n" +
            "#    '0':\n" +
            "#      Delay: 0\n" +
            "#      Options:\n" +
            "#        Radius: 0.0\n" +
            "#      Pitch: 2.0\n" +
            "#      Sound: 'ENTITY_ZOMBIE_ATTACK_IRON_DOOR'\n" +
            "#      Volume: 1.0\n" +
            "#\n" +
            "# Items support criteria, like the ones found on commands.yml, chat sounds.yml, hit sounds.yml, items clicked.yml and items swung.yml.\n" +
            "# You have the following criteria: Contains[], EndsWith[], Equals[], and StartsWith[].\n" +
            "#\n" +
            "# Examples:\n" +
            "#\n" +
            "#   If I want to play a sound for any kind of coral:\n" +
            "#   Contains[DIAMOND]\n" +
            "#\n" +
            "#   If I want to play a sound for any kind of sword:\n" +
            "#   EndsWith[SWORD]\n" +
            "#\n" +
            "#   If I want to play a sound for any diamond item:\n" +
            "#   StartsWith[DIAMOND]\n" +
            "#\n" +
            "# You can also use commas if you want to play the same sound for many criteria.\n" +
            "#\n" +
            "# Examples:\n" +
            "#\n" +
            "#   If I want to play the same sound wools and carpets:\n" +
            "#   Contains[WOOL,CARPET]\n" +
            "#\n" +
            "#   If I want to play the same sound for glass and glass panes:\n" +
            "#   EndsWith[GLASS,GLASS_PANE]\n" +
            "#\n" +
            "#   If I want to play the same sound for furnaces and blast furnaces:\n" +
            "#   Equals[FURNACE,BLAST_FURNACE]\n" +
            "#\n" +
            "#   If I want to play the same sound for iron and diamond items:\n" +
            "#   StartsWith[IRON,DIAMOND]\n" +
            "#\n" +
            "# Hope everything is clear, if you have any doubts of a criteria that you wanna use but can't find how, contact me on discord:\n" +
            "# https://discord.gg/eAHPbc3\n" +
            "\n" +
            "Version: '" + PlayMoreSoundsVersion.version + "'", StaticFields.version3_3_0),
    ITEMS_SWUNG(StaticFields.sounds.resolve("items swung.yml"), "# Set a sound to play when a player swings a specific item with their hand.\n" +
            "#\n" +
            "# To set a sound create a section with the name of the item.\n" +
            "# Bukkit item names: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html\n" +
            "#\n" +
            "#IRON_SWORD: # This sound will play when a player swings an iron sword.\n" +
            "#  Enabled: true\n" +
            "#  Cancellable: true\n" +
            "#  Prevent Other Sounds:\n" +
            "#    Default Sound: true # This will prevent the default Player Swing sound set on sounds.yml from playing.\n" +
            "#    Other Criteria: true # If the hand swing event matches more than one criteria, this will prevent the others from playing.\n" +
            "#  Sounds:\n" +
            "#    '0':\n" +
            "#      Delay: 0\n" +
            "#      Options:\n" +
            "#        Radius: 0.0\n" +
            "#      Pitch: 2.0\n" +
            "#      Sound: 'ENTITY_ZOMBIE_ATTACK_IRON_DOOR'\n" +
            "#      Volume: 1.0\n" +
            "#\n" +
            "# Items support criteria, like the ones found on commands.yml, chat sounds.yml, hit sounds.yml, items clicked.yml and items held.yml.\n" +
            "# You have the following criteria: Contains[], EndsWith[], Equals[], and StartsWith[].\n" +
            "#\n" +
            "# Examples:\n" +
            "#\n" +
            "#   If I want to play a sound for any kind of coral:\n" +
            "#   Contains[DIAMOND]\n" +
            "#\n" +
            "#   If I want to play a sound for any kind of sword:\n" +
            "#   EndsWith[SWORD]\n" +
            "#\n" +
            "#   If I want to play a sound for any diamond item:\n" +
            "#   StartsWith[DIAMOND]\n" +
            "#\n" +
            "# You can also use commas if you want to play the same sound for many criteria.\n" +
            "#\n" +
            "# Examples:\n" +
            "#\n" +
            "#   If I want to play the same sound wools and carpets:\n" +
            "#   Contains[WOOL,CARPET]\n" +
            "#\n" +
            "#   If I want to play the same sound for glass and glass panes:\n" +
            "#   EndsWith[GLASS,GLASS_PANE]\n" +
            "#\n" +
            "#   If I want to play the same sound for furnaces and blast furnaces:\n" +
            "#   Equals[FURNACE,BLAST_FURNACE]\n" +
            "#\n" +
            "#   If I want to play the same sound for iron and diamond items:\n" +
            "#   StartsWith[IRON,DIAMOND]\n" +
            "#\n" +
            "# Hope everything is clear, if you have any doubts of a criteria that you wanna use but can't find how, contact me on discord:\n" +
            "# https://discord.gg/eAHPbc3\n" +
            "\n" +
            "Version: '" + PlayMoreSoundsVersion.version + "'", StaticFields.version3_3_0),
    //TODO: Redo Languages
    LANGUAGE_EN_US(StaticFields.lang.resolve("Language EN-US.yml"), "#Language EN-US\n" +
            "Version: '" + PlayMoreSoundsVersion.version + "'\n" +
            "\n" +
            "Confirm:\n" +
            "  Error:\n" +
            "    Nothing Pending: '&cThere is nothing pending to confirm.'\n" +
            "  List:\n" +
            "    Confirmation: ' &f<id> &7- <description>'\n" +
            "    Header: '&8List of pending confirmations:'\n" +
            "\n" +
            "Description:\n" +
            "  Header: '&6&m------------&6[&9PlayMoreSounds v<version>&6]&m------------'\n" +
            "  Help: '&6Type \"&7&n/<label> help&6\" to see the list of commands.'\n" +
            "  No Permission: '&6You don''t have permission to use any commands.'\n" +
            "\n" +
            "Disc:\n" +
            "  Error:\n" +
            "    Not Found: '&cA disc with the ID \"&7<id>&c\" was not found.'\n" +
            "  Success: '&7Giving the disc &f<id>&7 to &f<target>&7.'\n" +
            "\n" +
            "Editor:\n" +
            "  GUI:\n" +
            "    Rich Sound:\n" +
            "      Default:\n" +
            "        Cancellable:\n" +
            "          Display Name: '&c&lCancellable'\n" +
            "          Lore: '&d<value>'\n" +
            "        Done:\n" +
            "          Display Name: '&2&lDone'\n" +
            "          Lore: ' Click to save changes.'\n" +
            "        Loop Period:\n" +
            "          Display Name: '&f&l<period>'\n" +
            "          Lore: ' This sound will play every<line> <period> ticks.'\n" +
            "        Loop Start Delay:\n" +
            "          Display Name: '&f&l<delay>'\n" +
            "          Lore: ' This sound will wait <delay> ticks<line> before start looping.'\n" +
            "        Name:\n" +
            "          Display Name: '&8&lName: &7&l<name>'\n" +
            "          Lore: ' Click to set the name<line> of this sound.'\n" +
            "        Sound:\n" +
            "          Display Name: '&3&l<id>'\n" +
            "          Lore: Click to edit this sound.\n" +
            "        Title: '&6Editing &e<name>&6 sound'\n" +
            "      General:\n" +
            "        Add New Sound:\n" +
            "          Display Name: '&e&lAdd new sound'\n" +
            "          Lore: ' Click to add a new sound.'\n" +
            "        Cancel:\n" +
            "          Display Name: '&4&lCancel'\n" +
            "          Lore: ' Discard everything.'\n" +
            "        Disabled:\n" +
            "          Display Name: '&4&lDisabled'\n" +
            "          Lore: ' This sound is disabled.<line> Click to enable.'\n" +
            "        Enabled:\n" +
            "          Display Name: '&2&lEnabled'\n" +
            "          Lore: ' This sound is enabled.<line> Click to disable.'\n" +
            "        Next Page:\n" +
            "          Display Name: '&7&lNext Page'\n" +
            "          Lore: ' Click to see more sounds.'\n" +
            "        Previous Page:\n" +
            "          Display Name: '&7&lPrevious Page'\n" +
            "          Lore: ' Click to go to the previous page.'\n" +
            "        Separator:\n" +
            "          Display Name: '&b&lSounds'\n" +
            "      New:\n" +
            "        Cancellable:\n" +
            "          Display Name: '&c&lCancellable'\n" +
            "          Lore: ' Should this sound be<line> cancellable by other plugins?'\n" +
            "        Done:\n" +
            "          Display Name: '&2&lDone'\n" +
            "          Lore: ' Click to cache this sound.'\n" +
            "        Loop Period:\n" +
            "          Display Name: '&f&lLoop Period'\n" +
            "          Lore: ' Click to set the time<line> this sound should wait<line> before\n" +
            "            playing again.'\n" +
            "        Loop Start Delay:\n" +
            "          Display Name: '&f&lLoop Start Delay'\n" +
            "          Lore: ' Click to set up the<line> delay to start the loop.'\n" +
            "        Name:\n" +
            "          Display Name: '&7&lSection'\n" +
            "          Lore: ' Click to set the section<line> of this sound.'\n" +
            "        Title: '&6Create a new sound'\n" +
            "    Sound:\n" +
            "      Default:\n" +
            "        Delay:\n" +
            "          Display Name: '&9&lDelay'\n" +
            "          Lore: ' <delay>'\n" +
            "        Done:\n" +
            "          Display Name: '&2&lDone'\n" +
            "          Lore: ' Click to save changes.'\n" +
            "        Id:\n" +
            "          Display Name: '&8&lId: &7&l<id>'\n" +
            "          Lore: ' Click to change the id.'\n" +
            "        Ignores Toggle:\n" +
            "          Display Name: '&8&lIgnores Toggle'\n" +
            "          Lore: ' &d<value>'\n" +
            "        Permission Required:\n" +
            "          Display Name: '&f&lPermission Required'\n" +
            "          Lore: ' <permissionrequired>'\n" +
            "        Permission To Listen:\n" +
            "          Display Name: '&f&lPermission To Listen'\n" +
            "          Lore: ' <permissiontolisten>'\n" +
            "        Pitch:\n" +
            "          Display Name: '&a&lPitch'\n" +
            "          Lore: ' <pitch>'\n" +
            "        Radius:\n" +
            "          Display Name: '&4&lRadius: &c&l<radius>'\n" +
            "          Lore: ' The range in blocks this<line> sound will be heard.'\n" +
            "        Relative Location:\n" +
            "          Display Name: '&3&lRelative Location'\n" +
            "          Lore: ' The location the sound will<line> be played taking the player<line>\n" +
            "            as the source.<line> <line> &dUp:    &5<up><line> &dDown:  &5<down><line>\n" +
            "            &dFront: &5<front><line> &dBack:  &5<back><line> &dRight: &5<right><line>\n" +
            "            &dLeft:  &5<left><line> <line> Click to change.'\n" +
            "        Sound:\n" +
            "          Display Name: '&6&lSound'\n" +
            "          Lore: ' <sound>'\n" +
            "        Title: '&6Sound: &e<id>'\n" +
            "        Volume:\n" +
            "          Display Name: '&2&lVolume'\n" +
            "          Lore: ' <volume>'\n" +
            "      General:\n" +
            "        Cancel:\n" +
            "          Display Name: '&4&lCancel'\n" +
            "          Lore Back: ' Discard everything and go<line> back to &7<parent>&5&o.'\n" +
            "          Lore: ' Discard everything.'\n" +
            "        Eye Location:\n" +
            "          Display Name: '&e&lEye Location'\n" +
            "          Lore: ' This sound will play in the<line> player''s &feye&5&o location.<line>\n" +
            "            <line> Click to change.'\n" +
            "        Feet Location:\n" +
            "          Display Name: '&e&lFeet Location'\n" +
            "          Lore: ' This sound will play in the<line> player''s &ffeet&5&o location.<line>\n" +
            "            <line> Click to change.'\n" +
            "      New:\n" +
            "        Delay:\n" +
            "          Display Name: '&9&lDelay'\n" +
            "          Lore: ' The time to wait before<line> playing this sound.'\n" +
            "        Done:\n" +
            "          Display Name: '&2&lDone'\n" +
            "          Lore Back: ' Add this sound to &7<parent>&5&o.'\n" +
            "          Lore: ' Add sound.'\n" +
            "        Id:\n" +
            "          Display Name: '&7&lSound Id'\n" +
            "          Lore: ' The id of this sound.'\n" +
            "        Ignores Toggle:\n" +
            "          Display Name: '&8&lIgnores Toggle'\n" +
            "          Lore: ' If this sound should be played<line> even if a player has disabled<line>\n" +
            "            their sounds.'\n" +
            "        Permission Required:\n" +
            "          Display Name: '&f&lPermission Required'\n" +
            "          Lore: ' Only those who have this<line> permission will play this sound.'\n" +
            "        Permission To Listen:\n" +
            "          Display Name: '&f&lPermission To Listen'\n" +
            "          Lore: ' Only those who have this<line> permission will hear this sound.'\n" +
            "        Pitch:\n" +
            "          Display Name: '&a&lPitch'\n" +
            "          Lore: ' The pitch of the sound.'\n" +
            "        Radius:\n" +
            "          Display Name: '&4&lRadius'\n" +
            "          Lore: ' To who this sound should be played:<line> <line> &60  »&e Yourself<line>\n" +
            "            &6-1 »&e Everyone online<line> &6-2 »&e Everyone in the world<line> &61+\n" +
            "            »&e Everyone in this range of blocks'\n" +
            "        Relative Location:\n" +
            "          Display Name: '&3&lRelative Location'\n" +
            "          Lore: ' The location the sound will<line> be played taking the player<line>\n" +
            "            as the source.<line> <line> Click to set.'\n" +
            "        Sound:\n" +
            "          Display Name: '&6&lSound'\n" +
            "          Lore: ' The sound to be played. It can<line> be a resource pack sound.'\n" +
            "        Title: '&6New child sound'\n" +
            "        Volume:\n" +
            "          Display Name: '&2&lVolume'\n" +
            "          Lore: ' The volume of the sound.'\n" +
            "\n" +
            "Finder:\n" +
            "  Back:\n" +
            "    Display Name: '&d&lGo back'\n" +
            "    Lore: ' Click to go back to &7<path>&5&o.'\n" +
            "  Error: '&cSomething went wrong while executing this operation on finder.'\n" +
            "  File:\n" +
            "    Display Name: '&f&l<name>'\n" +
            "    Lore: ' Click to edit or add sounds<line> to &7<name>&5&o.'\n" +
            "  Folder:\n" +
            "    Display Name: '&e&l<name>'\n" +
            "    Lore: ' Click to enter &7<name>&5&o folder.'\n" +
            "  Next Page:\n" +
            "    Display Name: '&7&lNext page'\n" +
            "    Lore: ' Click to go to the next page.'\n" +
            "  Previous Page:\n" +
            "    Display Name: '&7&lPrevious page'\n" +
            "    Lore: ' Click to go to the previous page.'\n" +
            "  Sound:\n" +
            "    Display Name: '&b&l<name>'\n" +
            "    Lore: ' Click to edit the sound &7<name>&5&o.'\n" +
            "  Title:\n" +
            "    Folder: '&6&n<path>&e folder &8[&7<page>&8/&7<totalPages>&8]'\n" +
            "    Sound: '&1&n<path>&9 sounds &8[&7<page>&8/&7<totalPages>&8]'\n" +
            "\n" +
            "General:\n" +
            "  And: and\n" +
            "  Description: description\n" +
            "  Everyone: Everyone\n" +
            "  Id: id\n" +
            "  Invalid Arguments: '&cIncorrect command syntax! Use \"&7/&n<label> <label2> <args>&c\".'\n" +
            "  Name: name\n" +
            "  No Permission: '&4You don''t have permission to do this!'\n" +
            "  Nobody Online: '&cThere are no online players on the server.'\n" +
            "  Not A Number: '&cThe value \"&7<number>&c\" is not a valid number!'\n" +
            "  Not A Player: '&cYou must be a player to do this.'\n" +
            "  Player Not Found: '&cThe player \"&7<player>&c\" was not found.'\n" +
            "  Player: player\n" +
            "  Prefix: '&6[&9PlayMoreSounds&6] '\n" +
            "  Target: target\n" +
            "  Unknown Command: '&cUnknown command. Use \"&7&n/<label> help&c\" to see the list of\n" +
            "    commands available to you.'\n" +
            "  World: world\n" +
            "  You: You\n" +
            "\n" +
            "Help:\n" +
            "  Check: |-\n" +
            "    &e/<label> check [target]\n" +
            "    &7 > Checks if sounds are enabled.\n" +
            "  Confirm: |-\n" +
            "    &e/<label> confirm [id|page]\n" +
            "    &7 > Confirms something.\n" +
            "  Disc: |-\n" +
            "    &e/<label> disc <id> [target]\n" +
            "    &7 > Gives a configured custom disc.\n" +
            "  Header: 'List of PlayMoreSounds commands:'\n" +
            "  Help: |-\n" +
            "    &e/<label> help [command]\n" +
            "    &7 > Shows the description of commands.\n" +
            "  List: |-\n" +
            "    &e/<label> list [page] [--gui]\n" +
            "    &7 > Shows the sounds available on your version.\n" +
            "  Play: |-\n" +
            "    &e/<label> play <sound> [target] [vol] [pitch]\n" +
            "    &7 > Plays a sound.\n" +
            "  Region: |-\n" +
            "    &e/<label> region <create|info|list|remove|rename|set|teleport|wand>\n" +
            "    &7 > Regions command.\n" +
            "  Reload: |-\n" +
            "    &e/<label> reload\n" +
            "    &7 > Reloads configurations and events.\n" +
            "  Stop Sound: |-\n" +
            "    &e/<label> stopsound [target] [sounds]\n" +
            "    &7 > Stops sounds from playing.\n" +
            "  Toggle: |-\n" +
            "    &e/<label> toggle [target] [on|off]\n" +
            "    &7 > Enables or disables sounds from playing.\n" +
            "  Update: |-\n" +
            "    &e/<label> update [download] [--force]\n" +
            "    &7 > Checks and downloads updates.\n" +
            "\n" +
            "List:\n" +
            "  Error:\n" +
            "    Not Exists: '&cThe page &7<page>&c doesn''t exist! Max: <totalpages>.'\n" +
            "  Footer: '&f&l - &aView more sounds with \"&f/&n<label> list <page>&a\"'\n" +
            "  GUI:\n" +
            "    Error:\n" +
            "      Not Supported: '&cSound list GUI only works for version 1.14+'\n" +
            "    Next Page:\n" +
            "      Display Name: '&7&lNext page'\n" +
            "      Lore: ' Click to go to the next page.'\n" +
            "    Previous Page:\n" +
            "      Display Name: '&7&lPrevious page'\n" +
            "      Lore: ' Click to go to the previous page.'\n" +
            "    Sound:\n" +
            "      Display Name: '&d&n<sound>'\n" +
            "      Lore: ' Click to play this sound.'\n" +
            "    Stop Sound:\n" +
            "      Display Name: '&6&lStop Sounds'\n" +
            "      Lore: ' Stop all currently playing sounds.'\n" +
            "    Title: '&8List of sounds, page &c<page>&8 of &c<totalpages>&8'\n" +
            "  Header: '&aList of available sounds [Page <page> of <totalpages>]:'\n" +
            "  Page: page\n" +
            "  Sound Tooltip: '&5Click me to play the sound &d<sound>'\n" +
            "\n" +
            "Play:\n" +
            "  Error:\n" +
            "    Not A Sound: '&cThe section \"&7<section>&c\" in the file &7<file>&c is not a valid\n" +
            "      sound!'\n" +
            "    Unauthorized: '&cYou can''t go in that folder!'\n" +
            "  Pitch: pitch\n" +
            "  Sound: sound\n" +
            "  Success:\n" +
            "    Config: '&7Playing the sound &f<sound>&7 of the file &f<file>&7 to &f<player>&7.'\n" +
            "    Default: '&7Playing the sound &f<sound>&7 with volume &f<volume>&7 and pitch &f<pitch>&7\n" +
            "      to &f<player>&7.'\n" +
            "  Volume: volume\n" +
            "\n" +
            "Region:\n" +
            "  Create:\n" +
            "    Default Description: A sound playing region.\n" +
            "    Error:\n" +
            "      Already Exists: '&cThis name was already taken, chose another.'\n" +
            "      Default: '&cSomething went wrong while creating the region \"&7<name>&c\".'\n" +
            "      Different Worlds: '&cYour selections are in different worlds!'\n" +
            "      Max Area: '&cThe selected area exceeds the maximum of <max> blocks.'\n" +
            "      Max Regions: '&cYou cannot create more than <max> regions.'\n" +
            "      Not Selected: '&cYou did not select positions, type &7&n/<label> <label2> wand&c\n" +
            "        to get the region selection tool.'\n" +
            "    Success: '&aThe region &7<name>&a was created successfully.'\n" +
            "  General:\n" +
            "    Error:\n" +
            "      Illegal Characters: '&cRegion names can only have alpha-numeric characters.'\n" +
            "      Max Name Characters: '&cRegion names cannot be longer than <max> characters.'\n" +
            "      Not Found:\n" +
            "        Name: '&cNo region with that name was found. Type &7/<label> <label2> list&c\n" +
            "          to see the list of regions.'\n" +
            "        UUID: '&cNo region with that uuid was found. Type &7/<label> <label2> list&c\n" +
            "          to see the list of regions.'\n" +
            "      Save: '&cSomething went wrong while saving <name> region.'\n" +
            "  Info:\n" +
            "    Creation Date: '&7Creation Date:&f <date>'\n" +
            "    Description: '&7Description:&f <description>'\n" +
            "    Error:\n" +
            "      No Regions: '&7There are no regions on this location.'\n" +
            "    Header: '&8Information of the region &f<name>&8:'\n" +
            "    Id: '&7UUID:&f <uuid>'\n" +
            "    Owner: '&7Owner:&f <owner>'\n" +
            "    World: '&7World:&f <world>'\n" +
            "  List:\n" +
            "    Error:\n" +
            "      No Regions: '&c<targets> have no regions.'\n" +
            "      Not Exists: '&cThe page &7<page>&c doesn''t exist! Max: <totalPages>.'\n" +
            "    Footer: '&8Type &7/<label> <label2> <label3> <label4> <next>&8 to see more regions.'\n" +
            "    Header:\n" +
            "      Default: '&8Your regions [Page <page> of <totalPages>]:'\n" +
            "      Player: '&8Regions of <targets> [Page <page> of <totalPages>]:'\n" +
            "    Region: '&7- <uuid>: &f<name>'\n" +
            "  Region: region\n" +
            "  Remove:\n" +
            "    Confirm: '&aType &7/<label> confirm&a to confirm the removal of the region &7<region>&a.'\n" +
            "    Description: Delete the region <region>\n" +
            "    Success: '&aThe region &7<region>&a was deleted successfully.'\n" +
            "  Rename:\n" +
            "    Error:\n" +
            "      Already Exists: '&cThe new name was already taken, chose another.'\n" +
            "      Same: '&cThe new name is not different than the previous.'\n" +
            "    New Name: new name\n" +
            "    Success: '&aThe region <region> was renamed to &7<newName>&a.'\n" +
            "  Select:\n" +
            "    Error:\n" +
            "      Overlap: '&cAn already existing region is on that location!'\n" +
            "  Set:\n" +
            "    Description:\n" +
            "      Error:\n" +
            "        Max Characters: '&cRegion descriptions cannot be longer than 100 characters.'\n" +
            "      Success: '&aDescription of <region> region was set to &7<description>&a.'\n" +
            "    Select:\n" +
            "      Error:\n" +
            "        Not A World: '&cThe value &7<value>&c is not a valid world.'\n" +
            "      Position:\n" +
            "        First: '&6First position selected! World: &e<w>&6, X: &e<x>&6, Y: &e<y>&6,\n" +
            "          Z: &e<z>&6.'\n" +
            "        Second: '&6Second position selected! World: &e<w>&6, X: &e<x>&6, Y: &e<y>&6,\n" +
            "          Z: &e<z>&6.'\n" +
            "  Teleport:\n" +
            "    Success: '&aYou were teleported to region <region>.'\n" +
            "  Wand:\n" +
            "    Error:\n" +
            "      Config: '&cYou are missing settings on your configuration. Wand could not be\n" +
            "        given.'\n" +
            "    Success: '&6Selection tool: Left-click selects first position and Right-click\n" +
            "      selects second position.'\n" +
            "\n" +
            "Relative Location Setter:\n" +
            "  Sound Source: '&2&nSound Source'\n" +
            "  Where To Play: '&4&nWhere To Play'\n" +
            "\n" +
            "Reload:\n" +
            "  Error: '&cSomething went wrong while reloading config. PMS must be shut down immediately.'\n" +
            "  Success: '&7Configuration reloaded.'\n" +
            "\n" +
            "Resource Packs:\n" +
            "  Error: '&cSomething went wrong while requesting <player> to download the resource\n" +
            "    pack. Please try another URL.'\n" +
            "  Kick Message: '&cYou must be using the resource pack to play on this server.'\n" +
            "  Request Message: '&ePlease download the resource pack to continue.'\n" +
            "\n" +
            "Stop Sound:\n" +
            "  Success:\n" +
            "    All: '&7Stopped all sounds playing to &f<target>&7.'\n" +
            "    Default: '&7Stopped &f<sounds>&7 sounds playing to &f<target>&7.'\n" +
            "\n" +
            "Toggle:\n" +
            "  Check:\n" +
            "    Disabled:\n" +
            "      Default: '&cYour sounds are disabled.'\n" +
            "      Player: '&cSounds of &f<target>&c are disabled.'\n" +
            "    Enabled:\n" +
            "      Default: '&aYour sounds are enabled.'\n" +
            "      Player: '&aSounds of &f<target>&a are enabled.'\n" +
            "  Disabled:\n" +
            "    Default: '&cYour sounds were toggled to off!'\n" +
            "    Player: '&cToggled the sounds of &f<target>&c to off!'\n" +
            "  Enabled:\n" +
            "    Default: '&aYour sounds were toggled to on!'\n" +
            "    Player: '&aToggled the sounds of &f<target>&a to on!'\n" +
            "\n" +
            "Update:\n" +
            "  Available: '&2PlayMoreSounds v<version> is available. Type &7/<label> update download&2\n" +
            "    to download it.'\n" +
            "  Check: '&eChecking for updates...'\n" +
            "  Download:\n" +
            "    Default: '&eDownloading update...'\n" +
            "    Error: '&7An update was already downloaded.'\n" +
            "    Latest: '&eDownloading latest version...'\n" +
            "    Lower: '&7Warning: the downloaded version is lower than the current running version.'\n" +
            "    Success: '&7PlayMoreSounds v<version> was downloaded successfully.'\n" +
            "  Error:\n" +
            "    Default: '&cSomething went wrong while using updater.'\n" +
            "    Offline: '&cYou are offline or spigot.org is down.'\n" +
            "    Timeout: '&cTook too long to establish a connection.'\n" +
            "  Not Available: '&eNo updates available.'", StaticFields.version3_3_0),
    LANGUAGE_ES_LA(StaticFields.lang.resolve("Language ES-LA.yml"), "", StaticFields.version3_3_0),
    LANGUAGE_PT_BR(StaticFields.lang.resolve("Language PT-BR.yml"), "", StaticFields.version3_3_0),
    LANGUAGE_ZH_CN(StaticFields.lang.resolve("Language ZH-CN.yml"), "", StaticFields.version3_3_0),
    NATURE_SOUND_REPLACER(StaticFields.sounds.resolve("nature sound replacer.yml"), "# Replace any sound played by nature in your server.\n" +
            "#\n" +
            "#  When a sound here is played, PlayMoreSounds interrupts the sound packets from being sent to the\n" +
            "# players and plays the sound set here instead. This way you can take advantage of PlayMoreSounds\n" +
            "# features, like play multiple sounds, delayed sounds, toggleable sounds, permissible sounds,\n" +
            "# resource pack sounds etc.\n" +
            "#\n" +
            "# Warnings:\n" +
            "# >> ProtocolLib is required for this feature to work.\n" +
            "# >> Only sounds played by the server are replaceable, sounds played to the client (like walking or\n" +
            "# building) are replaceable only if the source is another player that's not you.\n" +
            "#\n" +
            "#  To replace a sound, create a section with the sound name and set the replacing sound\n" +
            "# in it, for example:\n" +
            "#\n" +
            "#ENTITY_ZOMBIE_HURT: # This is the sound that I want to replace.\n" +
            "#  Enabled: true\n" +
            "#  Sounds: # The sounds that will play instead.\n" +
            "#    '0':\n" +
            "#      Delay: 0\n" +
            "#      Options:\n" +
            "#        Ignores Disabled: true\n" +
            "#        #Permission Required: '' # Permission Required is available but it's not recommended, use Permission To Listen instead.\n" +
            "#        Permission To Listen: 'listen.zombiehurt'\n" +
            "#        Radius: 0.0 # Radius > 0 is not recommended\n" +
            "#        Relative Location:\n" +
            "#          FRONT_BACK: 0.0\n" +
            "#          RIGHT_LEFT: 0.0\n" +
            "#          UP_DOWN: 0.0\n" +
            "#      Pitch: 0.5\n" +
            "#      Sound: ENTITY_SKELETON_HURT\n" +
            "#      Volume: 1.0\n" +
            "#\n" +
            "#  If you want to completely stop a sound from being played in your server, add as in the example:\n" +
            "#\n" +
            "#ENTITY_ZOMBIE_AMBIENT: # This is the sound that I want to stop from playing in my server.\n" +
            "#  Enabled: true\n" +
            "#  #Sounds: # Don't add 'Sounds' section since you don't want sounds to play.\n" +
            "#\n" +
            "#  A more in deep tutorial of all sound options can be found in sounds.yml file.\n" +
            "#  If you have any other doubts on how to set this configuration up, feel free to ask in\n" +
            "# PlayMoreSounds' discord: https://discord.gg/eAHPbc3\n" +
            "\n" +
            "Version: '" + PlayMoreSoundsVersion.version + "'", StaticFields.version4_0_0),
    REGIONS(StaticFields.sounds.resolve("regions.yml"), "# Set a sound to play when you enter, exit or stand on a specific region.\n" +
            "#\n" +
            "# Sample:\n" +
            "# (Take a note that this is a sample and the sounds may not be available\n" +
            "# on your MC version.)\n" +
            "#\n" +
            "#PlayMoreSounds: # The region plugin.\n" +
            "#  Spawn: # The region name, replace 'Spawn' with the name of your region.\n" +
            "#    Enter: # When a player enters this region.\n" +
            "#      Cancellable: true\n" +
            "#      Enabled: true\n" +
            "#      Stop On Exit:\n" +
            "#        Enabled: true # If enabled, the sound will be stopped when the player leaves the region.\n" +
            "#        Delay: 20 # The time to wait before stopping the sound.\n" +
            "#      Prevent Default Sound: true # If enabled, Region Enter sound in sounds.yml won't be played.\n" +
            "#      Sounds:\n" +
            "#        '0':\n" +
            "#          Delay: 0\n" +
            "#          Options:\n" +
            "#            Radius: 0\n" +
            "#          Pitch: 1\n" +
            "#          Sound: BLOCK_NOTE_BLOCK_PLING\n" +
            "#          Volume: 10\n" +
            "#    Leave: # When a player exits this region.\n" +
            "#      Cancellable: true\n" +
            "#      Enabled: true\n" +
            "#      Prevent Default Sound: true # If enabled, Region Leave sound in sounds.yml won't be played.\n" +
            "#      Sounds:\n" +
            "#        '0':\n" +
            "#          Delay: 0\n" +
            "#          Options:\n" +
            "#            Radius: 0\n" +
            "#          Pitch: 1\n" +
            "#          Sound: BLOCK_NOTE_BLOCK_BASS\n" +
            "#          Volume: 10\n" +
            "#    Loop: # When a player enters the region, a loop will be triggered and play.\n" +
            "#      Cancellable: true\n" +
            "#      Delay: 0 # Time in ticks to wait to start the loop once the player enters the region.\n" +
            "#      Enabled: true\n" +
            "#      Period: 100 # Time in tick the loop will wait until playing the sound again.\n" +
            "#      # If you have a long song playing, when the player leaves this region, the song\n" +
            "#      #will be stopped instead of playing until the end. This setting applies to sounds only,\n" +
            "#      #the loop function is stopped automatically.\n" +
            "#      Stop On Exit:\n" +
            "#        Delay: 10\n" +
            "#        Enabled: true\n" +
            "#      Prevent Other Sounds:\n" +
            "#        Enter Sound: true # If enabled, Enter sound in regions.yml won't be played.\n" +
            "#        Default Sound: true # If enabled, Region Enter sound in sounds.yml won't be played.\n" +
            "#      Sounds:\n" +
            "#        '0':\n" +
            "#          Delay: 0\n" +
            "#          Options:\n" +
            "#            Radius: 0\n" +
            "#          Pitch: 1\n" +
            "#          Sound: BLOCK_NOTE_BLOCK_BASS\n" +
            "#          Volume: 10\n" +
            "#\n" +
            "# You can only play sounds in PMS native regions. To play to other plugins, search for compatibility\n" +
            "#addons on https://www.spigotmc.org/resources/37429/\n" +
            "#\n" +
            "# More information about sounds on sounds.yml.\n" +
            "\n" +
            "Version: '" + PlayMoreSoundsVersion.version + "'", StaticFields.version3_2_0),
    //TODO: Redo sounds.yml
    SOUNDS(PlayMoreSoundsCore.getFolder().resolve("sounds.yml"), "# Set a sound to play when a player triggers an event.\n" +
            "#\n" +
            "# To set a sound to be played when a player triggers a event, create a\n" +
            "# section with the name of the event and add the sound options.\n" +
            "#\n" +
            "# -> Event and multiple sound sample:\n" +
            "#\n" +
            "#Teleport: # The event that when triggered a sound will be played.\n" +
            "#  # Cancellable stops the sound from playing when another plugin cancels\n" +
            "#  #the event. This boolean helps events to be compatible with other\n" +
            "#  #plugins.\n" +
            "#  Cancellable: true\n" +
            "#  # Tell if a sound should be played or not. In sounds.yml case, this\n" +
            "#  #boolean helps with the server performance.\n" +
            "#  Enabled: true\n" +
            "#  # This is the sound list. Set the sound name by the section name. By\n" +
            "#  #default I left with numbers just to organize things, but it can be\n" +
            "#  #any name you want as long as it respects YAML section name rules.\n" +
            "#  Sounds:\n" +
            "#    '0':\n" +
            "#      # This is the time in ticks the sound will wait before playing.\n" +
            "#      # By default most of them is 0, so the sound plays immediately.\n" +
            "#      Delay: 0\n" +
            "#      # Sounds can have multiple options, all of them are optional.\n" +
            "#      Options:\n" +
            "#        # Even if a player has toggled it's sounds to off, the sound will\n" +
            "#        #be played.\n" +
            "#        Ignores Disabled: true\n" +
            "#        # The sound will be only listened by who have this permission.\n" +
            "#        Permission To Listen: 'pms.listen.playerteleport'\n" +
            "#        # The sound will be only played to who have this permission.\n" +
            "#        Permission Required: 'pms.reproduce.playerteleport'\n" +
            "#        # Set how blocks far the sound will be listenable by players.\n" +
            "#        # To play to everyone in the server, set to -1. To play to\n" +
            "#        #everyone in the world of the player, set to -2. To play to the\n" +
            "#        #player itself, set to 0. To play to players around the player, set\n" +
            "#        #a value greater than 0.\n" +
            "#        Radius: 15.2\n" +
            "#        # You can specify in blocks the location of the sound. The sound\n" +
            "#        #can be played to player's Front, Back, Right, Left, Up and Down.\n" +
            "#        #You can use negative numbers to add to the opposite direction.\n" +
            "#        Relative Location:\n" +
            "#          FRONT_BACK: 1.3\n" +
            "#          RIGHT_LEFT: -0.8\n" +
            "#          UP_DOWN: 0.13\n" +
            "#      # Set how pitchy the sound will be. Values greater than 2 don't have\n" +
            "#      #any difference.\n" +
            "#      Pitch: 1.0\n" +
            "#      # Here you can add a Sound Type or a sound modifier (check below).\n" +
            "#      # Check Sound Type names on https://www.spigotmc.org/resources/37429/\n" +
            "#      # Sound Types are not the same thing as bukkit sounds and sounds can be\n" +
            "#      #available or not depending on your version.\n" +
            "#      Sound: 'BLOCK_NOTE_BLOCK_PLING'\n" +
            "#      # Minecraft volume is the distance the sound can be heard. For the\n" +
            "#      #player who played, volume has only effect when the value is lower than 1.\n" +
            "#      #For near players, volume 1 = 15 blocks.\n" +
            "#      # Volumes may or may not be available depending on your sound modifier.\n" +
            "#      Volume: 0.7\n" +
            "#\n" +
            "# -> Resource pack sound sample:\n" +
            "#\n" +
            "# To play a resource pack sound, simply add the custom sound name into the \"Sound:\"\n" +
            "#setting.\n" +
            "#\n" +
            "#Teleport:\n" +
            "#  Enabled: true\n" +
            "#  Sounds:\n" +
            "#    '0':\n" +
            "#      Delay: 0\n" +
            "#      Options:\n" +
            "#        Radius: 0.0\n" +
            "#      Pitch: 1.0\n" +
            "#      Sound: 'customsoundname'\n" +
            "#      Volume: 10\n" +
            "\n" +
            "Version: '" + PlayMoreSoundsVersion.version + "'\n" +
            "\n" +
            "# When a player lies in bed.\n" +
            "# This sound is cancellable.\n" +
            "Bed Enter:\n" +
            "  Cancellable: true\n" +
            "  Enabled: true\n" +
            "  Sounds:\n" +
            "    '0':\n" +
            "      Delay: 0\n" +
            "      Options:\n" +
            "        Radius: 15.0\n" +
            "      Pitch: 0.65\n" +
            "      Sound: ENTITY_VILLAGER_AMBIENT\n" +
            "      Volume: 0.5\n" +
            "\n" +
            "# When a player gets out of bed.\n" +
            "# This sound is disabled by default. To enable it, copy the options from the sound\n" +
            "#above and set 'Enabled' to true.\n" +
            "# This sound is not cancellable.\n" +
            "Bed Leave:\n" +
            "  Enabled: false\n" +
            "\n" +
            "# When a player changes the item slot of the hotbar.\n" +
            "# This sound is cancellable.\n" +
            "Change Held Item:\n" +
            "  Cancellable: false\n" +
            "  Enabled: true\n" +
            "  Sounds:\n" +
            "    '0':\n" +
            "      Delay: 0\n" +
            "      Options:\n" +
            "        Radius: 0.0\n" +
            "      Pitch: 2.0\n" +
            "      Sound: BLOCK_NOTE_BLOCK_HAT\n" +
            "      Volume: 10.0\n" +
            "\n" +
            "# When a player changes their level of experience.\n" +
            "# This sound is disabled by default. To enable it, copy the options from the sound\n" +
            "#above and set 'Enabled' to true.\n" +
            "# This sound is not cancellable.\n" +
            "Change Level:\n" +
            "  Enabled: false\n" +
            "\n" +
            "# When a player crafts an item.\n" +
            "# This sound is disabled by default. To enable it, copy the options from another sound\n" +
            "#and set 'Enabled' to true.\n" +
            "# This sound is cancellable.\n" +
            "Craft Item:\n" +
            "  Enabled: false\n" +
            "  Cancellable: true\n" +
            "\n" +
            "# When a player drops an item.\n" +
            "# This sound is cancellable.\n" +
            "Drop Item:\n" +
            "  Cancellable: false\n" +
            "  Enabled: true\n" +
            "  Sounds:\n" +
            "    '0':\n" +
            "      Delay: 0\n" +
            "      Options:\n" +
            "        Radius: 15.0\n" +
            "      Pitch: 1.0\n" +
            "      Sound: ENTITY_EGG_THROW\n" +
            "      Volume: 0.5\n" +
            "\n" +
            "# When a player edits or creates a book using a book and quill.\n" +
            "# This sound is cancellable.\n" +
            "Edit Book:\n" +
            "  Cancellable: false\n" +
            "  Enabled: " + (PlayMoreSoundsCore.getServerVersion().compareTo(new Version("1.9")) < 0 ? "false # ITEM_ARMOR_EQUIP_LEATHER is not available in " + PlayMoreSoundsCore.getServerVersion() + " please choose another sound.\n" : "true\n") +
            "  Sounds:\n" +
            "    '0':\n" +
            "      Delay: 0\n" +
            "      Options:\n" +
            "        Radius: 10.0\n" +
            "      Pitch: 2.0\n" +
            "      Sound: ITEM_ARMOR_EQUIP_LEATHER\n" +
            "      Volume: 0.4\n" +
            "\n" +
            "# When an entity is hit by another entity.\n" +
            "# This sound is cancellable.\n" +
            "Entity Hit:\n" +
            "  Cancellable: true\n" +
            "  Enabled: true\n" +
            "  Sounds:\n" +
            "    '0':\n" +
            "      Delay: 0\n" +
            "      Options:\n" +
            "        Radius: 15.0\n" +
            "      Pitch: 2.0\n" +
            "      Sound: ENTITY_GENERIC_HURT\n" +
            "      Volume: 10.0\n" +
            "\n" +
            "# When any entity jumps.\n" +
            "# This sound only plays if you are running PaperMC.\n" +
            "# This sound is disabled by default. To enable it, copy the options from the sound\n" +
            "#above and set 'Enabled' to true.\n" +
            "# This sound is cancellable.\n" +
            "Entity Jump:\n" +
            "  Enabled: false\n" +
            "  Cancellable: true\n" +
            "\n" +
            "# When a player joins the server for the first time.\n" +
            "# This sound is not cancellable.\n" +
            "First Join:\n" +
            "  Enabled: true\n" +
            "  Sounds:\n" +
            "    '0':\n" +
            "      Delay: 0\n" +
            "      Options:\n" +
            "        Radius: -1.0\n" +
            "      Pitch: 2.0\n" +
            "      Sound: BLOCK_NOTE_BLOCK_PLING\n" +
            "      Volume: 10.0\n" +
            "\n" +
            "# When a player extracts something from a furnace.\n" +
            "# This sound is not cancellable.\n" +
            "Furnace Extract:\n" +
            "  Enabled: true\n" +
            "  Sounds:\n" +
            "    '0':\n" +
            "      Delay: 0\n" +
            "      Options:\n" +
            "        Radius: 15.0\n" +
            "      Pitch: 1.3\n" +
            "      Sound: ENTITY_GENERIC_EXTINGUISH_FIRE\n" +
            "      Volume: 0.5\n" +
            "\n" +
            "# When a player changes their game mode.\n" +
            "# This sound is cancellable.\n" +
            "Game Mode Change:\n" +
            "  Cancellable: true\n" +
            "  Enabled: true\n" +
            "  Sounds:\n" +
            "    '0':\n" +
            "      Delay: 0\n" +
            "      Options:\n" +
            "        Radius: 0.0\n" +
            "      Pitch: 1.0\n" +
            "      Sound: BLOCK_ANVIL_LAND\n" +
            "      Volume: 10.0\n" +
            "\n" +
            "# When a player clicks on an inventory.\n" +
            "# This sound is cancellable.\n" +
            "Inventory Click:\n" +
            "  Cancellable: true\n" +
            "  Enabled: true\n" +
            "  Sounds:\n" +
            "    '0':\n" +
            "      Delay: 0\n" +
            "      Options:\n" +
            "        Radius: 0.0\n" +
            "      Pitch: 1.5\n" +
            "      Sound: BLOCK_COMPARATOR_CLICK\n" +
            "      Volume: 0.4\n" +
            "\n" +
            "# When a player closes an inventory.\n" +
            "# This sound is not cancellable.\n" +
            "Inventory Close:\n" +
            "  Enabled: " + (PlayMoreSoundsCore.getServerVersion().compareTo(new Version("1.12")) < 0 ? "false # UI_TOAST_OUT is not available in " + PlayMoreSoundsCore.getServerVersion() + " please choose another sound.\n" : "true\n") +
            "  Sounds:\n" +
            "    '0':\n" +
            "      Delay: 0\n" +
            "      Options:\n" +
            "        Radius: 0.0\n" +
            "      Pitch: 2.0\n" +
            "      Sound: UI_TOAST_OUT\n" +
            "      Volume: 10.0\n" +
            "\n" +
            "# When a player joins the server.\n" +
            "# This sound is not cancellable.\n" +
            "Join Server:\n" +
            "  Enabled: true\n" +
            "  Sounds:\n" +
            "    '0':\n" +
            "      Delay: 0\n" +
            "      Options:\n" +
            "        Radius: -1.0\n" +
            "      Pitch: 1.0\n" +
            "      Sound: BLOCK_NOTE_BLOCK_PLING\n" +
            "      Volume: 10.0\n" +
            "\n" +
            "# When a player leaves the server.\n" +
            "# This sound is not cancellable.\n" +
            "Leave Server:\n" +
            "  Enabled: true\n" +
            "  Sounds:\n" +
            "    '0':\n" +
            "      Delay: 0\n" +
            "      Options:\n" +
            "        Radius: -1.0\n" +
            "      Pitch: 1.0\n" +
            "      Sound: BLOCK_NOTE_BLOCK_BASS\n" +
            "      Volume: 10.0\n" +
            "\n" +
            "# When a player bans another player from the server.\n" +
            "# This sound is not cancellable.\n" +
            "Player Ban:\n" +
            "  Enabled: true\n" +
            "  Sounds:\n" +
            "    '0':\n" +
            "      Delay: 0\n" +
            "      Options:\n" +
            "        Radius: -1.0\n" +
            "      Pitch: 1.3\n" +
            "      Sound: ENTITY_ENDER_DRAGON_DEATH\n" +
            "      Volume: 10.0\n" +
            "\n" +
            "# When a player sends a message on chat.\n" +
            "# This sound is cancellable.\n" +
            "Player Chat:\n" +
            "  Cancellable: true\n" +
            "  Enabled: true\n" +
            "  Sounds:\n" +
            "    '0':\n" +
            "      Delay: 0\n" +
            "      Options:\n" +
            "        Radius: -1.0\n" +
            "      Pitch: 1.0\n" +
            "      Sound: ENTITY_ITEM_PICKUP\n" +
            "      Volume: 10.0\n" +
            "\n" +
            "# When a player dies.\n" +
            "# This sound is not cancellable.\n" +
            "Player Death:\n" +
            "  Enabled: true\n" +
            "  Sounds:\n" +
            "    '0':\n" +
            "      Delay: 0\n" +
            "      Options:\n" +
            "        Radius: 0.0\n" +
            "      Pitch: 1.0\n" +
            "      Sound: ENTITY_WITHER_SPAWN\n" +
            "      Volume: 10.0\n" +
            "\n" +
            "# When a player is kicked from the server.\n" +
            "# This sound is cancellable.\n" +
            "Player Kicked:\n" +
            "  Cancellable: true\n" +
            "  Enabled: true\n" +
            "  Sounds:\n" +
            "    '0':\n" +
            "      Delay: 0\n" +
            "      Options:\n" +
            "        Radius: -1.0\n" +
            "      Pitch: 1.3\n" +
            "      Sound: ENTITY_ENDER_DRAGON_HURT\n" +
            "      Volume: 10.0\n" +
            "\n" +
            "# When a player kills another player. (Damager)\n" +
            "# This sound is disabled by default. To enable it, copy the options from the sound\n" +
            "#above and set 'Enabled' to true.\n" +
            "# This sound is not cancellable.\n" +
            "Player Kill:\n" +
            "  Enabled: false\n" +
            "\n" +
            "# When a player is killed by another player. (Victim)\n" +
            "# This sound only plays if you are running PaperMC.\n" +
            "# This sound is disabled by default. To enable it, copy the options from another sound\n" +
            "#and set 'Enabled' to true.\n" +
            "# This sound is not cancellable.\n" +
            "Player Killed:\n" +
            "  Enabled: false\n" +
            "  # Prevents the default 'Player Death' and sounds from 'death types.yml' from playing.\n" +
            "  Prevent Death Sounds: true\n" +
            "\n" +
            "# When a player jumps.\n" +
            "# This sound only plays if you are running PaperMC.\n" +
            "# This sound is disabled by default. To enable it, copy the options from another sound\n" +
            "#and set 'Enabled' to true.\n" +
            "# This sound is cancellable.\n" +
            "Player Jump:\n" +
            "  Enabled: false\n" +
            "  Cancellable: true\n" +
            "\n" +
            "# When a player swings their hand.\n" +
            "# This sound is disabled by default. To enable it, copy the options from another sound\n" +
            "#and set 'Enabled' to true.\n" +
            "# This sound is cancellable.\n" +
            "Player Swing:\n" +
            "  Enabled: false\n" +
            "  Cancellable: true\n" +
            "\n" +
            "# When a nether portal is opened.\n" +
            "# This sound is cancellable.\n" +
            "Portal Create:\n" +
            "  Cancellable: true\n" +
            "  Enabled: " + (PlayMoreSoundsCore.getServerVersion().compareTo(new Version("1.12")) < 0 ? "false # BLOCK_END_PORTAL_SPAWN is not available in " + PlayMoreSoundsCore.getServerVersion() + " please choose another sound.\n" : "true\n") +
            "  Sounds:\n" +
            "    '0':\n" +
            "      Delay: 0\n" +
            "      Options:\n" +
            "        Radius: 15.0\n" +
            "      Pitch: 0.8\n" +
            "      Sound: BLOCK_END_PORTAL_SPAWN\n" +
            "      Volume: 0.8\n" +
            "\n" +
            "# When a player enters a PlayMoreSounds region.\n" +
            "# This sound can also be played when entering another plugin's region. To do that you\n" +
            "#need to install addons.\n" +
            "# This sound is disabled by default. To enable it, copy the options from the sound\n" +
            "#above and set 'Enabled' to true.\n" +
            "# This sound is cancellable.\n" +
            "# The sound played by entering this region can also be stopped on exit. To do that,\n" +
            "#add the following options:\n" +
            "#\n" +
            "#  Stop On Exit:\n" +
            "#    Enabled: true\n" +
            "#    Delay: 20 # The time in ticks to wait before stopping the sound\n" +
            "Region Enter:\n" +
            "  Enabled: false\n" +
            "  Cancellable: true\n" +
            "\n" +
            "# When a player leaves a PlayMoreSounds region.\n" +
            "# This sound can also be played when leaving another plugin's region. To do that you\n" +
            "#need to install addons.\n" +
            "# This sound is disabled by default. To enable it, copy the options from another sound\n" +
            "#and set 'Enabled' to true.\n" +
            "# This sound is cancellable.\n" +
            "Region Leave:\n" +
            "  Enabled: false\n" +
            "  Cancellable: true\n" +
            "\n" +
            "# When a player respawns.\n" +
            "# This sound is disabled by default. To enable it, copy the options from another sound\n" +
            "#and set 'Enabled' to true.\n" +
            "# This sound is not cancellable.\n" +
            "Respawn:\n" +
            "  Enabled: false\n" +
            "\n" +
            "# When a player sends a command.\n" +
            "# This sound is cancellable.\n" +
            "Send Command:\n" +
            "  Cancellable: true\n" +
            "  Enabled: true\n" +
            "  Sounds:\n" +
            "    '0':\n" +
            "      Delay: 0\n" +
            "      Options:\n" +
            "        Radius: 0.0\n" +
            "      Pitch: 2.0\n" +
            "      Sound: ENTITY_ITEM_PICKUP\n" +
            "      Volume: 10.0\n" +
            "\n" +
            "# When a player starts flying.\n" +
            "# This sound is cancellable.\n" +
            "Start Flying:\n" +
            "  Cancellable: true\n" +
            "  Enabled: true\n" +
            "  Sounds:\n" +
            "    '0':\n" +
            "      Delay: 0\n" +
            "      Options:\n" +
            "        Radius: 12.0\n" +
            "        Relative Location:\n" +
            "          UP_DOWN: 2.0\n" +
            "      Pitch: 1.0\n" +
            "      Sound: BLOCK_PISTON_EXTEND\n" +
            "      Volume: 0.5\n" +
            "\n" +
            "# When a player stops flying.\n" +
            "# This sound is cancellable.\n" +
            "Stop Flying:\n" +
            "  Cancellable: true\n" +
            "  Enabled: true\n" +
            "  Sounds:\n" +
            "    '0':\n" +
            "      Delay: 0\n" +
            "      Options:\n" +
            "        Radius: 12.0\n" +
            "        Relative Location:\n" +
            "          UP_DOWN: -1.0\n" +
            "      Pitch: 1.0\n" +
            "      Sound: BLOCK_PISTON_CONTRACT\n" +
            "      Volume: 0.5\n" +
            "\n" +
            "# When the player moves their current item to their off hand.\n" +
            "# This sound is cancellable.\n" +
            "Swap Hands:\n" +
            "  Cancellable: true\n" +
            "  Enabled: " + (PlayMoreSoundsCore.getServerVersion().compareTo(new Version("1.9")) < 0 ? "false # This event is not available in " + PlayMoreSoundsCore.getServerVersion() + ".\n" : "true\n") +
            "  Sounds:\n" +
            "    '0':\n" +
            "      Delay: 0\n" +
            "      Options:\n" +
            "        Radius: 0.0\n" +
            "      Pitch: 1.3\n" +
            "      Sound: ITEM_ARMOR_EQUIP_GENERIC\n" +
            "      Volume: 10.0\n" +
            "\n" +
            "# When a player teleports using a command.\n" +
            "# This sound is cancellable.\n" +
            "Teleport:\n" +
            "  Cancellable: true\n" +
            "  Enabled: true\n" +
            "  Sounds:\n" +
            "    '0':\n" +
            "      Delay: 0\n" +
            "      Options:\n" +
            "        Radius: 15.0\n" +
            "      Pitch: 1.0\n" +
            "      Sound: ENTITY_ENDERMAN_TELEPORT\n" +
            "      Volume: 1.0\n" +
            "\n" +
            "# When a player crouches.\n" +
            "# This sound is disabled by default. To enable it, copy the options from the sound\n" +
            "#above and set 'Enabled' to true.\n" +
            "# This sound is cancellable.\n" +
            "Toggle Sneak:\n" +
            "  Enabled: false\n" +
            "  Cancellable: true\n" +
            "\n" +
            "# When a player gets out of bed and is morning.\n" +
            "# This sound is not cancellable.\n" +
            "Wake Up:\n" +
            "  Enabled: true\n" +
            "  Sounds:\n" +
            "    '0':\n" +
            "      Delay: 0\n" +
            "      Options:\n" +
            "        Radius: 0.0\n" +
            "      Pitch: 1.0\n" +
            "      Sound: ENTITY_CHICKEN_HURT\n" +
            "      Volume: 0.4\n" +
            "\n" +
            "# When it starts raining on the world.\n" +
            "# This sound is cancellable.\n" +
            "Weather Rain:\n" +
            "  Cancellable: true\n" +
            "  Enabled: " + (PlayMoreSoundsCore.getServerVersion().compareTo(new Version("1.9")) < 0 ? "false # ITEM_ELYTRA_FLYING is not available in " + PlayMoreSoundsCore.getServerVersion() + " please choose another sound.\n" : "true\n") +
            "  Sounds:\n" +
            "    '0':\n" +
            "      Delay: 0\n" +
            "      Options:\n" +
            "        Radius: -2.0\n" +
            "      Pitch: 1.0\n" +
            "      Sound: ITEM_ELYTRA_FLYING\n" +
            "      Volume: 0.3\n" +
            "\n" +
            "# When it stops raining on the world.\n" +
            "# This sound is disabled by default. To enable it, copy the options from the sound\n" +
            "#above and set 'Enabled' to true.\n" +
            "# This sound is cancellable.\n" +
            "Weather Rain End:\n" +
            "  Cancellable: true\n" +
            "  Enabled: false", StaticFields.version4_0_0),
    WORLD_TIME_TRIGGERS(StaticFields.sounds.resolve("world time triggers.yml"), "# Set a sound to play when a world reaches a specific time of the day.\n" +
            "#\n" +
            "#world: # The name of the world that you want to track time.\n" +
            "#  '13000': # The time that you want to play a sound.\n" +
            "#    Enabled: true\n" +
            "#    Sounds:\n" +
            "#      '0':\n" +
            "#        Delay: 0\n" +
            "#        Options:\n" +
            "#          Radius: -2.0 # The radius is counted by the world's spawn location. Set to -2 so everyone in the world can hear it.\n" +
            "#        Pitch: 1.0\n" +
            "#        Sound: 'AMBIENT_CAVE'\n" +
            "#        Volume: 10.0\n" +
            "#\n" +
            "# More information about sounds on sounds.yml\n" +
            "\n" +
            "Version: '" + PlayMoreSoundsVersion.version + "'", StaticFields.version3_0_0);

    private static final @NotNull ConfigurationLoader configurationLoader = new ConfigurationLoader();

    static {
        for (Configurations configuration : Configurations.values()) {
            configurationLoader.registerConfiguration(configuration.configurationHolder, configuration.minVersion, PlayMoreSoundsVersion.getVersion());
        }
    }

    private final @NotNull ConfigurationHolder configurationHolder;
    private final @NotNull Version minVersion;

    Configurations(@NotNull Path path, @NotNull String contents, @NotNull Version minVersion)
    {
        this.configurationHolder = new ConfigurationHolder(path, contents);
        this.minVersion = minVersion;
    }

    public static @NotNull ConfigurationLoader getConfigurationLoader()
    {
        return configurationLoader;
    }

    public @NotNull ConfigurationHolder getConfigurationHolder()
    {
        return configurationHolder;
    }

    private static class StaticFields
    {
        protected static final @NotNull Path sounds = PlayMoreSoundsCore.getFolder().resolve("Sounds");
        protected static final @NotNull Path lang = PlayMoreSoundsCore.getFolder().resolve("Language");
        // These versions are used to set the configurations' minimum versions.
        protected static final @NotNull Version version3_0_0 = new Version("3.0.0");
        protected static final @NotNull Version version3_2_0 = new Version("3.2.0");
        protected static final @NotNull Version version3_3_0 = new Version("3.3.0");
        protected static final @NotNull Version version4_0_0 = new Version("4.0.0");
    }
}
