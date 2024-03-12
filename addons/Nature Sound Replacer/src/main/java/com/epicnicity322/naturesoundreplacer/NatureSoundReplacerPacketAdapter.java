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

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.epicnicity322.epicpluginlib.core.config.ConfigurationHolder;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsVersion;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.sound.SoundType;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class NatureSoundReplacerPacketAdapter extends PacketAdapter {
    public static final @NotNull ConfigurationHolder natureSoundReplacerConfig;
    private static final @NotNull HashMap<String, PlayableRichSound> sounds = new HashMap<>();
    private static NatureSoundReplacerPacketAdapter instance;
    private static boolean registered = false;

    static {
        natureSoundReplacerConfig = new ConfigurationHolder(Configurations.BIOMES.getConfigurationHolder().getPath().getParent().resolve("nature sound replacer.yml"), """
                # Replace any sound played by nature in your server.
                #
                #  When a sound here is played, PlayMoreSounds interrupts the sound packets from being sent to the
                # players and plays the sound set here instead. This way you can take advantage of PlayMoreSounds
                # features, like play multiple sounds, delayed sounds, toggleable sounds, permissible sounds,
                # resource pack sounds etc.
                #
                # Warnings:
                # >> ProtocolLib is required for this feature to work.
                # >> Only sounds played by the server are replaceable, sounds played to the client (like walking or
                # building) are replaceable only if the source is another player that's not you.
                #
                #  To replace a sound, create a section with the sound name and set the replacing sound in it, for
                # example:
                #
                #ENTITY_ZOMBIE_HURT: # This is the sound that I want to replace.
                #  Enabled: true
                #  Sounds: # The sounds that will play instead.
                #    '0':
                #      Delay: 0
                #      Options:
                #        Ignores Disabled: true
                #        #Permission Required: '' # Permission Required is available but it's not recommended, use Permission To Listen instead.
                #        Permission To Listen: 'listen.zombiehurt'
                #        Radius: 0.0 # Radius > 0 is not recommended
                #      Pitch: 0.5
                #      Sound: ENTITY_SKELETON_HURT
                #      Volume: 1.0
                #
                #  If you want to completely stop a sound from being played in your server, add as in the example:
                #
                #ENTITY_ZOMBIE_AMBIENT: # This is the sound that I want to stop from playing in my server.
                #  Enabled: true
                #  #Sounds: # Don't add 'Sounds' section since you don't want sounds to play.
                #
                #  A more in depth tutorial of all sound options can be found in sounds.yml file.
                #  If you have any other doubts on how to set this configuration up, feel free to ask in
                # PlayMoreSounds' discord: https://discord.gg/eAHPbc3

                Version: '$version'""".replace("$version", PlayMoreSoundsVersion.version));
    }

    private NatureSoundReplacerPacketAdapter(@NotNull PlayMoreSounds plugin) {
        // It is changing sounds so priority is set to lowest.
        super(plugin, ListenerPriority.LOWEST, PacketType.Play.Server.NAMED_SOUND_EFFECT);
    }

    public synchronized static void loadNatureSoundReplacer(@NotNull PlayMoreSounds plugin) {
        if (instance == null) instance = new NatureSoundReplacerPacketAdapter(plugin);

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        boolean anySoundEnabled = false;

        sounds.clear();

        for (Map.Entry<String, Object> node : natureSoundReplacerConfig.getConfiguration().getNodes().entrySet()) {
            String key = node.getKey();
            Object value = node.getValue();

            if (!(value instanceof ConfigurationSection section)) continue;
            if (!section.getBoolean("Enabled").orElse(false)) continue;

            SoundType type;

            try {
                type = SoundType.valueOf(key);
            } catch (IllegalArgumentException ex) {
                PlayMoreSounds.getConsoleLogger().log("&cInvalid sound to replace on nature sound replacer.yml: " + key);
                continue;
            }

            String bukkitSound = toBukkit(type);

            if (bukkitSound == null) {
                PlayMoreSounds.getConsoleLogger().log("&cInvalid sound to replace on nature sound replacer.yml: " + key);
                continue;
            }

            try {
                sounds.put(bukkitSound, new PlayableRichSound(section));
                anySoundEnabled = true;
            } catch (IllegalArgumentException ignored) {
                //Not a sound.
            }
        }

        if (anySoundEnabled) {
            if (!registered) {
                protocolManager.addPacketListener(instance);
                registered = true;
            }
        } else {
            if (registered) {
                protocolManager.removePacketListener(instance);
                registered = false;
            }
        }
    }

    private static String toBukkit(SoundType type) {
        Optional<String> soundKey = type.getSound();

        if (soundKey.isEmpty()) return null;

        String soundKeyString = soundKey.get();

        for (Sound sound : Sound.values()) {
            if (soundKeyString.equals(sound.getKey().getKey())) {
                return sound.name();
            }
        }

        return null;
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        PlayableRichSound sound = sounds.get(event.getPacket().getSoundEffects().read(0).name());

        if (sound != null) {
            Player player = event.getPlayer();
            StructureModifier<Integer> xyz = event.getPacket().getIntegers();

            event.setCancelled(true);
            // The server multiplies the xyz by 8 before sending the packet.
            sound.play(player, new Location(player.getWorld(), xyz.read(0) / 8.0, xyz.read(1) / 8.0, xyz.read(2) / 8.0));
        }
    }
}
