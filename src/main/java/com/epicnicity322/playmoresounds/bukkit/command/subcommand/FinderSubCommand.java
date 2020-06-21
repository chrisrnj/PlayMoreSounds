package com.epicnicity322.playmoresounds.bukkit.command.subcommand;

import com.epicnicity322.epicpluginlib.bukkit.command.Command;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.inventory.FinderInventory;
import com.epicnicity322.yamlhandler.exceptions.InvalidConfigurationException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public final class FinderSubCommand extends Command
{
    @Override
    public @NotNull String getName()
    {
        return "finder";
    }

    @Override
    public void run(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args)
    {
        if (sender instanceof HumanEntity) {
            try {
                FinderInventory inventory = new FinderInventory(PlayMoreSounds.getFolder(), 1);

                inventory.openInventory((HumanEntity) sender);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
    }
}
