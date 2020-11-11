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
import com.epicnicity322.playmoresounds.bukkit.listener.OnPlayerInteract;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public final class DiscSubCommand extends Command implements Helpable
{
    private static final @NotNull MessageSender lang = PlayMoreSounds.getMessageSender();

    @Override
    public @NotNull CommandRunnable onHelp()
    {
        return (label, sender, args) -> lang.send(sender, false, lang.get("Help.Disc").replace("<label>", label));
    }

    @Override
    public @NotNull String getName()
    {
        return "disc";
    }

    @Override
    public @Nullable String[] getAliases()
    {
        return new String[]{"musicdisc"};
    }

    @Override
    protected @Nullable CommandRunnable getNoPermissionRunnable()
    {
        return (label, sender, args) -> lang.send(sender, lang.get("General.No Permission"));
    }

    @Override
    public @Nullable String getPermission()
    {
        return "playmoresounds.disc.give";
    }

    private String getInvalidArgsMessage(String label, CommandSender sender, String[] args)
    {
        return lang.get("General.Invalid Arguments")
                .replace("<label>", label).replace("<label2>", args[0])
                .replace("<args>", "<" + lang.get("General.Id") + "> " +
                        (sender instanceof Player ? "[" + lang.get("General.Target") + "]" : "<" + lang.get("General.Target") + ">"));
    }

    @Override
    public void run(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args)
    {
        String invalidArgsMessage = getInvalidArgsMessage(label, sender, args);
        HashSet<Player> targets = CommandUtils.getTargets(sender, args, 2, invalidArgsMessage,
                "playmoresounds.disc.give.others");

        if (targets == null)
            return;

        if (args.length < 2) {
            lang.send(sender, invalidArgsMessage);
            return;
        }

        ItemStack disc = OnPlayerInteract.getCustomDisc(args[1]);

        if (disc == null) {
            lang.send(sender, lang.get("Disc.Error.Not Found").replace("<id>", args[1]));
            return;
        }

        for (Player player : targets)
            player.getInventory().addItem(disc);

        lang.send(sender, lang.get("Disc.Success").replace("<id>", args[1]).replace("<target>", CommandUtils.getWho(targets, sender)));
    }
}
