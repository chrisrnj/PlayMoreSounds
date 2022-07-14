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

package com.epicnicity322.customdiscs;

import com.epicnicity322.epicpluginlib.bukkit.command.Command;
import com.epicnicity322.epicpluginlib.bukkit.command.CommandRunnable;
import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.command.CommandUtils;
import com.epicnicity322.playmoresounds.bukkit.command.subcommands.Helpable;
import com.epicnicity322.playmoresounds.bukkit.listeners.OnPlayerInteract;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public final class DiscCommand extends Command implements Helpable {
    private static final @NotNull String[] aliases = new String[]{"discs", "customdiscs"};

    @Override
    public @NotNull CommandRunnable onHelp() {
        return (label, sender, args) -> PlayMoreSounds.getLanguage().send(sender, false, PlayMoreSounds.getLanguage().get("Custom Discs.Help").replace("<label>", label));
    }

    @Override
    public @NotNull String getName() {
        return "disc";
    }

    @Override
    public @Nullable String[] getAliases() {
        return aliases;
    }

    @Override
    protected @NotNull CommandRunnable getNoPermissionRunnable() {
        return (label, sender, args) -> PlayMoreSounds.getLanguage().send(sender, PlayMoreSounds.getLanguage().get("General.No Permission"));
    }

    @Override
    public @NotNull String getPermission() {
        return "playmoresounds.disc.give";
    }

    private String getInvalidArgsMessage(String label, CommandSender sender, String[] args) {
        MessageSender lang = PlayMoreSounds.getLanguage();
        return lang.get("General.Invalid Arguments")
                .replace("<label>", label).replace("<label2>", args[0])
                .replace("<args>", "<" + lang.get("General.Id") + "> " +
                        (sender instanceof Player ? "[" + lang.get("General.Target") + "]" : "<" + lang.get("General.Target") + ">"));
    }

    @Override
    public void run(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args) {
        MessageSender lang = PlayMoreSounds.getLanguage();
        String invalidArgsMessage = getInvalidArgsMessage(label, sender, args);
        HashSet<Player> targets = CommandUtils.getTargets(sender, args, 2, invalidArgsMessage,
                "playmoresounds.disc.give.others");

        if (targets == null)
            return;

        if (args.length < 2) {
            lang.send(sender, invalidArgsMessage);
            return;
        }

        ItemStack disc = CustomDiscs.getCustomDisc(args[1]);

        if (disc == null) {
            lang.send(sender, lang.get("Custom Discs.Error.Not Found").replace("<id>", args[1]));
            return;
        }

        for (Player player : targets)
            player.getInventory().addItem(disc);

        lang.send(sender, lang.get("Custom Discs.Success").replace("<id>", args[1]).replace("<target>", CommandUtils.getWho(targets, sender)));
    }
}
