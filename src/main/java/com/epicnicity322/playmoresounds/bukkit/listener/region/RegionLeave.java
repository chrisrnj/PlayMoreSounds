package com.epicnicity322.playmoresounds.bukkit.listener.region;

import com.epicnicity322.playmoresounds.bukkit.region.SoundRegion;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionLeaveEvent;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashSet;

public class RegionLeave implements Listener
{
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRegionLeave(RegionLeaveEvent e)
    {
        Player player = e.getPlayer();

        for (String s : new HashSet<>(RegionEnter.STOP_ON_EXIT.keySet())) {
            if (s.startsWith(player.getName())) {
                long delay = Long.parseLong(s.substring(s.lastIndexOf(";") + 1));

                PMSHelper.stopSound(player, RegionEnter.STOP_ON_EXIT.get(s), delay);
                RegionEnter.STOP_ON_EXIT.remove(s);
            }
        }

        SoundRegion region = e.getRegion();
        String key = region.getName() + ";" + player.getName();

        if (RegionEnter.SOUNDS_IN_LOOP.containsKey(key)) {
            RegionEnter.SOUNDS_IN_LOOP.get(key).cancel();
            RegionEnter.SOUNDS_IN_LOOP.remove(key);
        }

        boolean play = true;

        if (PMSHelper.getConfig("regions").contains("PlayMoreSounds." + region.getName() + ".Leave")) {
            ConfigurationSection section = PMSHelper.getConfig("regions").getConfigurationSection(
                    "PlayMoreSounds." + region.getName() + ".Leave");

            if (section.getBoolean("Enabled")) {
                if (!e.isCancelled() || !section.getBoolean("Cancellable")) {
                    new RichSound(section).play(player);

                    play = !section.getBoolean("Stop Other Sounds");
                }
            }
        }

        if (play) {
            ConfigurationSection section = PMSHelper.getConfig("sounds").getConfigurationSection("Region Leave");
            RichSound sound = new RichSound(section);

            if (!e.isCancelled() || !sound.isCancellable()) {
                sound.play(e.getPlayer());
            }
        }
    }
}
