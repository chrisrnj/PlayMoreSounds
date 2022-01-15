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
import com.epicnicity322.playmoresounds.bukkit.command.CommandUtils;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundManager;
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import com.epicnicity322.playmoresounds.core.sound.SoundType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public final class StopSoundSubCommand extends Command implements Helpable
{
    private static final @NotNull MessageSender lang = PlayMoreSounds.getLanguage();

    @Override
    public @NotNull CommandRunnable onHelp()
    {
        return (label, sender, args) -> lang.send(sender, false, lang.get("Help.Stop Sound").replace("<label>", label));
    }

    @Override
    public @NotNull String getName()
    {
        return "stopsound";
    }

    @Override
    public @Nullable String[] getAliases()
    {
        return new String[]{"stop", "stopsounds"};
    }

    @Override
    protected @Nullable CommandRunnable getNoPermissionRunnable()
    {
        return (label, sender, args) -> lang.send(sender, lang.get("General.No Permission"));
    }

    @Override
    public @Nullable String getPermission()
    {
        return "playmoresounds.stopsound";
    }

    @Override
    public void run(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args)
    {
        HashSet<Player> targets = CommandUtils.getTargets(sender, args, 1, lang.get("General.Invalid Arguments")
                        .replace("<label>", label).replace("<label2>", args[0])
                        .replace("<args>", (sender instanceof Player ?
                                "[" + lang.get("General.Target") + "]" : "<" + lang.get("General.Target") + ">")),
                "playmoresounds.stopsound.others");
        HashSet<String> toStop = null;

        if (targets == null)
            return;

        if (VersionUtils.hasStopSound() && args.length > 2) {
            toStop = new HashSet<>();

            for (String sound : args[2].split(","))
                if (SoundType.getPresentSoundNames().contains(sound.toUpperCase()))
                    toStop.add(SoundType.valueOf(sound.toUpperCase()).getSound().orElse(""));
                else
                    toStop.add(sound);
        }

        for (Player player : targets)
            SoundManager.stopSounds(player, toStop, 0);

        String who = CommandUtils.getWho(targets, sender);

        if (toStop == null)
            lang.send(sender, lang.get("Stop Sound.Success.All").replace("<target>", who));
        else
            lang.send(sender, lang.get("Stop Sound.Success.Default").replace("<target>", who).replace("<sounds>", toStop.toString()));
    }
}
