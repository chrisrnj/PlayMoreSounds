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
import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.epicpluginlib.core.util.StringUtils;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public final class ConfirmSubCommand extends Command implements Helpable
{
    private static final @NotNull HashMap<CommandSender, LinkedHashMap<Runnable, String>> pendingConfirmations = new HashMap<>();
    private static final @NotNull MessageSender lang = PlayMoreSounds.getLanguage();

    public static void addPendingConfirmation(@NotNull CommandSender sender, @NotNull Runnable confirmation, @NotNull String description)
    {
        LinkedHashMap<Runnable, String> confirmations = pendingConfirmations.get(sender);

        if (confirmations == null) confirmations = new LinkedHashMap<>();

        confirmations.put(confirmation, description);
        pendingConfirmations.put(sender, confirmations);
    }

    @Override
    public @NotNull CommandRunnable onHelp()
    {
        return (label, sender, args) -> lang.send(sender, false, lang.get("Help.Confirm").replace("<label>", label));
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
        return (label, sender, args) -> lang.send(sender, lang.get("General.No Permission"));
    }

    @Override
    public void run(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args)
    {
        LinkedHashMap<Runnable, String> confirmations = pendingConfirmations.get(sender);

        if (confirmations == null || confirmations.isEmpty()) {
            lang.send(sender, lang.get("Confirm.Error.Nothing Pending"));
            return;
        }

        long id = 1;

        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("list")) {
                lang.send(sender, lang.get("Confirm.List.Header"));

                for (Map.Entry<Runnable, String> confirmation : confirmations.entrySet())
                    lang.send(sender, false, lang.get("Confirm.List.Confirmation").replace("<id>", Long.toString(id++))
                            .replace("<description>", confirmation.getValue()));

                return;
            } else if (StringUtils.isNumeric(args[1])) {
                id = Long.parseLong(args[1]);
            } else {
                lang.send(sender, lang.get("General.Invalid Arguments")
                        .replace("<label>", label).replace("<label2>", args[0])
                        .replace("<args>", "[list|id]"));
                return;
            }
        }

        long l = 1;

        for (Runnable runnable : new LinkedHashSet<>(confirmations.keySet())) {
            if (l++ == id) {
                runnable.run();
                confirmations.remove(runnable);
                break;
            }
        }
    }
}
