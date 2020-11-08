/*
 * Copyright (c) 2020 Christiano Rangel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.epicnicity322.playmoresounds.bukkit.command.subcommand;

import com.epicnicity322.epicpluginlib.bukkit.command.Command;
import com.epicnicity322.epicpluginlib.bukkit.command.CommandRunnable;
import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.command.CommandUtils;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public final class CheckSubCommand extends Command implements Helpable
{
    private static final @NotNull MessageSender lang = PlayMoreSounds.getMessageSender();

    @Override
    public @NotNull CommandRunnable onHelp()
    {
        return (label, sender, args) -> lang.send(sender, false, lang.get("Help.Check").replace("<label>", label));
    }

    @Override
    public @NotNull String getName()
    {
        return "check";
    }

    @Override
    public @Nullable String[] getAliases()
    {
        return new String[]{"sounds"};
    }

    @Override
    protected @Nullable CommandRunnable getNoPermissionRunnable()
    {
        return (label, sender, args) -> lang.send(sender, lang.get("General.No Permission"));
    }

    @Override
    public @Nullable String getPermission()
    {
        return "playmoresounds.toggle.check";
    }

    @Override
    public void run(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args)
    {
        HashSet<Player> targets = CommandUtils.getTargets(sender, args, 1, lang.get("General.Invalid Arguments")
                        .replace("<label>", label).replace("<label2>", args[0])
                        .replace("<args>", (sender instanceof Player ?
                                "[" + lang.get("General.Target") + "]" : "<" + lang.get("General.Target") + ">")),
                "playmoresounds.toggle.check");

        if (targets == null)
            return;

        HashSet<Player> enabledSounds = new HashSet<>();
        HashSet<Player> disabledSounds = new HashSet<>();

        for (Player player : targets)
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
