/*
 * PlayMoreSounds - A bukkit plugin that manages and plays sounds.
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

package com.epicnicity322.playmoresounds.bukkit.command.subcommands;

import com.epicnicity322.epicpluginlib.bukkit.command.Command;
import com.epicnicity322.epicpluginlib.bukkit.command.CommandRunnable;
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.command.CommandLoader;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class HelpSubCommand extends Command implements Helpable {
    @Override
    public @NotNull String getName() {
        return "help";
    }

    @Override
    public @Nullable String getPermission() {
        return "playmoresounds.help";
    }

    @Override
    public @NotNull CommandRunnable onHelp() {
        return (label, sender, args) -> PlayMoreSounds.getLanguage().send(sender, false, PlayMoreSounds.getLanguage().get("Help.Help").replace("<label>", label));
    }

    @Override
    protected @Nullable CommandRunnable getNoPermissionRunnable() {
        return (label, sender, args) -> PlayMoreSounds.getLanguage().send(sender, PlayMoreSounds.getLanguage().get("General.No Permission"));
    }

    @Override
    public void run(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args) {
        var lang = PlayMoreSounds.getLanguage();
        lang.send(sender, lang.get("Help.Header").replace("<page>", "1").replace("<totalpages>", "1"));

        for (var command : CommandLoader.getCommands()) {
            if (command instanceof Helpable) {
                if (sender.hasPermission(command.getPermission())) {
                    try {
                        ((Helpable) command).onHelp().run(label, sender, args);
                    } catch (Throwable t) {
                        PlayMoreSounds.getConsoleLogger().log("Something went wrong when trying to run onHelp for command: " + command.getName(), ConsoleLogger.Level.WARN);
                        PlayMoreSoundsCore.getErrorHandler().report(t, "On Help for command " + command.getName() + ":");
                    }
                }
            }
        }
    }
}
