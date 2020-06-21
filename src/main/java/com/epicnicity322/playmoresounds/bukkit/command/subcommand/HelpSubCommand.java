package com.epicnicity322.playmoresounds.bukkit.command.subcommand;

import com.epicnicity322.epicpluginlib.bukkit.command.Command;
import com.epicnicity322.epicpluginlib.bukkit.command.CommandRunnable;
import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.command.CommandLoader;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class HelpSubCommand extends Command implements Helpable
{
    private static final @NotNull MessageSender lang = PlayMoreSounds.getMessageSender();

    @Override
    public @NotNull String getName()
    {
        return "help";
    }

    @Override
    public @Nullable String getPermission()
    {
        return "playmoresounds.help";
    }

    @Override
    public @NotNull CommandRunnable onHelp()
    {
        return (label, sender, args) -> lang.send(sender, false, lang.get("Help.Help").replace("<label>", label));
    }

    @Override
    protected @Nullable CommandRunnable getNoPermissionRunnable()
    {
        return (label, sender, args) -> lang.send(sender, lang.get("General.No Permission"));
    }

    @Override
    public void run(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args)
    {
        lang.send(sender, lang.get("Help.Header"));

        for (Command command : CommandLoader.getCommands()) {
            if (command instanceof Helpable) {
                if (sender.hasPermission(command.getPermission()))
                    ((Helpable) command).onHelp().run(label, sender, args);
            }
        }
    }
}
