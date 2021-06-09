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

package com.epicnicity322.playmoresounds.bukkit.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
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

public final class NatureSoundReplacer extends PacketAdapter
{
    private static final @NotNull HashMap<String, PlayableRichSound> sounds = new HashMap<>();
    private static NatureSoundReplacer natureSoundReplacer;
    private static boolean registered = false;

    private NatureSoundReplacer(@NotNull PlayMoreSounds plugin)
    {
        // It is changing sounds so priority is set to lowest.
        super(plugin, ListenerPriority.LOWEST, PacketType.Play.Server.NAMED_SOUND_EFFECT);
    }

    public synchronized static void loadNatureSoundReplacer(@NotNull PlayMoreSounds plugin)
    {
        if (natureSoundReplacer == null) {
            natureSoundReplacer = new NatureSoundReplacer(plugin);
        }

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        boolean anySoundEnabled = false;

        sounds.clear();

        for (Map.Entry<String, Object> node : Configurations.NATURE_SOUND_REPLACER.getConfigurationHolder().getConfiguration().getNodes().entrySet()) {
            String key = node.getKey();
            Object value = node.getValue();

            if (!(value instanceof ConfigurationSection)) continue;

            ConfigurationSection section = (ConfigurationSection) value;

            if (!section.getBoolean("Enabled").orElse(false)) continue;

            SoundType type;

            try {
                type = SoundType.valueOf(key);
            } catch (IllegalArgumentException ex) {
                PlayMoreSounds.getConsoleLogger().log("&cInvalid sound to replace on nature sound replacer.yml: " + key);
                continue;
            }

            String bukkitSound;

            if (VersionUtils.hasSoundEffects()) {
                bukkitSound = toBukkit(type);
            } else {
                bukkitSound = type.getSound().orElse(null);
            }

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

        if (anySoundEnabled && !registered) {
            protocolManager.addPacketListener(natureSoundReplacer);
            registered = true;
        } else if (!anySoundEnabled && registered) {
            protocolManager.removePacketListener(natureSoundReplacer);
            registered = false;
        }
    }

    private static String toBukkit(SoundType type)
    {
        Optional<String> soundKey = type.getSound();

        if (!soundKey.isPresent()) return null;

        String soundKeyString = soundKey.get();

        for (Sound sound : Sound.values()) {
            if (soundKeyString.equals(sound.getKey().getKey())) {
                return sound.name();
            }
        }

        return null;
    }

    @Override
    public void onPacketSending(PacketEvent event)
    {
        PlayableRichSound sound;

        if (VersionUtils.hasSoundEffects()) {
            sound = sounds.get(event.getPacket().getSoundEffects().read(0).name());
        } else {
            sound = sounds.get(event.getPacket().getStrings().read(0));
        }

        if (sound != null) {
            Player player = event.getPlayer();
            StructureModifier<Integer> xyz = event.getPacket().getIntegers();

            event.setCancelled(true);
            // The server multiplies the xyz by 8 before sending the packet.
            sound.play(player, new Location(player.getWorld(), xyz.read(0) / 8.0, xyz.read(1) / 8.0, xyz.read(2) / 8.0));
        }
    }
}