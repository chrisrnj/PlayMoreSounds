/*
 * Copyright (c) 2020 Christiano Rangel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.util.PMSHelper;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import com.epicnicity322.yamlhandler.YamlConfigurationLoader;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Random;

public final class TimeTrigger
{
    private static final @NotNull HashMap<World, BukkitTask> runningWorlds = new HashMap<>();
    private static final @NotNull Random random = new Random();

    private TimeTrigger()
    {
    }

    public static void load()
    {
        Configuration worldTimes = Configurations.WORLD_TIMES.getPluginConfig().getConfiguration();

        if (PMSHelper.halloweenEvent())
            worldTimes = getHalloweenWorldTimesConfig();

        for (World world : Bukkit.getWorlds()) {
            if (runningWorlds.containsKey(world)) {
                runningWorlds.get(world).cancel();
                runningWorlds.remove(world);
            }

            ConfigurationSection worldSection = worldTimes.getConfigurationSection(world.getName());

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

    private static Configuration getHalloweenWorldTimesConfig()
    {
        Configuration worldTimes = new Configuration(new YamlConfigurationLoader());

        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == World.Environment.NORMAL) {
                for (int i = 0; i < 6; ++i) {
                    int randomTime = random.nextInt(24001);

                    worldTimes.set(world.getName() + "." + randomTime + ".Enabled", true);
                    worldTimes.set(world.getName() + "." + randomTime + ".Sounds.0.Delay", 0);
                    worldTimes.set(world.getName() + "." + randomTime + ".Sounds.0.Sound", "ENTITY_CREEPER_PRIMED");
                    worldTimes.set(world.getName() + "." + randomTime + ".Sounds.0.Volume", 10);
                    worldTimes.set(world.getName() + "." + randomTime + ".Sounds.0.Pitch", 1);
                    worldTimes.set(world.getName() + "." + randomTime + ".Sounds.0.Options.Relative Location.BACK", 2);
                    worldTimes.set(world.getName() + "." + randomTime + ".Sounds.0.Options.Radius", -2);
                }
            }
        }

        return worldTimes;
    }
}
