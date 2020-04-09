package com.epicnicity322.playmoresounds.bukkit.region.selector;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.region.RegionManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class AreaSelector implements Listener
{
    public static HashMap<String, Location> P1 = new HashMap<>();
    public static HashMap<String, Location> P2 = new HashMap<>();

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInteract(PlayerInteractEvent e)
    {
        Player p = e.getPlayer();

        if (p.hasPermission("playmoresounds.region.select.wand")) {
            ItemStack i;

            if (PlayMoreSounds.BUKKIT_VERSION.contains("1.7") | PlayMoreSounds.BUKKIT_VERSION.contains("1.8")) {
                i = p.getItemInHand();
            } else {
                i = ItemInHand.getItemInMainHand(p);
            }

            if (i.isSimilar(RegionManager.getWand())) {
                Location clicked = e.getClickedBlock().getLocation();

                e.setCancelled(true);

                if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                    if (!p.hasPermission("playmoresounds.region.denypoint.bypass") &&
                            RegionManager.isInDenyPoint(clicked)) {
                        PlayMoreSounds.MESSAGE_SENDER.send(p, true, PlayMoreSounds.MESSAGE_SENDER.get(
                                "Region.Deny Point.Cant Select Here"));
                        return;
                    }

                    if (!P1.containsKey(p.getName()) || !P1.get(p.getName()).equals(clicked)) {
                        P1.put(p.getName(), clicked);

                        PlayMoreSounds.MESSAGE_SENDER.send(p, true, PlayMoreSounds.MESSAGE_SENDER.get(
                                "Region.PositionSelected1").replace("<x>", Integer.toString(clicked.getBlockX()))
                                .replace("<y>", Integer.toString(clicked.getBlockY()))
                                .replace("<z>", Integer.toString(clicked.getBlockZ()))
                                .replace("<world>", clicked.getWorld().getName()));
                    }
                } else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                    if (!p.hasPermission("playmoresounds.region.denypoint.bypass") &&
                            RegionManager.isInDenyPoint(clicked)) {
                        PlayMoreSounds.MESSAGE_SENDER.send(p, true, PlayMoreSounds.MESSAGE_SENDER.get(
                                "Region.Deny Point.Cant Select Here"));
                        return;
                    }

                    if (!P2.containsKey(p.getName()) || !P2.get(p.getName()).equals(clicked)) {
                        P2.put(p.getName(), clicked);

                        PlayMoreSounds.MESSAGE_SENDER.send(p, true, PlayMoreSounds.MESSAGE_SENDER.get(
                                "Region.Position Selected 1").replace("<x>", Integer.toString(clicked.getBlockX()))
                                .replace("<y>", Integer.toString(clicked.getBlockY()))
                                .replace("<z>", Integer.toString(clicked.getBlockZ()))
                                .replace("<world>", clicked.getWorld().getName()));
                    }
                }
            }
        }
    }
}
