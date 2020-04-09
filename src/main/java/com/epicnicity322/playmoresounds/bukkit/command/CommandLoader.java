package com.epicnicity322.playmoresounds.bukkit.command;

import com.epicnicity322.epicpluginlib.command.Command;
import com.epicnicity322.epicpluginlib.command.CommandManager;
import com.epicnicity322.playmoresounds.bukkit.command.subcommand.*;
import org.bukkit.Bukkit;

import java.util.HashSet;

public class CommandLoader
{
    public static void loadCommands()
    {
        HashSet<Command> commands = new HashSet<>();

        commands.add(new ToggleSubCommand());
        commands.add(new PlaySubCommand());
        commands.add(new ReloadSubCommand());
        commands.add(new ListSubCommand());
        commands.add(new FactorySubCommand());

        CommandManager.registerCommand(Bukkit.getPluginCommand("playmoresounds"), commands, new CommandDescription());
    }
}
