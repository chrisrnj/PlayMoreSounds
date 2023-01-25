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

package com.epicnicity322.playmoresounds.core.config;

import com.epicnicity322.epicpluginlib.core.EpicPluginLib;
import com.epicnicity322.epicpluginlib.core.config.ConfigurationHolder;
import com.epicnicity322.epicpluginlib.core.config.ConfigurationLoader;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.playmoresounds.core.PlayMoreSounds;
import org.jetbrains.annotations.NotNull;

public final class Configurations {
    public static final @NotNull ConfigurationHolder SOUNDS = new ConfigurationHolder(PlayMoreSounds.DATA_FOLDER.resolve("sounds.yml"), """
            ###########################################################################################################
            # Set a sound to play when an event is triggered.
            #
            # Create a section with the name of the event you want to play a sound, for example:
            #
            Teleport: # The event that when triggered, a sound will be played.
              # Cancellable prevents the sound from playing if another plugin cancelled the event. This boolean helps
              #sounds be compatible with other plugins.
              Cancellable: true
              # Enables or disables a sound. In sounds.yml when this boolean is set to false the event is unregistered
              #as well, so you can use this plugin if you want to play sounds just for one event and don't worry about
              #performance being spent on things you don't use.
              Enabled: true
              # This is a list of sounds that will be played. You can copy and paste the options below to play
              #multiple sounds, each section must have a different name, here I numbered them just for organization.
              Sounds:
                '1':
                  # The category this sound will be played. You can find available categories in file
                  #'available sounds.txt'.
                  Category: MASTER
                  # This is a delay in ticks the sound will wait before playing. Set to 0 for no delay.
                  Delay: 0
                  # Sounds can have multiple options, all of them are optional.
                  Options:
                    # Even if a player has toggled their sounds off using "/pms toggle", the sound will be played.
                    Ignores Disabled: false
                    # The sound will be only played if the player has this permission.
                    # In case the event is not triggered by a player, this option is ignored.
                    Permission Required: 'playmoresounds.reproduce.teleport'
                    # The sound will be only listened by who have this permission.
                    # In case this is a Radius sound and the player has the Permission Required and not
                    #Permission To Listen, the sound will be played anyway, but only those in the Radius with the
                    #Permission To Listen will hear.
                    Permission To Listen: 'playmoresounds.listen.teleport'
                    # A range of blocks the sound will be hearable.
                    # A distance in blocks is calculated to every player in the world if the value is greater than 0.
                    # If you want the sound to play only to the player who triggered the event, set this to 0.
                    # If you want the sound to play to everyone online in the server, set this to -1.
                    # If you want the sound to play to everyone in the event's world, set this to -2.
                    Radius: 15.2
                  # What the sound pitch is, values greater than 2 have no difference.
                  Pitch: 1.0
                  # You can set this to either a Sound Type or a Custom Sound.
                  # Custom sounds do not need to be listed anywhere in the plugin, just set this value to the name of
                  #custom sound you set in your resource pack's sounds.json.
                  # PlayMoreSounds' sound types are different than bukkit's sound types, PlayMoreSounds' sound types
                  #are always the same no matter which version of the server you are running, so sounds have the same
                  #names in all versions.
                  # You can find a list of available sounds for the version %PLATFORM_VER% in the file 'available sounds.txt'.
                  Sound: ENTITY_ENDERMAN_TELEPORT
                  # The volume of the sound. The way minecraft does it is by distance, volume 1 = ~15 blocks.
                  # If you are playing region sounds you might want to set this to a big number so it plays with the
                  #same volume the whole region.
                  # You will notice the volume go lower if you set it to a decimal lower than 1.
                  # Set to -1 to use the maximum possible volume.
                  Volume: 0.9
            #
            # If you don't want to use a sound, you can either completely remove it from this configuration or set
            #'Enabled' to false, like this:
            Respawn:
              Enabled: false # Disabled sounds will be unregistered and not affect server performance.
            #
            # If you have any other doubts on how to set this configuration up, feel free to ask in PlayMoreSounds'
            #Discord: https://discord.gg/eAHPbc3
            ###########################################################################################################
                        
            Version: '%VER%' # Configuration version, don't change if you don't want your configuration reset.
                        
            # When a player lies in bed.
            # This sound is cancellable.
            Bed Enter:
              Enabled: true
              Cancellable: true
              Sounds:
                '1':
                  Options:
                    Radius: 15.0
                  Pitch: 0.65
                  Sound: ENTITY_VILLAGER_AMBIENT
                  Volume: 0.5
                        
            # When a player leaves bed.
            # This sound is cancellable.
            Bed Leave:
              Enabled: false
              Cancellable: true
                        
            # When a player changes the item slot of the hotbar.
            # This sound is cancellable.
            Change Held Item:
              Enabled: true
              Cancellable: false
              Sounds:
                '1':
                  Pitch: 2.0
                  Sound: BLOCK_NOTE_BLOCK_HAT
                        
            # When a player changes their level of experience.
            # This sound is disabled by default. To enable it, copy the options from the sound above and set
            #'Enabled' to true.
            # This sound is NOT cancellable.
            Change Level:
              Enabled: false
                        
            # When a player crafts an item.
            # This sound is disabled by default. To enable it, copy the options from another sound and set
            #'Enabled' to true.
            # This sound is cancellable.
            Craft Item:
              Enabled: false
              Cancellable: true
                        
            # When a player drops an item.
            # This sound is cancellable.
            Drop Item:
              Enabled: true
              Cancellable: false
              Sounds:
                '1':
                  Options:
                    Radius: 15.0
                  Sound: ENTITY_EGG_THROW
                  Volume: 0.5
                        
            # When a player edits or creates a book using a book and quill.
            # This sound is cancellable.
            Edit Book:
              Enabled: true
              Cancellable: false
              Sounds:
                '1':
                  Options:
                    Radius: 10.0
                  Pitch: 2.0
                  Sound: ITEM_ARMOR_EQUIP_LEATHER
                  Volume: 0.4
                        
            # When an entity is hit by another entity.
            # This sound is cancellable.
            Entity Hit:
              Enabled: true
              Cancellable: true
              Sounds:
                '1':
                  Options:
                    Radius: 16.0
                  Pitch: 2.0
                  Sound: ENTITY_GENERIC_HURT
                  Volume: 0.9
                        
            # When any entity jumps.
            # This sound only plays if you are running PaperMC.
            # This sound is disabled by default. To enable it, copy the options from the sound
            #above and set 'Enabled' to true.
            # This sound is cancellable.
            Entity Jump:
              Enabled: false
              Cancellable: true
                        
            # When a player joins the server for the first time.
            # This sound is not cancellable.
            First Join:
              Enabled: true
              Sounds:
                '1':
                  Options:
                    Radius: -1.0
                  Pitch: 2.0
                  Sound: BLOCK_NOTE_BLOCK_PLING
                        
            # When a player extracts something from a furnace.
            # This sound is NOT cancellable.
            Furnace Extract:
              Enabled: true
              Sounds:
                '1':
                  Options:
                    Radius: 15.0
                  Pitch: 1.3
                  Sound: ENTITY_GENERIC_EXTINGUISH_FIRE
                  Volume: 0.5
                        
            # When a player changes their game mode.
            # This sound is cancellable.
            Game Mode Change:
              Enabled: true
              Cancellable: true
              Sounds:
                '1':
                  Sound: BLOCK_ANVIL_LAND
                  Volume: 0.4
                        
            # When a player clicks on an inventory.
            # This sound is cancellable.
            Inventory Click:
              Enabled: true
              Cancellable: true
              Sounds:
                '1':
                  Pitch: 1.5
                  Sound: BLOCK_COMPARATOR_CLICK
                  Volume: 0.4
                        
            # When a player closes an inventory.
            # This sound is NOT cancellable.
            Inventory Close:
              Enabled: true
              Sounds:
                '1':
                  Pitch: 2.0
                  Sound: UI_TOAST_OUT
                        
            # When a player joins the server.
            # This sound is NOT cancellable.
            Join Server:
              Enabled: true
              Sounds:
                '1':
                  Options:
                    Radius: -1.0
                  Sound: BLOCK_NOTE_BLOCK_PLING
                        
            # When a player leaves the server.
            # This sound is NOT cancellable.
            Leave Server:
              Enabled: true
              Sounds:
                '1':
                  Options:
                    Radius: -1.0
                  Sound: BLOCK_NOTE_BLOCK_BASS
                        
            # When a player bans another player from the server.
            # This sound is cancellable.
            Player Ban:
              Enabled: true
              Cancellable: true
              Sounds:
                '1':
                  Options:
                    Radius: -1.0
                  Pitch: 1.3
                  Sound: ENTITY_ENDER_DRAGON_DEATH
                        
            # When a player sends a message on chat.
            # This sound is cancellable.
            Player Chat:
              Enabled: true
              Cancellable: true
              Sounds:
                '1':
                  Options:
                    Radius: -1.0
                  Sound: ENTITY_ITEM_PICKUP
                        
            # When a player dies.
            # This sound is cancellable.
            Player Death:
              Enabled: true
              Cancellable: true
              Sounds:
                '1':
                  Sound: ENTITY_WITHER_SPAWN
                        
            # When a player is kicked from the server.
            # This sound is cancellable.
            Player Kick:
              Enabled: true
              Cancellable: true
              Sounds:
                '1':
                  Options:
                    Radius: -1.0
                  Pitch: 1.3
                  Sound: ENTITY_ENDER_DRAGON_HURT
                        
            # When a player kills another player. (Damager)
            # This sound is disabled by default. To enable it, copy the options from the sound above and set
            #'Enabled' to true.
            # This sound is not cancellable.
            Player Kill:
              Enabled: false
                        
            # When a player is killed by another player. (Victim)
            # This sound is disabled by default. To enable it, copy the options from another sound and set
            #'Enabled' to true.
            # This sound is not cancellable.
            Player Killed:
              Enabled: false
              # Prevents the default 'Player Death' and sounds from 'death types.yml' from playing.
              Prevent Death Sounds: true
                        
            # When a player jumps.
            # This sound only plays if you are running PaperMC.
            # This sound is disabled by default. To enable it, copy the options from another sound and set
            #'Enabled' to true.
            # This sound is cancellable.
            Player Jump:
              Enabled: false
              Cancellable: true
                        
            # When a player swings their hand.
            # This sound is disabled by default. To enable it, copy the options from another sound and set
            #'Enabled' to true.
            # This sound is cancellable.
            Player Swing:
              Enabled: false
              Cancellable: true
                        
            # When a nether portal is opened.
            # This sound is cancellable.
            Portal Create:
              Enabled: true
              Cancellable: true
              Sounds:
                '1':
                  Options:
                    Radius: 15.0
                  Pitch: 0.8
                  Sound: BLOCK_END_PORTAL_SPAWN
                  Volume: 0.8
                        
            # When a player enters a PlayMoreSounds region.
            # This sound can also be played when entering another plugin's region. To do that you need to
            #install addons.
            # This sound is disabled by default. To enable it, copy the options from the sound above and set
            #'Enabled' to true.
            # This sound is cancellable.
            Region Enter:
              Enabled: false
              Cancellable: true
              # This sound can be stopped when the player leaves the region.
              Stop On Exit:
                Enabled: true
                Delay: 20 # The time in ticks to wait before stopping the sound
                        
            # When a player leaves a PlayMoreSounds region.
            # This sound can also be played when leaving another plugin's region. To do that you need to
            #install addons.
            # This sound is disabled by default. To enable it, copy the options from another sound and set
            #'Enabled' to true.
            # This sound is cancellable.
            Region Leave:
              Enabled: false
              Cancellable: true
                        
            # When a player sends a command.
            # This sound is cancellable.
            Send Command:
              Enabled: true
              Cancellable: true
              Sounds:
                '1':
                  Pitch: 2.0
                  Sound: ENTITY_ITEM_PICKUP
                        
            # When a player starts flying.
            # This sound is cancellable.
            Start Flying:
              Enabled: true
              Cancellable: true
              Sounds:
                '1':
                  Options:
                    Radius: 12.0
                  Sound: BLOCK_PISTON_EXTEND
                  Volume: 0.5
                        
            # When a player stops flying.
            # This sound is cancellable.
            Stop Flying:
              Enabled: true
              Cancellable: true
              Sounds:
                '1':
                  Options:
                    Radius: 12.0
                  Sound: BLOCK_PISTON_CONTRACT
                  Volume: 0.5
                        
            # When the player moves their current item to their off hand.
            # This sound is cancellable.
            Swap Hands:
              Enabled: true
              Cancellable: true
              Sounds:
                '1':
                  Pitch: 1.3
                  Sound: ITEM_ARMOR_EQUIP_GENERIC
                        
            # When a player crouches.
            # This sound is disabled by default. To enable it, copy the options from the sound above and set
            #'Enabled' to true.
            # This sound is cancellable.
            Toggle Sneak:
              Enabled: false
              Cancellable: true
                        
            # When a player gets out of bed and is morning.
            # This sound is not cancellable.
            Wake Up:
              Enabled: true
              Sounds:
                '1':
                  Sound: ENTITY_CHICKEN_HURT
                  Volume: 0.4
                        
            # When it starts raining on the world.
            # This sound is cancellable.
            Weather Rain:
              Enabled: true
              Cancellable: true
              Sounds:
                '1':
                  Options:
                    Radius: -2.0 # This sound is played in the world's spawn if the radius is greater than 0.
                  Sound: ITEM_ELYTRA_FLYING
                  Volume: 0.3
                        
            # When it stops raining on the world.
            # This sound is disabled by default. To enable it, copy the options from the sound above and set
            #'Enabled' to true.
            # This sound is cancellable.
            Weather Rain End:
              Enabled: false
              Cancellable: true
                        
            # When a player teleports to a different world.
            # This sound is cancellable.
            World Change:
              Enabled: true
              Cancellable: true
              # Makes so when the player teleports to a different world, the default sound of 'Teleport' does
              #not play.
              Prevent Teleport Sound: true
              Sounds:
                '1':
                  Options:
                    Radius: 15.2
                  Pitch: 2.0
                  Sound: BLOCK_PORTAL_TRAVEL
                  Volume: 0.6""".replace("%VER%", PlayMoreSounds.VERSION_STRING).replace("%PLATFORM_VER%", EpicPluginLib.Platform.getVersion().toString()));

    private static final @NotNull ConfigurationLoader loader = new ConfigurationLoader();

    static {
        loader.registerConfiguration(SOUNDS, new Version("4.0.0"), PlayMoreSounds.VERSION);
    }

    private Configurations() {
    }

    public static @NotNull ConfigurationLoader loader() {
        return loader;
    }
}
