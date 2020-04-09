package com.epicnicity322.playmoresounds.bukkit.listener.player;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class PlayerDeath implements Listener
{
    @EventHandler
    public void onDeath(PlayerDeathEvent e)
    {
        Player p = e.getEntity();

        if (p.hasMetadata("pms.lastDmg")) {
            String cause = p.getMetadata("pms.lastDmg").get(0).asString();

            if (PMSHelper.getConfig("deathtypes").contains(cause)) {
                ConfigurationSection section = PMSHelper.getConfig("deathtypes").getConfigurationSection(cause);

                if (section.getBoolean("Enabled")) {
                    if (section.contains("Sounds")) {
                        new RichSound(section).play(e.getEntity());
                    }
                    if (section.getBoolean("Stop Other Sounds")) {
                        return;
                    }
                }
            }
        }

        ConfigurationSection section = PMSHelper.getConfig("sounds").getConfigurationSection("Player Death");

        new RichSound(section).play(e.getEntity());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent e)
    {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();

            if (p.hasMetadata("pms.lastDmg")) {
                p.removeMetadata("pms.lastDmg", PlayMoreSounds.getPlugin());
            }

            p.setMetadata("pms.lastDmg", new FixedMetadataValue(PlayMoreSounds.getPlugin(), e.getCause().toString()));
        }
    }
}
