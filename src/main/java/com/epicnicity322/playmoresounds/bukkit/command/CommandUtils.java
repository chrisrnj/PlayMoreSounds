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

package com.epicnicity322.playmoresounds.bukkit.command;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public final class CommandUtils
{
    // Lists useful for tab completion
    private static final @NotNull List<String> targetAllArgs = Arrays.asList("*", "all", "everybody", "everyone", "online");
    private static final @NotNull List<String> targetSelfArgs = Arrays.asList("i", "me", "myself", "self");
    private static final @NotNull ArrayList<String> targetArgs = targetAllArgs.stream().collect(Collectors.toCollection(() -> new ArrayList<>(targetSelfArgs)));

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
        var targets = new HashSet<Player>();
        var lang = PlayMoreSounds.getLanguage();

        if (args.length <= targetPosition) {
            if (sender instanceof Player player) {
                targets.add(player);
            } else {
                lang.send(sender, invalidArgsMsgToConsole);
                return null;
            }
        } else {
            String target = args[targetPosition];

            switch (target.toLowerCase()) {
                case "*", "all", "everybody", "everyone", "online" -> {
                    if (sender.hasPermission(permissionOthers)) {
                        var online = Bukkit.getOnlinePlayers();

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
                }

                case "i", "me", "myself", "self" -> {
                    if (sender instanceof Player player) {
                        targets.add(player);
                    } else {
                        lang.send(sender, lang.get("General.Not A Player"));
                        return null;
                    }
                }

                default -> {
                    if (target.toLowerCase().startsWith("radius=") && target.length() > 7) {
                        if (sender instanceof Player) {
                            var number = target.substring(target.indexOf("=") + 1);

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
                        var names = target.split(",");

                        for (String name : names) {
                            var player = Bukkit.getPlayer(name);

                            if (player == null) {
                                lang.send(sender, lang.get("General.Player Not Found").replace("<player>", name));
                                return null;
                            }

                            targets.add(player);
                        }
                    }
                }
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
        var lang = PlayMoreSounds.getLanguage();

        if (players.size() == 1) {
            Player theOne = players.iterator().next();

            if (theOne == sender)
                return lang.get("General.You");
            else
                return theOne.getName();
        }

        if (players.containsAll(Bukkit.getOnlinePlayers()))
            return lang.get("General.Everyone");

        var names = new StringBuilder();
        int count = 1;

        for (var player : players) {
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

    public static void addTargetTabCompletion(@NotNull ArrayList<String> possibleCompletions, @NotNull String argument, @NotNull CommandSender sender, @NotNull String permissionOthers)
    {
        List<String> list;

        if (sender.hasPermission(permissionOthers)) {
            list = new ArrayList<>(targetArgs);

            for (Player player : Bukkit.getOnlinePlayers()) {
                list.add(player.getName());
            }
        } else {
            list = new ArrayList<>(targetSelfArgs);
            if (sender instanceof Player) list.add(sender.getName());
        }

        for (var selfArg : list) {
            if (selfArg.toLowerCase(Locale.ROOT).startsWith(argument.toLowerCase(Locale.ROOT)))
                possibleCompletions.add(selfArg);
        }
    }
}
