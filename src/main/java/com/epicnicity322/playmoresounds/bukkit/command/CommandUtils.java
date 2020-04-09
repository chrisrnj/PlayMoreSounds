package com.epicnicity322.playmoresounds.bukkit.command;

import com.epicnicity322.epicpluginlib.lang.MessageSender;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;

public class CommandUtils
{
    /**
     * Gets all players by name in a string. Supports multiple players by adding commas, using "radius=" or "everyone".
     *
     * @param sender                  The sender of the command.
     * @param args                    The args of the command.
     * @param targetPosition          The position where the target is in the args array.
     * @param invalidArgsMsgToConsole The invalid args message with formatted variables.
     * @return The targets get by name or null if an error occurred and a message was sent.
     */
    @Nullable
    public static HashSet<Player> getTargets(@NotNull CommandSender sender, @NotNull String[] args, int targetPosition,
                                             @NotNull String invalidArgsMsgToConsole, @NotNull String permissionOthers)
    {
        MessageSender lang = PlayMoreSounds.MESSAGE_SENDER;
        HashSet<Player> targets = new HashSet<>();

        if (args.length <= targetPosition) {
            if (sender instanceof Player) {
                targets.add((Player) sender);
            } else {
                lang.send(sender, true, invalidArgsMsgToConsole);
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
                            lang.send(sender, true, lang.get("General.Nobody Online"));
                            return null;
                        } else {
                            targets.addAll(online);
                        }
                    } else {
                        lang.send(sender, true, lang.get("General.No Permission"));
                        return null;
                    }
                    break;

                case "self":
                case "me":
                case "myself":
                    if (sender instanceof Player) {
                        targets.add((Player) sender);
                    } else {
                        lang.send(sender, true, lang.get("General.Not A Player"));
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
                                lang.send(sender, true, lang.get("General.Not A Number"));
                                return null;
                            }
                        } else {
                            lang.send(sender, true, lang.get("General.Not A Player"));
                            return null;
                        }
                    } else {
                        String[] names = target.split(",");

                        for (String name : names) {
                            Player player = Bukkit.getPlayer(name);

                            if (player == null) {
                                lang.send(sender, true, lang.get("General.Player Not Found").replace(
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
                    lang.send(sender, true, lang.get("General.No Permission"));
                    return null;
                }
            }
        } else if (targets.size() > 1) {
            if (!sender.hasPermission(permissionOthers)) {
                lang.send(sender, true, lang.get("General.No Permission"));
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
    public static String getWho(HashSet<Player> players, CommandSender sender)
    {
        MessageSender lang = PlayMoreSounds.MESSAGE_SENDER;

        if (players.size() == 1) {
            if (players.iterator().next() == sender) {
                return lang.get("General.You");
            }
        }

        if (players.containsAll(Bukkit.getOnlinePlayers())) {
            return lang.get("General.Everyone");
        }

        StringBuilder names = new StringBuilder();
        int count = 1;

        for (Player player : players) {
            if (count == players.size()) {
                names.append(player.getName());
            } else {
                if (count == players.size() - 1) {
                    names.append(player.getName()).append(", ").append(lang.get("General.And")).append(" ");
                } else {
                    names.append(player.getName()).append(", ");
                }
            }

            ++count;
        }

        return names.toString();
    }
}
