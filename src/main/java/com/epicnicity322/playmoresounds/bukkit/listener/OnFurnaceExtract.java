package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.jetbrains.annotations.NotNull;

public final class OnFurnaceExtract extends PMSListener
{
    public OnFurnaceExtract(@NotNull PlayMoreSounds plugin)
    {
        super(plugin);
    }

    @Override
    public @NotNull String getName()
    {
        return "Craft Item";
    }

    @EventHandler
    public void onFurnaceExtract(FurnaceExtractEvent event)
    {
        getRichSound().play(event.getPlayer());
    }
}
