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

package com.epicnicity322.soundcommands;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.events.PlaySoundEvent;
import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class SoundCommands extends PMSAddon implements Listener
{
    @Override
    protected void onStart()
    {
        Bukkit.getPluginManager().registerEvents(this, PlayMoreSounds.getInstance());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlaySound(PlaySoundEvent event)
    {
        ConfigurationSection section = event.getSound().getSection();

        if (section != null) {
            Player player = event.getSourcePlayer();

            if (player != null && section.contains("Execute Commands.Player"))
                for (String command : section.getCollection("Execute Commands.Player", Object::toString))
                    Bukkit.dispatchCommand(player, command);

            if (section.contains("Execute Commands.Console")) {
                if (!section.getBoolean("Execute Commands.Prevent if player is null").orElse(true) || player != null) {
                    CommandSender console = Bukkit.getConsoleSender();
                    String playerName = player == null ? "null" : player.getName();

                    for (String command : section.getCollection("Execute Commands.Console", Object::toString))
                        Bukkit.dispatchCommand(console, command.replace("<player>", playerName));
                }
            }
        }
    }
}
