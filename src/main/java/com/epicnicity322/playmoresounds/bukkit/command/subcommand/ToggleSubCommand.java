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
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.command.CommandUtils;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public final class ToggleSubCommand extends Command implements Helpable
{
    @Override
    public @NotNull CommandRunnable onHelp()
    {
        return (label, sender, args) -> PlayMoreSounds.getLanguage().send(sender, false, PlayMoreSounds.getLanguage().get("Help.Toggle").replace("<label>", label));
    }

    @Override
    public @NotNull String getName()
    {
        return "toggle";
    }

    @Override
    public @Nullable String getPermission()
    {
        return "playmoresounds.toggle";
    }

    @Override
    protected @Nullable CommandRunnable getNoPermissionRunnable()
    {
        return (label, sender, args) -> PlayMoreSounds.getLanguage().send(sender, PlayMoreSounds.getLanguage().get("General.No Permission"));
    }

    private String getInvalidArgsMessage(String label, CommandSender sender, String[] args)
    {
        var lang = PlayMoreSounds.getLanguage();
        return lang.get("General.Invalid Arguments")
                .replace("<label>", label).replace("<label2>", args[0])
                .replace("<args>", (sender instanceof Player ?
                        "[" + lang.get("General.Target") + "]" : "<" + lang.get("General.Target") + ">") +
                        " [on|off|toggle]");
    }

    @Override
    public void run(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args)
    {
        var lang = PlayMoreSounds.getLanguage();
        Boolean on = null;
        var invalidArgsMessage = getInvalidArgsMessage(label, sender, args);
        HashSet<Player> targets = CommandUtils.getTargets(sender, args, 1, invalidArgsMessage,
                "playmoresounds.toggle.others");

        if (targets == null)
            return;

        if (args.length > 2) {
            if (args[2].equalsIgnoreCase("on"))
                on = true;
            else if (args[2].equalsIgnoreCase("off"))
                on = false;
            else if (!args[2].equalsIgnoreCase("toggle")) {
                lang.send(sender, invalidArgsMessage);
                return;
            }
        }

        if (on == null) {
            HashSet<Player> toOff = new HashSet<>();
            HashSet<Player> toOn = new HashSet<>();

            for (Player player : targets) {
                if (SoundManager.getSoundsState(player)) {
                    SoundManager.toggleSoundsState(player, false);
                    toOff.add(player);
                } else {
                    SoundManager.toggleSoundsState(player, true);
                    toOn.add(player);
                }
            }

            if (!toOff.isEmpty()) {
                String who = CommandUtils.getWho(toOff, sender);

                if (who.equals(lang.get("General.You")))
                    lang.send(sender, lang.get("Toggle.Disabled.Default"));
                else
                    lang.send(sender, lang.get("Toggle.Disabled.Player").replace("<target>", who));
            }
            if (!toOn.isEmpty()) {
                String who = CommandUtils.getWho(toOn, sender);

                if (who.equals(lang.get("General.You")))
                    lang.send(sender, lang.get("Toggle.Enabled.Default"));
                else
                    lang.send(sender, lang.get("Toggle.Enabled.Player").replace("<target>", who));
            }
        } else {
            String who = CommandUtils.getWho(targets, sender);
            String mode = "Enabled";

            if (on) {
                for (Player player : targets)
                    SoundManager.toggleSoundsState(player, true);
            } else {
                for (Player player : targets)
                    SoundManager.toggleSoundsState(player, false);

                mode = "Disabled";
            }

            if (who.equals(lang.get("General.You")))
                lang.send(sender, lang.get("Toggle." + mode + ".Default"));
            else
                lang.send(sender, lang.get("Toggle." + mode + ".Player").replace("<player>", who));
        }
    }
}
