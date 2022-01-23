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
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.inventory.AddonsInventory;
import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public final class AddonsSubCommand extends Command implements Helpable
{
    @Override
    public @NotNull String getName()
    {
        return "addons";
    }

    @Override
    public @Nullable String[] getAliases()
    {
        return new String[]{"addon"};
    }

    @Override
    public @Nullable String getPermission()
    {
        return "playmoresounds.addons";
    }

    @Override
    protected @Nullable CommandRunnable getNoPermissionRunnable()
    {
        return (label, sender, args) -> PlayMoreSounds.getLanguage().send(sender, PlayMoreSounds.getLanguage().get("General.No Permission"));
    }

    @Override
    public @NotNull CommandRunnable onHelp()
    {
        return (label, sender, args) -> PlayMoreSounds.getLanguage().send(sender, false, PlayMoreSounds.getLanguage().get("Help.Addons").replace("<label>", label));
    }

    @Override
    public void run(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args)
    {
        MessageSender lang = PlayMoreSounds.getLanguage();

        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("list")) {
                if (!sender.hasPermission("playmoresounds.addons.list")) {
                    lang.send(sender, lang.get("General.No Permission"));
                    return;
                }

                lang.send(sender, lang.get("Addons.List.Header"));

                HashSet<PMSAddon> addons = PlayMoreSounds.getAddonManager().getAddons();
                StringBuilder data = new StringBuilder();
                int count = 0;

                for (PMSAddon addon : addons) {
                    data.append(addon.isLoaded() ? "&a" : "&c").append(addon.getDescription().getName());

                    if (++count != addons.size()) {
                        data.append(lang.get("Addons.List.Separator", "&f, "));
                    }
                }

                lang.send(sender, false, data.toString());
            } else {
                lang.send(sender, lang.get("General.Invalid Arguments").replace("<label>", label).replace("<label2>", args[0]).replace("<args>", "<list|start|stop>"));
            }

            return;
        }

        if (!(sender instanceof HumanEntity)) {
            lang.send(sender, lang.get("General.Not A Player"));
            return;
        }
        if (!sender.hasPermission("playmoresounds.addons.management")) {
            lang.send(sender, lang.get("General.No Permission"));
            return;
        }

        new AddonsInventory().openInventory((HumanEntity) sender);
    }
}