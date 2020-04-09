package com.epicnicity322.playmoresounds.bukkit.listener.region;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.region.RegionManager;
import com.epicnicity322.playmoresounds.bukkit.region.SoundRegion;
import com.epicnicity322.playmoresounds.bukkit.region.events.RegionEnterEvent;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundType;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;

public class RegionEnter implements Listener
{
    public static HashMap<String, BukkitRunnable> SOUNDS_IN_LOOP = new HashMap<>();
    public static HashMap<String, HashSet<String>> STOP_ON_EXIT = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRegionEnter(RegionEnterEvent e)
    {
        Player player = e.getPlayer();
        SoundRegion region = e.getRegion();
        boolean playRegionsYML = true;
        boolean playSoundsYML = true;

        if (PMSHelper.getConfig("regions").contains("PlayMoreSounds." + region.getName() + ".Loop")) {
            ConfigurationSection section = PMSHelper.getConfig("regions").getConfigurationSection(
                    "PlayMoreSounds." + region.getName() + ".Loop");

            if (section.getBoolean("Enabled")) {
                if (!e.isCancelled() || !section.getBoolean("Cancellable")) {
                    if (section.contains("Sounds")) {
                        String key = region.getName() + ";" + player.getName();

                        if (SOUNDS_IN_LOOP.containsKey(key)) {
                            SOUNDS_IN_LOOP.get(key).cancel();
                        }

                        SOUNDS_IN_LOOP.put(key, new BukkitRunnable()
                        {
                            @Override
                            public void run()
                            {
                                ConfigurationSection loopSection = PMSHelper.getConfig("regions")
                                        .getConfigurationSection("PlayMoreSounds." + region.getName() + ".Loop");
                                SoundRegion currentRegion = RegionManager.getRegion(player.getLocation());

                                if (!loopSection.getBoolean("Enabled") || currentRegion == null ||
                                        !currentRegion.equals(region) || !player.isOnline()) {
                                    cancel();
                                    SOUNDS_IN_LOOP.remove(key);
                                    return;
                                }

                                new RichSound(loopSection).play(player);
                            }
                        });

                        SOUNDS_IN_LOOP.get(key).runTaskTimer(PlayMoreSounds.getPlugin(), section.getLong("Delay"),
                                section.getLong("Period"));

                        stopOnExit(player, section);
                    }

                    playRegionsYML = !section.getBoolean("Stop Other Sounds.RegionsYML");
                    playSoundsYML = !section.getBoolean("Stop Other Sounds.SoundsYML");
                }
            }
        }

        if (playRegionsYML) {
            if (PMSHelper.getConfig("regions").contains("PlayMoreSounds." + region.getName() + ".Enter")) {
                ConfigurationSection section = PMSHelper.getConfig("regions").getConfigurationSection(
                        "PlayMoreSounds." + region.getName() + ".Enter");

                if (section.getBoolean("Enabled")) {
                    if (!e.isCancelled() || !section.getBoolean("Cancellable")) {
                        if (section.contains("Sounds")) {
                            new RichSound(section).play(player);
                            stopOnExit(player, section);
                        }

                        playSoundsYML = !section.getBoolean("Stop Other Sounds");
                    }
                }
            }
        }

        if (playSoundsYML) {
            ConfigurationSection section = PMSHelper.getConfig("sounds").getConfigurationSection("Region Enter");
            RichSound sound = new RichSound(section);

            if (section.getBoolean("Enabled")) {
                if (!e.isCancelled() || !sound.isCancellable()) {
                    if (section.contains("Sounds")) {
                        sound.play(player);
                        stopOnExit(player, section);
                    }
                }
            }
        }
    }

    private void stopOnExit(Player player, ConfigurationSection section)
    {
        if (section.getBoolean("Stop On Exit.Enabled")) {
            HashSet<String> sounds = STOP_ON_EXIT.getOrDefault(player.getName() + ";" +
                    section.getLong("Stop On Exit.Delay"), new HashSet<>());

            for (String s : section.getConfigurationSection("Sounds").getKeys(false)) {
                String sound = section.getString("Sounds." + s + ".Sound");

                sounds.add(PlayMoreSounds.SOUND_LIST.contains(sound) ? SoundType.valueOf(sound)
                        .getSoundOnVersion() : sound);
            }

            STOP_ON_EXIT.put(player.getName() + ";" + section.getLong("Stop On Exit.Delay"),
                    sounds);
        }
    }
}