package com.epicnicity322.playmoresounds.bukkit.command.subcommand;

import com.epicnicity322.epicpluginlib.command.Command;
import com.epicnicity322.epicpluginlib.lang.MessageSender;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.inventory.RichSoundInventory;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class FactorySubCommand implements Command
{
    public static HashMap<Player, RichSound> cachedSounds = new HashMap<>();

    @Override
    public String getName()
    {
        return "factory";
    }

    @Override
    public int minArgsAmount()
    {
        return 2;
    }

    @Override
    public void onNotEnoughArgs(String label, CommandSender sender, String[] args)
    {
        MessageSender lang = PlayMoreSounds.MESSAGE_SENDER;
        String arguments = "";

        if (sender.hasPermission("playmoresounds.factory.create")) {
            arguments = "create";
        }
        if (sender.hasPermission("playmoresounds.factory.save")) {
            if (!arguments.equals("")) {
                arguments = arguments + "|";
            }
            arguments = arguments + "save";
        }

        if (arguments.equals("")) {
            lang.send(sender, true, lang.get("General.No Permission"));
        } else {
            lang.send(sender, true, lang.get("General.Invalid Arguments").replace("<label>", label)
                    .replace("<label2>", args[0]).replace("<args>", arguments));
        }
    }

    @Override
    public void onCommand(String label, CommandSender sender, String[] args)
    {
        MessageSender lang = PlayMoreSounds.MESSAGE_SENDER;

        switch (args[1]) {
            case "create":
            case "new":
                if (sender.hasPermission("playmoresounds.factory.create")) {
                    RichSoundInventory richSoundInventory = new RichSoundInventory(null);
                    Player player = (Player) sender;

                    richSoundInventory.openInventory(player);
                } else {
                    lang.send(sender, true, lang.get("General.No Permission"));
                }

                break;
            case "save":
                if (sender.hasPermission("playmoresounds.factory.save")) {

                } else {
                    lang.send(sender, true, lang.get("General.No Permission"));
                }

                break;
            default:
                onNotEnoughArgs(label, sender, args);
                break;
        }
    }
}
