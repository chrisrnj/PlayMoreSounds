package com.epicnicity322.playmoresounds.bukkit.util;

import com.epicnicity322.epicpluginlib.config.type.ConfigType;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;

public class Storage
{
    public static HashMap<String, ConfigType> TYPES = new HashMap<>();

    public static void loadTypes()
    {
        TYPES.put("biomes", new ConfigType("biomes.yml", "# Set a sound to play when you enter, exit or stand on a specific biome."
                + "\n#"
                + "\n# Sample:"
                + "\n# (Take a note that this is a sample and the sounds may not be available"
                + "\n# on your MC version.)"
                + "\n#"
                + "\n#world: #The world name."
                + "\n#  PLAINS: #The biome name."
                + "\n#    Enter: #When a player enters this biome."
                + "\n#      Cancellable: true"
                + "\n#      Enabled: true"
                + "\n#      Sounds:"
                + "\n#        '0':"
                + "\n#          Delay: 0"
                + "\n#          Options:"
                + "\n#            Radius: 0"
                + "\n#          Pitch: 1"
                + "\n#          Sound: BLOCK_NOTE_BLOCK_PLING"
                + "\n#          Volume: 10"
                + "\n#    Leave: #When a player exits this biome."
                + "\n#      Cancellable: true"
                + "\n#      Enabled: true"
                + "\n#      Sounds:"
                + "\n#        '0':"
                + "\n#          Delay: 0"
                + "\n#          Options:"
                + "\n#            Radius: 0"
                + "\n#          Pitch: 1"
                + "\n#          Sound: BLOCK_NOTE_BLOCK_BASS"
                + "\n#          Volume: 10"
                + "\n#    Loop: #When a player enters the biome, a loop will be triggered and play."
                + "\n#      Cancellable: true"
                + "\n#      Delay: 0 #Time in ticks to wait to start the loop once triggered."
                + "\n#      Enabled: true"
                + "\n#      Period: 100 #Time in ticks to wait before playing these sounds again."
                + "\n#      Stop On Exit: #Stops the sound only, the loop is stopped automatically."
                + "\n#        Enabled: true #When the player leaves the biome, the sound will be stopped."
                + "\n#        Delay: 20 #The delay to stop the sound."
                + "\n#      Stop Enter Sound: true #Stops Enter sound from playing if enabled."
                + "\n#      Sounds:"
                + "\n#        '0':"
                + "\n#          Delay: 0"
                + "\n#          Options:"
                + "\n#            Radius: 0"
                + "\n#          Pitch: 1"
                + "\n#          Sound: BLOCK_NOTE_BLOCK_BASS"
                + "\n#          Volume: 10"
                + "\n#"
                + "\n# This is a small sample. You can add more biomes, worlds and more options"
                + "\n# to the sound options."
                + "\n# More information about sounds on sounds.yml." +
                "\nVersion: '3.0.0#12'", getSoundsFolder(), "3.0.0#12"
        ));

        // FIXME: 07/01/2020 Add filters documentation
        TYPES.put("chat", new ConfigType("chat.yml", "# Set a sound to play when a player say a specific word/sentence in chat."
                + "\n#"
                + "\n#play pling to everyone: # The word/sentence that is said on chat."
                + "\n#  # Stops the sound PlayerChat on sounds.yml from playing, so this one here is the only sound that the player will hear."
                + "\n#  Stop Other Sounds: true"
                + "\n#  Cancellable: true"
                + "\n#  Enabled: true"
                + "\n#  Sounds:"
                + "\n#    '0':"
                + "\n#      Delay: 0"
                + "\n#      Options:"
                + "\n#        Radius: -1"
                + "\n#      Pitch: 1"
                + "\n#      Sound: BLOCK_NOTE_BLOCK_PLING"
                + "\n#      Volume: 10"
                + "\n#"
                + "\n# More information about sounds on sounds.yml" +
                "\nVersion: '3.0.0#12'", getSoundsFolder(), "3.0.0#12"
        ));

        TYPES.put("commands", new ConfigType("commands.yml",
                "# Set a sound to play when a player type a specific command."
                        + "\n#"
                        + "\n#  There are five filters to choose:"
                        + "\n#"
                        + "\n#  -> Contains:"
                        + "\n#  Use this section to play a sound to every command that contains the sentence you specify."
                        + "\n#  Sample:"
                        + "\n#"
                        + "\n#Contains:"
                        + "\n#  play:"
                        + "\n#    Cancellable: true"
                        + "\n#    Enabled: true"
                        + "\n#    # This prevents sound overlapping."
                        + "\n#    Stop Other Sounds:"
                        + "\n#      # Prevents the sound 'Send Command' on sounds.yml from playing."
                        + "\n#      SoundsYML: true"
                        + "\n#      # If the same command is in more than one filter, then only the one that is matched first will play."
                        + "\n#      CommandsYML: true"
                        + "\n#"
                        + "\n#  -> Ends With:"
                        + "\n#  Self explanatory. If a command ends with the sentence specified, the sound will play."
                        + "\n#  Sample:"
                        + "\n#"
                        + "\n#Ends With:"
                        + "\n#  -force:"
                        + "\n#    Cancellable: true"
                        + "\n#    Enabled: true"
                        + "\n#    Stop Other Sounds:"
                        + "\n#      SoundsYML: true"
                        + "\n#      CommandsYML: true"
                        + "\n#    Sounds:"
                        + "\n#      '0':"
                        + "\n#        Delay: 0"
                        + "\n#        Options:"
                        + "\n#          Radius: 0.0"
                        + "\n#        Pitch: 1.0"
                        + "\n#        Sound: 'ENTITY_CREEPER_PRIMED'"
                        + "\n#        Volume: 10.0"
                        + "\n#"
                        + "\n#  -> Equals Exactly:"
                        + "\n#  When a command equals exactly like the specified here. (Case sensitive)"
                        + "\n#  Sample:"
                        + "\n#"
                        + "\n#Equals Exactly:"
                        + "\n#  /wArP MALl2:"
                        + "\n#    Cancellable: true"
                        + "\n#    Enabled: true"
                        + "\n#    Stop Other Sounds:"
                        + "\n#      SoundsYML: true"
                        + "\n#      CommandsYML: true"
                        + "\n#    Sounds:"
                        + "\n#      '0':"
                        + "\n#        Delay: 1"
                        + "\n#        Options:"
                        + "\n#          Radius: 0.0"
                        + "\n#        Pitch: 2.0"
                        + "\n#        Sound: 'BLOCK_PORTAL_TRAVEL'"
                        + "\n#        Volume: 0.4"
                        + "\n#"
                        + "\n#  -> Equals Ignore Case:"
                        + "\n#  When a command is equals to the specified but, it doesn't matter if it's on lower case or"
                        + "\n# upper case."
                        + "\n#  If a player accidentally toggled upper case on it's keyboard and typed /SPAWN and you want"
                        + "\n# to set a sound for the command \"/spawn\", put him in this section so the sound will be played"
                        + "\n# even if is on upper case."
                        + "\n#  Sample:"
                        + "\n#"
                        + "\n#Equals Ignore Case:"
                        + "\n#  /spawn:"
                        + "\n#    Cancellable: false"
                        + "\n#    Enabled: true"
                        + "\n#    Stop Other Sounds:"
                        + "\n#      SoundsYML: true"
                        + "\n#      CommandsYML: true"
                        + "\n#    Sounds:"
                        + "\n#      '1':"
                        + "\n#        Delay: 0"
                        + "\n#        Options:"
                        + "\n#          Radius: 0.0"
                        + "\n#        Pitch: 2.0"
                        + "\n#        Sound: 'BLOCK_PORTAL_TRAVEL'"
                        + "\n#        Volume: 0.4"
                        + "\n#"
                        + "\n#  -> Starts With:"
                        + "\n#  This is the most used of them all. Plays a sound when a command starts with the sentence"
                        + "\n# you specify."
                        + "\n#  Sample:"
                        + "\n#"
                        + "\n#Starts With:"
                        + "\n#  /tp:"
                        + "\n#    Cancellable: false"
                        + "\n#    Enabled: true"
                        + "\n#    Stop Other Sounds:"
                        + "\n#      SoundsYML: true"
                        + "\n#      CommandsYML: true"
                        + "\n#    Sounds:"
                        + "\n#      '1':"
                        + "\n#        Delay: 0"
                        + "\n#        Options:"
                        + "\n#          Radius: 0.0"
                        + "\n#        Pitch: 2.0"
                        + "\n#        Sound: 'BLOCK_PORTAL_TRAVEL'"
                        + "\n#        Volume: 0.4"
                        + "\n#  /warp:"
                        + "\n#    Cancellable: false"
                        + "\n#    Enabled: true"
                        + "\n#    Stop Other Sounds:"
                        + "\n#      SoundsYML: true"
                        + "\n#      CommandsYML: true"
                        + "\n#    Sounds:"
                        + "\n#      '1':"
                        + "\n#        Delay: 0"
                        + "\n#        Options:"
                        + "\n#          Radius: 0.0"
                        + "\n#        Pitch: 2.0"
                        + "\n#        Sound: 'BLOCK_PORTAL_TRAVEL'"
                        + "\n#        Volume: 0.4"
                        + "\n#  /spawn:"
                        + "\n#    Cancellable: false"
                        + "\n#    Enabled: true"
                        + "\n#    Stop Other Sounds:"
                        + "\n#      SoundsYML: true"
                        + "\n#      CommandsYML: true"
                        + "\n#    Sounds:"
                        + "\n#      '1':"
                        + "\n#        Delay: 0"
                        + "\n#        Options:"
                        + "\n#          Radius: 0.0"
                        + "\n#        Pitch: 2.0"
                        + "\n#        Sound: 'BLOCK_PORTAL_TRAVEL'"
                        + "\n#        Volume: 0.4"
                        + "\n#"
                        + "\n# More information about sounds on sounds.yml"
                        + "\n# The following sounds are here just to prevent the default sound on sounds.yml from playing."
                        + "\nVersion: '3.0.0#12'"
                        + "\n"
                        + "\nStarts With:"
                        + "\n  /tp:"
                        + "\n    Enabled: true"
                        + "\n    Stop Other Sounds:"
                        + "\n      SoundsYML: true"
                        + "\n"
                        + "\n  /warp:"
                        + "\n    Enabled: true"
                        + "\n    Stop Other Sounds:"
                        + "\n      SoundsYML: true"
                        + "\n"
                        + "\n  /spawn:"
                        + "\n    Enabled: true"
                        + "\n    Stop Other Sounds:"
                        + "\n      SoundsYML: true"
                        + "\n"
                        + "\n  /pms:"
                        + "\n    Enabled: true"
                        + "\n    Stop Other Sounds:"
                        + "\n      SoundsYML: true"
                        + "\n"
                        + "\n  /playmoresounds:"
                        + "\n    Enabled: true"
                        + "\n    Stop Other Sounds:"
                        + "\n      SoundsYML: true"
                        + "\n"
                        + "\n  /gamemode:"
                        + "\n    Enabled: true"
                        + "\n    Stop Other Sounds:"
                        + "\n      SoundsYML: true"
                        + "\n"
                        + "\n  /gm:"
                        + "\n    Enabled: true"
                        + "\n    Stop Other Sounds:"
                        + "\n      SoundsYML: true", getSoundsFolder(), "3.0.0#12"
        ));

        TYPES.put("config", new ConfigType("config.yml", "#################################"
                + "\n##  PlayMoreSounds Configuration"
                + "\n##  v3.0.0-SNAPSHOT"
                + "\n#################################"
                + "\n"
                + "\n# Config version. Please don't change."
                + "\nVersion: '3.0.0#13'"
                + "\n"
                + "\n# Auto enables the sounds of a person who has disabled their sounds with /pms toggle on login."
                + "\nEnable Sounds After Re-Login: true"
                + "\n"
                + "\n# Available languages: (EN-US, ES-LA, PT-BR)"
                + "\nLocale: EN-US" +
//                + "\n" +
//                "\n# Sound region settings:" +
//                "\nRegions:" +
//                "\n  Border:" +
//                "\n    # Since to find a region's border it's necessary to loop through all region's blocks," +
//                "\n    #this setting limits how much players can see the border at the same time." +
//                "\n    MaxPlayersViewingBorderAtTheSameTime: 2" +
//                "\n    # For how long in ticks the border should appear." +
//                "\n    ShowTime: 140" +
//                "\n  RegionLimits:" +
//                "\n    Area:" +
//                "\n      # What are the maximum blocks a region can contain within?" +
//                "\n      Max: 3375" +
//                "\n      # And the minimum?" +
//                "\n      Min: 8" +
//                "\n    # How much regions a player can have?" +
//                "\n    MaxPerPlayer: 5" +
                "\n" +
                "\nInventories:" +
                "\n  List:" +
                "\n    Stop Sound Item: " +
                "\n      Material: 'BARRIER'" +
                "\n      Glowing: true" +
                "\n    Next Page Item:" +
                "\n      Material: 'SPECTRAL_ARROW'" +
                "\n      Glowing: false" +
                "\n    Previous Page Item:" +
                "\n      Material: 'SPECTRAL_ARROW'" +
                "\n      Glowing: false" +
                "\n    # The items representing the sounds." +
                "\n    # It can have multiple items." +
                "\n    Sound Item:" +
                "\n      Material:" +
                "\n        - 'MUSIC_DISC_13'" +
                "\n        - 'MUSIC_DISC_CAT'" +
                "\n        - 'MUSIC_DISC_BLOCKS'" +
                "\n        - 'MUSIC_DISC_CHIRP'" +
                "\n        - 'MUSIC_DISC_FAR'" +
                "\n        - 'MUSIC_DISC_MALL'" +
                "\n        - 'MUSIC_DISC_MELLOHI'" +
                "\n        - 'MUSIC_DISC_STAL'" +
                "\n        - 'MUSIC_DISC_STRAD'" +
                "\n        - 'MUSIC_DISC_WARD'" +
                "\n        - 'MUSIC_DISC_WAIT'" +
                "\n      Glowing: false" +
                "\n    # The amount of sound rows this inventory will have." +
                "\n    # Max sound rows per inventory: 4" +
                "\n    Rows Per Page: 4" +
                "\n  Sound Editor:" +
                "\n    Id Item:" +
                "\n      # Set to parent so the material is the same as the one clicked in the previous inventory." +
                "\n      Material: 'parent'" +
                "\n      Glowing: false" +
                "\n    Separator Item:" +
                "\n      Material:" +
                "\n        - 'RED_STAINED_GLASS_PANE'" +
                "\n        - 'ORANGE_STAINED_GLASS_PANE'" +
                "\n        - 'YELLOW_STAINED_GLASS_PANE'" +
                "\n        - 'GREEN_STAINED_GLASS_PANE'" +
                "\n        - 'LIGHT_BLUE_STAINED_GLASS_PANE'" +
                "\n        - 'BLUE_STAINED_GLASS_PANE'" +
                "\n        - 'PURPLE_STAINED_GLASS_PANE'" +
                "\n      Glowing: false" +
                "\n    Ignores Toggle Item:" +
                "\n      Material: 'LEVER'" +
                "\n      Glowing: false" +
                "\n    Decoration Item:" +
                "\n      Material: 'GLASS_PANE'" +
                "\n      Glowing: false" +
                "\n    Radius Item:" +
                "\n      Material: 'GRASS_BLOCK'" +
                "\n      Glowing: false" +
                "\n    Eye Location Item:" +
                "\n      Material: 'PLAYER_HEAD'" +
                "\n      Glowing: false" +
                "\n    Feet Location Item:" +
                "\n      Material: 'LEATHER_BOOTS'" +
                "\n      Glowing: false" +
                "\n    Relative Location Item:" +
                "\n      Material: 'END_CRYSTAL'" +
                "\n      Glowing: false" +
                "\n    Permission Required Item:" +
                "\n      Material: 'STRING'" +
                "\n      Glowing: false" +
                "\n    Permission To Listen Item:" +
                "\n      Material: 'STRING'" +
                "\n      Glowing: false" +
                "\n    Sound Item:" +
                "\n      Material: 'NOTE_BLOCK'" +
                "\n      Glowing: false" +
                "\n    Delay Item:" +
                "\n      Material: 'REPEATER'" +
                "\n      Glowing: false" +
                "\n    Volume Item:" +
                "\n      Material: 'GUNPOWDER'" +
                "\n      Glowing: false" +
                "\n    Pitch Item:" +
                "\n      Material: 'REDSTONE'" +
                "\n      Glowing: false" +
                "\n    Done Item:" +
                "\n      Material: 'GREEN_STAINED_GLASS'" +
                "\n      Glowing: false" +
                "\n    Cancel Item:" +
                "\n      Material: 'RED_STAINED_GLASS'" +
                "\n      Glowing: false" +
                "\n  Rich Sound Editor:" +
                "\n    Add New Sound Item:" +
                "\n      Material: 'NETHER_STAR'" +
                "\n      Glowing: false" +
                "\n    Cancel Item:" +
                "\n      Material: 'RED_STAINED_GLASS'" +
                "\n      Glowing: false" +
                "\n    Cancellable Item:" +
                "\n      Material: 'BARRIER'" +
                "\n      Glowing: true" +
                "\n    Loop Start Delay Item:" +
                "\n      Material: 'REDSTONE'" +
                "\n      Glowing: true" +
                "\n    Disabled Item:" +
                "\n      Material: 'RED_WOOL'" +
                "\n      Glowing: true" +
                "\n    Done Item:" +
                "\n      Material: 'GREEN_STAINED_GLASS'" +
                "\n      Glowing: false" +
                "\n    Enabled Item:" +
                "\n      Material: 'LIME_WOOL'" +
                "\n      Glowing: true" +
                "\n    Name Item:" +
                "\n      Material: 'NAME_TAG'" +
                "\n      Glowing: true" +
                "\n    Next Page Item:" +
                "\n      Material: 'SPECTRAL_ARROW'" +
                "\n      Glowing: false" +
                "\n    Loop Period Item:" +
                "\n      Material: 'REPEATER'" +
                "\n      Glowing: true" +
                "\n    Previous Page Item:" +
                "\n      Material: 'SPECTRAL_ARROW'" +
                "\n      Glowing: false" +
                "\n    # The separator item will change every 20 ticks to the next on this list." +
                "\n    Separator Item:" +
                "\n      Material:" +
                "\n        - 'RED_STAINED_GLASS_PANE'" +
                "\n        - 'ORANGE_STAINED_GLASS_PANE'" +
                "\n        - 'YELLOW_STAINED_GLASS_PANE'" +
                "\n        - 'GREEN_STAINED_GLASS_PANE'" +
                "\n        - 'LIGHT_BLUE_STAINED_GLASS_PANE'" +
                "\n        - 'BLUE_STAINED_GLASS_PANE'" +
                "\n        - 'PURPLE_STAINED_GLASS_PANE'" +
                "\n      Glowing: false" +
                "\n    # The items representing the sounds." +
                "\n    # It can have multiple items." +
                "\n    Sound Item:" +
                "\n      Material:" +
                "\n        - 'MUSIC_DISC_13'" +
                "\n        - 'MUSIC_DISC_CAT'" +
                "\n        - 'MUSIC_DISC_BLOCKS'" +
                "\n        - 'MUSIC_DISC_CHIRP'" +
                "\n        - 'MUSIC_DISC_FAR'" +
                "\n        - 'MUSIC_DISC_MALL'" +
                "\n        - 'MUSIC_DISC_MELLOHI'" +
                "\n        - 'MUSIC_DISC_STAL'" +
                "\n        - 'MUSIC_DISC_STRAD'" +
                "\n        - 'MUSIC_DISC_WARD'" +
                "\n        - 'MUSIC_DISC_WAIT'" +
                "\n      Glowing: false" +
                "\n" +
                "\nCommands:" +
                "\n    Default:" +
                "\n      Max Per Page: 10" +
                "\n      Color: '&e'" +
                "\n      Alternate Color: '&8'" +
                "\n      Separator: '&f, '" +
                "\n" +
                "\nResource Packs:" +
                "\n  # Request player to download a ResourcePack on join." +
                "\n  Request: false" +
                "\n  # The URL of the ResourcePack. Must be a direct link." +
                "\n  URL: ''" +
                "\n  # If a player denies the download, this player will be kicked immediately." +
                "\n  Force:" +
                "\n    Enabled: false" +
                "\n    # Should the player be kicked even if the download of the ResourcePack is accepted but fails?" +
                "\n    Even If Download Fail: false" +
                "\n" +
                "\n# Pretty much explained by the name." +
                "\n# PlayMoreSounds console messages will support color codes." +
                "\n# All messages will be tagged as 'INFO' in console as well, even if it's an error, so be aware." +
                "\nShow Colored Messages In Console: true" +
                "\n" +
                "\n# Sends a fancy little message to console whenever a new Error Report file is generated." +
                "\nNotify New Error Reports to Console: true" +
                "\n"
                + "\n# Settings of playmoresounds's sound regions."
                + "\nSound Regions:"
                + "\n  Wand:"
                + "\n    Glowing: true"
                + "\n    Material: 'STICK'"
                + "\n    Name: '&6&l&nPMS Region Wand'"
                + "\n"
                + "\n# Update Scheduler configuration."
                + "\nUpdater:"
                + "\n  Enabled: false"
                + "\n  # Shows the check status to console."
                + "\n  Log: false"
                + "\n  # How much it should wait until the next checkage."
                + "\n  # h = hour, m = minute, s = second."
                + "\n  Period: 2h"
                + "\n"
                + "\n# The worlds that will not play any sounds."
                + "\nWorld-BlackList:"
                + "\n- 'sample'"
                + "\n- 'sample2'", PlayMoreSounds.DATA_FOLDER, "3.0.0#13"
        ));

        TYPES.put("deathtypes", new ConfigType("deathtypes.yml",
                "# Set a sound to play when a player die for a specific cause of death."
                        + "\n#"
                        + "\n# To set a sound, just create a configuration section with the name of the"
                        + "\n# cause of death or just copy the sample below."
                        + "\n# (Causes of death: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/entity/EntityDamageEvent.DamageCause.html"
                        + "\n#"
                        + "\n# Sample:"
                        + "\n#"
                        + "\n#MAGIC:"
                        + "\n#  Enabled: true"
                        + "\n#  #This should stop the sound set in sounds.yml"
                        + "\n#  Stop Other Sounds: true"
                        + "\n#  Sounds:"
                        + "\n#    #This should play for players who has a specific vip perm."
                        + "\n#    '0':"
                        + "\n#      Delay: 0"
                        + "\n#      Options:"
                        + "\n#        Permission Required: 'vip.customdeathsound.magic'"
                        + "\n#        Radius: 5.5"
                        + "\n#        Relative Location:"
                        + "\n#          BACK: 2.0"
                        + "\n#          UP: 1.0"
                        + "\n#      Pitch: 1.0"
                        + "\n#      Sound: 'ENTITY_WITHER_DEATH'"
                        + "\n#      Volume: 1.0"
                        + "\n#    #Since this event should stop the regular death sound for whoever dies by magic,"
                        + "\n#    #another sound need to be set so players that aren't vip can hear the regular."
                        + "\n#    '0':"
                        + "\n#      Delay: 0"
                        + "\n#      Options:"
                        + "\n#        Permission Required: 'player.everyplayerexceptvipshavethispermission'"
                        + "\n#        Radius: 0.0"
                        + "\n#      Pitch: 1.0"
                        + "\n#      Sound: 'ENTITY_WITHER_SPAWN'"
                        + "\n#      Volume: 1.0"
                        + "\n#"
                        + "\n# More information about sounds on sounds.yml" +
                        "\nVersion: '3.0.0#12'", getSoundsFolder(), "3.0.0#12"
        ));

        TYPES.put("entityhit", new ConfigType("entityhit.yml",
                "#  Specify an item and select the sound that should"
                        + "\n# play when a player hits a mob with this item in"
                        + "\n# his hand."
                        + "\n#"
                        + "\n#  See the name of all items:"
                        + "\n# https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html"
                        + "\n#  Items may change names or be removed through"
                        + "\n# versions, so if you aren't on the latest version"
                        + "\n# of MC, google the name of items of your version."
                        + "\n#"
                        + "\n#  Specify the name of the item or use ALL to get"
                        + "\n# all items, Contains=NAME to get all items that"
                        + "\n# contains that name, Ends=NAME to get all items"
                        + "\n# that ends with this name and Starts=NAME to get"
                        + "\n# all items that starts with this name."
                        + "\n#"
                        + "\n#  You can use a bracket to specify all items that"
                        + "\n# are exceptions to this ALL/Contains/Ends/Starts"
                        + "\n# sorter. You can use the sorter inside these"
                        + "\n# brackets too, however, you may not use other"
                        + "\n# brackets to create exceptions to sorters that are"
                        + "\n# inside other brackets, so there wont be a"
                        + "\n# bracket-seption."
                        + "\n#"
                        + "\n#  If you want more than one item to play the same"
                        + "\n# sound, use a comma. However, if you're using a"
                        + "\n# sorter, use the comma after the bracket exception"
                        + "\n# list, so the bracket exception list sticks on the"
                        + "\n# side of the sorter. Also, you can use commas"
                        + "\n# inside bracket exception lists too."
                        + "\n#"
                        + "\n#  Damager and Victim criteria: Set Damager to the"
                        + "\n# name of the entity that should hit the victim,"
                        + "\n# and Victim to the name of the victim that will be"
                        + "\n# damaged by the Damager, so this event will only"
                        + "\n# be triggered when this specific damager hits this"
                        + "\n# specific victim with the specified items on hand."
                        + "\n#  If you don't want to add this criteria, just"
                        + "\n# don't add this option to the sound."
                        + "\n#"
                        + "\n#  See the name of all entities:"
                        + "\n# https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html"
                        + "\n#  Entities may change names or be removed through"
                        + "\n# versions, so if you aren't on the latest version"
                        + "\n# of MC, google the name of entities of your version."
                        + "\n#"
                        + "\n# Ex: When a Player holding an item that starts with"
                        + "\n# DIAMOND or an item that ends with the name SWORD"
                        + "\n# and that's not GOLDEN_SWORD and not STONE_SWORD"
                        + "\n# hits a zombie:"
                        + "\n#"
                        + "\n#Ends=SWORD[GOLDEN_SWORD, STONE_SWORD], Starts=DIAMOND:"
                        + "\n#  Damager: PLAYER"
                        + "\n#  Victim: ZOMBIE"
                        + "\n#  #Play sound on the location of DAMAGER or VICTIM"
                        + "\n#  Location: DAMAGER"
                        + "\n#  Sounds:"
                        + "\n#    '0':"
                        + "\n#      Options:"
                        + "\n#        Radius: 3"
                        + "\n#      Sound: BLOCK_ANVIL_LAND"
                        + "\n#      Pitch: 1.5"
                        + "\n#      Volume: 0.4"
                        + "\n#"
                        + "\n# More information about sounds on sounds.yml" +
                        "\nVersion: '3.0.0#10'", getSoundsFolder(), "3.0.0#10"
        ));

        TYPES.put("gamemodes", new ConfigType("gamemodes.yml",
                "# Set a sound to play when you change your gamemode."
                        + "\n#"
                        + "\n# Sample:"
                        + "\n# (Take a note that this is a sample and the sounds may not be available"
                        + "\n# on your MC version.)"
                        + "\n#"
                        + "\n#CREATIVE: # The gamemode that you changed to."
                        + "\n#  Cancellable: true"
                        + "\n#  Enabled: true"
                        + "\n#  # Stops other sounds on sounds.yml from playing."
                        + "\n#  Stop Other Sounds: true"
                        + "\n#  Sounds:"
                        + "\n#    '0':"
                        + "\n#      Delay: 0"
                        + "\n#      Options:"
                        + "\n#        Radius: 0"
                        + "\n#      Pitch: 1"
                        + "\n#      Sound: BLOCK_NOTE_BLOCK_PLING"
                        + "\n#      Volume: 10"
                        + "\n#"
                        + "\n# This is a small sample. You can add more gamemodes and more options"
                        + "\n# to the sound options."
                        + "\n# More information about sounds on sounds.yml." +
                        "\nVersion: '3.0.0#12'", getSoundsFolder(), "3.0.0#12"
        ));

        TYPES.put("regions", new ConfigType("regions.yml",
                "# Set a sound to play when you enter, exit or stand on a specific region."
                        + "\n#"
                        + "\n# Sample:"
                        + "\n# (Take a note that this is a sample and the sounds may not be available"
                        + "\n# on your MC version.)"
                        + "\n#"
                        + "\n#PlayMoreSounds: # The region plugin."
                        + "\n#  Spawn: # The region name, replace the name here."
                        + "\n#    Enter: # When a player enters this region."
                        + "\n#      Cancellable: true"
                        + "\n#      Enabled: true"
                        + "\n#      Stop On Exit:"
                        + "\n#        Enabled: true # If enabled, the sound will be stopped when the player leaves the region."
                        + "\n#        Delay: 20 # The time to wait before stopping the sound."
                        + "\n#      Stop Other Sounds: true # If enabled, region sounds in sounds.yml won't be played."
                        + "\n#      Sounds:"
                        + "\n#        '0':"
                        + "\n#          Delay: 0"
                        + "\n#          Options:"
                        + "\n#            Radius: 0"
                        + "\n#          Pitch: 1"
                        + "\n#          Sound: BLOCK_NOTE_BLOCK_PLING"
                        + "\n#          Volume: 10"
                        + "\n#    Leave: # When a player exits this region."
                        + "\n#      Cancellable: true"
                        + "\n#      Enabled: true"
                        + "\n#      Stop Other Sounds: true"
                        + "\n#      Sounds:"
                        + "\n#        '0':"
                        + "\n#          Delay: 0"
                        + "\n#          Options:"
                        + "\n#            Radius: 0"
                        + "\n#          Pitch: 1"
                        + "\n#          Sound: BLOCK_NOTE_BLOCK_BASS"
                        + "\n#          Volume: 10"
                        + "\n#    Loop: # When a player enters the region, a loop will be triggered and play."
                        + "\n#      Cancellable: true"
                        + "\n#      Delay: 0 # Time in ticks to wait to start the loop once the player enters the region."
                        + "\n#      Enabled: true"
                        + "\n#      Period: 100 # Time in tick the loop will wait until playing the sound again."
                        + "\n#      # If you have a long song playing, when the player leaves this region, the song"
                        + "\n#      #will be stopped instead of playing until the end. This setting applies to sounds only,"
                        + "\n#      #the loop function is stopped automatically."
                        + "\n#      Stop On Exit:"
                        + "\n#        Delay: 10"
                        + "\n#        Enabled: true"
                        + "\n#      Stop Other Sounds:"
                        + "\n#        RegionsYML: true # If enabled, region enter sounds in regions.yml won't be played."
                        + "\n#        SoundsYML: true # If enabled, region enter sounds in sounds.yml won't be played."
                        + "\n#      Sounds:"
                        + "\n#        '0':"
                        + "\n#          Delay: 0"
                        + "\n#          Options:"
                        + "\n#            Radius: 0"
                        + "\n#          Pitch: 1"
                        + "\n#          Sound: BLOCK_NOTE_BLOCK_BASS"
                        + "\n#          Volume: 10"
                        + "\n#"
                        + "\n# You can only play sounds in PMS native regions. To play to other plugins, search for compatibility"
                        + "\n#addons on https://www.spigotmc.org/resources/37429/"
                        + "\n#"
                        + "\n# More information about sounds on sounds.yml." +
                        "\nVersion: '3.0.0#12'", getSoundsFolder(), "3.0.0#12"
        ));

        TYPES.put("sounds", new ConfigType("sounds.yml",
                "# Set a sound to play when a player triggers an event."
                        + "\n#"
                        + "\n# Here you can set sounds to play when a event is triggered or create a"
                        + "\n# sound to play with a command in-game."
                        + "\n#"
                        + "\n# To set a sound to be played by command, create a section with any name and"
                        + "\n# add the sound options. In-game, execute the following command:"
                        + "\n# /playmoresounds play Config.SECTION_NAME_HERE"
                        + "\n# as SECTION_NAME_HERE is the name of the section you created."
                        + "\n#"
                        + "\n# To set a sound to be played when a player triggers a event, create a"
                        + "\n# section with the name of the event and add the sound options."
                        + "\n# Available PMS events: [Bed Enter, Bed Leave, Change Held Item, Change Level,"
                        + "\n# Craft Item, First Join, Furnace Extract, Game Mode Change,"
                        + "\n# Inventory Click, Join Server, Leave Server, Player Ban, Player Chat,"
                        + "\n# Send Command, Player Death, Drop Item, Start Flying, Stop Flying"
                        + "\n# Player Kicked, Teleport, Region Enter, Region Leave]."
                        + "\n#"
                        + "\n# -> Event and multiple sound sample:"
                        + "\n#"
                        + "\n#Teleport: # The event that when triggered a sound will be played."
                        + "\n#  # Cancellable stops the sound from playing when another plugin cancels"
                        + "\n#  #the event. This boolean helps events to be compatible with other"
                        + "\n#  #plugins."
                        + "\n#  Cancellable: true"
                        + "\n#  # Tell if a sound should be played or not. In sounds.yml case, this"
                        + "\n#  #boolean helps with the server performance."
                        + "\n#  Enabled: true"
                        + "\n#  # This is the sound list. Set the sound name by the section name. By"
                        + "\n#  #default I left with numbers just to organize things, but it can be"
                        + "\n#  #any name you want as long as it respects YAML section name rules."
                        + "\n#  Sounds:"
                        + "\n#    '0':"
                        + "\n#      # This is the time in ticks the sound will wait before playing."
                        + "\n#      # By default most of them is 0, so the sound plays immediately."
                        + "\n#      Delay: 0"
                        + "\n#      # Sounds can have multiple options, all of them are optional."
                        + "\n#      Options:"
                        + "\n#        # Even if a player has toggled it's sounds to off, the sound will"
                        + "\n#        #be played."
                        + "\n#        Ignore Toggle: true"
                        + "\n#        # You can say if you want the sound to be played in player's EYE"
                        + "\n#         #location."
                        + "\n#        # Remember that eye location changes when crouching."
                        + "\n#        Eye Location: false"
                        + "\n#        # The sound will be only listened by who have this permission."
                        + "\n#        Permission To Listen: 'pms.listen.playerteleport'"
                        + "\n#        # The sound will be only played to who have this permission."
                        + "\n#        Permission Required: 'pms.reproduce.playerteleport'"
                        + "\n#        # Set how blocks far the sound will be listenable by players."
                        + "\n#        # To play to everyone in the server, set to -1. To play to"
                        + "\n#        #everyone in the world of the player, set to -2. To play to the"
                        + "\n#        #player itself, set to 0. To play to players around the player, set"
                        + "\n#        #a value greater than 0."
                        + "\n#        Radius: 15.2"
                        + "\n#        # You can specify in blocks the location of the sound. The sound"
                        + "\n#        #can be played to player's Front, Back, Right, Left, Up and Down."
                        + "\n#        #It's not necessary to add the ones that you wont use."
                        + "\n#        Relative Location:"
                        + "\n#          FRONT: 1.3"
                        + "\n#          BACK: 0.1"
                        + "\n#          RIGHT: 0.8"
                        + "\n#          LEFT: 4.2"
                        + "\n#          UP: 0.13"
                        + "\n#          DOWN: 1.0"
                        + "\n#      # Set how pitchy the sound will be. Values greater than 2 don't have"
                        + "\n#      #any difference."
                        + "\n#      Pitch: 1.0"
                        + "\n#      # Here you can add a Sound Type or a sound modifier (check below)."
                        + "\n#      # Check Sound Type names on https://www.spigotmc.org/resources/37429/"
                        + "\n#      # Sound Types are not the same thing as bukkit sounds and sounds can be"
                        + "\n#      #available or not depending on your version."
                        + "\n#      Sound: 'BLOCK_NOTE_BLOCK_PLING'"
                        + "\n#      # Minecraft volume is the distance the sound can be heard. For the"
                        + "\n#      #player who played, volume has only effect when the value is lower than 1."
                        + "\n#      #For near players, volume 1 = 15 blocks."
                        + "\n#      # Volumes may or may not be available depending on your sound modifier."
                        + "\n#      Volume: 0.7"
                        + "\n#"
                        + "\n# -> Resource pack sound sample:"
                        + "\n#"
                        + "\n# To play a resource pack sound, simply add the custom sound name into the \"Sound:\""
                        + "\n#setting."
                        + "\n#"
                        + "\n#Teleport:"
                        + "\n#  Enabled: true"
                        + "\n#  Sounds:"
                        + "\n#    '0':"
                        + "\n#      Delay: 0"
                        + "\n#      Options:"
                        + "\n#        Radius: 0.0"
                        + "\n#      Pitch: 1.0"
                        + "\n#      Sound: 'customsoundname'"
                        + "\n#      Volume: 10"
                        + "\n"
                        + "\n# Config version. Please don't change."
                        + "\nVersion: '3.0.0#12'"
                        + "\n"
                        + "\nBed Enter:"
                        + "\n  Cancellable: true"
                        + "\n  Enabled: true"
                        + "\n  Sounds:"
                        + "\n    '0':"
                        + "\n      Delay: 0"
                        + "\n      Options:"
                        + "\n        Radius: 15.0"
                        + "\n      Pitch: 0.65"
                        + "\n      Sound: 'ENTITY_VILLAGER_AMBIENT'"
                        + "\n      Volume: 0.5"
                        + "\n"
                        + "\nBed Leave:"
                        + "\n  Enabled: true"
                        + "\n  Sounds:"
                        + "\n    '0':"
                        + "\n      Delay: 0"
                        + "\n      Options:"
                        + "\n        Radius: 0.0"
                        + "\n      Pitch: 1.0"
                        + "\n      Sound: 'ENTITY_CHICKEN_HURT'"
                        + "\n      Volume: 0.2"
                        + "\n"
                        + "\nChange Held Item:"
                        + "\n  Cancellable: false"
                        + "\n  Enabled: true"
                        + "\n  Sounds:"
                        + "\n    '0':"
                        + "\n      Delay: 0"
                        + "\n      Options:"
                        + "\n        Radius: 0.0"
                        + "\n      Pitch: 2.0"
                        + "\n      Sound: 'BLOCK_NOTE_BLOCK_HAT'"
                        + "\n      Volume: 10"
                        + "\n"
                        + "\n# These are disabled, but to play a sound just add the regular sound setting like the one above."
                        + "\nChange Level:"
                        + "\n  Enabled: false"
                        + "\n"
                        + "\nCraft Item:"
                        + "\n  Enabled: false"
                        + "\n"
                        + "\nFirst Join:"
                        + "\n  Enabled: true"
                        + "\n  Sounds:"
                        + "\n    '0':"
                        + "\n      Delay: 0"
                        + "\n      Options:"
                        + "\n        Radius: -1.0"
                        + "\n      Pitch: 1.0"
                        + "\n      Sound: 'BLOCK_NOTE_BLOCK_PLING'"
                        + "\n      Volume: 10"
                        + "\n    '1':"
                        + "\n      Delay: 5"
                        + "\n      Options:"
                        + "\n        Radius: -1.0"
                        + "\n      Pitch: 1.0"
                        + "\n      Sound: 'BLOCK_NOTE_BLOCK_PLING'"
                        + "\n      Volume: 10"
                        + "\n    '2':"
                        + "\n      Delay: 15"
                        + "\n      Options:"
                        + "\n        Radius: -1.0"
                        + "\n      Pitch: 2.0"
                        + "\n      Sound: 'BLOCK_NOTE_BLOCK_PLING'"
                        + "\n      Volume: 10"
                        + "\n"
                        + "\nFurnace Extract:"
                        + "\n  Enabled: true"
                        + "\n  Sounds:"
                        + "\n    '0':"
                        + "\n      Delay: 0"
                        + "\n      Options:"
                        + "\n        Radius: 16.0"
                        + "\n      Pitch: 1.0"
                        + "\n      Sound: 'ENTITY_GENERIC_EXTINGUISH_FIRE'"
                        + "\n      Volume: 0.6"
                        + "\n"
                        + "\nGame Mode Change:"
                        + "\n  Cancellable: true"
                        + "\n  Enabled: true"
                        + "\n  Sounds:"
                        + "\n    '0':"
                        + "\n      Delay: 0"
                        + "\n      Options:"
                        + "\n        Radius: 0.0"
                        + "\n      Pitch: 1.0"
                        + "\n      Sound: 'BLOCK_ANVIL_LAND'"
                        + "\n      Volume: 10.0"
                        + "\n"
                        + "\nInventory Click:"
                        + "\n  Cancellable: true"
                        + "\n  Enabled: true"
                        + "\n  Sounds:"
                        + "\n    '0':"
                        + "\n      Delay: 0"
                        + "\n      Options:"
                        + "\n        Radius: 0.0"
                        + "\n      Pitch: 1.0"
                        + "\n      Sound: 'UI_BUTTON_CLICK'"
                        + "\n      Volume: 0.4"
                        + "\n"
                        + "\nInventory Close:"
                        + "\n  Enabled: true"
                        + "\n  Sounds:"
                        + "\n    '0':"
                        + "\n      Delay: 0"
                        + "\n      Options:"
                        + "\n        Radius: 0.0"
                        + "\n      Pitch: 2.0"
                        + "\n      Sound: 'UI_TOAST_OUT'"
                        + "\n      Volume: 10.0"
                        + "\n"
                        + "\nJoin Server:"
                        + "\n  Enabled: true"
                        + "\n  Sounds:"
                        + "\n    '0':"
                        + "\n      Delay: 0"
                        + "\n      Options:"
                        + "\n        Radius: -1.0"
                        + "\n      Pitch: 1.0"
                        + "\n      Sound: 'BLOCK_NOTE_BLOCK_PLING'"
                        + "\n      Volume: 10.0"
                        + "\n"
                        + "\nLeave Server:"
                        + "\n  Enabled: true"
                        + "\n  Sounds:"
                        + "\n    '0':"
                        + "\n      Delay: 0"
                        + "\n      Options:"
                        + "\n        Radius: -1.0"
                        + "\n      Pitch: 1.0"
                        + "\n      Sound: 'BLOCK_NOTE_BLOCK_BASS'"
                        + "\n      Volume: 10.0"
                        + "\n"
                        + "\nPlayer Ban:"
                        + "\n  Enabled: true"
                        + "\n  Sounds:"
                        + "\n    '0':"
                        + "\n      Delay: 0"
                        + "\n      Options:"
                        + "\n        Radius: -1.0"
                        + "\n      Pitch: 1.3"
                        + "\n      Sound: 'ENTITY_ENDER_DRAGON_DEATH'"
                        + "\n      Volume: 10.0"
                        + "\n"
                        + "\nPlayer Chat:"
                        + "\n  Cancellable: true"
                        + "\n  Enabled: true"
                        + "\n  Sounds:"
                        + "\n    '0':"
                        + "\n      Delay: 0"
                        + "\n      Options:"
                        + "\n        Radius: -1.0"
                        + "\n      Pitch: 1.0"
                        + "\n      Sound: 'ENTITY_ITEM_PICKUP'"
                        + "\n      Volume: 10.0"
                        + "\n"
                        + "\nSend Command:"
                        + "\n  Cancellable: false"
                        + "\n  Enabled: true"
                        + "\n  Sounds:"
                        + "\n    '0':"
                        + "\n      Delay: 0"
                        + "\n      Options:"
                        + "\n        Radius: 0.0"
                        + "\n      Pitch: 1.5"
                        + "\n      Sound: 'ENTITY_ITEM_PICKUP'"
                        + "\n      Volume: 10.0"
                        + "\n"
                        + "\nPlayer Death:"
                        + "\n  Enabled: true"
                        + "\n  Sounds:"
                        + "\n    '0':"
                        + "\n      Delay: 0"
                        + "\n      Options:"
                        + "\n        Radius: 0.0"
                        + "\n      Pitch: 1.0"
                        + "\n      Sound: 'ENTITY_WITHER_SPAWN'"
                        + "\n      Volume: 10.0"
                        + "\n"
                        + "\nDrop Item:"
                        + "\n  Cancellable: true"
                        + "\n  Enabled: true"
                        + "\n  Sounds:"
                        + "\n    '0':"
                        + "\n      Delay: 0"
                        + "\n      Options:"
                        + "\n        Radius: 0.0"
                        + "\n      Pitch: 1.0"
                        + "\n      Sound: 'ENTITY_EGG_THROW'"
                        + "\n      Volume: 0.5"
                        + "\n"
                        + "\nStart Flying:"
                        + "\n  Cancellable: true"
                        + "\n  Enabled: true"
                        + "\n  Sounds:"
                        + "\n    '0':"
                        + "\n      Delay: 0"
                        + "\n      Options:"
                        + "\n        Radius: 12.0"
                        + "\n        Relative Location:"
                        + "\n          UP: 2.0"
                        + "\n      Pitch: 1.0"
                        + "\n      Sound: 'BLOCK_PISTON_EXTEND'"
                        + "\n      Volume: 0.5"
                        + "\n"
                        + "\nStop Flying:"
                        + "\n  Cancellable: true"
                        + "\n  Enabled: true"
                        + "\n  Sounds:"
                        + "\n    '0':"
                        + "\n      Delay: 0"
                        + "\n      Options:"
                        + "\n        Radius: 12.0"
                        + "\n        Relative Location:"
                        + "\n          DOWN: 1.0"
                        + "\n      Pitch: 1.0"
                        + "\n      Sound: 'BLOCK_PISTON_CONTRACT'"
                        + "\n      Volume: 0.5"
                        + "\n"
                        + "\nPlayer Kicked:"
                        + "\n  Enabled: true"
                        + "\n  Sounds:"
                        + "\n    '0':"
                        + "\n      Delay: 0"
                        + "\n      Options:"
                        + "\n        Radius: -1.0"
                        + "\n      Pitch: 1.3"
                        + "\n      Sound: 'ENTITY_ENDER_DRAGON_HURT'"
                        + "\n      Volume: 10.0"
                        + "\n"
                        + "\nTeleport:"
                        + "\n  Cancellable: true"
                        + "\n  Enabled: true"
                        + "\n  Sounds:"
                        + "\n    '0':"
                        + "\n      Delay: 0"
                        + "\n      Options:"
                        + "\n        Radius: 16.0"
                        + "\n      Pitch: 2.0"
                        + "\n      Sound: 'BLOCK_PORTAL_TRAVEL'"
                        + "\n      Volume: 0.3"
                        + "\n"
                        + "\n# These are disabled, but to play a sound just add the regular sound setting like the one above."
                        + "\nRegion Enter:"
                        + "\n  Enabled: false"
                        + "\n#  Stop On Exit:"
                        + "\n#    Enabled: true # If enabled, the sound will be stopped when the player leaves the region."
                        + "\n#    Delay: 20 # The time to wait before stopping the sound."
                        + "\n"
                        + "\nRegion Leave:"
                        + "\n  Enabled: false", PlayMoreSounds.DATA_FOLDER, "3.0.0#12"
        ));

        String worldData = "# Set a sound to play when a world reaches a specific time of the day."
                + "\n#"
                + "\n# Sample:"
                + "\n#";

        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment().equals(World.Environment.NORMAL)) {
                worldData = worldData + "\n#" + w.getName() + ": # The world that you want to track the time";
                break;
            }
        }

        worldData = worldData + "\n#  '13000': # The time that you want to play a sound."
                + "\n#    Enabled: true"
                + "\n#    Sounds:"
                + "\n#      '0':"
                + "\n#        Delay: 0"
                + "\n#        Options:"
                + "\n#          Radius: -2.0 # The radius is counted by the world's spawn location. Set to -2 by default so everyone in the world hear it."
                + "\n#        Pitch: 1.0"
                + "\n#        Sound: 'AMBIENT_CAVE'"
                + "\n#        Volume: 10.0"
                + "\n#"
                + "\n# More informafion about sounds on sounds.yml" +
                "\nVersion: '3.0.0#12'";

        TYPES.put("worldtimer", new ConfigType("worldtimer.yml", worldData, getSoundsFolder(), "3.0.0#12"));
    }

    public static HashSet<ConfigType> getTypes()
    {
        return new HashSet<>(TYPES.values());
    }

    public static Path getSoundsFolder()
    {
        return PlayMoreSounds.DATA_FOLDER.resolve("Sounds");
    }

    public static HashMap<String, String> getHardCodedLang()
    {
        HashMap<String, String> hardCodedLang = new HashMap<>();

        //TODO: Add all language keys to HashMap

        return hardCodedLang;
    }
}
