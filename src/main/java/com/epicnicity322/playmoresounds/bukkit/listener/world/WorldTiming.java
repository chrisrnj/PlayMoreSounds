package com.epicnicity322.playmoresounds.bukkit.listener.world;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class WorldTiming
{
    public static HashMap<World, BukkitRunnable> RUNNING_WORLDS = new HashMap<>();

    public static void time()
    {
        for (World w : Bukkit.getWorlds()) {
            if (PMSHelper.getConfig("worldtimer").contains(w.getName())) {
                if (!RUNNING_WORLDS.containsKey(w)) {
                    RUNNING_WORLDS.put(w, new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            if (PMSHelper.getConfig("worldtimer").contains(w.getName() + "." + w.getTime())) {
                                new RichSound(PMSHelper.getConfig("worldtimer").getConfigurationSection(
                                        w.getName() + "." + w.getTime())).play(w.getSpawnLocation());
                            }
                        }
                    });

                    RUNNING_WORLDS.get(w).runTaskTimer(PlayMoreSounds.getPlugin(), 0, 1);
                }
            } else {
                if (RUNNING_WORLDS.containsKey(w)) {
                    RUNNING_WORLDS.get(w).cancel();
                    RUNNING_WORLDS.remove(w);
                }
            }
        }
    }
}
