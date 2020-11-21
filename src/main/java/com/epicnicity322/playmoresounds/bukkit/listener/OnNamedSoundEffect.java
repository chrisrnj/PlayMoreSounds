/*
 * Copyright (c) 2020 Christiano Rangel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.epicnicity322.playmoresounds.bukkit.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.command.subcommand.ReloadSubCommand;
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

                Configuration natureSoundsReplacer = Configurations.NATURE_SOUNDS_REPLACER.getPluginConfig().getConfiguration();

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
        ReloadSubCommand.addOnReloadRunnable(loader);
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