package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

public final class OnInventoryClose extends PMSListener
{
    public OnInventoryClose(@NotNull PlayMoreSounds plugin)
    {
        super(plugin);
    }


    @Override
    public @NotNull String getName()
    {
        return "Inventory Close";
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event)
    {
        HumanEntity whoClosed = event.getPlayer();

        if (whoClosed instanceof Player) {
            getRichSound().play((Player) whoClosed);
        }
    }
}
