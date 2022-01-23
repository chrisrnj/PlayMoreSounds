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
import com.epicnicity322.playmoresounds.bukkit.inventory.ListInventory;
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.sound.SoundType;
import com.epicnicity322.playmoresounds.core.util.PMSHelper;
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
    private static final @NotNull HashMap<Integer, HashMap<Integer, ArrayList<String>>> soundPagesCache = new HashMap<>();

    static {
        // Clear cache on disable.
        PlayMoreSounds.onDisable(soundPagesCache::clear);
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
        return (label, sender, args) -> PlayMoreSounds.getLanguage().send(sender, false, PlayMoreSounds.getLanguage().get("Help.List").replace("<label>", label));
    }

    @Override
    protected @Nullable CommandRunnable getNoPermissionRunnable()
    {
        return (label, sender, args) -> PlayMoreSounds.getLanguage().send(sender, PlayMoreSounds.getLanguage().get("General.No Permission"));
    }

    // Using BaseComponent[] on HoverEvent is deprecated on newer versions of spigot but is necessary on older ones.
    @SuppressWarnings(value = "deprecation")
    @Override
    public void run(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args)
    {
        MessageSender lang = PlayMoreSounds.getLanguage();
        boolean player = sender instanceof Player;
        boolean gui = player && sender.hasPermission("playmoresounds.list.gui");
        int page = 1;
        String invalidArgs = lang.get("General.Invalid Arguments").replace("<label>", label).replace(
                "<label2>", args[0]).replace("<args>", "[" + lang.get("List.Page")
                + "] [--gui]");

        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
                gui = false;
            } catch (NumberFormatException e) {
                lang.send(sender, lang.get("General.Not A Number").replace("<number>", args[1]));
                return;
            }

            if (args.length > 2) {
                if (!args[2].equalsIgnoreCase("--gui")) {
                    lang.send(sender, invalidArgs);
                    return;
                }
                if (!player) {
                    lang.send(sender, lang.get("General.Not A Player"));
                    return;
                }
                if (!sender.hasPermission("playmoresounds.list.gui")) {
                    lang.send(sender, lang.get("General.No Permission"));
                    return;
                }
                gui = true;
            }
        }

        if (gui) {
            ListInventory.getListInventory(page).openInventory((Player) sender);
        } else {
            int soundsPerPage = Configurations.CONFIG.getConfigurationHolder().getConfiguration().getNumber("List.Chat.Max Per Page").orElse(10).intValue();

            if (soundsPerPage < 1)
                soundsPerPage = 1;

            HashMap<Integer, ArrayList<String>> soundPages = soundPagesCache.get(soundsPerPage);
            if (soundPages == null) {
                soundPages = PMSHelper.splitIntoPages(new TreeSet<>(SoundType.getPresentSoundNames()), soundsPerPage);

                soundPagesCache.put(soundsPerPage, soundPages);
            }

            if (page > soundPages.size()) {
                lang.send(sender, lang.get("List.Chat.Error.Not Exists").replace("<page>",
                        Long.toString(page)).replace("<totalpages>", Integer.toString(soundPages.size())));
                return;
            }

            lang.send(sender, lang.get("List.Chat.Header").replace("<page>", Long.toString(page))
                    .replace("<totalpages>", Integer.toString(soundPages.size())));

            boolean alternatePrefix = false;
            int count = 1;
            TextComponent text = new TextComponent("");
            StringBuilder data = new StringBuilder();
            ArrayList<String> soundList = soundPages.get(page);
            String color = lang.get("List.Chat.Color", "&e");
            String alternateColor = lang.get("List.Chat.Alternate Color", "&8");
            String defaultSeparator = lang.get("List.Chat.Separator", ", ");
            String tooltip = lang.get("List.Chat.Sound Tooltip").replace("&", "§");

            for (String sound : soundList) {
                String prefix;

                if (alternatePrefix)
                    prefix = alternateColor;
                else
                    prefix = color;

                String separator = defaultSeparator;

                if (count++ == soundList.size())
                    separator = "";

                if (player) {
                    TextComponent fancySound = new TextComponent((prefix + sound).replace("&", "§"));

                    if (VersionUtils.hasHoverContentApi())
                        fancySound.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(tooltip.replace("<sound>", sound))));
                    else
                        fancySound.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(tooltip.replace("<sound>", sound)).create()));

                    fancySound.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pms play " + sound + " " + sender.getName()));

                    text.addExtra(fancySound);
                    text.addExtra(separator.replace("&", "§"));
                } else {
                    data.append(prefix).append(sound).append(separator);
                }

                alternatePrefix = !alternatePrefix;
            }

            if (player)
                ((Player) sender).spigot().sendMessage(text);
            else
                lang.send(sender, false, data.toString());

            if (page != soundPages.size()) {
                String footer = lang.get("List.Chat.Footer").replace("<label>", label).replace("<page>", Long.toString(page + 1));

                if (player) {
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
