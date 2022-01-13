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

package com.epicnicity322.playmoresounds.bukkit.command;

import com.epicnicity322.epicpluginlib.bukkit.command.Command;
import com.epicnicity322.epicpluginlib.bukkit.command.CommandManager;
import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.command.subcommand.*;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsVersion;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;

public final class CommandLoader
{
    private static final @NotNull LinkedHashSet<Command> commands = new LinkedHashSet<>();

    static {
        PlayMoreSounds.onInstance(() -> {
            PlayMoreSounds plugin = PlayMoreSounds.getInstance();
            MessageSender lang = PlayMoreSounds.getLanguage();

            commands.add(new AddonsSubCommand());
            commands.add(new CheckSubCommand());
            commands.add(new ConfirmSubCommand());
            commands.add(new DiscSubCommand());
            commands.add(new HelpSubCommand());
            //commands.add(new FinderSubCommand());

            // List command requires the server to run spigot.
            try {
                Class.forName("net.md_5.bungee.api.chat.BaseComponent");
                commands.add(new ListSubCommand());
            } catch (ClassNotFoundException ignored) {
            }

            commands.add(new PlaySubCommand());
            commands.add(new RegionSubCommand(plugin));
            commands.add(new ReloadSubCommand());
            commands.add(new StopSoundSubCommand());
            commands.add(new ToggleSubCommand());
            commands.add(new UpdateSubCommand(plugin));

            CommandManager.registerCommand(Bukkit.getPluginCommand("playmoresounds"), commands,
                    (label, sender, args) -> {
                        lang.send(sender, false, lang.get("Description.Header").replace("<version>", PlayMoreSoundsVersion.version));
                        lang.send(sender, false, "&6Author: &7Epicnicity322");
                        lang.send(sender, false, "&6Description: &7" + plugin.getDescription().getDescription());

                        if (sender.hasPermission("playmoresounds.help"))
                            lang.send(sender, false, lang.get("Description.Help").replace("<label>", label));
                        else
                            lang.send(sender, false, lang.get("Description.No Permission"));
                    },
                    (label, sender, args) -> lang.send(sender, lang.get("General.Unknown Command").replace("<label>", label)));
        });
    }

    private CommandLoader()
    {
    }

    /**
     * Adds a sub command to PlayMoreSounds' main command.
     *
     * @param command The command to add.
     */
    public static void addCommand(@NotNull Command command)
    {
        commands.add(command);
    }

    /**
     * @return An immutable set of PlayMoreSounds' registered sub commands.
     */
    public static @NotNull LinkedHashSet<Command> getCommands()
    {
        return new LinkedHashSet<>(commands);
    }
}
