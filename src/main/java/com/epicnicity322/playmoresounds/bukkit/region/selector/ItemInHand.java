package com.epicnicity322.playmoresounds.bukkit.region.selector;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemInHand
{
    public static ItemStack getItemInMainHand(Player p)
    {
        return p.getInventory().getItemInMainHand();
    }

    public static ItemStack getItemInMainHand(LivingEntity entity)
    {
        return entity.getEquipment().getItemInMainHand();
    }
}
