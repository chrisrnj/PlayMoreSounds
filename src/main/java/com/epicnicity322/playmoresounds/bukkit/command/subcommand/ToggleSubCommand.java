package com.epicnicity322.playmoresounds.bukkit.command.subcommand;

import com.epicnicity322.epicpluginlib.command.Command;
import com.epicnicity322.epicpluginlib.lang.MessageSender;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.command.CommandUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class ToggleSubCommand implements Command
{
    @Override
    public String getName()
    {
        return "toggle";
    }

    @Override
    public void onCommand(String label, CommandSender sender, String[] args)
    {
        MessageSender lang = PlayMoreSounds.MESSAGE_SENDER;

        if (sender.hasPermission("playmoresounds.toggle")) {
            HashSet<Player> targets;
            Boolean on = null;

            targets = CommandUtils.getTargets(sender, args, 1, lang.get("General.Invalid Arguments")
                            .replace("<label>", label).replace("<label2>", args[0]).replace("<args>",
                            "<" + lang.get("General.Target") + "> [on|off|toggle]"),
                    "playmoresounds.toggle.others");

            if (targets == null) {
                return;
            }

            if (args.length > 2) {
                if (args[2].equalsIgnoreCase("on")) {
                    on = true;
                } else if (args[2].equalsIgnoreCase("off")) {
                    on = false;
                } else if (!args[2].equalsIgnoreCase("toggle")) {
                    lang.send(sender, true, lang.get("General.Invalid Arguments")
                            .replace("<label>", label).replace("<label2>", args[0])
                            .replace("<args>", (sender instanceof Player ?
                                    "[" + lang.get("General.Target") + "]" : "<" + lang.get("General.Target") + ">") +
                                    " [on|off|toggle]"));
                    return;
                }
            }

            if (on == null) {
                HashSet<Player> toOff = new HashSet<>();
                HashSet<Player> toOn = new HashSet<>();

                for (Player player : targets) {
                    if (PlayMoreSounds.IGNORED_PLAYERS.contains(player.getName())) {
                        PlayMoreSounds.IGNORED_PLAYERS.remove(player.getName());
                        toOn.add(player);
                    } else {
                        PlayMoreSounds.IGNORED_PLAYERS.add(player.getName());
                        toOff.add(player);
                    }
                }

                if (!toOff.isEmpty()) {
                    String who = CommandUtils.getWho(toOff, sender);

                    if (who.equals(lang.get("General.You"))) {
                        lang.send(sender, true, lang.get("Toggle.Disabled.Default"));
                    } else {
                        lang.send(sender, true, lang.get("Toggle.Disabled.Player").replace("<target>", who));
                    }
                }
                if (!toOn.isEmpty()) {
                    String who = CommandUtils.getWho(toOn, sender);

                    if (who.equals(lang.get("General.You"))) {
                        lang.send(sender, true, lang.get("Toggle.Enabled.Default"));
                    } else {
                        lang.send(sender, true, lang.get("Toggle.Enabled.Player").replace("<target>", who));
                    }
                }
            } else {
                String who = CommandUtils.getWho(targets, sender);

                if (on) {
                    for (Player player : targets) {
                        PlayMoreSounds.IGNORED_PLAYERS.remove(player.getName());
                    }

                    if (who.equals(lang.get("General.You"))) {
                        lang.send(sender, true, lang.get("Toggle.Enabled.Default"));
                    } else {
                        lang.send(sender, true, lang.get("Toggle.Enabled.Player").replace("<player>", who));
                    }
                } else {
                    for (Player player : targets) {
                        PlayMoreSounds.IGNORED_PLAYERS.add(player.getName());
                    }

                    if (who.equals(lang.get("General.You"))) {
                        lang.send(sender, true, lang.get("Toggle.Disabled.Default"));
                    } else {
                        lang.send(sender, true, lang.get("Toggle.Disabled.Player").replace("<player>", who));
                    }
                }
            }
        } else {
            lang.send(sender, true, lang.get("General.No Permission"));
        }
    }
}
