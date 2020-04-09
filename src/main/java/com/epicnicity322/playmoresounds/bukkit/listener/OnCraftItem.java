package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;

public class OnCraftItem implements Listener
{
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCraftItem(CraftItemEvent event)
    {
        ConfigurationSection section = PMSHelper.getConfig("sounds").getConfigurationSection("Craft Item");
        RichSound sound = new RichSound(section);

        if (!event.isCancelled() || !sound.isCancellable()) {
            sound.play((Player) event.getWhoClicked());
        }
    }
}
