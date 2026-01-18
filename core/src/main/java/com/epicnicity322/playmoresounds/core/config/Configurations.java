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

package com.epicnicity322.playmoresounds.core.config;

import com.epicnicity322.epicpluginlib.core.EpicPluginLib;
import com.epicnicity322.epicpluginlib.core.config.ConfigurationHolder;
import com.epicnicity322.epicpluginlib.core.config.ConfigurationManager;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.playmoresounds.core.PlayMoreSounds;
import org.jetbrains.annotations.NotNull;

public final class Configurations {
    private static final @NotNull ConfigurationManager manager = new ConfigurationManager();

    public static final @NotNull ConfigurationHolder SOUNDS = new ConfigurationHolder(PlayMoreSounds.DATA_FOLDER.resolve("sounds.yml"), manager, """
            ############################################################################################################
            # Set a sound to play when an event is triggered.
            #
            # Create a section with the name of the event you want to play a sound, for example:
            #
            Teleport:
              # Enables or disables a sound. The event listener is automatically unregistered if the sound is disabled
              #and no longer used in any other configuration.
              Enabled: true
              # Whether to not play the sound if another plugin has cancelled the event, for example: a player has
              #teleported into a WorldGuard region where entry is denied.
              Cancellable: true
              # Whether to pick a random sound from the sound list instead of playing all.
              # You can still play multiple sounds with the random setting enabled by creating groups. To create groups,
              #name the sections of the sounds as follows: '1.1', '1.2' and '1.3' for group 1, and '2.1', '2.2' and '2.3'
              #for group 2. Now each group has 50% chance of being selected, and they both play 3 different sounds.
              Random: false
              # A dynamic list of sounds to be played, add more sections to play multiple sounds simultaneously.
              # You can just copy and paste the options into a '2' section. It is recommended to use numbers as the
              #section name.
              Sounds:
                '1':
                  # The category this sound will be played. You can find available categories in the file
                  #'available sounds.txt'.
                  Category: MASTER
                  Options:
                    # A delay in ticks the sound will wait before playing.
                    Delay: 0
                    # Whether to play the sound even if a player has toggled their sounds off using '/pms toggle'.
                    Ignore Toggle State: false
                    # Check if the player has permission to listen to this sound.
                    Permission To Listen: 'playmoresounds.listen.teleport'
                    # The permission to play the sound. A player can play the sound, but not have the Permission To
                    #Listen, meaning only other players will be able to hear it.
                    Permission To Play: 'playmoresounds.play.teleport'
                    # Play the sound to all players within the specified range.
                    # Use a value greater than 0 to play for all players in a radius in blocks.
                    # Use SERVER_GLOBAL to play for all players online in the server.
                    # Use WORLD_GLOBAL to play for every player in the same world, at their respective locations.
                    # Use WORLD_LOCAL to play for every player in the same world, but at the source location.
                    Radius: 15.2
                    # Adds a relative position to the sound's final location. This location is calculated based on where
                    #the player is looking.
                    Relative Position:
                      Left: 0.0  # To the left of the player, use negative for the right.
                      Up: 0.0    # To above of the player, use negative for down.
                      Front: 0.0 # To the front of the player, use negative for the back.
                  # The sound's pitch. Values greater than 2 make no difference in Minecraft.
                  Pitch: 1.0
                  # The sound that will be played.
                  # You can use either the name of the sound in 'available sounds.txt', or a custom sound.
                  # To use custom sounds, simply type the namespace and sound as you would with the '/playsound' command,
                  #for example: 'custom:spawn_region_music'. The sound will be played to all players that have a
                  #resource pack active that has this sound name.
                  # This is the only required setting in this current section, every other option will use default
                  #values if absent.
                  Sound: ENTITY_ENDERMAN_TELEPORT
                  # The volume of the sound. The way volume works in Minecraft is by distance, volume 1 = ~15 blocks.
                  # You will notice the volume go quieter if you set it to a decimal lower than 1.
                  # Set to -1 to use the maximum possible volume.
                  Volume: 0.9
            #
            # If you don't want to use a sound, you can either completely remove it from this configuration or set
            #'Enabled' to false, like this:
            Respawn:
              Enabled: false # Disabled sounds will be unregistered and not affect server performance.
            #
            # If you have any questions on how to set this configuration up, feel free to ask in PlayMoreSounds'
            #Discord: https://discord.gg/eAHPbc3
            ############################################################################################################
            
            Version: '%VER%' # Configuration version used for upgrading between updates.
            
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

    static {
        manager.registerConfiguration(SOUNDS, new Version("6.0"), PlayMoreSounds.VERSION);
    }

    private Configurations() {
    }

    public static @NotNull ConfigurationManager manager() {
        return manager;
    }
}
