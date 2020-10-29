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

package com.epicnicity322.playmoresounds.bukkit.command;

import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class CommandUtils
{
    private static final @NotNull MessageSender lang = PlayMoreSounds.getMessageSender();

    private CommandUtils()
    {
    }

    /**
     * Gets all players by name in a string. Supports multiple players by adding commas, using "radius=" or "everyone".
     *
     * @param sender                  The sender of the command.
     * @param args                    The args of the command.
     * @param targetPosition          The position where the target is in the args array.
     * @param invalidArgsMsgToConsole The invalid args message with formatted variables.
     * @param permissionOthers        The permission to check if the sender has.
     * @return The targets get by name or null if an error occurred and a message was sent.
     */
    @Nullable
    public static HashSet<Player> getTargets(@NotNull CommandSender sender, @NotNull String[] args, int targetPosition,
                                             @NotNull String invalidArgsMsgToConsole, @NotNull String permissionOthers)
    {
        HashSet<Player> targets = new HashSet<>();

        if (args.length <= targetPosition) {
            if (sender instanceof Player) {
                targets.add((Player) sender);
            } else {
                lang.send(sender, invalidArgsMsgToConsole);
                return null;
            }
        } else {
            String target = args[targetPosition];

            switch (target.toLowerCase()) {
                case "all":
                case "everybody":
                case "everyone":
                case "online":
                case "*":
                    if (sender.hasPermission(permissionOthers)) {
                        Collection<? extends Player> online = Bukkit.getOnlinePlayers();

                        if (online.size() == 0) {
                            lang.send(sender, lang.get("General.Nobody Online"));
                            return null;
                        } else {
                            targets.addAll(online);
                        }
                    } else {
                        lang.send(sender, lang.get("General.No Permission"));
                        return null;
                    }
                    break;

                case "self":
                case "me":
                case "myself":
                    if (sender instanceof Player) {
                        targets.add((Player) sender);
                    } else {
                        lang.send(sender, lang.get("General.Not A Player"));
                        return null;
                    }
                    break;

                default:
                    if (target.toLowerCase().startsWith("radius=") && target.length() > 7) {
                        if (sender instanceof Player) {
                            String number = target.substring(target.indexOf("=") + 1);

                            try {
                                double radius = Double.parseDouble(number);
                                targets.addAll(SoundManager.getInRange(radius, ((Player) sender).getLocation()));
                            } catch (NumberFormatException e) {
                                lang.send(sender, lang.get("General.Not A Number"));
                                return null;
                            }
                        } else {
                            lang.send(sender, lang.get("General.Not A Player"));
                            return null;
                        }
                    } else {
                        String[] names = target.split(",");

                        for (String name : names) {
                            Player player = Bukkit.getPlayer(name);

                            if (player == null) {
                                lang.send(sender, lang.get("General.Player Not Found").replace(
                                        "<player>", name));
                                return null;
                            }

                            targets.add(player);
                        }
                    }
                    break;
            }
        }

        if (targets.size() == 1) {
            if (targets.iterator().next() != sender) {
                if (!sender.hasPermission(permissionOthers)) {
                    lang.send(sender, lang.get("General.No Permission"));
                    return null;
                }
            }
        } else if (targets.size() > 1) {
            if (!sender.hasPermission(permissionOthers)) {
                lang.send(sender, lang.get("General.No Permission"));
                return null;
            }
        }

        return targets;
    }

    /**
     * Gets who is in the set, returns "you" message if the set contains only the sender or "everyone" message if the set
     * contains everyone online in the server.
     *
     * @param players The set to get the who.
     * @param sender  The sender of the command.
     * @return The names of who is in the set.
     */
    public static String getWho(@NotNull Set<Player> players, @NotNull CommandSender sender)
    {
        if (players.size() == 1) {
            Player theOne = players.iterator().next();

            if (theOne == sender)
                return lang.get("General.You");
            else
                return theOne.getName();
        }

        if (players.containsAll(Bukkit.getOnlinePlayers()))
            return lang.get("General.Everyone");

        StringBuilder names = new StringBuilder();
        int count = 1;

        for (Player player : players) {
            if (count == players.size())
                names.append(player.getName());
            else if (count == players.size() - 1)
                names.append(player.getName()).append(", ").append(lang.get("General.And")).append(" ");
            else
                names.append(player.getName()).append(", ");

            ++count;
        }

        return names.toString();
    }
}
