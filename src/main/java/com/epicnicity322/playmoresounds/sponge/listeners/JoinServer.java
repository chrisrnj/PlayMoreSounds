package com.epicnicity322.playmoresounds.sponge.listeners;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;

public class JoinServer
{
    @Listener
    public void onJoin(ClientConnectionEvent e)
    {
        for (Player p : Sponge.getServer().getOnlinePlayers()) {
            p.sendMessage(Text.of("Hey"));
            p.playSound(SoundTypes.BLOCK_NOTE_PLING, p.getPosition(), 10);
        }
    }
}
