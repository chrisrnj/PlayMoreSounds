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
import com.epicnicity322.epicpluginlib.core.util.StringUtils;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class ConfirmSubCommand extends Command implements Helpable
{
    static @Nullable HashMap<UUID, LinkedHashMap<Runnable, String>> pendingConfirmations;

    /**
     * Adds a runnable that will run when the player gives confirmation. Players will be able to list and confirm
     * runnables through the command "/pms confirm". Runnables are removed once the player confirms them.
     *
     * @param who          The {@link UUID} of the player you want to add a confirmation, null if it's for console.
     * @param confirmation The runnable to run when the player confirms.
     * @param description  The description of what will have when the player confirms this runnable.
     */
    public static void addPendingConfirmation(@Nullable UUID who, @NotNull Runnable confirmation, @NotNull String description)
    {
        if (pendingConfirmations == null) pendingConfirmations = new HashMap<>();
        pendingConfirmations.computeIfAbsent(who, k -> new LinkedHashMap<>()).put(confirmation, description);
    }

    /**
     * Adds a runnable that will run when the player gives confirmation. Players will be able to list and confirm
     * runnables through the command "/pms confirm". Runnables are removed once the player confirms them.
     * <p>
     * This has the same effect as {@link #addPendingConfirmation(UUID, Runnable, String)}. If the sender is a player,
     * then their {@link UUID} is get, if not, then null is used to store confirmations.
     *
     * @param who          The sender you want to add a confirmation.
     * @param confirmation The runnable to run when the player confirms.
     * @param description  The description of what will have when the player confirms this runnable.
     */
    public static void addPendingConfirmation(@NotNull CommandSender who, @NotNull Runnable confirmation, @NotNull String description)
    {
        addPendingConfirmation(who instanceof Player player ? player.getUniqueId() : null, confirmation, description);
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
        UUID uuid = sender instanceof Player player ? player.getUniqueId() : null;

        if (pendingConfirmations == null || (confirmations = pendingConfirmations.get(uuid)) == null || confirmations.isEmpty()) {
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
                    PlayMoreSounds.getConsoleLogger().log("Something went wrong when trying to confirm \"" + desc + "\" for " + uuid + ".", ConsoleLogger.Level.WARN);
                    PlayMoreSoundsCore.getErrorHandler().report(t, "Confirm Description: \"" + desc + "\"\nOn Confirm for " + uuid + " Error:");
                }
                confirmations.remove(r);
                return;
            }
        }

        lang.send(sender, lang.get("Confirm.Error.Not Found").replace("<id>", args[1]).replace("<label>", label));
    }
}
