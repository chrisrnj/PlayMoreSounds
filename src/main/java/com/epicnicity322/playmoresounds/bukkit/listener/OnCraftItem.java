package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;
import org.jetbrains.annotations.NotNull;

public final class OnCraftItem extends PMSListener
{
    public OnCraftItem(@NotNull PlayMoreSounds plugin)
    {
        super(plugin);
    }

    @Override
    public @NotNull String getName()
    {
        return "Craft Item";
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCraftItem(CraftItemEvent event)
    {
        HumanEntity whoClicked = event.getWhoClicked();

        if (whoClicked instanceof Player) {
            RichSound sound = getRichSound();

            if (!event.isCancelled() || !sound.isCancellable())
                sound.play((Player) whoClicked);
        }
    }
}
