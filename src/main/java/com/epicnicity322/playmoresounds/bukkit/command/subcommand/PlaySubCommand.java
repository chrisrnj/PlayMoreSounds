package com.epicnicity322.playmoresounds.bukkit.command.subcommand;

import com.epicnicity322.epicpluginlib.command.Command;
import com.epicnicity322.epicpluginlib.lang.MessageSender;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.command.CommandUtils;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.sound.Sound;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundOptions;
import com.epicnicity322.playmoresounds.bukkit.util.Finder;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.nio.file.Files;
import java.util.HashSet;

public class PlaySubCommand implements Command
{
    @Override
    public String getName()
    {
        return "play";
    }

    @Override
    public void onCommand(String label, CommandSender sender, String[] args)
    {
        MessageSender lang = PlayMoreSounds.MESSAGE_SENDER;

        if (sender.hasPermission("playmoresounds.play")) {
            String invalidArgs = lang.get("General.Invalid Arguments").replace("<label>", label).replace(
                    "<label2>", args[0]).replace("<args>", "<" + lang.get("Play.Sound") + "> "
                    + (sender instanceof Player ? "[" + lang.get("General.Player") + "]" : "<" +
                    lang.get("General.Player") + ">") + " [" + lang.get("Play.Volume") + "] [" + lang.get("Play.Pitch")
                    + "]");
            HashSet<Player> targets = CommandUtils.getTargets(sender, args, 2, invalidArgs,
                    "playmoresounds.play.others");

            if (targets == null) {
                return;
            }

            if (args.length > 1) {
                String sound = args[1];
                String who = CommandUtils.getWho(targets, sender);

                if (sound.startsWith("/")) {
                    if (sound.toLowerCase().startsWith("/data")) {
                        lang.send(sender, true, lang.get("Play.Error.Unauthorized"));
                    } else {
                        try {
                            Finder finder = new Finder(sound);

                            if (!Files.exists(finder.getPath())) {
                                lang.send(sender, true, lang.get("Finder.File Not Found")
                                        .replace("<file>", finder.getPathValue()));
                            }

                            ConfigurationSection section = finder.getSection();

                            if (section == null) {
                                lang.send(sender, true, lang.get("Finder.Invalid Section")
                                        .replace("<file>", finder.getSectionValue()));
                                return;
                            }

                            try {
                                RichSound richSound = new RichSound(section);

                                for (Player player : targets) {
                                    richSound.play(player);
                                }

                                lang.send(sender, true, lang.get("Play.Success.Config")
                                        .replace("<sound>", section.getCurrentPath())
                                        .replace("<file>", finder.getPathValue().substring(1))
                                        .replace("<player>", who));
                            } catch (Exception ex) {
                                lang.send(sender, true, lang.get("Play.Error.Not A Sound")
                                        .replace("<section>", finder.getSectionValue())
                                        .replace("<file>", finder.getPathValue().substring(1)));
                            }
                        } catch (Exception ex) {
                            lang.send(sender, true, lang.get("Finder.Missing Section"));
                        }
                    }

                    return;
                }

                float volume = 10;
                float pitch = 1;

                if (args.length > 3) {
                    try {
                        volume = Float.parseFloat(args[3]);
                    } catch (NumberFormatException e) {
                        lang.send(sender, true, lang.get("General.Not A Number").replace("<number>",
                                args[3]));
                    }

                    if (args.length > 4) {
                        try {
                            pitch = Float.parseFloat(args[4]);
                        } catch (NumberFormatException e) {
                            lang.send(sender, true, lang.get("General.Not A Number").replace("<number>",
                                    args[4]));
                        }
                    }
                }

                Sound sound1 = new Sound(null, sound, volume, pitch, 0, new SoundOptions(true,
                        false, null, null, 0, null));

                for (Player player : targets) {
                    sound1.play(player);
                }

                lang.send(sender, true, lang.get("Play.Success.Default").replace("<sound>", sound).replace(
                        "<player>", who).replace("<volume>", Float.toString(volume)).replace(
                        "<pitch>", Float.toString(pitch)));
            } else {
                lang.send(sender, true, invalidArgs);
            }
        } else {
            lang.send(sender, true, lang.get("General.No Permission"));
        }
    }
}
