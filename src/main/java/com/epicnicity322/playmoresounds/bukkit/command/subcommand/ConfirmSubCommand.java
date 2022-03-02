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

package com.epicnicity322.playmoresounds.bukkit.command.subcommand;

import com.epicnicity322.epicpluginlib.bukkit.command.Command;
import com.epicnicity322.epicpluginlib.bukkit.command.CommandRunnable;
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.core.util.StringUtils;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public final class ConfirmSubCommand extends Command implements Helpable
{
    private static @Nullable HashMap<CommandSender, LinkedHashMap<Runnable, String>> pendingConfirmations;

    public static void addPendingConfirmation(@NotNull CommandSender sender, @NotNull Runnable confirmation, @NotNull String description)
    {
        if (pendingConfirmations == null) pendingConfirmations = new HashMap<>();
        LinkedHashMap<Runnable, String> confirmations = pendingConfirmations.computeIfAbsent(sender, k -> new LinkedHashMap<>());

        confirmations.put(confirmation, description);
    }

    @Override
    public @NotNull CommandRunnable onHelp()
    {
        return (label, sender, args) -> PlayMoreSounds.getLanguage().send(sender, false, PlayMoreSounds.getLanguage().get("Help.Confirm").replace("<label>", label));
    }

    @Override
    public @NotNull String getName()
    {
        return "confirm";
    }

    @Override
    public @Nullable String getPermission()
    {
        return "playmoresounds.confirm";
    }

    @Override
    protected @Nullable CommandRunnable getNoPermissionRunnable()
    {
        return (label, sender, args) -> PlayMoreSounds.getLanguage().send(sender, PlayMoreSounds.getLanguage().get("General.No Permission"));
    }

    @Override
    public void run(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args)
    {
        var lang = PlayMoreSounds.getLanguage();
        LinkedHashMap<Runnable, String> confirmations;

        if (pendingConfirmations == null || (confirmations = pendingConfirmations.get(sender)) == null || confirmations.isEmpty()) {
            lang.send(sender, lang.get("Confirm.Error.Nothing Pending"));
            return;
        }

        int id = 1;

        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("list")) {
                lang.send(sender, lang.get("Confirm.List.Header"));

                for (Map.Entry<Runnable, String> confirmation : confirmations.entrySet())
                    lang.send(sender, false, lang.get("Confirm.List.Confirmation").replace("<id>", Integer.toString(id++))
                            .replace("<description>", confirmation.getValue()));

                return;
            } else if (StringUtils.isNumeric(args[1])) {
                try {
                    id = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    lang.send(sender, lang.get("General.Not A Number").replace("<number>", args[1]));
                }
            } else {
                lang.send(sender, lang.get("General.Invalid Arguments")
                        .replace("<label>", label).replace("<label2>", args[0])
                        .replace("<args>", "[list|<" + lang.get("General.Id") + ">]"));
                return;
            }
        }

        if (id < 0) {
            lang.send(sender, lang.get("Confirm.Error.Not Found").replace("<id>", args[1]).replace("<label>", label));
            return;
        }

        int i = 1;

        for (Map.Entry<Runnable, String> confirmation : new LinkedHashSet<>(confirmations.entrySet())) {
            if (i++ == id) {
                var r = confirmation.getKey();
                var desc = confirmation.getValue();

                try {
                    r.run();
                } catch (Throwable t) {
                    PlayMoreSounds.getConsoleLogger().log("Something went wrong when trying to confirm \"" + desc + "\" for " + sender.getName() + ".", ConsoleLogger.Level.WARN);
                    PlayMoreSoundsCore.getErrorHandler().report(t, "Confirm Description: \"" + desc + "\"\nOn Confirm for " + sender.getName() + " Error:");
                }
                confirmations.remove(r);
                return;
            }
        }

        lang.send(sender, lang.get("Confirm.Error.Not Found").replace("<id>", args[1]).replace("<label>", label));
    }
}
