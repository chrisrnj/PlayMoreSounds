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
    CONFIG(PlayMoreSoundsCore.getFolder().resolve("config.yml"),
            //TODO: Redo config.yml
            "", StaticFields.version3_2_0),
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
    LANGUAGE_EN_US(StaticFields.lang.resolve("Language EN-US.yml"), "", StaticFields.version4_0_0),
    LANGUAGE_ES_LA(StaticFields.lang.resolve("Language ES-LA.yml"), "", StaticFields.version4_0_0),
    LANGUAGE_PT_BR(StaticFields.lang.resolve("Language PT-BR.yml"), "", StaticFields.version4_0_0),
    LANGUAGE_ZH_CN(StaticFields.lang.resolve("Language ZH-CN.yml"), "", StaticFields.version4_0_0),
    NATURE_SOUNDS_REPLACER(StaticFields.sounds.resolve("nature sounds replacer.yml"), "# Replace any sound played by nature on your server.\n" +
            "#\n" +
            "#  When a sound here is played, PlayMoreSounds interrupts the sound packets from being\n" +
            "# sent to the players and plays the sound set here instead. This way you can take\n" +
            "# advantage of PlayMoreSounds features, like replace a nature sound with resource pack\n" +
            "# sound, radius sound, delayed sound etc.\n" +
            "#\n" +
            "# Warnings:\n" +
            "# >> ProtocolLib is required to work.\n" +
            "# >> Server needs to be running on version 1.9+.\n" +
            "# >> Sounds to replace are bukkit sounds, that means the name changes depending on the\n" +
            "# version you are running, unlike PlayMoreSounds sounds that have the same name on all\n" +
            "# versions.\n" +
            "#\n" +
            "#  To replace a sound create a section with the sound name and set the replacing sound\n" +
            "# on it, for example:\n" +
            "#\n" +
            "#ENTITY_ZOMBIE_HURT: # This is the sound to replace\n" +
            "#  Enabled: true\n" +
            "#  Sounds: # The sounds that will play instead.\n" +
            "#    '0':\n" +
            "#      Delay: 0\n" +
            "#      Options:\n" +
            "#        Ignores Disabled: true\n" +
            "#        Permission To Listen: 'listen.zombiehurt'\n" +
            "#        Radius: 15.2\n" +
            "#        Relative Location:\n" +
            "#          FRONT_BACK: 0.0\n" +
            "#          RIGHT_LEFT: 0.0\n" +
            "#          UP_DOWN: 0.0\n" +
            "#      Pitch: 1.0\n" +
            "#      Sound: ENTITY_SKELETON_HURT\n" +
            "#      Volume: 10.0\n" +
            "#\n" +
            "# More information about sounds on sounds.yml.\n" +
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
    SOUNDS(PlayMoreSoundsCore.getFolder().resolve("sounds.yml"),
            //TODO: Redo sounds.yml
            "# Set a sound to play when a player triggers an event.\n" +
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
                    "  Enabled: true\n" +
                    "  Sounds:\n" +
                    "    '0':\n" +
                    "      Delay: 0\n" +
                    "      Options:\n" +
                    "        Radius: 10.0\n" +
                    "      Pitch: 2.0\n" +
                    "      Sound: ITEM_ARMOR_EQUIP_LEATHER\n" +
                    "      Volume: 0.7\n" +
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
                    "      Sound: UI_BUTTON_CLICK\n" +
                    "      Volume: 0.4\n" +
                    "\n" +
                    "# When a player closes an inventory.\n" +
                    "# This sound is not cancellable.\n" +
                    "Inventory Close:\n" +
                    "  Enabled: true\n" +
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
                    "# This sound is not cancellable.\n" +
                    "Player Kicked:\n" +
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
                    "# When a player jumps.\n" +
                    "# This sound only plays if you are running PaperMC.\n" +
                    "# This sound is disabled by default. To enable it, copy the options from the sound\n" +
                    "#above and set 'Enabled' to true.\n" +
                    "# This sound is cancellable.\n" +
                    "Player Jump:\n" +
                    "  Enabled: false\n" +
                    "  Cancellable: true\n" +
                    "\n" +
                    "# When a player swings their hand.\n" +
                    "# This sound is disabled by default. To enable it, copy the options from anptjer sound\n" +
                    "#and set 'Enabled' to true.\n" +
                    "# This sound is cancellable.\n" +
                    "Player Swing:\n" +
                    "  Enabled: false\n" +
                    "  Cancellable: true\n" +
                    "\n" +
                    "# When a player enters a PlayMoreSounds region.\n" +
                    "# This sound can also be played when entering another plugin's region. To do that you\n" +
                    "#need to install addons.\n" +
                    "# This sound is disabled by default. To enable it, copy the options from another sound\n" +
                    "#and set 'Enabled' to true.\n" +
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
                    "# When the player moves their current item to their off hand.\n" +
                    "# This sound is cancellable.\n" +
                    "Swap Hand Item:\n" +
                    "  Enabled: true\n" +
                    "  Sounds:\n" +
                    "    '0':\n" +
                    "      Delay: 0\n" +
                    "      Options:\n" +
                    "        Radius: 0.0\n" +
                    "      Pitch: 1.3\n" +
                    "      Sound: ITEM_ARMOR_EQUIP_GENERIC\n" +
                    "      Volume: 10.0\n" +
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
                    "      Volume: 0.4\n", StaticFields.version3_3_0),
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
