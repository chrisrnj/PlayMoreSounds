package com.epicnicity322.playmoresounds.bukkit.command.subcommand;

import com.epicnicity322.epicpluginlib.command.Command;
import com.epicnicity322.epicpluginlib.lang.MessageSender;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.util.Storage;
import com.epicnicity322.playmoresounds.bukkit.util.UpdateManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashSet;

public class ReloadSubCommand implements Command
{
    @Override
    public String getName()
    {
        return "reload";
    }

    @Override
    public void onCommand(String label, CommandSender sender, String[] args)
    {
        MessageSender lang = PlayMoreSounds.MESSAGE_SENDER;

        if (sender.hasPermission("playmoresounds.reload")) {
            try {
                PlayMoreSounds.CONFIG.loadConfig();
                PlayMoreSounds.CONFIG.loadLanguage(new HashSet<>(Arrays.asList(PlayMoreSounds.LANG_VERSION)),
                        Storage.getHardCodedLang());
                UpdateManager.check(Bukkit.getConsoleSender(), true);
                lang.send(sender, true, lang.get("Reload.Success"));
            } catch (Exception e) {
                lang.send(sender, true, lang.get("Reload.Error"));
                PlayMoreSounds.ERROR_LOGGER.report(e, "Reload Config Exception:");
            }
        } else {
            lang.send(sender, true, lang.get("General.No Permission"));
        }
    }
}
