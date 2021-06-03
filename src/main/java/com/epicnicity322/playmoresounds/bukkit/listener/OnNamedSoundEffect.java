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
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class OnNamedSoundEffect extends PacketAdapter
{
    private final @NotNull HashMap<String, RichSound> sounds = new HashMap<>();

    public OnNamedSoundEffect(@NotNull PlayMoreSounds plugin)
    {
        // It is changing sounds so priority is set to lowest.
        super(plugin, ListenerPriority.LOWEST, PacketType.Play.Server.NAMED_SOUND_EFFECT);
        OnNamedSoundEffect onNamedSoundEffect = this;

        Runnable loader = new Runnable()
        {
            private final @NotNull ProtocolManager manager = ProtocolLibrary.getProtocolManager();
            private boolean registered = false;

            @Override
            public void run()
            {
                sounds.clear();

                Configuration natureSoundsReplacer = Configurations.NATURE_SOUNDS_REPLACER.getConfigurationHolder().getConfiguration();

                for (Map.Entry<String, Object> toReplace : natureSoundsReplacer.getNodes().entrySet()) {
                    if (toReplace.getValue() instanceof ConfigurationSection) {
                        ConfigurationSection section = (ConfigurationSection) toReplace.getValue();

                        if (section.getBoolean("Enabled").orElse(false))
                            sounds.put(toReplace.getKey(), new RichSound(section));
                    }
                }

                if (sounds.isEmpty()) {
                    if (registered) {
                        manager.removePacketListener(onNamedSoundEffect);
                        registered = false;
                    }
                } else {
                    if (!registered) {
                        manager.addPacketListener(onNamedSoundEffect);
                        registered = true;
                    }
                }
            }
        };

        loader.run();
        PlayMoreSounds.onReload(loader);
    }

    @Override
    public void onPacketSending(PacketEvent event)
    {
        RichSound sound = sounds.get(event.getPacket().getSoundEffects().read(0).name());

        if (sound != null) {
            event.setCancelled(true);
            sound.play(event.getPlayer());
        }
    }
}