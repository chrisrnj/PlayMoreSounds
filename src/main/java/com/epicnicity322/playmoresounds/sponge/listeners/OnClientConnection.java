package com.epicnicity322.playmoresounds.sponge.listeners;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public final class OnClientConnection
{
    @Listener
    public void onClientConnection(@SuppressWarnings("unused") ClientConnectionEvent event)
    {
        for (Player player : Sponge.getServer().getOnlinePlayers())
            player.playSound(SoundTypes.BLOCK_NOTE_PLING, player.getPosition(), 10);
    }
}
