package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public final class TimeTrigger
{
    private static final @NotNull HashMap<World, BukkitTask> runningWorlds = new HashMap<>();

    public static void load()
    {
        Configuration timeTriggers = Configurations.TIME_TRIGGERS.getPluginConfig().getConfiguration();

        for (World world : Bukkit.getWorlds()) {
            if (runningWorlds.containsKey(world)) {
                runningWorlds.get(world).cancel();
                runningWorlds.remove(world);
            }

            ConfigurationSection worldSection = timeTriggers.getConfigurationSection(world.getName());

            if (worldSection != null) {
                HashMap<Long, ConfigurationSection> times = new HashMap<>();

                // Filtering the nodes of worldSection to get only configuration sections that are numeric.
                worldSection.getNodes().forEach((key, value) -> {
                    // This exception will only be caught if the key is a long greater than Long#MAX_VALUE.
                    try {
                        if (StringUtils.isNumeric(key) && value instanceof ConfigurationSection)
                            times.put(Long.parseLong(key), (ConfigurationSection) value);
                    } catch (Exception ignored) {
                    }
                });

                runningWorlds.put(world, Bukkit.getScheduler().runTaskTimer(PlayMoreSounds.getInstance(), () -> {
                    long time = world.getTime();

                    if (times.containsKey(time)) {
                        ConfigurationSection timeSection = times.get(time);

                        new RichSound(timeSection).play(world.getSpawnLocation());
                    }
                }, 0, 1));
            }
        }
    }
}
