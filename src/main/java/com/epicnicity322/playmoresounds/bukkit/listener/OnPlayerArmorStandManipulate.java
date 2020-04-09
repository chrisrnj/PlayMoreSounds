package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.sound.RelativeLocationSetter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.persistence.PersistentDataType;

public class OnPlayerArmorStandManipulate implements Listener
{
    @EventHandler
    public void onManipulate(PlayerArmorStandManipulateEvent e)
    {
        if (e.getRightClicked().getPersistentDataContainer().has(RelativeLocationSetter.locked, PersistentDataType.STRING)) {
            e.setCancelled(true);
        }
    }
}
