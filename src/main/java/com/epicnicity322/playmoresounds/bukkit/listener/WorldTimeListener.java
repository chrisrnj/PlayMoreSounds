/*
 * PlayMoreSounds - A bukkit plugin that manages and plays sounds.
 * Copyright (C) 2021 Christiano Rangel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
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

public final class WorldTimeListener
{
    private static final @NotNull HashMap<World, BukkitTask> runningWorlds = new HashMap<>();
    private static final @NotNull Random random = new Random();

    private WorldTimeListener()
    {
    }

    public static void load()
    {
        Configuration worldTimes = Configurations.WORLD_TIME_TRIGGERS.getConfigurationHolder().getConfiguration();

        if (PMSHelper.halloweenEvent())
            worldTimes = getHalloweenWorldTimeTriggersConfig();

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

                        new PlayableRichSound(timeSection).play(world.getSpawnLocation());
                    }
                }, 0, 1));
            }
        }
    }

    private static Configuration getHalloweenWorldTimeTriggersConfig()
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
