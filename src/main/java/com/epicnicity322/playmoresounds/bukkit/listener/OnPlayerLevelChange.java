package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.jetbrains.annotations.NotNull;

public final class OnPlayerLevelChange extends PMSListener
{
    public OnPlayerLevelChange(@NotNull PlayMoreSounds plugin)
    {
        super(plugin);
    }

    @Override
    public @NotNull String getName()
    {
        return "Change Level";
    }

    @EventHandler
    public void onPlayerLevelChange(PlayerLevelChangeEvent event)
    {
        getRichSound().play(event.getPlayer());
    }
}
