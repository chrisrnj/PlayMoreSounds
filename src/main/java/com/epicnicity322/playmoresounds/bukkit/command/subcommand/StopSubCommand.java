package com.epicnicity322.playmoresounds.bukkit.command.subcommand;

import com.epicnicity322.epicpluginlib.command.Command;
import org.bukkit.command.CommandSender;

public class StopSubCommand implements Command
{
    @Override
    public String getName()
    {
        return "stopsound";
    }

    @Override
    public int minArgsAmount()
    {
        return 0;
    }

    @Override
    public void onCommand(String s, CommandSender commandSender, String[] strings)
    {

    }

    @Override
    public void onNotEnoughArgs(String label, CommandSender sender, String[] args)
    {

    }
}
