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

package com.epicnicity322.nbssongplayer;

import com.epicnicity322.epicpluginlib.bukkit.command.Command;
import com.epicnicity322.epicpluginlib.bukkit.command.CommandRunnable;
import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.command.CommandUtils;
import com.epicnicity322.playmoresounds.bukkit.command.subcommands.Helpable;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public class NBSCommand extends Command implements Helpable
{
    private static final @NotNull MessageSender lang = PlayMoreSounds.getLanguage();

    @Override
    public @NotNull String getName()
    {
        return "nbssongplayer";
    }

    @Override
    public @NotNull CommandRunnable onHelp()
    {
        return (label, sender, args) -> lang.send(sender, false, lang.get("NBS Song Player.Help").replace("<label>", label));
    }

    @Override
    public @Nullable String[] getAliases()
    {
        return new String[]{"nbs", "noteblocksongs"};
    }

    @Override
    public @Nullable String getPermission()
    {
        return "playmoresounds.nbssongplayer";
    }

    @Override
    protected @Nullable CommandRunnable getNoPermissionRunnable()
    {
        return (label, sender, args) -> lang.send(sender, lang.get("General.No Permission"));
    }

    @Override
    public void run(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args)
    {
        String invalidArgs = lang.get("General.Invalid Arguments").replace("<label>", label).replace("<label2>", args[0]).replace("<args>", "play|stop");

        if (args.length < 2) {
            lang.send(sender, invalidArgs);
            return;
        }

        if (args[1].equalsIgnoreCase("stop")) {
            HashSet<Player> targets = CommandUtils.getTargets(sender, args, 2, lang.get("General.Invalid Arguments")
                            .replace("<label>", label).replace("<label2>", args[0]).replace("<args>", "stop <" + lang.get("General.Target") + ">"),
                    "playmoresounds.nbssongplayer.others");

            if (targets == null)
                return;

            for (Player target : targets)
                NBSSongPlayer.stop(target, null);

            lang.send(sender, lang.get("NBS Song Player.Stop.Success").replace("<target>", CommandUtils.getWho(targets, sender)));
        } else if (args[1].equalsIgnoreCase("play")) {
            String invalidPlayArgs = lang.get("General.Invalid Arguments").replace("<label>", label).replace("<label2>", args[0]).replace("<args>", "play <" + lang.get("General.Target") + "> <song>");

            if (args.length < 4) {
                lang.send(sender, invalidPlayArgs);
                return;
            }

            HashSet<Player> targets = CommandUtils.getTargets(sender, args, 2, invalidPlayArgs, "playmoresounds.nbssongplayer.others");

            if (targets == null)
                return;

            String nbs = args[3];

            if (args.length > 4) {
                StringBuilder builder = new StringBuilder(nbs);

                for (int i = 4; i < args.length; ++i)
                    builder.append(" ").append(args[i]);

                nbs = builder.toString().trim();
            }

            if (NBSSongPlayer.getSongNames().contains(nbs)) {
                for (Player target : targets) {
                    NBSSongPlayer.play(target, nbs);
                }

                lang.send(sender, lang.get("NBS Song Player.Play.Success").replace("<target>", CommandUtils.getWho(targets, sender)).replace("<song>", nbs));
            } else {
                lang.send(sender, lang.get("NBS Song Player.Play.Error.Invalid Song").replace("<song>", ""));
            }
        } else {
            lang.send(sender, invalidArgs);
        }
    }
}
