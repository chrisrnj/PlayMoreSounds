package com.epicnicity322.playmoresounds.bukkit.command.subcommand;

import com.epicnicity322.epicpluginlib.command.Command;
import com.epicnicity322.epicpluginlib.lang.MessageSender;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.inventory.ListInventory;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class ListSubCommand implements Command
{
    public static HashSet<Player> toOpenAgain = new HashSet<>();
    public static HashSet<Player> openListGUIs = new HashSet<>();

    @Override
    public String getName()
    {
        return "list";
    }

    @Override
    public void onCommand(String label, CommandSender sender, String[] args)
    {
        MessageSender lang = PlayMoreSounds.MESSAGE_SENDER;

        if (sender.hasPermission("playmoresounds.list")) {
            boolean gui = sender instanceof Player && sender.hasPermission("playmoresounds.list.gui") &&
                    PlayMoreSounds.HAS_PERSISTENT_DATA_CONTAINER;
            int page = 1;
            String invalidArgs = lang.get("General.Invalid Arguments").replace("<label>", label).replace(
                    "<label2>", args[0]).replace("<args>", "[" + lang.get("List.Page")
                    + "] [--gui]");

            if (args.length > 1) {
                try {
                    page = Integer.parseInt(args[1]);
                    gui = false;
                } catch (NumberFormatException e) {
                    lang.send(sender, true, lang.get("General.Not A Number").replace("<number>", args[1]));
                    return;
                }

                if (args.length > 2) {
                    if (args[2].equalsIgnoreCase("--gui")) {
                        if (sender instanceof Player) {
                            if (sender.hasPermission("playmoresounds.list.gui")) {
                                if (PlayMoreSounds.HAS_PERSISTENT_DATA_CONTAINER) {
                                    gui = true;
                                } else {
                                    lang.send(sender, true, lang.get("List.GUI.Error.Not Supported"));
                                    return;
                                }
                            } else {
                                lang.send(sender, true, lang.get("General.No Permission"));
                                return;
                            }
                        } else {
                            lang.send(sender, true, lang.get("General.Not A Player"));
                            return;
                        }
                    } else {
                        lang.send(sender, true, invalidArgs);
                        return;
                    }
                }
            }

            if (page < 1) {
                page = 1;
            }

            if (gui) {
                Inventory listGUI = new ListInventory(page).getInventory();
                Player player = (Player) sender;

                player.openInventory(listGUI);
                openListGUIs.add(player);
            } else {
                HashMap<Integer, LinkedHashSet<String>> pages = PMSHelper.chopSet(PlayMoreSounds.SOUND_LIST,
                        PMSHelper.getConfig("config").getInt("Commands.List.Default.Max Per Page"));

                if (page > pages.size()) {
                    lang.send(sender, true, lang.get("List.Error.Not Exists").replace("<page>",
                            Integer.toString(page)).replace("<totalpages>", Integer.toString(pages.size())));
                    return;
                }

                lang.send(sender, true, lang.get("List.Header").replace("<page>", Integer.toString(page))
                        .replace("<totalpages>", Integer.toString(pages.size())));

                boolean alternatePrefix = false;
                int count = 1;
                TextComponent text = new TextComponent("");
                StringBuilder data = new StringBuilder();

                for (String sound : pages.get(page)) {
                    String prefix;

                    if (alternatePrefix) {
                        prefix = PMSHelper.getConfig("config").getString("Commands.List.Default.Alternate Color");
                    } else {
                        prefix = PMSHelper.getConfig("config").getString("Commands.List.Default.Color");
                    }

                    String separator = PMSHelper.getConfig("config").getString("Commands.List.Default.Separator");

                    if (count == pages.get(page).size()) {
                        separator = "";
                    }

                    if (sender instanceof Player) {
                        TextComponent fancySound = new TextComponent((prefix + sound).replace("&",
                                "§"));

                        fancySound.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(lang
                                .get("List.Sound Tooltip").replace("&", "§").replace("<sound>",
                                        sound)).create()));
                        fancySound.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pms play " +
                                sound + " " + sender.getName()));

                        text.addExtra(fancySound);
                        text.addExtra(separator.replace("&", "§"));
                    } else {
                        data.append(prefix).append(sound).append(separator);
                    }

                    alternatePrefix = !alternatePrefix;
                    ++count;
                }

                if (sender instanceof Player) {
                    ((Player) sender).spigot().sendMessage(text);
                } else {
                    lang.send(sender, true, data.toString());
                }

                if (page != pages.size()) {
                    String footer = lang.get("List.Footer").replace("<label>", label).replace("<page>",
                            Integer.toString(page + 1));

                    if (sender instanceof Player) {
                        TextComponent fancyFooter = new TextComponent(footer.replace("&", "§"));

                        fancyFooter.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pms list " + (page + 1)));
                        ((Player) sender).spigot().sendMessage(fancyFooter);
                    } else {
                        lang.send(sender, true, footer);
                    }
                }
            }
        } else {
            lang.send(sender, true, lang.get("General.No Permission"));
        }
    }
}
