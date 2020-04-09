package com.epicnicity322.playmoresounds.bukkit.command;

import com.epicnicity322.epicpluginlib.command.Command;
import com.epicnicity322.epicpluginlib.lang.MessageSender;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import org.bukkit.command.CommandSender;

public class CommandDescription implements Command
{
    @Override
    public String getName()
    {
        return null;
    }

    @Override
    public void onCommand(String label, CommandSender sender, String[] args)
    {
        MessageSender lang = PlayMoreSounds.MESSAGE_SENDER;

        lang.send(sender, false, lang.get("Description.Header").replace("<version>",
                PlayMoreSounds.PMS_VERSION));
        lang.send(sender, false, "&6Author: &7Epicnicity322");
        lang.send(sender, false, "&6Description: &7" + PlayMoreSounds.getPlugin().getDescription()
                .getDescription());

        if (sender.hasPermission("playmoresounds.help")) {
            lang.send(sender, false, lang.get("Description.Help").replace("<label>", label));
        } else {
            lang.send(sender, false, lang.get("Description.No Permission"));
        }
    }
}
