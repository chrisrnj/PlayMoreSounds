package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceExtractEvent;

public class OnFurnaceExtract implements Listener
{
    @EventHandler
    public void onFurnaceExtract(FurnaceExtractEvent event)
    {
        ConfigurationSection section = PMSHelper.getConfig("sounds").getConfigurationSection("Furnace Extract");

        new RichSound(section).play(event.getPlayer());
    }
}
