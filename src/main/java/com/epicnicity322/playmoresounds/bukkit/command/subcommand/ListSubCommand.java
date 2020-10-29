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
import com.epicnicity322.epicpluginlib.core.config.PluginConfig;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.inventory.ListInventory;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundManager;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public final class ListSubCommand extends Command implements Helpable
{
    private static final @NotNull PluginConfig config = Configurations.CONFIG.getPluginConfig();
    private static final @NotNull MessageSender lang = PlayMoreSounds.getMessageSender();
    private static final @NotNull HashMap<Integer, HashMap<Long, ArrayList<String>>> soundPagesCache = new HashMap<>();

    static {
        // Clear cache on disable.
        PlayMoreSounds.addOnDisableRunnable(soundPagesCache::clear);
    }

    @Override
    public @NotNull String getName()
    {
        return "list";
    }

    @Override
    public @Nullable String getPermission()
    {
        return "playmoresounds.list";
    }

    @Override
    public @NotNull CommandRunnable onHelp()
    {
        return (label, sender, args) -> lang.send(sender, false, lang.get("Help.List").replace("<label>", label));
    }

    @Override
    protected @Nullable CommandRunnable getNoPermissionRunnable()
    {
        return (label, sender, args) -> lang.send(sender, lang.get("General.No Permission"));
    }

    // Using BaseComponent[] on HoverEvent is deprecated on newer versions of spigot but is necessary on older ones.
    @SuppressWarnings(value = "deprecation")
    @Override
    public void run(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args)
    {
        boolean gui = sender instanceof Player && sender.hasPermission("playmoresounds.list.gui")
                && VersionUtils.hasPersistentData();
        long page = 1;
        String invalidArgs = lang.get("General.Invalid Arguments").replace("<label>", label).replace(
                "<label2>", args[0]).replace("<args>", "[" + lang.get("List.Page")
                + "] [--gui]");

        if (args.length > 1) {
            try {
                page = Long.parseLong(args[1]);
                gui = false;
            } catch (NumberFormatException e) {
                lang.send(sender, lang.get("General.Not A Number").replace("<number>", args[1]));
                return;
            }

            if (args.length > 2) {
                if (args[2].equalsIgnoreCase("--gui")) {
                    if (sender instanceof Player) {
                        if (sender.hasPermission("playmoresounds.list.gui")) {
                            if (VersionUtils.hasPersistentData()) {
                                gui = true;
                            } else {
                                lang.send(sender, lang.get("List.GUI.Error.Not Supported"));
                                return;
                            }
                        } else {
                            lang.send(sender, lang.get("General.No Permission"));
                            return;
                        }
                    } else {
                        lang.send(sender, lang.get("General.Not A Player"));
                        return;
                    }
                } else {
                    lang.send(sender, invalidArgs);
                    return;
                }
            }
        }

        if (gui) {
            ListInventory listInventory = new ListInventory((int) page);
            Player player = (Player) sender;

            listInventory.openInventory(player);
        } else {
            Configuration yamlConfig = ListSubCommand.config.getConfiguration();
            HashMap<Long, ArrayList<String>> soundPages;
            int soundsPerPage = yamlConfig.getNumber("Commands.List.Default.Max Per Page").orElse(10).intValue();

            if (soundsPerPage < 1)
                soundsPerPage = 1;

            if (soundPagesCache.containsKey(soundsPerPage)) {
                soundPages = soundPagesCache.get(soundsPerPage);
            } else {
                soundPages = PMSHelper.splitIntoPages(new TreeSet<>(SoundManager.getSoundList()), soundsPerPage);

                soundPagesCache.put(soundsPerPage, soundPages);
            }

            if (page > soundPages.size()) {
                lang.send(sender, lang.get("List.Error.Not Exists").replace("<page>",
                        Long.toString(page)).replace("<totalpages>", Integer.toString(soundPages.size())));
                return;
            }

            lang.send(sender, lang.get("List.Header").replace("<page>", Long.toString(page))
                    .replace("<totalpages>", Integer.toString(soundPages.size())));

            boolean alternatePrefix = false;
            int count = 1;
            TextComponent text = new TextComponent("");
            StringBuilder data = new StringBuilder();
            ArrayList<String> soundList = soundPages.get(page);

            for (String sound : soundList) {
                String prefix;

                if (alternatePrefix)
                    prefix = yamlConfig.getString("Commands.List.Default.Alternate Color").orElse("&8");
                else
                    prefix = yamlConfig.getString("Commands.List.Default.Color").orElse("&e");

                String separator = yamlConfig.getString("Commands.List.Default.Separator").orElse(", ");

                if (count++ == soundList.size())
                    separator = "";

                if (sender instanceof Player) {
                    TextComponent fancySound = new TextComponent((prefix + sound).replace("&",
                            "§"));

                    if (VersionUtils.hasHoverContentApi())
                        fancySound.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(lang
                                .get("List.Sound Tooltip").replace("&", "§").replace("<sound>", sound))));
                    else
                        fancySound.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(lang
                                .get("List.Sound Tooltip").replace("&", "§").replace("<sound>", sound)).create()));

                    fancySound.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pms play " +
                            sound + " " + sender.getName()));

                    text.addExtra(fancySound);
                    text.addExtra(separator.replace("&", "§"));
                } else {
                    data.append(prefix).append(sound).append(separator);
                }

                alternatePrefix = !alternatePrefix;
            }

            if (sender instanceof Player)
                ((Player) sender).spigot().sendMessage(text);
            else
                lang.send(sender, false, data.toString());

            if (page != soundPages.size()) {
                String footer = lang.get("List.Footer").replace("<label>", label).replace("<page>",
                        Long.toString(page + 1));

                if (sender instanceof Player) {
                    TextComponent fancyFooter = new TextComponent(footer.replace("&", "§"));

                    fancyFooter.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pms list " + (page + 1)));
                    ((Player) sender).spigot().sendMessage(fancyFooter);
                } else {
                    lang.send(sender, false, footer);
                }
            }
        }
    }
}
