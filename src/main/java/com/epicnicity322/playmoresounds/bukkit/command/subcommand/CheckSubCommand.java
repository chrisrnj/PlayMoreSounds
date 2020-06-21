package com.epicnicity322.playmoresounds.bukkit.command.subcommand;

import com.epicnicity322.epicpluginlib.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CheckSubCommand extends Command
{
    @Override
    public @NotNull String getName()
    {
        return "check";
    }

    @Override
    public @Nullable String[] getAliases()
    {
        return new String[]{"sounds"};
    }

    @Override
    public void run(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args)
    {

    }
}
