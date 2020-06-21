package com.epicnicity322.playmoresounds.bukkit.command.subcommand;

import com.epicnicity322.epicpluginlib.bukkit.command.CommandRunnable;
import org.jetbrains.annotations.NotNull;

public interface Helpable
{
    @NotNull CommandRunnable onHelp();
}
