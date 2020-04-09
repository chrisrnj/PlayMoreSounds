package com.epicnicity322.playmoresounds.bukkit.region.selector;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.region.RegionManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class InappropriateEvents implements Listener
{
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e)
    {
        if (e.getDamager() instanceof Player) {
            Player p = (Player) e.getDamager();
            ItemStack i;

            if (PlayMoreSounds.BUKKIT_VERSION.contains("1.7") | PlayMoreSounds.BUKKIT_VERSION.contains("1.8")) {
                i = p.getItemInHand();
            } else {
                i = ItemInHand.getItemInMainHand(p);
            }

            if (i.isSimilar(RegionManager.getWand())) {
                e.setCancelled(true);
                e.setDamage(0.0);
            }
        }
    }
}
