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
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.command.CommandUtils;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public final class CheckSubCommand extends Command implements Helpable {
    @Override
    public @NotNull CommandRunnable onHelp() {
        return (label, sender, args) -> PlayMoreSounds.getLanguage().send(sender, false, PlayMoreSounds.getLanguage().get("Help.Check").replace("<label>", label));
    }

    @Override
    public @NotNull String getName() {
        return "check";
    }

    @Override
    public @Nullable String[] getAliases() {
        return new String[]{"sounds"};
    }

    @Override
    protected @Nullable CommandRunnable getNoPermissionRunnable() {
        return (label, sender, args) -> PlayMoreSounds.getLanguage().send(sender, PlayMoreSounds.getLanguage().get("General.No Permission"));
    }

    @Override
    public @Nullable String getPermission() {
        return "playmoresounds.toggle.check";
    }

    @Override
    public void run(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args) {
        var lang = PlayMoreSounds.getLanguage();
        HashSet<Player> targets = CommandUtils.getTargets(sender, args, 1, lang.get("General.Invalid Arguments")
                .replace("<label>", label).replace("<label2>", args[0])
                .replace("<args>", (sender instanceof Player ? "[" + lang.get("General.Target") + "]" : "<" + lang.get("General.Target") + ">")), "playmoresounds.toggle.check.others");

        if (targets == null)
            return;

        var enabledSounds = new HashSet<Player>();
        var disabledSounds = new HashSet<Player>();

        for (var player : targets)
            if (SoundManager.getSoundsState(player))
                enabledSounds.add(player);
            else
                disabledSounds.add(player);

        if (!disabledSounds.isEmpty()) {
            String who = CommandUtils.getWho(disabledSounds, sender);

            if (who.equals(lang.get("General.You")))
                lang.send(sender, lang.get("Toggle.Check.Disabled.Default"));
            else
                lang.send(sender, lang.get("Toggle.Check.Disabled.Player").replace("<target>", who));
        }
        if (!enabledSounds.isEmpty()) {
            String who = CommandUtils.getWho(enabledSounds, sender);

            if (who.equals(lang.get("General.You")))
                lang.send(sender, lang.get("Toggle.Check.Enabled.Default"));
            else
                lang.send(sender, lang.get("Toggle.Check.Enabled.Player").replace("<target>", who));
        }
    }
}
