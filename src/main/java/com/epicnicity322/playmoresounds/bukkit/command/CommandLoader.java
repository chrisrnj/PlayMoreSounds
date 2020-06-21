package com.epicnicity322.playmoresounds.bukkit.command;

import com.epicnicity322.epicpluginlib.bukkit.command.Command;
import com.epicnicity322.epicpluginlib.bukkit.command.CommandManager;
import com.epicnicity322.epicpluginlib.bukkit.command.CommandRunnable;
import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.command.subcommand.*;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class CommandLoader
{
    private static final @NotNull MessageSender lang = PlayMoreSounds.getMessageSender();
    private static final @NotNull LinkedHashSet<Command> commands = new LinkedHashSet<>();
    private static CommandRunnable unknownCommand = null;
    private static CommandRunnable description = null;

    static {
        PlayMoreSounds.addOnInstanceRunnable(() -> {
            description = (label, sender, args) -> {
                lang.send(sender, false, lang.get("Description.Header").replace("<version>",
                        PlayMoreSounds.getVersion()));
                lang.send(sender, false, "&6Author: &7Epicnicity322");
                lang.send(sender, false, "&6Description: &7" + PlayMoreSounds.getInstance().getDescription().getDescription());

                if (sender.hasPermission("playmoresounds.help"))
                    lang.send(sender, false, lang.get("Description.Help").replace("<label>", label));
                else
                    lang.send(sender, false, lang.get("Description.No Permission"));
            };

            unknownCommand = (label, sender, args) ->
                    lang.send(sender, lang.get("General.Unknown Command").replace("<label>", label));

            commands.add(new ConfirmSubCommand());
            commands.add(new HelpSubCommand());
            commands.add(new ListSubCommand());
            commands.add(new PlaySubCommand());
            commands.add(new RegionSubCommand());
            commands.add(new ReloadSubCommand());
            commands.add(new ToggleSubCommand());
            commands.add(new UpdateSubCommand());
        });
    }

    private CommandLoader()
    {
    }

    /**
     * Adds a command to the list of command to be loaded on {@link #loadCommands()}.
     *
     * @param command The command to add to be registered.
     */
    public static void addCommand(@NotNull Command command)
    {
        commands.add(command);
    }

    /**
     * Removes a command from the list of command to be loaded on {@link #loadCommands()}.
     *
     * @param command The command to remove.
     */
    public static void removeCommand(@NotNull Command command)
    {
        commands.remove(command);
    }

    /**
     * @return A set with all the command that are being loaded on {@link #loadCommands()}.
     */
    public static @NotNull Set<Command> getCommands()
    {
        return Collections.unmodifiableSet(commands);
    }

    /**
     * Registers all sub commands to PlayMoreSounds main command.
     */
    public static void loadCommands()
    {
        CommandManager.registerCommand(Bukkit.getPluginCommand("playmoresounds"), commands, description,
                unknownCommand);
    }
}
